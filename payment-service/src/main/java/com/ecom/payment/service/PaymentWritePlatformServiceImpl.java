package com.ecom.payment.service;

import com.ecom.payment.dto.PaymentCreateOrderRequestDTO;
import com.ecom.payment.dto.PaymentCreateOrderResponseDTO;
import com.ecom.payment.dto.PaymentResultEventDTO;
import com.ecom.payment.exception.PaymentStateException;
import com.ecom.payment.exception.UnauthorizedException;
import com.ecom.payment.model.Payment;
import com.ecom.payment.model.PaymentStatus;
import com.ecom.payment.model.PaymentEvent;
import com.ecom.payment.repository.PaymentRepository;
import com.ecom.payment.repository.PaymentEventRepository;
import com.ecom.payment.util.JsonUtil;
import com.razorpay.Order;
import com.razorpay.RazorpayClient;
import com.razorpay.RazorpayException;
import com.razorpay.Utils;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.Instant;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Service
public class PaymentWritePlatformServiceImpl implements PaymentWritePlatformService {

    private static final Logger log = LoggerFactory.getLogger(PaymentWritePlatformServiceImpl.class);

    private final PaymentRepository paymentRepository;
    private final RazorpayClient razorpayClient;
    private final PaymentKafkaEventProducer paymentKafkaEventProducer;
    private final PaymentEventRepository paymentEventRepository;

    @Value("${razorpay.secretkey}")
    private String webhookSecretkey;

    public PaymentWritePlatformServiceImpl(final PaymentKafkaEventProducer paymentKafkaEventProducer,
                                           final PaymentRepository paymentRepository,
                                           final RazorpayClient razorpayClient,
                                           final PaymentEventRepository paymentEventRepository) {
        this.paymentKafkaEventProducer = paymentKafkaEventProducer;
        this.paymentRepository = paymentRepository;
        this.razorpayClient = razorpayClient;
        this.paymentEventRepository = paymentEventRepository;
    }

    @Transactional
    @Override
    public PaymentCreateOrderResponseDTO createOrder(PaymentCreateOrderRequestDTO req) {
        Boolean payementOrderExist = this.paymentRepository.existsByOrderId(req.orderId());
        if(payementOrderExist){
            throw new PaymentStateException("Payment for orderId = " +req.orderId()+ " has already been initiated.");
        }
        log.info("Initiating payment creation for orderId={}, userId={}, amount={}",
                req.orderId(), req.userId(), req.amount());

        Payment payment = Payment.create(req.amount(), req.orderId(), req.userId());
        this.paymentRepository.save(payment);

        try {
            JSONObject options = new JSONObject();
            options.put("amount", (req.amount().multiply(BigDecimal.valueOf(100L)))
                    .setScale(0, RoundingMode.UP).longValue());
            options.put("receipt", payment.getId().toString());
            options.put("currency", "INR");

            String reqJson = options.toString();

            log.debug("Razorpay order request payload for paymentId={}: {}", payment.getId(), reqJson);

            Order razorpayRes = this.razorpayClient.orders.create(options);

            String status = razorpayRes.get("status");
            log.info("Razorpay order created with status={} for paymentId={}", status, payment.getId());

            if (status != null && status.equals("created")) {
                payment.setRazorpayOrderId(razorpayRes.get("id"));
                payment.setStatus(PaymentStatus.PAYMENT_CREATED);
                this.paymentRepository.save(payment);

                log.info("Payment updated to PAYMENT_CREATED for paymentId={}, razorpayOrderId={}",
                        payment.getId(), payment.getRazorpayOrderId());
            }

            String resJson = razorpayRes.toString();

            this.paymentEventRepository.save(
                    PaymentEvent.create(payment.getId(), reqJson, resJson)
            );

            log.debug("Payment event logged for paymentId={}", payment.getId());
            Long amountInPaise = ((Number) razorpayRes.get("amount")).longValue();
            return new PaymentCreateOrderResponseDTO(
                    razorpayRes.get("id"),
                    razorpayRes.get("status"),
                    BigDecimal.valueOf(amountInPaise).divide(BigDecimal.valueOf(100))
            );

        } catch (RazorpayException e) {
            log.error("Razorpay exception while creating order for paymentId={}, orderId={}",
                    payment.getId(), req.orderId(), e);

            payment.setStatus(PaymentStatus.PAYMENT_CREATE_FAILED);
            this.paymentRepository.save(payment);

            throw new RuntimeException(e);

        } catch (DataIntegrityViolationException e) {
            log.warn("Duplicate payment initiation attempt for orderId={}", req.orderId());

            throw new PaymentStateException(
                    "Payment already initiated for orderId: " + req.orderId()
            );
        }
    }

    @Override
    public void handleWebhook(String signature, String eventId, String req) {

        log.info("Received webhook eventId={}", eventId);

        try {
            Boolean isVerified = Utils.verifyWebhookSignature(req, signature, webhookSecretkey);

            if (!isVerified) {
                log.warn("Webhook signature verification failed for eventId={}", eventId);
                throw new UnauthorizedException("Unauthorized access!");
            }

            JSONObject payload = new JSONObject(req);
            String event = payload.getString("event");

            log.info("Processing webhook eventType={} for eventId={}", event, eventId);

            switch (event) {
                case "payment.failed":
                    handlePaymentFailed(eventId, payload);
                    break;
                case "payment.captured":
                    handlePaymentPaid(eventId, payload);
                    break;
                default:
                    log.debug("Unhandled webhook eventType={} for eventId={}", event, eventId);
                    break;
            }

        } catch (RazorpayException e) {
            log.error("Error verifying webhook signature for eventId={}", eventId, e);
            throw new RuntimeException(e);
        }
    }

    private void handlePaymentPaid(String eventId, JSONObject payload) {

        log.info("Handling payment.captured for eventId={}", eventId);

        Boolean eventExists = this.paymentEventRepository.existsByRazorpayEventId(eventId);
        if (eventExists) {
            log.warn("Duplicate webhook event ignored for eventId={}", eventId);
            return;
        }

        String razorOrderId = payload.getJSONObject("payload")
                .getJSONObject("payment")
                .getJSONObject("entity")
                .getString("order_id");

        Long eventCreatedAt = payload.getJSONObject("payload")
                .getJSONObject("payment")
                .getJSONObject("entity")
                .getLong("created_at");

        Instant createdAt = Instant.ofEpochSecond(eventCreatedAt);

        Payment payment = this.paymentRepository.findByRazorpayOrderId(razorOrderId);

        if (payment == null) {
            log.warn("No payment found for razorpayOrderId={}", razorOrderId);
            return;
        }

        payment.setStatus(PaymentStatus.PAYMENT_SUCCESSFUL);
        this.paymentRepository.save(payment);

        log.info("Payment marked SUCCESS for paymentId={}, orderId={}",
                payment.getId(), payment.getOrderId());

        this.paymentEventRepository.save(
                PaymentEvent.success(createdAt, payment.getId(), eventId, payload.toString())
        );

        PaymentResultEventDTO msg = new PaymentResultEventDTO(
                payment.getId(),
                payment.getOrderId(),
                payment.getRazorpayOrderId(),
                payment.getAmount(),
                PaymentStatus.PAYMENT_SUCCESSFUL,
                payment.getUserId(),
                createdAt
        );

        this.paymentKafkaEventProducer.send("payment-success", payment.getOrderId(), msg);

        log.info("Kafka event sent: payment-success for orderId={}", payment.getOrderId());
    }

    private void handlePaymentFailed(String eventId, JSONObject payload) {

        log.info("Handling payment.failed for eventId={}", eventId);

        Boolean eventExists = this.paymentEventRepository.existsByRazorpayEventId(eventId);
        if (eventExists) {
            log.warn("Duplicate webhook event ignored for eventId={}", eventId);
            return;
        }

        String razorOrderId = payload.getJSONObject("payload")
                .getJSONObject("payment")
                .getJSONObject("entity")
                .getString("order_id");

        Long eventCreatedAt = payload.getJSONObject("payload")
                .getJSONObject("payment")
                .getJSONObject("entity")
                .getLong("created_at");

        Instant createdAt = Instant.ofEpochSecond(eventCreatedAt);

        Payment payment = this.paymentRepository.findByRazorpayOrderId(razorOrderId);

        if (payment == null) {
            log.warn("No payment found for razorpayOrderId={}", razorOrderId);
            return;
        }

        payment.setStatus(PaymentStatus.PAYMENT_FAILED);
        this.paymentRepository.save(payment);

        log.info("Payment marked FAILED for paymentId={}, orderId={}",
                payment.getId(), payment.getOrderId());

        this.paymentEventRepository.save(
                PaymentEvent.failed(createdAt, payment.getId(), eventId, payload.toString())
        );

        PaymentResultEventDTO msg = new PaymentResultEventDTO(
                payment.getId(),
                payment.getOrderId(),
                payment.getRazorpayOrderId(),
                payment.getAmount(),
                PaymentStatus.PAYMENT_FAILED,
                payment.getUserId(),
                createdAt
        );

        this.paymentKafkaEventProducer.send("payment-failed", payment.getOrderId(), msg);

        log.info("Kafka event sent: payment-failed for orderId={}", payment.getOrderId());
    }
}
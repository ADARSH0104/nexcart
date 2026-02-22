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

@Service
public class PaymentWritePlatformServiceImpl implements PaymentWritePlatformService{
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

        Payment payment = Payment.create(req.amount(),req.orderId(),req.userId());
        this.paymentRepository.save(payment);
        try {
//            Boolean paymentAlreadyInitiated = this.paymentRepository.existsByOrderId(req.orderId());
//            if(paymentAlreadyInitiated){
//                throw new PaymentStateException("Payment is already Initiated orderId : "+ req.orderId());
//            }

            JSONObject options = new JSONObject();
            options.put("amount",(req.amount().multiply(BigDecimal.valueOf(100L))).setScale(0, RoundingMode.UP).longValue());
            options.put("receipt",payment.getId().toString());
            options.put("currency","INR");
            String reqJson = options.toString();

            Order razorpayRes = this.razorpayClient.orders.create(options);
            String status = razorpayRes.get("status");
            if(status!=null && status.equals("created")){
                payment.setRazorpayOrderId(razorpayRes.get("id"));
                payment.setStatus(PaymentStatus.PAYMENT_CREATED);
                this.paymentRepository.save(payment);
            }
            String resJson = JsonUtil.toJson(razorpayRes);

            PaymentEvent log = PaymentEvent.create(payment.getId(),reqJson,resJson);
            this.paymentEventRepository.save(log);

            return new PaymentCreateOrderResponseDTO(razorpayRes.get("id"),razorpayRes.get("status"),razorpayRes.get("amount")) ;
        } catch (RazorpayException e) {
            payment.setStatus(PaymentStatus.PAYMENT_CREATE_FAILED);
            this.paymentRepository.save(payment);
            throw new RuntimeException(e);
        } catch (DataIntegrityViolationException e) {
            throw new PaymentStateException(
                    "Payment already initiated for orderId: " + req.orderId()
            );
        }
    }

    @Override
    public void handleWebhook(String signature, String eventId, String req) {

        try {
            Boolean isVerified = Utils.verifyWebhookSignature(req,signature,webhookSecretkey);
            if(!isVerified) throw new UnauthorizedException("Unauthorized access!");

            JSONObject payload = new JSONObject(req);

            String event = payload.getString("event");

            switch (event) {
//                case "order.paid":
//                    handleOrderPaid(eventId,payload);
//                    break;
                case "payment.failed":
                    handlePaymentFailed(eventId,payload);
                    break;
                case "payment.captured":
                    handlePaymentPaid(eventId,payload);
                    break;
                default:
                    break;
            }


        } catch (RazorpayException e) {
            throw new RuntimeException(e);
        }
    }

    private void handlePaymentPaid(String eventId, JSONObject payload) {
        Boolean eventExists = this.paymentEventRepository.existsByRazorpayEventId(eventId);
        if(eventExists) return;

        String razorOrderId = payload.getJSONObject("payload").getJSONObject("payment").getJSONObject("entity").getString("order_id");
        Long eventCreatedAt = payload.getJSONObject("payload").getJSONObject("payment").getJSONObject("entity").getLong("created_at");
        Instant createdAt = Instant.ofEpochSecond(eventCreatedAt);

        Payment payment = this.paymentRepository.findByRazorpayOrderId(razorOrderId);
        if (payment == null) return;
        payment.setStatus(PaymentStatus.PAYMENT_SUCCESSFUL);
        this.paymentRepository.save(payment);

        this.paymentEventRepository.save(PaymentEvent.success(createdAt,payment.getId(),eventId,payload.toString()));
        PaymentResultEventDTO msg = new PaymentResultEventDTO(payment.getId(),payment.getOrderId(),payment.getRazorpayOrderId(),payment.getAmount(),PaymentStatus.PAYMENT_SUCCESSFUL,payment.getUserId(),createdAt);
        this.paymentKafkaEventProducer.send("payment-success",payment.getOrderId(),msg);
    }

    private void handlePaymentFailed(String eventId, JSONObject payload) {
        Boolean eventExists = this.paymentEventRepository.existsByRazorpayEventId(eventId);
        if(eventExists) return;

        String razorOrderId = payload.getJSONObject("payload").getJSONObject("payment").getJSONObject("entity").getString("order_id");
        Long eventCreatedAt = payload.getJSONObject("payload").getJSONObject("payment").getJSONObject("entity").getLong("created_at");
        Instant createdAt = Instant.ofEpochSecond(eventCreatedAt);
        Payment payment = this.paymentRepository.findByRazorpayOrderId(razorOrderId);
        if (payment == null) return;
        payment.setStatus(PaymentStatus.PAYMENT_FAILED);
        this.paymentRepository.save(payment);

        this.paymentEventRepository.save(PaymentEvent.failed(createdAt,payment.getId(),eventId,payload.toString()));
        PaymentResultEventDTO msg = new PaymentResultEventDTO(payment.getId(),payment.getOrderId(),payment.getRazorpayOrderId(),payment.getAmount(),PaymentStatus.PAYMENT_FAILED,payment.getUserId(),createdAt);
        this.paymentKafkaEventProducer.send("payment-failed",payment.getOrderId(),msg);
    }

}

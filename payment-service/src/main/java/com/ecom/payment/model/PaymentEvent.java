package com.ecom.payment.model;

import jakarta.persistence.*;

import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "payment_transactions_logs")
public class PaymentEvent {


    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Enumerated(EnumType.STRING)
    @Column
    private PaymentEventStatus status;

    @Column
    private UUID paymentId;

    @Column(unique = true)
    private String razorpayEventId;
    @Column
    private Instant createdAt;

    @Column(columnDefinition= "JSON" )
    private String request;

    @Column(columnDefinition= "JSON" )
    private String response;
    protected PaymentEvent() {
    }
//    public void onCreate(){
//        this.createdAt = Instant.now();
//    }

    public PaymentEvent(Instant createdAt,UUID paymentId, String razorpayEventId, PaymentEventStatus status, String request, String response) {
        this.createdAt = createdAt;
        this.paymentId = paymentId;
        this.status = status;
        this.request = request;
        this.response = response;
        this.razorpayEventId = razorpayEventId;
    }

    public static PaymentEvent create(UUID paymentId, String req, String res){
        return new PaymentEvent(Instant.now(),paymentId,null, PaymentEventStatus.PAYMENT_ORDER_CREATED,req,res);
    }
    public static PaymentEvent failed(Instant createdAt,UUID paymentId, String razorpayEventId, String req){
        return new PaymentEvent(null,paymentId,razorpayEventId, PaymentEventStatus.PAYMENT_FAILED,req,null);
    }
    public static PaymentEvent success(Instant createdAt,UUID paymentId, String razorpayEventId, String req){
        return new PaymentEvent(null,paymentId,razorpayEventId, PaymentEventStatus.PAYMENT_SUCCESS,req,null);
    }

    public Instant getCreatedAt() {
        return createdAt;
    }

    public Long getId() {
        return id;
    }

    public String getRequest() {
        return request;
    }

    public String getResponse() {
        return response;
    }

    public PaymentEventStatus getStatus() {
        return status;
    }
}

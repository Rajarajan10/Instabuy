package com.ecommerce.payment_service.model;

import jakarta.persistence.*;
import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "payments")
public class Payment {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "payment_id")
    private Long paymentId;

    @Column(name = "order_id", nullable = false, unique = true)
    private Long orderId;

    @Column(name = "amount", nullable = false)
    private Double amount;

    @Enumerated(EnumType.STRING)
    @Column(name = "payment_method", nullable = false)
    private PaymentMethod paymentMethod;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false)
    private PaymentStatus status;

    @Column(name = "transaction_date", nullable = false)
    private LocalDateTime transactionDate;

    @Column(name = "failure_reason")
    private String failureReason;

    @Column(name = "transaction_id", unique = true, nullable = false)
    private String transactionId;

    public Payment() {}

    public Payment(Long orderId, Double amount, PaymentMethod paymentMethod,
                   PaymentStatus status, LocalDateTime transactionDate) {
        this.orderId = orderId;
        this.amount = amount;
        this.paymentMethod = paymentMethod;
        this.status = status;
        this.transactionDate = transactionDate;
    }

    //GETTERS

    public Long getPaymentId() {
        return paymentId;
    }

    public Long getOrderId() {
        return orderId;
    }

    public Double getAmount() {
        return amount;
    }

    public PaymentMethod getPaymentMethod() {
        return paymentMethod;
    }

    public PaymentStatus getPaymentStatus() {
        return status;
    }

    public LocalDateTime getTransactionDate() {
        return transactionDate;
    }

    public String getFailureReason() {
        return failureReason;
    }

    public String getTransactionId() {
        return transactionId;
    }

    //SETTERS

    public void setPaymentId(Long paymentId) {
        this.paymentId = paymentId;
    }

    public void setOrderId(Long orderId) {
        this.orderId = orderId;
    }

    public void setAmount(Double amount) {
        this.amount = amount;
    }

    public void setPaymentMethod(PaymentMethod paymentMethod) {
        this.paymentMethod = paymentMethod;
    }

    public void setPaymentStatus(PaymentStatus status) {
        this.status = status;
    }

    public void setTransactionDate(LocalDateTime transactionDate) {
        this.transactionDate = transactionDate;
    }

    public void setFailureReason(String failureReason) {
        this.failureReason = failureReason;
    }

    public void setTransactionId(String transactionId) {
        this.transactionId = transactionId;
    }

    @PrePersist
    protected void onCreate() {
        transactionDate = LocalDateTime.now();

        // 🔥 Auto-generate transaction ID if not set
        if (transactionId == null) {
            transactionId = UUID.randomUUID().toString();
        }
    }
}
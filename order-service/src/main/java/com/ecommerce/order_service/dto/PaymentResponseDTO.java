package com.ecommerce.order_service.dto;

public class PaymentResponseDTO {

    private Long paymentId;
    private String status;

    public Long getPaymentId() { return paymentId; }
    public void setPaymentId(Long paymentId) { this.paymentId = paymentId; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
}
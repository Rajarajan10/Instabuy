package com.ecommerce.payment_service.service;

import com.ecommerce.payment_service.model.Payment;
import com.ecommerce.payment_service.model.PaymentStatus;
import com.ecommerce.payment_service.repository.PaymentRepository;
import com.ecommerce.payment_service.dto.PaymentRequestDTO;
import com.ecommerce.payment_service.dto.PaymentResponseDTO;
import com.ecommerce.payment_service.exception.PaymentNotFoundException;
import com.ecommerce.payment_service.model.PaymentMethod;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class PaymentService {

    @Autowired
    private PaymentRepository paymentRepository;

    // Save Payment
    public PaymentResponseDTO savePayment(PaymentRequestDTO request) {

        Payment payment = new Payment();
        payment.setOrderId(request.getOrderId());
        payment.setAmount(request.getAmount());
        payment.setPaymentMethod(
                PaymentMethod.valueOf(request.getPaymentMethod())
        );
        payment.setPaymentStatus(PaymentStatus.SUCCESS);

        Payment savedPayment = paymentRepository.save(payment);

        return convertToDTO(savedPayment);
    }

    // Get Payment By ID
    public PaymentResponseDTO getPaymentById(Long id) {

        Payment payment = paymentRepository.findById(id)
                .orElseThrow(() -> new PaymentNotFoundException("Payment not found with id: " + id));

        return convertToDTO(payment);
    }

    // Get All Payments
    public List<PaymentResponseDTO> getAllPayments() {

        List<Payment> payments = paymentRepository.findAll();

        return payments.stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    // Update Payment
    public PaymentResponseDTO updatePayment(Long id, PaymentRequestDTO paymentDetails) {

        Payment payment = paymentRepository.findById(id)
                .orElseThrow(() -> new PaymentNotFoundException("Payment not found with id: " + id));

        payment.setOrderId(paymentDetails.getOrderId());
        payment.setAmount(paymentDetails.getAmount());
        payment.setPaymentMethod(
                PaymentMethod.valueOf(paymentDetails.getPaymentMethod())
        );

        Payment updatedPayment = paymentRepository.save(payment);

        return convertToDTO(updatedPayment);
    }

    // Delete Payment
    public void deletePayment(Long id) {

        if(!paymentRepository.existsById(id)) {
            throw new PaymentNotFoundException("Payment not found with id: " + id);
        }

        paymentRepository.deleteById(id);
    }

    // Convert Entity -> DTO
    private PaymentResponseDTO convertToDTO(Payment payment) {

        PaymentResponseDTO dto = new PaymentResponseDTO();

        dto.setPaymentId(payment.getPaymentId());
        dto.setOrderId(payment.getOrderId());
        dto.setAmount(payment.getAmount());
        dto.setPaymentMethod(payment.getPaymentMethod().name());
        dto.setStatus(payment.getPaymentStatus().name());
        dto.setTransactionDate(payment.getTransactionDate());

        return dto;
    }
}
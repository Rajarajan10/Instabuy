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

    // INITIATE PAYMENT (called by Order Service during checkout)
    public PaymentResponseDTO initiatePayment(PaymentRequestDTO request) {

        // Prevent duplicate payment for same order
        if(paymentRepository.findByOrderId(request.getOrderId()).isPresent()){
            throw new RuntimeException("Payment already exists for this order");
        }

        Payment payment = new Payment();

        payment.setOrderId(request.getOrderId());
        payment.setAmount(request.getAmount());

        // safer enum conversion
        payment.setPaymentMethod(
                PaymentMethod.valueOf(request.getPaymentMethod().toUpperCase())
        );

        // For project demo → payment succeeds immediately
        payment.setPaymentStatus(PaymentStatus.SUCCESS);

        Payment savedPayment = paymentRepository.save(payment);

        return convertToDTO(savedPayment);
    }


    // PROCESS PAYMENT (optional manual confirmation)
    public PaymentResponseDTO processPayment(Long orderId) {

        Payment payment = paymentRepository.findByOrderId(orderId)
                .orElseThrow(() ->
                        new PaymentNotFoundException("Payment not found for order: " + orderId));

        if(payment.getPaymentStatus() == PaymentStatus.SUCCESS){
            throw new RuntimeException("Payment already completed for this order");
        }

        payment.setPaymentStatus(PaymentStatus.SUCCESS);

        Payment updatedPayment = paymentRepository.save(payment);

        return convertToDTO(updatedPayment);
    }


    // GET PAYMENT BY ID
    public PaymentResponseDTO getPaymentById(Long id) {

        Payment payment = paymentRepository.findById(id)
                .orElseThrow(() ->
                        new PaymentNotFoundException("Payment not found with id: " + id));

        return convertToDTO(payment);
    }


    // GET PAYMENT BY ORDER
    public PaymentResponseDTO getPaymentByOrder(Long orderId) {

        Payment payment = paymentRepository.findByOrderId(orderId)
                .orElseThrow(() ->
                        new PaymentNotFoundException("Payment not found for order: " + orderId));

        return convertToDTO(payment);
    }


    // GET ALL PAYMENTS
    public List<PaymentResponseDTO> getAllPayments() {

        return paymentRepository.findAll()
                .stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }


    // DELETE PAYMENT
    public void deletePayment(Long id) {

        if(!paymentRepository.existsById(id)){
            throw new PaymentNotFoundException("Payment not found with id: " + id);
        }

        paymentRepository.deleteById(id);
    }


    // ENTITY → DTO
    private PaymentResponseDTO convertToDTO(Payment payment){

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
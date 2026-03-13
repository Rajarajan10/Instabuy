package com.ecommerce.payment_service.controller;

import com.ecommerce.payment_service.dto.PaymentRequestDTO;
import com.ecommerce.payment_service.dto.PaymentResponseDTO;
import com.ecommerce.payment_service.service.PaymentService;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/payments")
public class PaymentController {

    @Autowired
    private PaymentService paymentService;

    // Create Payment
    @PostMapping
    public PaymentResponseDTO createPayment(@RequestBody PaymentRequestDTO paymentRequest) {
        return paymentService.savePayment(paymentRequest);
    }

    // Get Payment by ID
    @GetMapping("/{id}")
    public PaymentResponseDTO getPaymentById(@PathVariable Long id) {
        return paymentService.getPaymentById(id);
    }

    // Get All Payments
    @GetMapping
    public List<PaymentResponseDTO> getAllPayments() {
        return paymentService.getAllPayments();
    }

    // Update Payment Status
    @PutMapping("/{id}")
    public PaymentResponseDTO updatePayment(@PathVariable Long id, @RequestBody PaymentRequestDTO paymentRequest) {
        return paymentService.updatePayment(id, paymentRequest);
    }

    // Delete Payment
    @DeleteMapping("/{id}")
    public String deletePayment(@PathVariable Long id) {
        paymentService.deletePayment(id);
        return "Payment deleted successfully";
    }
}
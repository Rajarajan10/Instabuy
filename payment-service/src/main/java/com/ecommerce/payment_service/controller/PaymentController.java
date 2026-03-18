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

    // Initiate Payment (Order Service)
    @PostMapping("/initiate")
    public PaymentResponseDTO initiatePayment(@RequestBody PaymentRequestDTO request) {
        return paymentService.initiatePayment(request);
    }

    // Process Payment (Customer)
    @PostMapping("/process/{orderId}")
    public PaymentResponseDTO processPayment(@PathVariable Long orderId) {
        return paymentService.processPayment(orderId);
    }

    // Get Payment by ID
    @GetMapping("/{id}")
    public PaymentResponseDTO getPaymentById(@PathVariable Long id) {
        return paymentService.getPaymentById(id);
    }

    // Get Payment by Order
    @GetMapping("/order/{orderId}")
    public PaymentResponseDTO getPaymentByOrder(@PathVariable Long orderId) {
        return paymentService.getPaymentByOrder(orderId);
    }

    // Get All Payments
    @GetMapping
    public List<PaymentResponseDTO> getAllPayments() {
        return paymentService.getAllPayments();
    }

    // Delete Payment
    @DeleteMapping("/{id}")
    public String deletePayment(@PathVariable Long id) {
        paymentService.deletePayment(id);
        return "Payment deleted successfully";
    }
}
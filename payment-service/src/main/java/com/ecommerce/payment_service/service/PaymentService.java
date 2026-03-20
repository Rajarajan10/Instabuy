package com.ecommerce.payment_service.service;

// ✅ Config
import com.ecommerce.payment_service.config.PaymentConfig;

// ✅ DTOs
import com.ecommerce.payment_service.dto.PaymentRequestDTO;
import com.ecommerce.payment_service.dto.PaymentResponseDTO;

// ✅ Exceptions
import com.ecommerce.payment_service.exception.PaymentNotFoundException;
import com.ecommerce.payment_service.exception.InvalidPaymentException;
import com.ecommerce.payment_service.exception.DuplicatePaymentException;
import com.ecommerce.payment_service.exception.PaymentAlreadyProcessedException;
import com.ecommerce.payment_service.exception.PaymentProcessingException;

// ✅ Models
import com.ecommerce.payment_service.model.Payment;
import com.ecommerce.payment_service.model.PaymentMethod;
import com.ecommerce.payment_service.model.PaymentStatus;

// ✅ Repository
import com.ecommerce.payment_service.repository.PaymentRepository;

// ✅ Utility
import com.ecommerce.payment_service.util.ProbabilityUtil;

// ✅ Spring
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

// ✅ Lifecycle
import jakarta.annotation.PostConstruct;

// ✅ Java
import java.util.List;
import java.util.stream.Collectors;

@Service
public class PaymentService {

    @Autowired
    private PaymentRepository paymentRepository;

    @Autowired
    private PaymentConfig paymentConfig;

    @PostConstruct
    public void validateConfigs() {
        ProbabilityUtil.validateProbabilities(paymentConfig.getMethodProbability());
        ProbabilityUtil.validateProbabilities(paymentConfig.getStatusProbability());

        if (paymentConfig.getFailureReasons() != null) {
            ProbabilityUtil.validateProbabilities(paymentConfig.getFailureReasons());
        }
    }

    // ✅ INITIATE PAYMENT
    public PaymentResponseDTO initiatePayment(PaymentRequestDTO request) {

        // 🔥 Input validation
        if (request.getOrderId() == null) {
            throw new InvalidPaymentException("Order ID cannot be null");
        }

        if (request.getAmount() == null || request.getAmount() <= 0) {
            throw new InvalidPaymentException("Amount must be greater than zero");
        }

        // 🔥 Duplicate check
        if (paymentRepository.findByOrderId(request.getOrderId()).isPresent()) {
            throw new DuplicatePaymentException(
                    "Payment already exists for order: " + request.getOrderId());
        }

        Payment payment = new Payment();
        payment.setOrderId(request.getOrderId());
        payment.setAmount(request.getAmount());

        try {
            // 🔥 Safe enum conversion
            String methodStr = ProbabilityUtil.pickByProbability(
                    paymentConfig.getMethodProbability()
            );
            payment.setPaymentMethod(PaymentMethod.valueOf(methodStr));

            String statusStr = ProbabilityUtil.pickByProbability(
                    paymentConfig.getStatusProbability()
            );
            PaymentStatus status = PaymentStatus.valueOf(statusStr);
            payment.setPaymentStatus(status);

            if (status == PaymentStatus.FAILED && paymentConfig.getFailureReasons() != null) {
                String reason = ProbabilityUtil.pickByProbability(
                        paymentConfig.getFailureReasons()
                );
                payment.setFailureReason(reason);
            }

        } catch (IllegalArgumentException e) {
            throw new PaymentProcessingException("Invalid configuration for payment processing", e);
        }

        Payment savedPayment = paymentRepository.save(payment);
        return convertToDTO(savedPayment);
    }


    // ✅ PROCESS PAYMENT
    public PaymentResponseDTO processPayment(Long orderId) {

        Payment payment = paymentRepository.findByOrderId(orderId)
                .orElseThrow(() ->
                        new PaymentNotFoundException("Payment not found for order: " + orderId));

        if (payment.getPaymentStatus() == PaymentStatus.SUCCESS) {
            throw new PaymentAlreadyProcessedException(
                    "Payment already completed for order: " + orderId);
        }

        try {
            String statusStr = ProbabilityUtil.pickByProbability(
                    paymentConfig.getStatusProbability()
            );

            PaymentStatus status = PaymentStatus.valueOf(statusStr);
            payment.setPaymentStatus(status);

            if (status == PaymentStatus.FAILED && paymentConfig.getFailureReasons() != null) {
                String reason = ProbabilityUtil.pickByProbability(
                        paymentConfig.getFailureReasons()
                );
                payment.setFailureReason(reason);
            } else {
                payment.setFailureReason(null);
            }

        } catch (IllegalArgumentException e) {
            throw new PaymentProcessingException("Error while processing payment", e);
        }

        Payment updatedPayment = paymentRepository.save(payment);
        return convertToDTO(updatedPayment);
    }


    // ✅ GET PAYMENT BY ID
    public PaymentResponseDTO getPaymentById(Long id) {

        Payment payment = paymentRepository.findById(id)
                .orElseThrow(() ->
                        new PaymentNotFoundException("Payment not found with id: " + id));

        return convertToDTO(payment);
    }


    // ✅ GET PAYMENT BY ORDER
    public PaymentResponseDTO getPaymentByOrder(Long orderId) {

        Payment payment = paymentRepository.findByOrderId(orderId)
                .orElseThrow(() ->
                        new PaymentNotFoundException("Payment not found for order: " + orderId));

        return convertToDTO(payment);
    }


    // ✅ GET ALL
    public List<PaymentResponseDTO> getAllPayments() {
        return paymentRepository.findAll()
                .stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }


    // ✅ DELETE
    public void deletePayment(Long id) {

        if (!paymentRepository.existsById(id)) {
            throw new PaymentNotFoundException("Payment not found with id: " + id);
        }

        paymentRepository.deleteById(id);
    }


    // 🔄 ENTITY → DTO
    private PaymentResponseDTO convertToDTO(Payment payment) {

        PaymentResponseDTO dto = new PaymentResponseDTO();

        dto.setPaymentId(payment.getPaymentId());
        dto.setOrderId(payment.getOrderId());
        dto.setAmount(payment.getAmount());
        dto.setPaymentMethod(payment.getPaymentMethod().name());
        dto.setStatus(payment.getPaymentStatus().name());
        dto.setTransactionDate(payment.getTransactionDate());

        if (payment.getFailureReason() != null) {
            dto.setFailureReason(payment.getFailureReason());
        }

        return dto;
    }
}
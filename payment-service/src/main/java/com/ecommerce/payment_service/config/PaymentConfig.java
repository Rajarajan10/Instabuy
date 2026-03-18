package com.ecommerce.payment_service.config;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

import java.util.Map;

@Configuration
@ConfigurationProperties(prefix = "payment")
public class PaymentConfig {

    private Map<String, Double> methodProbability;
    private Map<String, Double> statusProbability;
    private Map<String, Double> failureReasons;

    public Map<String, Double> getMethodProbability() {
        return methodProbability;
    }

    public void setMethodProbability(Map<String, Double> methodProbability) {
        this.methodProbability = methodProbability;
    }

    public Map<String, Double> getStatusProbability() {
        return statusProbability;
    }

    public void setStatusProbability(Map<String, Double> statusProbability) {
        this.statusProbability = statusProbability;
    }

    public Map<String, Double> getFailureReasons() {
        return failureReasons;
    }

    public void setFailureReasons(Map<String, Double> failureReasons) {
        this.failureReasons = failureReasons;
    }
}
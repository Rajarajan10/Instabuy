package com.ecommerce.payment_service.util;

import java.util.Map;

public class ConfigValidator {

    public static void validateProbability(Map<String, Double> map, String name) {
        double sum = map.values().stream().mapToDouble(Double::doubleValue).sum();

        if (Math.abs(sum - 1.0) > 0.01) {
            throw new RuntimeException(name + " probabilities must sum to 1");
        }
    }
}
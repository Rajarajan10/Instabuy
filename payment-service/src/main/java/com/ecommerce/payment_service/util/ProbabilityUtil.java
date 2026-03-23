package com.ecommerce.payment_service.util;

import java.util.Map;
import java.util.Random;

public class ProbabilityUtil {

    private static final Random random = new Random();

    public static String pickByProbability(Map<String, Double> probabilities) {

        if (probabilities == null || probabilities.isEmpty()) {
            throw new IllegalArgumentException("Probability map cannot be null or empty");
        }

        double rand = random.nextDouble(); // 0.0 to 1.0
        double cumulative = 0.0;

        for (Map.Entry<String, Double> entry : probabilities.entrySet()) {
            cumulative += entry.getValue();

            if (rand <= cumulative) {
                return entry.getKey();
            }
        }

        // fallback (edge case due to precision issues)
        return probabilities.keySet().iterator().next();
    }

    public static void validateProbabilities(Map<String, Double> probabilities) {
        double sum = probabilities.values()
                .stream()
                .mapToDouble(Double::doubleValue)
                .sum();

        if (Math.abs(sum - 1.0) > 0.01) {
            throw new IllegalArgumentException(
                    "Probabilities must sum to 1. Current sum = " + sum
            );
        }
    }
}
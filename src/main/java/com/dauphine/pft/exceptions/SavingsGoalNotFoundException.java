package com.dauphine.pft.exceptions;

import java.util.UUID;

public class SavingsGoalNotFoundException extends RuntimeException {

    public SavingsGoalNotFoundException(UUID id) {
        super("Savings goal not found with id: " + id);
    }
}

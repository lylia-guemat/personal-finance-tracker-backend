package com.dauphine.pft.exceptions;

import java.util.UUID;

public class SavingsGoalAlreadyCompletedException extends RuntimeException {

    public SavingsGoalAlreadyCompletedException(UUID id) {
        super("Savings goal with id " + id + " is already completed or cancelled and cannot be modified");
    }
}

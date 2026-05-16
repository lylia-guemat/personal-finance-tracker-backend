package com.dauphine.pft.exceptions;

public class InvalidSavingsGoalAmountException extends RuntimeException {

    public InvalidSavingsGoalAmountException() {
        super("Current amount cannot exceed target amount");
    }
}

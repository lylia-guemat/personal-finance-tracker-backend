package com.dauphine.pft.exceptions;

public class InvalidTransactionAmountException extends RuntimeException {

    public InvalidTransactionAmountException() {
        super("Transaction amount must be strictly positive");
    }
}

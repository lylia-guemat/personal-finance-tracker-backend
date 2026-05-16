package com.dauphine.pft.exceptions;

public class InvalidTransactionDateException extends RuntimeException {

    public InvalidTransactionDateException() {
        super("Transaction date cannot be in the future");
    }
}

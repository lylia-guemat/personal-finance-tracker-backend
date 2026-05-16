package com.dauphine.pft.exceptions;

public class InvalidTokenException extends RuntimeException {

    public InvalidTokenException() {
        super("Invalid or expired token");
    }
}

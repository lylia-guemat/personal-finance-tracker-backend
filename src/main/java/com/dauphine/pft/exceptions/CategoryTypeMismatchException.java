package com.dauphine.pft.exceptions;

public class CategoryTypeMismatchException extends RuntimeException {

    public CategoryTypeMismatchException() {
        super("Category type does not match transaction type");
    }
}

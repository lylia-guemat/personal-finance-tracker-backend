package com.dauphine.pft.exceptions;

public class BudgetCategoryTypeMismatchException extends RuntimeException {

    public BudgetCategoryTypeMismatchException() {
        super("A budget can only be attached to an expense category");
    }
}

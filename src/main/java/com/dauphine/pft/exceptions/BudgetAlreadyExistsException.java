package com.dauphine.pft.exceptions;

import java.util.UUID;

public class BudgetAlreadyExistsException extends RuntimeException {

    public BudgetAlreadyExistsException(UUID categoryId) {
        super("A monthly budget already exists for category: " + categoryId);
    }
}

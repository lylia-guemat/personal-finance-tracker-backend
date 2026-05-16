package com.dauphine.pft.exceptions;

import java.util.UUID;

public class CategoryInUseException extends RuntimeException {

    public CategoryInUseException(UUID id) {
        super("Category with id " + id + " cannot be deleted because it is used by existing transactions");
    }
}

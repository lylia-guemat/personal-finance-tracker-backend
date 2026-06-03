package com.dauphine.pft.handlers;

import com.dauphine.pft.dto.responses.ApiErrorResponse;
import com.dauphine.pft.exceptions.*;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.time.LocalDateTime;
import java.util.stream.Collectors;

@RestControllerAdvice
public class GlobalExceptionHandler {

    // 404 - Not Found
    @ExceptionHandler({
            CategoryNotFoundException.class,
            TransactionNotFoundException.class,
            SavingsGoalNotFoundException.class,
            BudgetNotFoundException.class
    })
    public ResponseEntity<ApiErrorResponse> handleNotFound(RuntimeException ex) {
        return ResponseEntity.status(HttpStatus.NOT_FOUND)
                .body(new ApiErrorResponse(404, ex.getMessage(), LocalDateTime.now()));
    }

    // 409 - Conflict
    @ExceptionHandler({
            CategoryAlreadyExistsException.class,
            CategoryInUseException.class,
            UserAlreadyExistsException.class,
            BudgetAlreadyExistsException.class
    })
    public ResponseEntity<ApiErrorResponse> handleConflict(RuntimeException ex) {
        return ResponseEntity.status(HttpStatus.CONFLICT)
                .body(new ApiErrorResponse(409, ex.getMessage(), LocalDateTime.now()));
    }

    // 400 - Bad Request métier
    @ExceptionHandler({
            InvalidTransactionAmountException.class,
            InvalidTransactionDateException.class,
            CategoryTypeMismatchException.class,
            InvalidSavingsGoalAmountException.class,
            SavingsGoalAlreadyCompletedException.class,
            BudgetCategoryTypeMismatchException.class
    })
    public ResponseEntity<ApiErrorResponse> handleBadRequest(RuntimeException ex) {
        return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(new ApiErrorResponse(400, ex.getMessage(), LocalDateTime.now()));
    }

    // 401 - Unauthorized
    @ExceptionHandler({
            InvalidCredentialsException.class,
            InvalidTokenException.class
    })
    public ResponseEntity<ApiErrorResponse> handleUnauthorized(RuntimeException ex) {
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                .body(new ApiErrorResponse(401, ex.getMessage(), LocalDateTime.now()));
    }

    // 400 - Validation des DTO (@NotBlank, @Positive, etc.)
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ApiErrorResponse> handleValidationErrors(MethodArgumentNotValidException ex) {
        String message = ex.getBindingResult()
                .getFieldErrors()
                .stream()
                .map(FieldError::getDefaultMessage)
                .collect(Collectors.joining(", "));
        return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(new ApiErrorResponse(400, message, LocalDateTime.now()));
    }

    // 500 - Erreur inattendue
    @ExceptionHandler(Exception.class)
    public ResponseEntity<ApiErrorResponse> handleGenericException(Exception ex) {
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(new ApiErrorResponse(500, "An unexpected error occurred", LocalDateTime.now()));
    }
}

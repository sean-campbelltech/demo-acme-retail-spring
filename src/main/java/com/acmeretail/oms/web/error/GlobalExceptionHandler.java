package com.acmeretail.oms.web.error;

import com.acmeretail.oms.exception.CouponNotApplicableException;
import com.acmeretail.oms.exception.InsufficientStockException;
import com.acmeretail.oms.exception.PricingException;
import com.acmeretail.oms.exception.ResourceNotFoundException;
import com.acmeretail.oms.exception.UnshippableOrderException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.stream.Collectors;

/**
 * Translates domain and validation exceptions into HTTP responses.
 */
@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(ResourceNotFoundException.class)
    public ResponseEntity<ApiError> handleNotFound(ResourceNotFoundException ex) {
        return build(HttpStatus.NOT_FOUND, ex.getMessage());
    }

    @ExceptionHandler(InsufficientStockException.class)
    public ResponseEntity<ApiError> handleStock(InsufficientStockException ex) {
        return build(HttpStatus.CONFLICT, ex.getMessage());
    }

    @ExceptionHandler(CouponNotApplicableException.class)
    public ResponseEntity<ApiError> handleCoupon(CouponNotApplicableException ex) {
        return build(HttpStatus.UNPROCESSABLE_CONTENT, ex.getMessage());
    }

    @ExceptionHandler(UnshippableOrderException.class)
    public ResponseEntity<ApiError> handleUnshippable(UnshippableOrderException ex) {
        return build(HttpStatus.UNPROCESSABLE_CONTENT, ex.getMessage());
    }

    @ExceptionHandler(PricingException.class)
    public ResponseEntity<ApiError> handlePricing(PricingException ex) {
        return build(HttpStatus.UNPROCESSABLE_CONTENT, ex.getMessage());
    }

    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<ApiError> handleBadRequest(IllegalArgumentException ex) {
        return build(HttpStatus.BAD_REQUEST, ex.getMessage());
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ApiError> handleValidation(MethodArgumentNotValidException ex) {
        String message = ex.getBindingResult().getFieldErrors().stream()
                .map(this::describe)
                .collect(Collectors.joining("; "));
        return build(HttpStatus.BAD_REQUEST, message.isEmpty() ? "validation failed" : message);
    }

    private String describe(FieldError error) {
        return error.getField() + " " + error.getDefaultMessage();
    }

    private ResponseEntity<ApiError> build(HttpStatus status, String message) {
        ApiError error = ApiError.of(status.value(), status.getReasonPhrase(), message);
        return ResponseEntity.status(status).body(error);
    }
}

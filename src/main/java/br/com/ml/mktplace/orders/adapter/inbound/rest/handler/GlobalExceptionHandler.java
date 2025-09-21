package br.com.ml.mktplace.orders.adapter.inbound.rest.handler;

import br.com.ml.mktplace.orders.adapter.inbound.rest.dto.ErrorResponse;
import br.com.ml.mktplace.orders.domain.model.ExternalServiceException;
import br.com.ml.mktplace.orders.domain.model.OrderNotFoundException;
import br.com.ml.mktplace.orders.domain.port.ProcessOrderUseCase.ProcessOrderException;
import jakarta.servlet.http.HttpServletRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

/**
 * Global exception handler for REST controllers
 */
@RestControllerAdvice
public class GlobalExceptionHandler {
    
    private static final Logger logger = LoggerFactory.getLogger(GlobalExceptionHandler.class);
    
    /**
     * Handle validation errors from @Valid annotations
     */
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ErrorResponse> handleValidationErrors(
            MethodArgumentNotValidException ex, HttpServletRequest request) {
        
        String correlationId = getCorrelationId(request);
        logger.warn("Validation error - Correlation ID: {}", correlationId, ex);
        
        Map<String, String> validationErrors = new HashMap<>();
        ex.getBindingResult().getAllErrors().forEach(error -> {
            String fieldName = ((FieldError) error).getField();
            String errorMessage = error.getDefaultMessage();
            validationErrors.put(fieldName, errorMessage);
        });
        
        ErrorResponse errorResponse = new ErrorResponse(
                HttpStatus.BAD_REQUEST.value(),
                "Validation Failed",
                "Invalid request data",
                request.getRequestURI(),
                correlationId
        );
        errorResponse.setValidationErrors(validationErrors);
        
        return ResponseEntity.badRequest().body(errorResponse);
    }
    
    /**
     * Handle order not found errors
     */
    @ExceptionHandler(OrderNotFoundException.class)
    public ResponseEntity<ErrorResponse> handleOrderNotFound(
            OrderNotFoundException ex, HttpServletRequest request) {
        
        String correlationId = getCorrelationId(request);
        logger.warn("Order not found - Correlation ID: {}, Message: {}", correlationId, ex.getMessage());
        
        ErrorResponse errorResponse = new ErrorResponse(
                HttpStatus.NOT_FOUND.value(),
                "Not Found",
                ex.getMessage(),
                request.getRequestURI(),
                correlationId
        );
        
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(errorResponse);
    }
    
    /**
     * Handle external service errors
     */
    @ExceptionHandler(ExternalServiceException.class)
    public ResponseEntity<ErrorResponse> handleExternalServiceError(
            ExternalServiceException ex, HttpServletRequest request) {
        
        String correlationId = getCorrelationId(request);
        logger.error("External service error - Correlation ID: {}, Service: {}, Message: {}", 
                correlationId, ex.getServiceName(), ex.getMessage(), ex);
        
        ErrorResponse errorResponse = new ErrorResponse(
                HttpStatus.SERVICE_UNAVAILABLE.value(),
                "Service Unavailable",
                "External service temporarily unavailable. Please try again later.",
                request.getRequestURI(),
                correlationId
        );
        errorResponse.setDetails(List.of("Service: " + ex.getServiceName()));
        
        return ResponseEntity.status(HttpStatus.SERVICE_UNAVAILABLE).body(errorResponse);
    }
    
    /**
     * Handle order processing errors
     */
    @ExceptionHandler(ProcessOrderException.class)
    public ResponseEntity<ErrorResponse> handleProcessOrderError(
            ProcessOrderException ex, HttpServletRequest request) {
        
        String correlationId = getCorrelationId(request);
        logger.error("Order processing error - Correlation ID: {}, Order ID: {}, Message: {}", 
                correlationId, ex.getOrderId(), ex.getMessage(), ex);
        
        ErrorResponse errorResponse = new ErrorResponse(
                HttpStatus.UNPROCESSABLE_ENTITY.value(),
                "Processing Failed",
                ex.getMessage(),
                request.getRequestURI(),
                correlationId
        );
        errorResponse.setDetails(List.of("Order ID: " + ex.getOrderId()));
        
        return ResponseEntity.status(HttpStatus.UNPROCESSABLE_ENTITY).body(errorResponse);
    }
    
    /**
     * Handle illegal argument errors
     */
    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<ErrorResponse> handleIllegalArgument(
            IllegalArgumentException ex, HttpServletRequest request) {
        
        String correlationId = getCorrelationId(request);
        logger.warn("Invalid argument - Correlation ID: {}, Message: {}", correlationId, ex.getMessage());
        
        ErrorResponse errorResponse = new ErrorResponse(
                HttpStatus.BAD_REQUEST.value(),
                "Bad Request",
                ex.getMessage(),
                request.getRequestURI(),
                correlationId
        );
        
        return ResponseEntity.badRequest().body(errorResponse);
    }
    
    /**
     * Handle all other unexpected errors
     */
    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorResponse> handleGeneralError(
            Exception ex, HttpServletRequest request) {
        
        String correlationId = getCorrelationId(request);
        logger.error("Unexpected error - Correlation ID: {}, Message: {}", 
                correlationId, ex.getMessage(), ex);
        
        ErrorResponse errorResponse = new ErrorResponse(
                HttpStatus.INTERNAL_SERVER_ERROR.value(),
                "Internal Server Error",
                "An unexpected error occurred. Please try again later.",
                request.getRequestURI(),
                correlationId
        );
        
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorResponse);
    }
    
    /**
     * Extract correlation ID from request headers or generate a new one
     */
    private String getCorrelationId(HttpServletRequest request) {
        String correlationId = request.getHeader("X-Correlation-ID");
        if (correlationId == null || correlationId.trim().isEmpty()) {
            correlationId = UUID.randomUUID().toString();
        }
        return correlationId;
    }
}
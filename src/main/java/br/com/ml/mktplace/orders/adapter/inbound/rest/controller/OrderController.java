package br.com.ml.mktplace.orders.adapter.inbound.rest.controller;

import br.com.ml.mktplace.orders.adapter.inbound.rest.dto.OrderRequest;
import br.com.ml.mktplace.orders.adapter.inbound.rest.dto.OrderResponse;
import br.com.ml.mktplace.orders.adapter.inbound.rest.mapper.OrderRestMapper;
import br.com.ml.mktplace.orders.domain.model.Order;
import br.com.ml.mktplace.orders.domain.port.CreateOrderUseCase;
import br.com.ml.mktplace.orders.domain.port.ProcessOrderUseCase;
import br.com.ml.mktplace.orders.domain.port.QueryOrderUseCase;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

/**
 * REST Controller for Order management endpoints
 */
@RestController
@RequestMapping("/api/v1/orders")
@Tag(name = "Orders", description = "Order processing and management endpoints")
public class OrderController {
    
    private static final Logger logger = LoggerFactory.getLogger(OrderController.class);
    private static final String API_VERSION = "1.0";
    
    private final CreateOrderUseCase createOrderUseCase;
    private final ProcessOrderUseCase processOrderUseCase;
    private final QueryOrderUseCase queryOrderUseCase;
    private final OrderRestMapper mapper;
    
    @Autowired
    public OrderController(CreateOrderUseCase createOrderUseCase, 
                          ProcessOrderUseCase processOrderUseCase,
                          QueryOrderUseCase queryOrderUseCase,
                          OrderRestMapper mapper) {
        this.createOrderUseCase = createOrderUseCase;
        this.processOrderUseCase = processOrderUseCase;
        this.queryOrderUseCase = queryOrderUseCase;
        this.mapper = mapper;
    }
    
    /**
     * Process a new order
     * POST /api/v1/orders
     */
    @PostMapping
    @Operation(summary = "Process a new order", 
               description = "Creates and processes a new order asynchronously")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "Order created and processing started",
                        content = @Content(schema = @Schema(implementation = OrderResponse.class))),
            @ApiResponse(responseCode = "400", description = "Invalid request data"),
            @ApiResponse(responseCode = "422", description = "Order processing failed"),
            @ApiResponse(responseCode = "503", description = "External service unavailable")
    })
    public ResponseEntity<OrderResponse> processOrder(
            @Valid @RequestBody OrderRequest request,
            @Parameter(description = "Correlation ID for request tracking", example = "550e8400-e29b-41d4-a716-446655440000")
            @RequestHeader(value = "X-Correlation-ID", required = false) String correlationId) {
        
        // Generate correlation ID if not provided
        if (correlationId == null || correlationId.trim().isEmpty()) {
            correlationId = UUID.randomUUID().toString();
        }
        
        logger.info("Processing new order request - Correlation ID: {}, Customer: {}, Items: {}", 
                correlationId, request.getCustomerId(), request.getItems().size());
        
        try {
            // Convert DTO to domain object
            Order order = mapper.toDomain(request, null);
            
            // Create the order first
            Order createdOrder = createOrderUseCase.createOrder(
                    order.getCustomerId(), 
                    order.getItems(), 
                    order.getDeliveryAddress()
            );
            
            // Process the order asynchronously
            Order processedOrder = processOrderUseCase.processOrder(createdOrder.getId());
            
            // Convert back to response DTO
            OrderResponse response = mapper.toResponse(processedOrder);
            
            // Build response headers
            HttpHeaders headers = buildResponseHeaders(correlationId);
            
            logger.info("Order processed successfully - ID: {}, Status: {}, Correlation ID: {}", 
                    processedOrder.getId(), processedOrder.getStatus(), correlationId);
            
            return ResponseEntity.status(HttpStatus.CREATED)
                    .headers(headers)
                    .body(response);
                    
        } catch (Exception e) {
            logger.error("Failed to process order - Correlation ID: {}", correlationId, e);
            throw e; // Will be handled by GlobalExceptionHandler
        }
    }
    
    /**
     * Get order by ID
     * GET /api/v1/orders/{id}
     */
    @GetMapping("/{id}")
    @Operation(summary = "Get order by ID", 
               description = "Retrieves a specific order by its identifier")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Order found",
                        content = @Content(schema = @Schema(implementation = OrderResponse.class))),
            @ApiResponse(responseCode = "404", description = "Order not found"),
            @ApiResponse(responseCode = "400", description = "Invalid order ID")
    })
    public ResponseEntity<OrderResponse> getOrder(
            @Parameter(description = "Order identifier", example = "01HKG6RXRZ8N9QQP8VQXK7PXJY")
            @PathVariable String id,
            @Parameter(description = "Correlation ID for request tracking", example = "550e8400-e29b-41d4-a716-446655440000")
            @RequestHeader(value = "X-Correlation-ID", required = false) String correlationId) {
        
        // Generate correlation ID if not provided
        if (correlationId == null || correlationId.trim().isEmpty()) {
            correlationId = UUID.randomUUID().toString();
        }
        
        logger.info("Fetching order by ID: {} - Correlation ID: {}", id, correlationId);
        
        try {
            Order order = queryOrderUseCase.getOrderByIdRequired(id);
            OrderResponse response = mapper.toResponse(order);
            
            HttpHeaders headers = buildResponseHeaders(correlationId);
            
            logger.info("Order retrieved successfully - ID: {}, Status: {}, Correlation ID: {}", 
                    order.getId(), order.getStatus(), correlationId);
            
            return ResponseEntity.ok()
                    .headers(headers)
                    .body(response);
                    
        } catch (Exception e) {
            logger.error("Failed to retrieve order - ID: {}, Correlation ID: {}", id, correlationId, e);
            throw e; // Will be handled by GlobalExceptionHandler
        }
    }
    
    /**
     * List all orders
     * GET /api/v1/orders
     */
    @GetMapping
    public ResponseEntity<List<OrderResponse>> listOrders(
            @RequestHeader(value = "X-Correlation-ID", required = false) String correlationId) {
        
        // Generate correlation ID if not provided
        if (correlationId == null || correlationId.trim().isEmpty()) {
            correlationId = UUID.randomUUID().toString();
        }
        
        logger.info("Listing all orders - Correlation ID: {}", correlationId);
        
        try {
            List<Order> orders = queryOrderUseCase.getAllOrders();
            List<OrderResponse> responses = orders.stream()
                    .map(mapper::toResponse)
                    .collect(Collectors.toList());
            
            HttpHeaders headers = buildResponseHeaders(correlationId);
            
            logger.info("Listed {} orders successfully - Correlation ID: {}", orders.size(), correlationId);
            
            return ResponseEntity.ok()
                    .headers(headers)
                    .body(responses);
                    
        } catch (Exception e) {
            logger.error("Failed to list orders - Correlation ID: {}", correlationId, e);
            throw e; // Will be handled by GlobalExceptionHandler
        }
    }
    
    /**
     * Get orders by customer ID
     * GET /api/v1/orders/customer/{customerId}
     */
    @GetMapping("/customer/{customerId}")
    public ResponseEntity<List<OrderResponse>> getOrdersByCustomer(
            @PathVariable String customerId,
            @RequestHeader(value = "X-Correlation-ID", required = false) String correlationId) {
        
        // Generate correlation ID if not provided
        if (correlationId == null || correlationId.trim().isEmpty()) {
            correlationId = UUID.randomUUID().toString();
        }
        
        logger.info("Fetching orders for customer: {} - Correlation ID: {}", customerId, correlationId);
        
        try {
            List<Order> orders = queryOrderUseCase.getOrdersByCustomerId(customerId);
            List<OrderResponse> responses = orders.stream()
                    .map(mapper::toResponse)
                    .collect(Collectors.toList());
            
            HttpHeaders headers = buildResponseHeaders(correlationId);
            
            logger.info("Found {} orders for customer: {} - Correlation ID: {}", 
                    orders.size(), customerId, correlationId);
            
            return ResponseEntity.ok()
                    .headers(headers)
                    .body(responses);
                    
        } catch (Exception e) {
            logger.error("Failed to retrieve orders for customer: {} - Correlation ID: {}", 
                    customerId, correlationId, e);
            throw e; // Will be handled by GlobalExceptionHandler
        }
    }
    
    /**
     * Build standard response headers
     */
    private HttpHeaders buildResponseHeaders(String correlationId) {
        HttpHeaders headers = new HttpHeaders();
        headers.set("API-Version", API_VERSION);
        headers.set("X-Correlation-ID", correlationId);
        headers.set("Content-Type", "application/json");
        return headers;
    }
}
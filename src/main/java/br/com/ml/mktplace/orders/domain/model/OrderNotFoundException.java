package br.com.ml.mktplace.orders.domain.model;

/**
 * Exceção lançada quando um pedido não é encontrado no sistema
 */
public class OrderNotFoundException extends RuntimeException {
    
    private final String orderId;
    
    public OrderNotFoundException(String orderId) {
        super("Order not found with ID: " + orderId);
        this.orderId = orderId;
    }
    
    public OrderNotFoundException(String orderId, Throwable cause) {
        super("Order not found with ID: " + orderId, cause);
        this.orderId = orderId;
    }
    
    public String getOrderId() {
        return orderId;
    }
}
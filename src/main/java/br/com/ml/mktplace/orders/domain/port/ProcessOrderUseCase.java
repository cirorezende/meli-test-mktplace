package br.com.ml.mktplace.orders.domain.port;

import br.com.ml.mktplace.orders.domain.model.Order;
import br.com.ml.mktplace.orders.domain.model.OrderNotFoundException;
import br.com.ml.mktplace.orders.domain.model.ExternalServiceException;

/**
 * Porta de entrada para processamento de pedidos
 */
public interface ProcessOrderUseCase {
    
    /**
     * Processa um pedido, realizando o roteamento inteligente dos itens
     * para os centros de distribuição mais próximos
     * 
     * @param orderId identificador do pedido a ser processado
     * @return o pedido processado
     * @throws OrderNotFoundException se o pedido não for encontrado
     * @throws ExternalServiceException se houver falha na comunicação com serviços externos
     * @throws IllegalArgumentException se orderId for null ou vazio
     * @throws ProcessOrderException se houver falha no processamento
     */
    Order processOrder(String orderId);
    
    /**
     * Reprocessa um pedido que falhou anteriormente
     * 
     * @param orderId identificador do pedido a ser reprocessado
     * @return o pedido processado
     * @throws OrderNotFoundException se o pedido não for encontrado
     * @throws ExternalServiceException se houver falha na comunicação com serviços externos
     * @throws IllegalArgumentException se orderId for null ou vazio
     * @throws ProcessOrderException se houver falha no processamento
     */
    Order reprocessOrder(String orderId);
    
    /**
     * Resultado do processamento de pedido
     */
    record ProcessOrderResult(
        Order order,
        boolean success,
        String message,
        int itemsProcessed,
        int itemsFailed
    ) {
        public ProcessOrderResult {
            if (order == null) {
                throw new IllegalArgumentException("Order cannot be null");
            }
        }
        
        public boolean hasFailures() {
            return itemsFailed > 0;
        }
        
        public boolean isPartialSuccess() {
            return success && hasFailures();
        }
    }
    
    /**
     * Exceção específica para falhas no processamento de pedidos
     */
    class ProcessOrderException extends RuntimeException {
        private final String orderId;
        
        public ProcessOrderException(String orderId, String message) {
            super("Failed to process order [" + orderId + "]: " + message);
            this.orderId = orderId;
        }
        
        public ProcessOrderException(String orderId, String message, Throwable cause) {
            super("Failed to process order [" + orderId + "]: " + message, cause);
            this.orderId = orderId;
        }
        
        public String getOrderId() {
            return orderId;
        }
    }
}
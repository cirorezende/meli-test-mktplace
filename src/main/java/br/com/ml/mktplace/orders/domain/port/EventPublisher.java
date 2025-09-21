package br.com.ml.mktplace.orders.domain.port;

import br.com.ml.mktplace.orders.domain.model.Order;

/**
 * Porta de saída para publicação de eventos de domínio
 */
public interface EventPublisher {
    
    /**
     * Publica evento de pedido processado com sucesso
     * 
     * @param order o pedido que foi processado
     * @throws IllegalArgumentException se order for null
     */
    void publishOrderProcessed(Order order);
    
    /**
     * Publica evento de falha no processamento do pedido
     * 
     * @param order o pedido que falhou
     * @param reason motivo da falha
     * @param error exceção que causou a falha (opcional)
     * @throws IllegalArgumentException se order ou reason forem null
     */
    void publishOrderFailed(Order order, String reason, Throwable error);
    
    /**
     * Publica evento de falha no processamento do pedido
     * 
     * @param order o pedido que falhou  
     * @param reason motivo da falha
     * @throws IllegalArgumentException se order ou reason forem null
     */
    void publishOrderFailed(Order order, String reason);
    
    /**
     * Publica evento de pedido criado
     * 
     * @param order o pedido que foi criado
     * @throws IllegalArgumentException se order for null
     */
    void publishOrderCreated(Order order);
    
    /**
     * Publica evento genérico de domínio
     * 
     * @param eventType tipo do evento
     * @param eventData dados do evento
     * @throws IllegalArgumentException se eventType for null ou vazio
     */
    void publishDomainEvent(String eventType, Object eventData);
}
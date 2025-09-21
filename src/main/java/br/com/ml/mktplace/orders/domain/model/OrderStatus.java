package br.com.ml.mktplace.orders.domain.model;

/**
 * Status possíveis para um pedido no sistema
 */
public enum OrderStatus {
    
    /**
     * Pedido recebido e aguardando processamento
     */
    RECEIVED,
    
    /**
     * Pedido em processamento (algoritmo de roteamento)
     */
    PROCESSING,
    
    /**
     * Pedido processado com sucesso
     */
    PROCESSED,
    
    /**
     * Falha no processamento do pedido
     */
    FAILED
}
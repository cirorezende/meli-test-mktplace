package br.com.ml.mktplace.orders.domain.port;

import br.com.ml.mktplace.orders.domain.model.Order;

import java.util.Optional;

/**
 * Porta de saída para cache de pedidos
 */
public interface OrderCacheRepository {
    
    /**
     * Armazena um pedido no cache
     * 
     * @param order o pedido a ser cacheado
     */
    void cache(Order order);
    
    /**
     * Busca um pedido no cache
     * 
     * @param orderId o ID do pedido
     * @return Optional contendo o pedido se encontrado no cache
     */
    Optional<Order> getCachedOrder(String orderId);
    
    /**
     * Remove um pedido específico do cache
     * 
     * @param orderId o ID do pedido a ser removido
     */
    void evict(String orderId);
    
    /**
     * Remove todos os pedidos do cache
     */
    void evictAll();
    
    /**
     * Verifica se um pedido existe no cache
     * 
     * @param orderId o ID do pedido
     * @return true se o pedido existir no cache, false caso contrário
     */
    boolean exists(String orderId);
}
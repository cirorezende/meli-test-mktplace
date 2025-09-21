package br.com.ml.mktplace.orders.domain.port;

import br.com.ml.mktplace.orders.domain.model.Order;
import br.com.ml.mktplace.orders.domain.model.OrderNotFoundException;
import br.com.ml.mktplace.orders.domain.model.OrderStatus;

import java.util.List;
import java.util.Optional;

/**
 * Porta de entrada para consulta de pedidos
 */
public interface QueryOrderUseCase {
    
    /**
     * Busca um pedido pelo seu identificador
     * 
     * @param orderId identificador do pedido
     * @return Optional contendo o pedido se encontrado
     * @throws IllegalArgumentException se orderId for null ou vazio
     */
    Optional<Order> getOrderById(String orderId);
    
    /**
     * Busca um pedido pelo seu identificador, lançando exceção se não encontrado
     * 
     * @param orderId identificador do pedido
     * @return o pedido encontrado
     * @throws OrderNotFoundException se o pedido não for encontrado
     * @throws IllegalArgumentException se orderId for null ou vazio
     */
    Order getOrderByIdRequired(String orderId);
    
    /**
     * Busca pedidos de um cliente específico
     * 
     * @param customerId identificador do cliente
     * @return lista de pedidos do cliente
     * @throws IllegalArgumentException se customerId for null ou vazio
     */
    List<Order> getOrdersByCustomerId(String customerId);
    
    /**
     * Busca pedidos por status
     * 
     * @param status status dos pedidos a serem buscados
     * @return lista de pedidos com o status especificado
     * @throws IllegalArgumentException se status for null
     */
    List<Order> getOrdersByStatus(OrderStatus status);
    
    /**
     * Lista todos os pedidos do sistema
     * 
     * @return lista de todos os pedidos
     */
    List<Order> getAllOrders();
    
    /**
     * Verifica se um pedido existe
     * 
     * @param orderId identificador do pedido
     * @return true se o pedido existir, false caso contrário
     * @throws IllegalArgumentException se orderId for null ou vazio
     */
    boolean orderExists(String orderId);
    
    /**
     * Critérios de busca para pedidos
     */
    record OrderSearchCriteria(
        String customerId,
        OrderStatus status,
        String itemId,
        int page,
        int size
    ) {
        public OrderSearchCriteria {
            if (page < 0) {
                throw new IllegalArgumentException("Page cannot be negative");
            }
            if (size <= 0) {
                throw new IllegalArgumentException("Size must be positive");
            }
        }
        
        public static OrderSearchCriteria defaultCriteria() {
            return new OrderSearchCriteria(null, null, null, 0, 10);
        }
    }
    
    /**
     * Resultado paginado de busca de pedidos
     */
    record OrderSearchResult(
        List<Order> orders,
        int totalElements,
        int totalPages,
        int currentPage,
        int pageSize
    ) {
        public OrderSearchResult {
            if (orders == null) {
                throw new IllegalArgumentException("Orders list cannot be null");
            }
            if (totalElements < 0) {
                throw new IllegalArgumentException("Total elements cannot be negative");
            }
            if (totalPages < 0) {
                throw new IllegalArgumentException("Total pages cannot be negative");
            }
            if (currentPage < 0) {
                throw new IllegalArgumentException("Current page cannot be negative");
            }
            if (pageSize <= 0) {
                throw new IllegalArgumentException("Page size must be positive");
            }
        }
        
        public boolean hasNext() {
            return currentPage < totalPages - 1;
        }
        
        public boolean hasPrevious() {
            return currentPage > 0;
        }
        
        public boolean isEmpty() {
            return orders.isEmpty();
        }
    }
    
    /**
     * Busca pedidos com critérios de filtro e paginação
     * 
     * @param criteria critérios de busca
     * @return resultado paginado da busca
     * @throws IllegalArgumentException se criteria for null
     */
    OrderSearchResult searchOrders(OrderSearchCriteria criteria);
}
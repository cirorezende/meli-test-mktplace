package br.com.ml.mktplace.orders.domain.port;

import br.com.ml.mktplace.orders.domain.model.Order;
import br.com.ml.mktplace.orders.domain.model.OrderNotFoundException;

import java.util.List;
import java.util.Optional;

/**
 * Porta de saída para persistência de pedidos
 */
public interface OrderRepository {
    
    /**
     * Salva um pedido no repositório
     * 
     * @param order o pedido a ser salvo
     * @return o pedido salvo
     * @throws IllegalArgumentException se o pedido for null
     */
    Order save(Order order);
    
    /**
     * Busca um pedido pelo seu identificador
     * 
     * @param orderId o ID do pedido
     * @return Optional contendo o pedido se encontrado
     * @throws IllegalArgumentException se o ID for null ou vazio
     */
    Optional<Order> findById(String orderId);
    
    /**
     * Busca um pedido pelo seu identificador, lançando exceção se não encontrado
     * 
     * @param orderId o ID do pedido
     * @return o pedido encontrado
     * @throws OrderNotFoundException se o pedido não for encontrado
     * @throws IllegalArgumentException se o ID for null ou vazio
     */
    Order getById(String orderId);
    
    /**
     * Lista todos os pedidos do sistema
     * 
     * @return lista de todos os pedidos
     */
    List<Order> findAll();
    
    /**
     * Verifica se um pedido existe pelo seu ID
     * 
     * @param orderId o ID do pedido
     * @return true se o pedido existir, false caso contrário
     * @throws IllegalArgumentException se o ID for null ou vazio
     */
    boolean existsById(String orderId);
    
}
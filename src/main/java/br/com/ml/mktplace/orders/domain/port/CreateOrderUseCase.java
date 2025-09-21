package br.com.ml.mktplace.orders.domain.port;

import br.com.ml.mktplace.orders.domain.model.Order;
import br.com.ml.mktplace.orders.domain.model.OrderItem;
import br.com.ml.mktplace.orders.domain.model.Address;

import java.util.List;

/**
 * Porta de entrada para criação de pedidos
 */
public interface CreateOrderUseCase {
    
    /**
     * Cria um novo pedido no sistema
     * 
     * @param customerId identificador do cliente
     * @param items lista de itens do pedido
     * @param deliveryAddress endereço de entrega
     * @return o pedido criado
     * @throws IllegalArgumentException se algum parâmetro for inválido
     */
    Order createOrder(String customerId, List<OrderItem> items, Address deliveryAddress);
    
    /**
     * Dados de entrada para criação de pedido
     */
    record CreateOrderCommand(
        String customerId,
        List<OrderItem> items,
        Address deliveryAddress
    ) {
        public CreateOrderCommand {
            if (customerId == null || customerId.trim().isEmpty()) {
                throw new IllegalArgumentException("Customer ID is required");
            }
            if (items == null || items.isEmpty()) {
                throw new IllegalArgumentException("Items list cannot be empty");
            }
            if (deliveryAddress == null) {
                throw new IllegalArgumentException("Delivery address is required");
            }
        }
    }
}
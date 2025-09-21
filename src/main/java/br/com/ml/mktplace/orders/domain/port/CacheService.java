package br.com.ml.mktplace.orders.domain.port;

import java.time.Duration;
import java.util.Optional;

/**
 * Porta de saída para operações de cache
 */
public interface CacheService {
    
    /**
     * Recupera um valor do cache
     * 
     * @param key a chave do cache
     * @param valueType o tipo do valor esperado
     * @return Optional contendo o valor se encontrado no cache
     * @throws IllegalArgumentException se key for null ou vazia, ou valueType for null
     */
    <T> Optional<T> get(String key, Class<T> valueType);
    
    /**
     * Armazena um valor no cache
     * 
     * @param key a chave do cache
     * @param value o valor a ser armazenado
     * @param ttl tempo de vida do cache
     * @throws IllegalArgumentException se key for null ou vazia, ou ttl for negativo
     */
    <T> void put(String key, T value, Duration ttl);
    
    /**
     * Armazena um valor no cache com TTL padrão
     * 
     * @param key a chave do cache
     * @param value o valor a ser armazenado
     * @throws IllegalArgumentException se key for null ou vazia
     */
    <T> void put(String key, T value);
    
    /**
     * Remove um valor do cache
     * 
     * @param key a chave do cache
     * @throws IllegalArgumentException se key for null ou vazia
     */
    void evict(String key);
    
    /**
     * Remove todos os valores do cache com padrão de chave
     * 
     * @param pattern padrão de chaves a serem removidas (ex: "orders:*")
     * @throws IllegalArgumentException se pattern for null ou vazio
     */
    void evictByPattern(String pattern);
    
    /**
     * Limpa todo o cache
     */
    void clear();
    
    /**
     * Verifica se uma chave existe no cache
     * 
     * @param key a chave do cache
     * @return true se a chave existir, false caso contrário
     * @throws IllegalArgumentException se key for null ou vazia
     */
    boolean exists(String key);
}
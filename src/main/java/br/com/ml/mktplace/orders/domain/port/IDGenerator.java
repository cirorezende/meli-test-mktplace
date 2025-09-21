package br.com.ml.mktplace.orders.domain.port;

/**
 * Porta de saída para geração de identificadores únicos
 */
public interface IDGenerator {
    
    /**
     * Gera um identificador único no formato ULID
     * 
     * @return um identificador único como string
     */
    String generate();
    
    /**
     * Gera múltiplos identificadores únicos
     * 
     * @param count quantidade de identificadores a serem gerados
     * @return array de identificadores únicos
     * @throws IllegalArgumentException se count for menor ou igual a zero
     */
    String[] generateMultiple(int count);
    
    /**
     * Valida se um identificador está no formato correto
     * 
     * @param id o identificador a ser validado
     * @return true se o identificador for válido, false caso contrário
     * @throws IllegalArgumentException se id for null
     */
    boolean isValid(String id);
}
package br.com.ml.mktplace.orders.domain.model;

/**
 * Exception lançada quando um centro de distribuição não é encontrado
 */
public class DistributionCenterNotFoundException extends RuntimeException {
    
    public DistributionCenterNotFoundException(String message) {
        super(message);
    }
    
    public DistributionCenterNotFoundException(String message, Throwable cause) {
        super(message, cause);
    }
}
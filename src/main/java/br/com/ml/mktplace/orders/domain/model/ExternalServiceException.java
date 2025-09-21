package br.com.ml.mktplace.orders.domain.model;

/**
 * Exceção lançada quando ocorre falha na comunicação com serviços externos
 */
public class ExternalServiceException extends RuntimeException {
    
    private final String serviceName;
    
    public ExternalServiceException(String serviceName, String message) {
        super("External service error [" + serviceName + "]: " + message);
        this.serviceName = serviceName;
    }
    
    public ExternalServiceException(String serviceName, String message, Throwable cause) {
        super("External service error [" + serviceName + "]: " + message, cause);
        this.serviceName = serviceName;
    }
    
    public String getServiceName() {
        return serviceName;
    }
}
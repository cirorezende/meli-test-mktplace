package br.com.ml.mktplace.orders.domain.port;

import br.com.ml.mktplace.orders.domain.model.DistributionCenter;
import br.com.ml.mktplace.orders.domain.model.ExternalServiceException;

import java.util.List;

/**
 * Porta de saída para consulta de centros de distribuição por item
 */
public interface DistributionCenterService {
    
    /**
     * Busca centros de distribuição que possuem um determinado item em estoque
     * 
     * @param itemId o identificador do item
     * @return lista de centros de distribuição que possuem o item
     * @throws ExternalServiceException se houver falha na comunicação com o serviço externo
     * @throws IllegalArgumentException se o itemId for null ou vazio
     */
    List<DistributionCenter> findDistributionCentersByItem(String itemId);
    
    /**
     * Busca centros de distribuição que possuem múltiplos itens em estoque
     * 
     * @param itemIds lista de identificadores dos itens
     * @return lista de centros de distribuição que possuem pelo menos um dos itens
     * @throws ExternalServiceException se houver falha na comunicação com o serviço externo
     * @throws IllegalArgumentException se a lista for null ou vazia
     */
    List<DistributionCenter> findDistributionCentersByItems(List<String> itemIds);
    
    /**
     * Busca todos os centros de distribuição disponíveis
     * 
     * @return lista de todos os centros de distribuição
     * @throws ExternalServiceException se houver falha na comunicação com o serviço externo
     */
    List<DistributionCenter> findAllDistributionCenters();
}
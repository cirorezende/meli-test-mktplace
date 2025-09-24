package br.com.ml.mktplace.orders.domain.port;

import java.util.List;

/**
 * Porta de saída para consulta de centros de distribuição por item.
 * A API externa retorna ESTRITAMENTE um array de strings contendo os IDs (códigos) dos CDs.
 */
public interface DistributionCenterService {

    /**
     * Busca códigos dos centros de distribuição que possuem um determinado item em estoque.
     * @param itemId o identificador do item
     * @return lista de códigos dos CDs
     */
    List<String> findDistributionCentersByItem(String itemId);

    /**
     * Busca códigos dos centros de distribuição que possuem pelo menos um dos itens informados.
     * @param itemIds lista de identificadores dos itens
     * @return lista de códigos dos CDs
     */
    List<String> findDistributionCentersByItems(List<String> itemIds);

    /**
     * Busca todos os códigos de centros de distribuição disponíveis.
     * @return lista de códigos dos CDs
     */
    List<String> findAllDistributionCenters();
}
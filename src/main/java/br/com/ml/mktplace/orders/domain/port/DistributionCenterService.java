package br.com.ml.mktplace.orders.domain.port;

import java.util.List;

/**
 * Porta de saída para consulta de centros de distribuição por item.
 * A API externa retorna ESTRITAMENTE um array de strings contendo os IDs (códigos) dos CDs.
 *
 * Restrições obrigatórias:
 * - Apenas consultas por 1 item por vez são permitidas.
 * - Não é permitido buscar múltiplos items em uma única chamada.
 * - Não é permitido buscar a lista de todos os CDs.
 */
public interface DistributionCenterService {

    /**
     * Busca códigos dos centros de distribuição que possuem um determinado item em estoque.
     * @param itemId o identificador do item
     * @return lista de códigos dos CDs
     */
    List<String> findDistributionCentersByItem(String itemId);
}
package br.com.ml.mktplace.orders.domain.model;

import jakarta.validation.constraints.NotBlank;

/**
 * Value object representando um Centro de Distribuição próximo a um endereço
 * com a distância calculada (em quilômetros).
 */
public record NearbyDistributionCenter(
        @NotBlank String code,
        double distanceKm
) {}

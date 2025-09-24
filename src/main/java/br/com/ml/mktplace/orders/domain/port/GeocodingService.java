package br.com.ml.mktplace.orders.domain.port;

import br.com.ml.mktplace.orders.domain.model.Address;

/**
 * Port for geocoding lookup. Implementations call external APIs to resolve coordinates
 * from a textual address. We keep it minimal: street/number/city/state/country/zip.
 */
public interface GeocodingService {
    /**
     * Resolve coordinates for a given address textual components.
     * @return Address.Coordinates or null if not found.
     */
    Address.Coordinates geocode(String street, String number, String city, String state, String country, String zipCode);
}

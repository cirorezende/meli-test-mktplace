package br.com.ml.mktplace.orders.domain.service;

import br.com.ml.mktplace.orders.domain.model.Address;
import br.com.ml.mktplace.orders.domain.model.DistributionCenter;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.util.List;

import static org.assertj.core.api.Assertions.*;

@DisplayName("DistributionCenterSelectionService Unit Tests")
class DistributionCenterSelectionServiceTest {

    private DistributionCenterSelectionService service;
    
    private Address deliveryAddress;
    private DistributionCenter closeCenter;
    private DistributionCenter farCenter;
    private DistributionCenter mediumCenter;
    private List<DistributionCenter> availableCenters;
    
    @BeforeEach
    void setUp() {
        service = new DistributionCenterSelectionService();
        
        // Delivery address in Springfield, IL
        deliveryAddress = new Address(
            "123 Main St",
            "Springfield",
            "IL",
            "USA",
            "12345-678",
            new Address.Coordinates(
                BigDecimal.valueOf(39.7817), // Springfield, IL coordinates
                BigDecimal.valueOf(-89.6501)
            )
        );
        
        // Close distribution center (nearby in Springfield)
        closeCenter = new DistributionCenter(
            "DC-CLOSE",
            "Close Distribution Center",
            new Address(
                "456 Warehouse Ave",
                "Springfield",
                "IL",
                "USA",
                "12345-000",
                new Address.Coordinates(
                    BigDecimal.valueOf(39.7900), // Very close to delivery
                    BigDecimal.valueOf(-89.6400)
                )
            )
        );
        
        // Medium distance distribution center (Chicago, IL)
        mediumCenter = new DistributionCenter(
            "DC-MEDIUM",
            "Medium Distribution Center",
            new Address(
                "789 Distribution Blvd",
                "Chicago",
                "IL",
                "USA",
                "60601-000",
                new Address.Coordinates(
                    BigDecimal.valueOf(41.8781), // Chicago coordinates
                    BigDecimal.valueOf(-87.6298)
                )
            )
        );
        
        // Far distribution center (Los Angeles, CA)
        farCenter = new DistributionCenter(
            "DC-FAR",
            "Far Distribution Center",
            new Address(
                "321 Logistics Way",
                "Los Angeles",
                "CA",
                "USA",
                "90210-000",
                new Address.Coordinates(
                    BigDecimal.valueOf(34.0522), // Los Angeles coordinates
                    BigDecimal.valueOf(-118.2437)
                )
            )
        );
        
        availableCenters = List.of(farCenter, mediumCenter, closeCenter); // Intentionally unsorted
    }
    
    @Test
    @DisplayName("Should select closest distribution center")
    void shouldSelectClosestDistributionCenter() {
        // When
        DistributionCenter result = service.selectDistributionCenter(availableCenters, deliveryAddress);
        
        // Then
        assertThat(result).isEqualTo(closeCenter);
        assertThat(result.code()).isEqualTo("DC-CLOSE");
    }
    
    @Test
    @DisplayName("Should select closest from multiple centers with different distances")
    void shouldSelectClosestFromMultipleCentersWithDifferentDistances() {
        // When
        DistributionCenter result = service.selectDistributionCenter(availableCenters, deliveryAddress);
        
        // Then
        assertThat(result).isEqualTo(closeCenter);
        assertThat(result.code()).isEqualTo("DC-CLOSE");
        assertThat(result.name()).isEqualTo("Close Distribution Center");
    }
    
    @Test
    @DisplayName("Should handle single distribution center")
    void shouldHandleSingleDistributionCenter() {
        // Given
        List<DistributionCenter> singleCenter = List.of(mediumCenter);
        
        // When
        DistributionCenter result = service.selectDistributionCenter(singleCenter, deliveryAddress);
        
        // Then
        assertThat(result).isEqualTo(mediumCenter);
        assertThat(result.code()).isEqualTo("DC-MEDIUM");
    }
    
    @Test
    @DisplayName("Should calculate distance correctly between same coordinates")
    void shouldCalculateDistanceCorrectlyBetweenSameCoordinates() {
        // Given - delivery address and center at exact same location
        DistributionCenter sameLocationCenter = new DistributionCenter(
            "DC-SAME",
            "Same Location Center",
            deliveryAddress
        );
        List<DistributionCenter> centers = List.of(closeCenter, sameLocationCenter);
        
        // When
        DistributionCenter result = service.selectDistributionCenter(centers, deliveryAddress);
        
        // Then - should select the one at exact same location (distance = 0)
        assertThat(result).isEqualTo(sameLocationCenter);
    }
    
    @Test
    @DisplayName("Should throw exception when available centers list is null")
    void shouldThrowExceptionWhenAvailableCentersListIsNull() {
        // When/Then
        assertThatThrownBy(() -> service.selectDistributionCenter(null, deliveryAddress))
            .isInstanceOf(IllegalArgumentException.class)
            .hasMessage("Available centers list cannot be null or empty");
    }
    
    @Test
    @DisplayName("Should throw exception when available centers list is empty")
    void shouldThrowExceptionWhenAvailableCentersListIsEmpty() {
        // When/Then
        assertThatThrownBy(() -> service.selectDistributionCenter(List.of(), deliveryAddress))
            .isInstanceOf(IllegalArgumentException.class)
            .hasMessage("Available centers list cannot be null or empty");
    }
    
    @Test
    @DisplayName("Should throw exception when delivery address is null")
    void shouldThrowExceptionWhenDeliveryAddressIsNull() {
        // When/Then
        assertThatThrownBy(() -> service.selectDistributionCenter(availableCenters, null))
            .isInstanceOf(IllegalArgumentException.class)
            .hasMessage("Delivery address cannot be null");
    }
    
    @Test
    @DisplayName("Should handle distribution centers with identical coordinates")
    void shouldHandleDistributionCentersWithIdenticalCoordinates() {
        // Given - two centers at exactly the same location
        DistributionCenter identicalCenter1 = new DistributionCenter(
            "DC-ID1",
            "Identical Center 1",
            closeCenter.address()
        );
        
        DistributionCenter identicalCenter2 = new DistributionCenter(
            "DC-ID2", 
            "Identical Center 2",
            closeCenter.address()
        );
        
        List<DistributionCenter> identicalCenters = List.of(identicalCenter1, identicalCenter2, farCenter);
        
        // When
        DistributionCenter result = service.selectDistributionCenter(identicalCenters, deliveryAddress);
        
        // Then - should select one of the identical centers (first one found)
        assertThat(result.getCoordinates()).isEqualTo(closeCenter.getCoordinates());
        assertThat(result.code()).isIn("DC-ID1", "DC-ID2");
    }
    
    @Test
    @DisplayName("Should work with extreme coordinates")
    void shouldWorkWithExtremeCoordinates() {
        // Given - addresses at extreme coordinates
        Address northPole = new Address(
            "North Pole",
            "Arctic",
            "Arctic",
            "NORTH",
            "00000-000",
            new Address.Coordinates(
                BigDecimal.valueOf(90.0),  // North Pole
                BigDecimal.valueOf(0.0)
            )
        );
        
        Address southPole = new Address(
            "South Pole",
            "Antarctic",
            "Antarctic", 
            "SOUTH",
            "00000-000",
            new Address.Coordinates(
                BigDecimal.valueOf(-90.0), // South Pole
                BigDecimal.valueOf(0.0)
            )
        );
        
        DistributionCenter northCenter = new DistributionCenter("DC-NORTH", "North Center", northPole);
        DistributionCenter southCenter = new DistributionCenter("DC-SOUTH", "South Center", southPole);
        
        List<DistributionCenter> extremeCenters = List.of(northCenter, southCenter);
        
        // When - delivery to Springfield should be closer to North Pole than South Pole
        DistributionCenter result = service.selectDistributionCenter(extremeCenters, deliveryAddress);
        
        // Then
        assertThat(result).isEqualTo(northCenter);
    }
    
    @Test
    @DisplayName("Should handle international date line crossing")
    void shouldHandleInternationalDateLineCrossing() {
        // Given - addresses that cross international date line
        Address eastOfDateLine = new Address(
            "East of Date Line",
            "Fiji",
            "Pacific",
            "FIJI",
            "00000-000",
            new Address.Coordinates(
                BigDecimal.valueOf(-18.1248),
                BigDecimal.valueOf(178.4501) // East of date line
            )
        );
        
        Address westOfDateLine = new Address(
            "West of Date Line",
            "Samoa",
            "Pacific",
            "SAMOA", 
            "00000-000",
            new Address.Coordinates(
                BigDecimal.valueOf(-13.7590),
                BigDecimal.valueOf(-172.1046) // West of date line
            )
        );
        
        DistributionCenter eastCenter = new DistributionCenter("DC-EAST", "East Center", eastOfDateLine);
        DistributionCenter westCenter = new DistributionCenter("DC-WEST", "West Center", westOfDateLine);
        
        List<DistributionCenter> dateline_Centers = List.of(eastCenter, westCenter);
        
        // When - should select one without throwing exception
        DistributionCenter result = service.selectDistributionCenter(dateline_Centers, deliveryAddress);
        
        // Then - should select one of them (algorithm should handle date line crossing)
        assertThat(result).isIn(eastCenter, westCenter);
    }
    
    @Test
    @DisplayName("Should consistently select same center for same input")
    void shouldConsistentlySelectSameCenterForSameInput() {
        // When - call multiple times with same input
        DistributionCenter result1 = service.selectDistributionCenter(availableCenters, deliveryAddress);
        DistributionCenter result2 = service.selectDistributionCenter(availableCenters, deliveryAddress);
        DistributionCenter result3 = service.selectDistributionCenter(availableCenters, deliveryAddress);
        
        // Then - should always return the same result
        assertThat(result1).isEqualTo(result2);
        assertThat(result2).isEqualTo(result3);
        assertThat(result1).isEqualTo(closeCenter);
    }
    
    @Test
    @DisplayName("Should handle very small coordinate differences")
    void shouldHandleVerySmallCoordinateDifferences() {
        // Given - centers with very small coordinate differences (within same block)
        DistributionCenter microClose1 = new DistributionCenter(
            "DC-MICRO1",
            "Micro Close 1",
            new Address(
                "100 Main St",
                "Springfield",
                "IL",
                "USA",
                "12345-678",
                new Address.Coordinates(
                    BigDecimal.valueOf(39.7817),
                    BigDecimal.valueOf(-89.6501)
                )
            )
        );
        
        DistributionCenter microClose2 = new DistributionCenter(
            "DC-MICRO2", 
            "Micro Close 2",
            new Address(
                "101 Main St", 
                "Springfield",
                "IL",
                "USA",
                "12345-678",
                new Address.Coordinates(
                    BigDecimal.valueOf(39.7818), // Tiny difference
                    BigDecimal.valueOf(-89.6502)
                )
            )
        );
        
        List<DistributionCenter> microCenters = List.of(microClose1, microClose2);
        
        // When
        DistributionCenter result = service.selectDistributionCenter(microCenters, deliveryAddress);
        
        // Then - should select one of them consistently 
        assertThat(result).isIn(microClose1, microClose2);
    }
}
package br.com.ml.mktplace.orders.domain.service;

import br.com.ml.mktplace.orders.domain.model.Address;
import br.com.ml.mktplace.orders.domain.model.DistributionCenter;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * Service responsible for selecting the most appropriate distribution center
 * based on geographic proximity using simple distance calculation.
 */
@Service
public class DistributionCenterSelectionService {

    /**
     * Selects the closest distribution center to the delivery address
     * using simple distance calculation based on coordinates.
     * 
     * @param availableCenters list of available distribution centers
     * @param deliveryAddress target delivery address
     * @return the closest distribution center
     * @throws IllegalArgumentException if parameters are null or centers list is empty
     */
    public DistributionCenter selectDistributionCenter(
            List<DistributionCenter> availableCenters, 
            Address deliveryAddress) {
        
        if (availableCenters == null || availableCenters.isEmpty()) {
            throw new IllegalArgumentException("Available centers list cannot be null or empty");
        }
        
        if (deliveryAddress == null) {
            throw new IllegalArgumentException("Delivery address cannot be null");
        }
        
        DistributionCenter closestCenter = null;
        double minDistance = Double.MAX_VALUE;
        
        for (DistributionCenter center : availableCenters) {
            double distance = calculateDistance(
                deliveryAddress.coordinates().latitude().doubleValue(), 
                deliveryAddress.coordinates().longitude().doubleValue(),
                center.getCoordinates().latitude().doubleValue(),
                center.getCoordinates().longitude().doubleValue()
            );
            
            if (distance < minDistance) {
                minDistance = distance;
                closestCenter = center;
            }
        }
        
        if (closestCenter == null) {
            throw new IllegalStateException("Could not select any distribution center");
        }
        
        return closestCenter;
    }
    
    /**
     * Calculates the distance between two geographic points using Haversine formula.
     * This is a simple implementation for the first version (ADR-009).
     * 
     * @param lat1 latitude of first point
     * @param lon1 longitude of first point
     * @param lat2 latitude of second point
     * @param lon2 longitude of second point
     * @return distance in kilometers
     */
    private double calculateDistance(double lat1, double lon1, double lat2, double lon2) {
        final double EARTH_RADIUS_KM = 6371.0;
        
        double dLat = Math.toRadians(lat2 - lat1);
        double dLon = Math.toRadians(lon2 - lon1);
        
        double a = Math.sin(dLat / 2) * Math.sin(dLat / 2) +
                  Math.cos(Math.toRadians(lat1)) * Math.cos(Math.toRadians(lat2)) *
                  Math.sin(dLon / 2) * Math.sin(dLon / 2);
        
        double c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));
        
        return EARTH_RADIUS_KM * c;
    }
}
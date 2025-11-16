package co.edu.uniquindio.poo.ProyectoFinal2025_2.Util.UtilModel;

import co.edu.uniquindio.poo.ProyectoFinal2025_2.Model.Address;

/**
 * Utility class for calculating distances between addresses.
 * Uses the Haversine formula to calculate great-circle distances.
 */
public class DistanceCalculator {

    private static final int EARTH_RADIUS_KM = 6371;

    /**
     * Calculates the distance between two coordinates using the Haversine formula.
     * @param lat1 Latitude of point 1 in degrees
     * @param lon1 Longitude of point 1 in degrees
     * @param lat2 Latitude of point 2 in degrees
     * @param lon2 Longitude of point 2 in degrees
     * @return Distance in kilometers
     */
    public static double calculateDistance(double lat1, double lon1, double lat2, double lon2) {
        if (lat1 == lat2 && lon1 == lon2) {
            return 0.0;
        }

        double dLat = Math.toRadians(lat2 - lat1);
        double dLon = Math.toRadians(lon2 - lon1);

        double a = Math.sin(dLat / 2) * Math.sin(dLat / 2) +
                   Math.cos(Math.toRadians(lat1)) * Math.cos(Math.toRadians(lat2)) *
                   Math.sin(dLon / 2) * Math.sin(dLon / 2);

        double c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));

        return EARTH_RADIUS_KM * c;
    }

    /**
     * Calculates the distance between two addresses.
     * Note: This is a placeholder implementation. In a real system, you would use
     * a geocoding service to get coordinates from addresses.
     * For now, we'll use a simplified distance based on city/zone.
     *
     * @param origin Origin address
     * @param destination Destination address
     * @return Distance in kilometers
     */
    public static double calculateDistance(Address origin, Address destination) {
        if (origin == null || destination == null) {
            return 0.0;
        }

        // If same city and same zipCode, assume short distance
        if (origin.getCity().equalsIgnoreCase(destination.getCity()) &&
            origin.getZipCode().equals(destination.getZipCode())) {
            return 5.0; // 5 km for same zone
        }

        // If same city but different zipCode
        if (origin.getCity().equalsIgnoreCase(destination.getCity())) {
            return 15.0; // 15 km for different zones in same city
        }

        // If same state but different city
        if (origin.getState().equalsIgnoreCase(destination.getState())) {
            return 50.0; // 50 km for different cities in same state
        }

        // Different states
        return 150.0; // 150 km for different states
    }

    /**
     * Estimates travel time in hours based on distance and average speed.
     * @param distanceKm Distance in kilometers
     * @param averageSpeedKmh Average speed in km/h (default 40 km/h for urban delivery)
     * @return Estimated time in hours
     */
    public static double estimateTravelTime(double distanceKm, double averageSpeedKmh) {
        if (averageSpeedKmh <= 0) {
            averageSpeedKmh = 40.0; // Default urban speed
        }
        return distanceKm / averageSpeedKmh;
    }

    /**
     * Estimates travel time in hours with default urban speed (40 km/h).
     * @param distanceKm Distance in kilometers
     * @return Estimated time in hours
     */
    public static double estimateTravelTime(double distanceKm) {
        return estimateTravelTime(distanceKm, 40.0);
    }
}

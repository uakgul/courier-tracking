package com.example.couriertracking.service.distance;

import com.example.couriertracking.model.enums.DistanceStrategy;
import org.springframework.stereotype.Component;

/**
 * reference:
 * https://www.baeldung.com/java-find-distance-between-points#calculate-the-distance-using-vincentys-formula
 */
@Component
public class VincentyStrategy implements DistanceCalculator {

    private static final double SEMI_MAJOR_AXIS_MT = 6378137;
    private static final double SEMI_MINOR_AXIS_MT = 6356752.314245;
    private static final double FLATTENING = 1 / 298.257223563;
    private static final double ERROR_TOLERANCE = 1e-12;

    private static final int MAX_ITERATIONS = 200;

    @Override
    public double calculateDistance(double startLat, double startLon, double endLat, double endLon) {
        double U1 = Math.atan((1 - FLATTENING) * Math.tan(Math.toRadians(startLat)));
        double U2 = Math.atan((1 - FLATTENING) * Math.tan(Math.toRadians(endLat)));

        double sinU1 = Math.sin(U1);
        double cosU1 = Math.cos(U1);
        double sinU2 = Math.sin(U2);
        double cosU2 = Math.cos(U2);

        double longitudeDifference = Math.toRadians(endLon - startLon);
        double previousLongitudeDifference;

        double sinSigma, cosSigma, sigma, sinAlpha, cosSqAlpha, cos2SigmaM;
        int iterations = 0;

        do {
            sinSigma = Math.sqrt(Math.pow(cosU2 * Math.sin(longitudeDifference), 2) +
                    Math.pow(cosU1 * sinU2 - sinU1 * cosU2 * Math.cos(longitudeDifference), 2));

            if (sinSigma == 0) {
                return 0d;
            }

            cosSigma = sinU1 * sinU2 + cosU1 * cosU2 * Math.cos(longitudeDifference);
            sigma = Math.atan2(sinSigma, cosSigma);
            sinAlpha = cosU1 * cosU2 * Math.sin(longitudeDifference) / sinSigma;
            cosSqAlpha = 1 - Math.pow(sinAlpha, 2);
            cos2SigmaM = cosSigma - 2 * sinU1 * sinU2 / cosSqAlpha;
            if (Double.isNaN(cos2SigmaM)) {
                cos2SigmaM = 0;
            }
            previousLongitudeDifference = longitudeDifference;
            double C = FLATTENING / 16 * cosSqAlpha * (4 + FLATTENING * (4 - 3 * cosSqAlpha));
            longitudeDifference = Math.toRadians(endLon - startLon) + (1 - C) * FLATTENING * sinAlpha *
                    (sigma + C * sinSigma * (cos2SigmaM + C * cosSigma * (-1 + 2 * Math.pow(cos2SigmaM, 2))));
        } while (Math.abs(longitudeDifference - previousLongitudeDifference) > ERROR_TOLERANCE
                && ++iterations < MAX_ITERATIONS);

        double uSq = cosSqAlpha * (Math.pow(SEMI_MAJOR_AXIS_MT, 2) - Math.pow(SEMI_MINOR_AXIS_MT, 2))
                / Math.pow(SEMI_MINOR_AXIS_MT, 2);

        double A = 1 + uSq / 16384 * (4096 + uSq * (-768 + uSq * (320 - 175 * uSq)));
        double B = uSq / 1024 * (256 + uSq * (-128 + uSq * (74 - 47 * uSq)));

        double deltaSigma = B * sinSigma * (cos2SigmaM + B / 4 * (cosSigma * (-1 + 2 * Math.pow(cos2SigmaM, 2))
                - B / 6 * cos2SigmaM * (-3 + 4 * Math.pow(sinSigma, 2)) * (-3 + 4 * Math.pow(cos2SigmaM, 2))));

        return SEMI_MINOR_AXIS_MT * A * (sigma - deltaSigma);
    }

    @Override
    public DistanceStrategy strategy() {
        return DistanceStrategy.VINCENTY;
    }
}

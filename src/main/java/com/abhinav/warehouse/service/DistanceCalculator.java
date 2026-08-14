package com.abhinav.warehouse.service;

import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.math.RoundingMode;

/** Haversine great-circle distance. Good enough for warehouse ranking. */
@Component
public class DistanceCalculator {

    private static final double EARTH_RADIUS_KM = 6371.0;

    public BigDecimal kmBetween(BigDecimal lat1, BigDecimal lon1,
                                BigDecimal lat2, BigDecimal lon2) {
        double φ1 = Math.toRadians(lat1.doubleValue());
        double φ2 = Math.toRadians(lat2.doubleValue());
        double Δφ = Math.toRadians(lat2.doubleValue() - lat1.doubleValue());
        double Δλ = Math.toRadians(lon2.doubleValue() - lon1.doubleValue());

        double a = Math.sin(Δφ / 2) * Math.sin(Δφ / 2)
                 + Math.cos(φ1) * Math.cos(φ2) * Math.sin(Δλ / 2) * Math.sin(Δλ / 2);
        double c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));

        return BigDecimal.valueOf(EARTH_RADIUS_KM * c).setScale(3, RoundingMode.HALF_UP);
    }
}

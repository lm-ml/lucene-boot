package com.potato.service;

import com.potato.dto.AirlineRoute;
import com.potato.dto.Airport;

import java.util.List;

public interface PermissionService {

    void save();

    List<Airport> getAirports(String name, String iATA, Double latitude, Double longitude, String city, String country);

    List<AirlineRoute> getAirlineRoutes(String sourceAirport, String destinationAirport);
}

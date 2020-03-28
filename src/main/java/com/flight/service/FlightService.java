package com.flight.service;

import com.flight.dto.AirlineRoute;
import com.flight.dto.Airport;

import java.util.List;

public interface FlightService {

    List<Airport> getAirports(String name, String iATA, Double latitude, Double longitude, String city, String country);

    List<AirlineRoute> getAirlineRoutes(String sourceCity, String destinationCity);
}

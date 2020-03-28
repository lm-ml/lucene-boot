package com.flight.controller;

import com.flight.dto.AirlineRoute;
import com.flight.dto.Airport;
import com.flight.service.FlightService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@Slf4j
@RestController
@RequestMapping(value = "/flight")
public class FlightController {

    @Autowired
    private FlightService flightService;

    @GetMapping(value = "/airports")
    public ResponseEntity getAirports(@RequestParam(value = "name", required = false) String name,
                                      @RequestParam(value = "iATA", required = false) String iATA,
                                      @RequestParam(value = "latitude", required = false) Double latitude,
                                      @RequestParam(value = "longitude", required = false) Double longitude,
                                      @RequestParam(value = "city", required = false) String city,
                                      @RequestParam(value = "country", required = false) String country) {
        List<Airport> airportList = this.flightService.getAirports(name, iATA, latitude, longitude, city, country);
        return new ResponseEntity(airportList, HttpStatus.OK);
    }

    @GetMapping(value = "/airlineRoutes")
    public ResponseEntity getAirlineRoutes(@RequestParam(value = "sourceCity", required = false) String sourceCity,
                                           @RequestParam(value = "destinationCity", required = false) String destinationCity) {
        List<AirlineRoute> airlineRoutes = this.flightService.getAirlineRoutes(sourceCity, destinationCity);
        return new ResponseEntity(airlineRoutes, HttpStatus.OK);
    }

}


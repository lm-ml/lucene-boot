package com.flight.dto;

import lombok.Data;

import java.util.List;

@Data
public class AirlineRoute {

    private Airline airline;
    private List<Route> routes;
}

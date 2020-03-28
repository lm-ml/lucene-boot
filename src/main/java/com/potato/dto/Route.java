package com.potato.dto;

import lombok.Data;

@Data
public class Route {

    private String airline;
    private String airlineId;
    private String sourceAirport;
    private String sourceAirportId;
    private String destinationAirport;
    private String destinationAirportId;
    private String codeShare;
    private String stops;
    private String equipment;
}

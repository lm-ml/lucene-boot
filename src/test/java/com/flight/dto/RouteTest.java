package com.flight.dto;

import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.*;

public class RouteTest {

    private Route route;

    @Before
    public void setup() {
        route = new Route();
    }

    @Test
    public void should_return_same_airline_value_after_invoke_set() {
        String airline = "airline";
        route.setAirline(airline);
        assertEquals(airline, route.getAirline());
    }

    @Test
    public void should_return_same_airlineId_value_after_invoke_set() {
        String airlineId = "123";
        route.setAirlineId(airlineId);
        assertEquals(airlineId, route.getAirlineId());
    }

    @Test
    public void should_return_same_sourceAirport_value_after_invoke_set() {
        String sourceAirport = "sourceAirport";
        route.setSourceAirport(sourceAirport);
        assertEquals(sourceAirport, route.getSourceAirport());
    }

    @Test
    public void should_return_same_sourceAirportId_value_after_invoke_set() {
        String sourceAirportId = "123";
        route.setSourceAirportId(sourceAirportId);
        assertEquals(sourceAirportId, route.getSourceAirportId());
    }

    @Test
    public void should_return_same_destinationAirport_value_after_invoke_set() {
        String destinationAirport = "destinationAirport";
        route.setDestinationAirport(destinationAirport);
        assertEquals(destinationAirport, route.getDestinationAirport());
    }

    @Test
    public void should_return_same_destinationAirportId_value_after_invoke_set() {
        String destinationAirportId = "123";
        route.setDestinationAirportId(destinationAirportId);
        assertEquals(destinationAirportId, route.getDestinationAirportId());
    }

    @Test
    public void should_return_same_codeShare_value_after_invoke_set() {
        String codeShare = "codeShare";
        route.setCodeShare(codeShare);
        assertEquals(codeShare, route.getCodeShare());
    }

    @Test
    public void should_return_same_stops_value_after_invoke_set() {
        String stops = "stops";
        route.setStops(stops);
        assertEquals(stops, route.getStops());
    }

    @Test
    public void should_return_same_equipment_value_after_invoke_set() {
        String equipment = "equipment";
        route.setEquipment(equipment);
        assertEquals(equipment, route.getEquipment());
    }
}

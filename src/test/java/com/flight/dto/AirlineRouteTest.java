package com.flight.dto;

import org.assertj.core.util.Lists;
import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.*;

public class AirlineRouteTest {

    private AirlineRoute airlineRoute;

    @Before
    public void setup() {
        airlineRoute = new AirlineRoute();
    }

    @Test
    public void should_return_same_airline_value_after_invoke_set() {
        long airlineId = 1000l;
        Airline airline = new Airline();
        airline.setAirlineId(airlineId);
        airlineRoute.setAirline(airline);
        assertEquals(airlineId, airlineRoute.getAirline().getAirlineId());
    }

    @Test
    public void should_return_same_routes_value_after_invoke_set() {
        String codeShare = "codeShare";
        Route route = new Route();
        route.setCodeShare(codeShare);
        airlineRoute.setRoutes(Lists.newArrayList(route));
        assertEquals(codeShare, airlineRoute.getRoutes().get(0).getCodeShare());
    }
}

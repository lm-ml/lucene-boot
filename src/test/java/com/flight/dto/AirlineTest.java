package com.flight.dto;

import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.*;

public class AirlineTest {
    private Airline airline;

    @Before
    public void setup() {
        airline = new Airline();
    }

    @Test
    public void should_return_same_airlineId_value_after_invoke_set() {
        long airlineId = 1000l;
        airline.setAirlineId(airlineId);
        assertEquals(airlineId, airline.getAirlineId());
    }

    @Test
    public void should_return_same_name_value_after_invoke_set() {
        String name = "name";
        airline.setName(name);
        assertEquals(name, airline.getName());
    }

    @Test
    public void should_return_same_alias_value_after_invoke_set() {
        String alias = "alias";
        airline.setAlias(alias);
        assertEquals(alias, airline.getAlias());
    }

    @Test
    public void should_return_same_iATA_value_after_invoke_set() {
        String iATA = "iATA";
        airline.setIATA(iATA);
        assertEquals(iATA, airline.getIATA());
    }

    @Test
    public void should_return_same_iCAO_value_after_invoke_set() {
        String iCAO = "iCAO";
        airline.setICAO(iCAO);
        assertEquals(iCAO, airline.getICAO());
    }

    @Test
    public void should_return_same_callSign_value_after_invoke_set() {
        String callSign = "callSign";
        airline.setCallSign(callSign);
        assertEquals(callSign, airline.getCallSign());
    }

    @Test
    public void should_return_same_country_value_after_invoke_set() {
        String country = "country";
        airline.setCountry(country);
        assertEquals(country, airline.getCountry());
    }

    @Test
    public void should_return_same_active_value_after_invoke_set() {
        String active = "active";
        airline.setActive(active);
        assertEquals(active, airline.getActive());
    }
}

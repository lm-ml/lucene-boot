package com.flight.dto;

import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.*;

public class AirportTest {

    private Airport airport;

    @Before
    public void setup() {
        airport = new Airport();
    }

    @Test
    public void should_return_same_airportId_value_after_invoke_set() {
        long airportId = 1000l;
        airport.setAirportId(airportId);
        assertEquals(airportId, airport.getAirportId());
    }

    @Test
    public void should_return_same_name_value_after_invoke_set() {
        String name = "name";
        airport.setName(name);
        assertEquals(name, airport.getName());
    }

    @Test
    public void should_return_same_city_value_after_invoke_set() {
        String city = "city";
        airport.setCity(city);
        assertEquals(city, airport.getCity());
    }

    @Test
    public void should_return_same_country_value_after_invoke_set() {
        String country = "country";
        airport.setCountry(country);
        assertEquals(country, airport.getCountry());
    }

    @Test
    public void should_return_same_iATA_value_after_invoke_set() {
        String iATA = "iATA";
        airport.setIATA(iATA);
        assertEquals(iATA, airport.getIATA());
    }

    @Test
    public void should_return_same_iCAO_value_after_invoke_set() {
        String iCAO = "iCAO";
        airport.setICAO(iCAO);
        assertEquals(iCAO, airport.getICAO());
    }

    @Test
    public void should_return_same_latitude_value_after_invoke_set() {
        double latitude = 50.0d;
        airport.setLatitude(latitude);
        assertEquals(latitude, airport.getLatitude(), 0.1);
    }

    @Test
    public void should_return_same_longitude_value_after_invoke_set() {
        double longitude = 50.0d;
        airport.setLongitude(longitude);
        assertEquals(longitude, airport.getLongitude(), 0.1);
    }

    @Test
    public void should_return_same_altitude_value_after_invoke_set() {
        String altitude = "altitude";
        airport.setAltitude(altitude);
        assertEquals(altitude, airport.getAltitude());
    }

    @Test
    public void should_return_same_timezone_value_after_invoke_set() {
        String timezone = "timezone";
        airport.setTimezone(timezone);
        assertEquals(timezone, airport.getTimezone());
    }

    @Test
    public void should_return_same_dST_value_after_invoke_set() {
        String dST = "dST";
        airport.setDST(dST);
        assertEquals(dST, airport.getDST());
    }

    @Test
    public void should_return_same_tzDatabaseTimeZone_value_after_invoke_set() {
        String tzDatabaseTimeZone = "tzDatabaseTimeZone";
        airport.setTzDatabaseTimeZone(tzDatabaseTimeZone);
        assertEquals(tzDatabaseTimeZone, airport.getTzDatabaseTimeZone());
    }

    @Test
    public void should_return_same_type_value_after_invoke_set() {
        String type = "type";
        airport.setType(type);
        assertEquals(type, airport.getType());
    }

    @Test
    public void should_return_same_source_value_after_invoke_set() {
        String source = "source";
        airport.setSource(source);
        assertEquals(source, airport.getSource());
    }


}

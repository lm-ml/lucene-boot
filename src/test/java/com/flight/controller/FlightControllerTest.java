package com.flight.controller;

import com.flight.dto.Airline;
import com.flight.dto.AirlineRoute;
import com.flight.service.FlightService;
import org.assertj.core.util.Lists;
import org.junit.Before;
import org.junit.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

public class FlightControllerTest {

    @InjectMocks
    private FlightController controller;

    @Mock
    private FlightService flightService;

    private MockMvc mockMvc;

    @Before
    public void setup() {
        MockitoAnnotations.initMocks(this);
        mockMvc = MockMvcBuilders.standaloneSetup(controller).build();
    }

    @Test
    public void should_return_airportList_when_call_getAirports() throws Exception {
        String airportName = "airportName";
        mockMvc.perform(get("/flight/airports").
                param("name", airportName).
                contentType(MediaType.APPLICATION_JSON)).
                andExpect(status().isOk());
        ArgumentCaptor<String> nameCaptor = ArgumentCaptor.forClass(String.class);
        verify(flightService).getAirports(nameCaptor.capture(), eq(null), eq(null), eq(null), eq(null), eq(null));
        assertEquals(airportName, nameCaptor.getValue());
    }

    @Test
    public void should_return_airportList_when_call_getAirlineRoutes() throws Exception {
        String airlineName = "airlineName";
        String sourceCity = "sourceCity";
        AirlineRoute airlineRoute = new AirlineRoute();
        Airline airline = new Airline();
        airline.setName(airlineName);
        airlineRoute.setAirline(airline);
        List<AirlineRoute> airlineRoutes = Lists.newArrayList(airlineRoute);
        doReturn(airlineRoutes).when(flightService).getAirlineRoutes(sourceCity, null);
        mockMvc.perform(get("/flight/airlineRoutes").
                param("sourceCity", sourceCity).
                contentType(MediaType.APPLICATION_JSON)).
                andExpect(status().isOk()).
                andExpect(jsonPath("$[0].airline.name").value(airlineName));
        ArgumentCaptor<String> sourceCityCaptor = ArgumentCaptor.forClass(String.class);
        verify(flightService).getAirlineRoutes(sourceCityCaptor.capture(), eq(null));
        assertEquals(sourceCity, sourceCityCaptor.getValue());
    }

}

package com.flight.service;

import com.flight.dto.Airline;
import com.flight.dto.Airport;
import com.flight.dto.Route;
import org.apache.lucene.document.Document;

import java.io.File;
import java.util.List;

public interface ConvertService {

    List<Document> convertAirportFile(File fileResource) throws Exception;

    List<Document> convertRouteFile(File fileResource) throws Exception;

    List<Document> convertAirlineFile(File fileResource) throws Exception;

    Airport getAirport(String lineData);

    Airline getAirline(String lineData);

    Route getRoute(String lineData);
}

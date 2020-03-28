package com.flight.service;

import com.flight.dto.Airline;
import com.flight.dto.Airport;
import com.flight.dto.Route;
import org.apache.lucene.document.Document;

import java.io.File;
import java.io.IOException;
import java.util.List;

public interface ConvertService {

    List<Document> convertAirportFile(File fileResource) throws IOException;

    List<Document> convertRouteFile(File fileResource) throws IOException;

    List<Document> convertAirlineFile(File fileResource) throws IOException;

    Airport getAirport(String lineData);

    Airline getAirline(String lineData);

    Route getRoute(String lineData);
}

package com.flight.service.impl;

import com.flight.dto.Airline;
import com.flight.dto.Airport;
import com.flight.dto.Route;
import com.flight.service.ConvertService;
import org.apache.lucene.document.Document;
import org.junit.Before;
import org.junit.Test;
import org.mockito.InjectMocks;
import org.mockito.MockitoAnnotations;

import java.io.File;
import java.util.List;

import static org.junit.Assert.*;

public class ConvertServiceImplTest {

    @InjectMocks
    private ConvertService convertService = new ConvertServiceImpl();

    @InjectMocks
    private ConvertServiceImpl convertServiceImpl = new ConvertServiceImpl();

    @Before
    public void setup() {
        MockitoAnnotations.initMocks(this);
    }


    @Test
    public void should_return_emptyList_when_call_convertAirportFile_and_fileResource_isnull() throws Exception {
        List<Document> documents = convertService.convertAirportFile(null);
        assertTrue(documents.isEmpty());
    }

    @Test
    public void should_return_null_when_call_convertAirportFile_and_fileResource_notExists() throws Exception {
        File fileResource = new File("src/test/resources/airports.dat");
        List<Document> documents = convertService.convertAirportFile(fileResource);
        assertTrue(documents.isEmpty());
    }

    @Test
    public void should_return_documents_when_call_convertAirportFile_and_fileResource_isExists() throws Exception {
        File fileResource = new File("src/test/resources/data/airports.dat");
        List<Document> documents = convertService.convertAirportFile(fileResource);
        assertEquals(4, documents.size());
        assertEquals("mount hagen kagamuga airport", documents.get(2).get("name"));
    }

    @Test
    public void should_return_emptyList_when_call_convertRouteFile_and_fileResource_isnull() throws Exception {
        List<Document> documents = convertService.convertRouteFile(null);
        assertTrue(documents.isEmpty());
    }

    @Test
    public void should_return_null_when_call_convertRouteFile_and_fileResource_notExists() throws Exception {
        File fileResource = new File("src/test/resources/routes.dat");
        List<Document> documents = convertService.convertRouteFile(fileResource);
        assertTrue(documents.isEmpty());
    }

    @Test
    public void should_return_documents_when_call_convertRouteFile_and_fileResource_isExists() throws Exception {
        File fileResource = new File("src/test/resources/data/routes.dat");
        List<Document> documents = convertService.convertRouteFile(fileResource);
        assertEquals(6, documents.size());
        assertEquals("2968", documents.get(4).get("sourceAirportId"));
    }


    @Test
    public void should_return_emptyList_when_call_convertAirlineFile_and_fileResource_isnull() throws Exception {
        List<Document> documents = convertService.convertAirlineFile(null);
        assertTrue(documents.isEmpty());
    }

    @Test
    public void should_return_null_when_call_convertAirlineFile_and_fileResource_notExists() throws Exception {
        File fileResource = new File("src/test/resources/airlines.dat");
        List<Document> documents = convertService.convertAirlineFile(fileResource);
        assertTrue(documents.isEmpty());
    }

    @Test
    public void should_return_documents_when_call_convertAirlineFile_and_fileResource_isExists() throws Exception {
        File fileResource = new File("src/test/resources/data/airlines.dat");
        List<Document> documents = convertService.convertAirlineFile(fileResource);
        assertEquals(4, documents.size());
        assertEquals("3222", documents.get(2).get("airlineId"));
    }

    @Test
    public void should_return_null_when_call_getAirport_and_lineData_isnull() {
        Airport airport = convertService.getAirport(null);
        assertNull(airport);
    }

    @Test
    public void should_return_null_when_call_getAirport_and_lineData_mismatch() {
        String lineData = "28,\"CFB Bagotville\",\"Bagotville\",\"Canada\",\"YBG\",\"CYBG\",48.33060073852539,-5,\"A\",\"America/Toronto\",\"airport\",\"OurAirports\"\n";
        Airport airport = convertService.getAirport(lineData);
        assertNull(airport);
    }

    @Test
    public void should_return_airline_when_call_getAirport_and_lineData_match() {
        String lineData = "28,\"CFB Bagotville\",\"Bagotville\",\"Canada\",\"YBG\",\"CYBG\",48.33060073852539,-70.99639892578125,522,-5,\"A\",\"America/Toronto\",\"airport\",\"OurAirports\"\n";
        Airport airport = convertService.getAirport(lineData);
        assertEquals("CFB Bagotville", airport.getName());
    }

    @Test
    public void should_return_null_when_call_getAirline_and_lineData_isnull() {
        Airline airline = convertService.getAirline(null);
        assertNull(airline);
    }

    @Test
    public void should_return_null_when_call_getAirline_and_lineData_mismatch() {
        String lineData = "2B,410,DME,4029,KZN,2990";
        Airline airline = convertService.getAirline(lineData);
        assertNull(airline);
    }

    @Test
    public void should_return_airline_when_call_getAirline_and_lineData_match() {
        String lineData = "14,\"Abacus International\",\\N,\"1B\",\"\",\"\",\"Singapore\",\"Y\"";
        Airline airline = convertService.getAirline(lineData);
        assertEquals("Abacus International", airline.getName());
    }

    @Test
    public void should_return_airline_when_call_getAirline_and_lineData_match_have_doubleQuotes() {
        String lineData = "14,\"Abacus, International\",\\N,\"1B\",\"\",\"\",\"Singapore\",\"Y\"";
        Airline airline = convertService.getAirline(lineData);
        assertEquals("Abacus, International", airline.getName());
    }

    @Test
    public void should_return_airline_when_call_getAirline_and_lineData_mismatch_have_doubleQuotes() {
        String lineData = "14,\"Abacus, International\",\\N,\"1B\",\"\",\"\",\"Singapore\",\"Singapore\",\"Singapore\",\"Y\"";
        Airline airline = convertService.getAirline(lineData);
        assertNull(airline);
    }

    @Test
    public void should_return_null_when_call_getRoute_and_lineData_isnull() {
        Route route = convertService.getRoute(null);
        assertNull(route);
    }

    @Test
    public void should_return_null_when_call_getRoute_and_lineData_mismatch() {
        String lineData = "2B,410,DME,4029,KZN,2990";
        Route route = convertService.getRoute(lineData);
        assertNull(route);
    }

    @Test
    public void should_return_route_when_call_getRoute_and_lineData_match() {
        String lineData = "ZH,4611,HAK,4120\",SZX,3374,,0,320 738 319";
        Route route = convertService.getRoute(lineData);
        assertEquals("320 738 319", route.getEquipment());
    }

    @Test
    public void should_return_route_when_call_getRoute_and_lineData_mismatch() {
        String lineData = "ZH,4611,HAK,4120,SZX,3374\",,0,320 738 319,320 738 319";
        Route route = convertService.getRoute(lineData);
        assertNull(route);
    }


    @Test
    public void should_return_null_when_call_formatValue_and_value_isnull() {
        String value = convertServiceImpl.formatValue(null);
        assertNull(value);
    }

    @Test
    public void should_replaceAll_quotationMarks_when_call_formatValue_and_value_have_quotationMarks() {
        String value = convertServiceImpl.formatValue("\"value\"");
        assertEquals("value", value);
    }


    @Test
    public void should_return_null_when_call_toLowerCaseValue_and_value_is_not_null() {
        String value = convertServiceImpl.toLowerCaseValue(null);
        assertNull(value);
    }

    @Test
    public void should_toLowerCase_value_when_call_toLowerCaseValue_and_value_is_not_null() {
        String value = convertServiceImpl.toLowerCaseValue("VALUE");
        assertEquals("value", value);
    }


    @Test
    public void should_split_value_when_call_getFieldsData_have_doubleQuotes_case_1() {
        String lineData = "ZH,4611,HAK,4120,SZX,\"33,74\",,0,320 738 319";
        String[] fieldsData = convertServiceImpl.getFieldsData(lineData);
        assertEquals(9, fieldsData.length);
        assertEquals("\"33,74\"", fieldsData[5]);
    }

    @Test
    public void should_split_value_when_call_getFieldsData_have_doubleQuotes_case_2() {
        String lineData = "ZH,4611,HAK,4120,SZX,\"33,74,,0,320 738 319";
        String[] fieldsData = convertServiceImpl.getFieldsData(lineData);
        assertEquals(10, fieldsData.length);
        assertEquals("\"33", fieldsData[5]);
    }

    @Test
    public void should_split_value_when_call_getFieldsData_have_doubleQuotes_case_3() {
        String lineData = "ZH,4611,HAK,4120,SZX,33,74\",,0,320 738 319";
        String[] fieldsData = convertServiceImpl.getFieldsData(lineData);
        assertEquals(10, fieldsData.length);
        assertEquals("74\"", fieldsData[6]);
    }

    @Test
    public void should_split_value_when_call_getFieldsData_have_apostrophe_case_1() {
        String lineData = "ZH,4611,HAK,4120,SZX,'33,74',,0,320 738 319";
        String[] fieldsData = convertServiceImpl.getFieldsData(lineData);
        assertEquals(9, fieldsData.length);
        assertEquals("'33,74'", fieldsData[5]);
    }

    @Test
    public void should_split_value_when_call_getFieldsData_have_apostrophe_case_2() {
        String lineData = "ZH,4611,HAK,4120,SZX,'33,74,,0,320 738 319";
        String[] fieldsData = convertServiceImpl.getFieldsData(lineData);
        assertEquals(10, fieldsData.length);
        assertEquals("'33", fieldsData[5]);
    }

    @Test
    public void should_split_value_when_call_getFieldsData_have_apostrophe_case_3() {
        String lineData = "ZH,4611,HAK,4120,SZX,33,74',,0,320 738 319";
        String[] fieldsData = convertServiceImpl.getFieldsData(lineData);
        assertEquals(10, fieldsData.length);
        assertEquals("74'", fieldsData[6]);
    }

    @Test
    public void should_return_null_when_call_getFieldsData_lineData_isnull() {
        String[] fieldsData = convertServiceImpl.getFieldsData(null);
        assertNull(fieldsData);
    }


}

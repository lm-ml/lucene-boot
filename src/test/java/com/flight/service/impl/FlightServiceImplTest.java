package com.flight.service.impl;

import com.flight.dto.Airline;
import com.flight.dto.AirlineRoute;
import com.flight.dto.Airport;
import com.flight.dto.Route;
import com.flight.service.ConvertService;
import com.flight.service.FlightService;
import com.flight.service.LuceneService;
import org.apache.lucene.document.Document;
import org.apache.lucene.document.Field;
import org.apache.lucene.document.TextField;
import org.apache.lucene.index.IndexReader;
import org.apache.lucene.index.IndexableField;
import org.apache.lucene.search.*;
import org.assertj.core.util.Lists;
import org.junit.Before;
import org.junit.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.junit.Assert.*;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

public class FlightServiceImplTest {

    @InjectMocks
    private FlightService flightService = new FlightServiceImpl();

    @InjectMocks
    private FlightServiceImpl flightServiceImpl = new FlightServiceImpl();

    @InjectMocks
    private FlightServiceImplMock flightServiceImplMock = new FlightServiceImplMock();

    @Mock
    private LuceneService luceneService;

    @Mock
    private ConvertService convertService;

    @Before
    public void setup() {
        MockitoAnnotations.initMocks(this);
    }

    @Test
    public void should_return_airports_when_call_getAirports() throws Exception {
        File airportsResource = mock(File.class);
        doReturn(airportsResource).when(luceneService).copyInputStreamToFileByResourceName(anyString());
        IndexReader indexReader = mock(IndexReader.class);
        doReturn(indexReader).when(luceneService).openIndexReader();
        IndexSearcher indexSearcher = mock(IndexSearcher.class);
        doReturn(indexSearcher).when(luceneService).getIndexSearcher(any());
        ScoreDoc scoreDoc = new ScoreDoc(50, 0.5f);
        TopDocs topDocs = new TopDocs(99l, new ScoreDoc[]{scoreDoc}, 50);
        Document document = new Document();
        document.add(new TextField("lineData", "lineValue", Field.Store.YES));
        doReturn(document).when(indexSearcher).doc(anyInt());
        doReturn(topDocs).when(indexSearcher).search(any(), anyInt());
        List<Document> documents = new ArrayList<>();
        documents.add(document);
        doReturn(documents).when(convertService).convertAirportFile(any());
        Airport airport = new Airport();
        airport.setAirportId(66666);
        doReturn(airport).when(convertService).getAirport(anyString());
        List<Airport> airports = flightService.getAirports(null, null, null, null, null, null);
        assertEquals(66666, airports.get(0).getAirportId());
        ArgumentCaptor<String> resourceNameCaptor = ArgumentCaptor.forClass(String.class);
        verify(luceneService).copyInputStreamToFileByResourceName(resourceNameCaptor.capture());
        assertEquals(FlightServiceImpl.airports_data_resource_name, resourceNameCaptor.getValue());
        ArgumentCaptor<List> airlinesDocumentsCaptor = ArgumentCaptor.forClass(List.class);
        verify(luceneService).addDocuments(airlinesDocumentsCaptor.capture());
        List<Document> airlinesDocuments = (List<Document>) (airlinesDocumentsCaptor.getValue());
        Document documentParam = airlinesDocuments.get(0);
        IndexableField lineDataField = documentParam.getFields().get(0);
        assertTrue(lineDataField.fieldType().stored());
        assertEquals("lineValue", lineDataField.stringValue());
    }

    @Test(expected = RuntimeException.class)
    public void should_throw_exception_call_getAirports() throws Exception {
        doThrow(new RuntimeException()).when(luceneService).getIndexWriter();
        flightService.getAirports(null, null, null, null, null, null);
        verify(luceneService, never()).deleteAll();
    }

    @Test(expected = RuntimeException.class)
    public void should_throw_exception_call_getAirlineRoutes() throws Exception {
        doThrow(new RuntimeException()).when(luceneService).getIndexWriter();
        flightServiceImplMock.getAirlineRoutes("sourceCity", null);
        verify(luceneService, never()).deleteAll();
    }

    //方法不可测？
    @Test
    public void should_return_empty_list_call_getAirlineRoutes_and_params_isnull() throws Exception {
        flightServiceImplMock.getAirlineRoutes("sourceCity", "destinationCity");
        verify(luceneService, times(1)).getIndexWriter();
        verify(luceneService, times(2)).deleteAll();
        verify(luceneService, times(1)).getIndexWriter();
    }


    @Test
    public void should_return_list_call_getAirlineRoutes_and_params_is_not_null() throws Exception {
        flightServiceImplMock.getAirlineRoutes(null, null);
        verify(luceneService, never()).getIndexWriter();
    }


    @Test
    public void should_return_null_when_call_getAirlineMap_and_routeDocuments_isnull() throws Exception {
        doReturn(null).when(convertService).convertRouteFile(any());
        Map<String, ArrayList<Route>> airlineMap = flightServiceImpl.getAirlineMap(null, null);
        assertNull(airlineMap);
        verify(luceneService, never()).addDocuments(eq(null));
        verify(luceneService, never()).commit();
        verify(luceneService, never()).openIndexReader();
        verify(luceneService, never()).getIndexSearcher(eq(null));
        verify(convertService, never()).getRoute(eq(null));
        verify(luceneService, never()).closeIndexReader(eq(null));
        ArgumentCaptor<String> resourceNameCaptor = ArgumentCaptor.forClass(String.class);
        verify(luceneService).copyInputStreamToFileByResourceName(resourceNameCaptor.capture());
        assertEquals(FlightServiceImpl.routes_data_resource_name, resourceNameCaptor.getValue());
    }

    @Test
    public void should_return_idMap_when_call_getAirlineMap_sourceCityAirports_and_destinationCityAirports_is_not_null() throws Exception {
        IndexSearcher indexSearcher = mock(IndexSearcher.class);
        mockGetAirlineMapData(indexSearcher);
        Airport sourceCityAirport = new Airport();
        sourceCityAirport.setAirportId(123456);
        Airport destinationCityAirport = new Airport();
        destinationCityAirport.setAirportId(654321);
        Map<String, ArrayList<Route>> airlineMap = flightServiceImpl.getAirlineMap(Lists.newArrayList(sourceCityAirport), Lists.newArrayList(destinationCityAirport));
        assertEquals("666", airlineMap.get("666").get(0).getAirlineId());
        ArgumentCaptor<BooleanQuery> queryCaptor = ArgumentCaptor.forClass(BooleanQuery.class);
        verify(indexSearcher).search(queryCaptor.capture(), anyInt());
        BooleanQuery booleanQuery = queryCaptor.getValue();
        List<BooleanClause> clauses = booleanQuery.clauses();
        assertEquals(2, clauses.size());
        assertSourceAirportQuery(clauses, 0);
        assertDestinationAirportQuery(clauses, 1);
    }

    @Test
    public void should_return_idMap_when_call_getAirlineMap_destinationCityAirports_is_not_null() throws Exception {
        IndexSearcher indexSearcher = mock(IndexSearcher.class);
        mockGetAirlineMapData(indexSearcher);
        Airport destinationCityAirport = new Airport();
        destinationCityAirport.setAirportId(654321);
        Map<String, ArrayList<Route>> airlineMap = flightServiceImpl.getAirlineMap(null, Lists.newArrayList(destinationCityAirport));
        assertEquals("666", airlineMap.get("666").get(0).getAirlineId());
        ArgumentCaptor<BooleanQuery> queryCaptor = ArgumentCaptor.forClass(BooleanQuery.class);
        verify(indexSearcher).search(queryCaptor.capture(), anyInt());
        BooleanQuery booleanQuery = queryCaptor.getValue();
        List<BooleanClause> clauses = booleanQuery.clauses();
        assertEquals(1, clauses.size());
        assertDestinationAirportQuery(clauses, 0);
    }

    @Test
    public void should_return_idMap_when_call_getAirlineMap_sourceCityAirports_is_not_null() throws Exception {
        IndexSearcher indexSearcher = mock(IndexSearcher.class);
        mockGetAirlineMapData(indexSearcher);
        Airport sourceCityAirport = new Airport();
        sourceCityAirport.setAirportId(123456);
        Map<String, ArrayList<Route>> airlineMap = flightServiceImpl.getAirlineMap(Lists.newArrayList(sourceCityAirport), null);
        assertEquals("666", airlineMap.get("666").get(0).getAirlineId());
        ArgumentCaptor<BooleanQuery> queryCaptor = ArgumentCaptor.forClass(BooleanQuery.class);
        verify(indexSearcher).search(queryCaptor.capture(), anyInt());
        BooleanQuery booleanQuery = queryCaptor.getValue();
        List<BooleanClause> clauses = booleanQuery.clauses();
        assertEquals(1, clauses.size());
        assertSourceAirportQuery(clauses, 0);
    }


    void mockGetAirlineMapData(IndexSearcher indexSearcher) throws Exception {
        File routesResource = mock(File.class);
        doReturn(routesResource).when(luceneService).copyInputStreamToFileByResourceName(anyString());
        doReturn(indexSearcher).when(luceneService).getIndexSearcher(any());
        ScoreDoc scoreDoc = new ScoreDoc(50, 0.5f);
        TopDocs topDocs = new TopDocs(9999l, new ScoreDoc[]{scoreDoc}, 50);
        Document document = new Document();
        document.add(new TextField("lineData", "lineValue", Field.Store.YES));
        doReturn(document).when(indexSearcher).doc(anyInt());
        doReturn(topDocs).when(indexSearcher).search(any(), anyInt());
        List<Document> documents = new ArrayList<>();
        documents.add(document);
        doReturn(documents).when(convertService).convertRouteFile(any());
        Route route = new Route();
        route.setAirlineId("666");
        doReturn(route).when(convertService).getRoute(anyString());
    }

    void assertDestinationAirportQuery(List<BooleanClause> clauses, int index) {
        BooleanClause destinationCityClause = clauses.get(index);
        assertEquals("+destinationAirportId:654321", destinationCityClause.toString());
        assertTrue(destinationCityClause.getOccur().equals(BooleanClause.Occur.MUST));
        BooleanQuery destinationCityQuery = (BooleanQuery) destinationCityClause.getQuery();
        List<BooleanClause> destinationAirportIdClauses = destinationCityQuery.clauses();
        assertEquals(1, destinationAirportIdClauses.size());
        BooleanClause destinationAirportIdClause = destinationAirportIdClauses.get(0);
        assertEquals("destinationAirportId:654321", destinationAirportIdClause.toString());
        assertTrue(destinationAirportIdClause.getOccur().equals(BooleanClause.Occur.SHOULD));
    }

    void assertSourceAirportQuery(List<BooleanClause> clauses, int index) {
        BooleanClause sourceCityClause = clauses.get(index);
        assertEquals("+sourceAirportId:123456", sourceCityClause.toString());
        assertTrue(sourceCityClause.getOccur().equals(BooleanClause.Occur.MUST));
        BooleanQuery sourceCityQuery = (BooleanQuery) sourceCityClause.getQuery();
        List<BooleanClause> sourceAirportIdClauses = sourceCityQuery.clauses();
        assertEquals(1, sourceAirportIdClauses.size());
        BooleanClause sourceAirportIdClause = sourceAirportIdClauses.get(0);
        assertEquals("sourceAirportId:123456", sourceAirportIdClause.toString());
        assertTrue(sourceAirportIdClause.getOccur().equals(BooleanClause.Occur.SHOULD));
    }

    @Test
    public void should_return_empty_list_when_call_getAirlinesByAirlineIds_and_airlineIdMap_isnull() throws Exception {
        List<AirlineRoute> airlineRoutes = flightServiceImpl.getAirlinesByAirlineIds(null);
        assertNotNull(airlineRoutes);
        assertEquals(0, airlineRoutes.size());
        verify(luceneService, never()).openIndexReader();
        verify(luceneService, times(0)).copyInputStreamToFileByResourceName(eq(null));
        verify(convertService, never()).convertAirlineFile(eq(null));
        verify(convertService, never()).getAirline(eq(null));
        verify(luceneService, never()).addDocuments(eq(null));
        verify(luceneService, never()).commit();
    }

    @Test
    public void should_return_list_when_call_getAirlinesByAirlineIds() throws Exception {
        IndexReader indexReader = mock(IndexReader.class);
        doReturn(indexReader).when(luceneService).openIndexReader();
        IndexSearcher indexSearcher = mock(IndexSearcher.class);
        doReturn(indexSearcher).when(luceneService).getIndexSearcher(any());
        ScoreDoc scoreDoc = new ScoreDoc(50, 0.5f);
        TopDocs topDocs = new TopDocs(9999l, new ScoreDoc[]{scoreDoc}, 50);
        Document document = new Document();
        document.add(new TextField("lineData", "lineValue", Field.Store.YES));
        doReturn(document).when(indexSearcher).doc(anyInt());
        doReturn(topDocs).when(indexSearcher).search(any(), anyInt());
        List<Document> documents = new ArrayList<>();
        documents.add(document);
        doReturn(documents).when(convertService).convertAirlineFile(any());
        Map<String, ArrayList<Route>> airlineIdMap = new HashMap<>();
        long airlineId = 3214;
        Route route = new Route();
        String key = String.valueOf(airlineId);
        route.setAirlineId(String.valueOf(airlineId));
        airlineIdMap.put(key, Lists.newArrayList(route));
        Airline airline = new Airline();
        airline.setAirlineId(airlineId);
        doReturn(airline).when(convertService).getAirline(any());
        List<AirlineRoute> airlineRoutes = flightServiceImpl.getAirlinesByAirlineIds(airlineIdMap);
        assertEquals(1, airlineRoutes.size());
        assertEquals(airlineId, airlineRoutes.get(0).getAirline().getAirlineId());
        ArgumentCaptor<List> airlinesDocumentsCaptor = ArgumentCaptor.forClass(List.class);
        verify(luceneService).addDocuments(airlinesDocumentsCaptor.capture());
        List<Document> airlinesDocuments = (List<Document>) (airlinesDocumentsCaptor.getValue());
        Document documentParam = airlinesDocuments.get(0);
        IndexableField lineDataField = documentParam.getFields().get(0);
        assertTrue(lineDataField.fieldType().stored());
        assertEquals("lineValue", lineDataField.stringValue());
        ArgumentCaptor<String> airlineLineDataCaptor = ArgumentCaptor.forClass(String.class);
        verify(convertService).getAirline(airlineLineDataCaptor.capture());
        assertEquals("lineValue", airlineLineDataCaptor.getValue());


    }

    @Test
    public void should_return_builder_when_call_getQueryAirportBuilder_and_name_is_not_null() {
        String name = "queryName";
        BooleanQuery.Builder builder = flightServiceImpl.getQueryAirportBuilder(name, null, null, null, null, null);
        List<BooleanClause> clauses = builder.build().clauses();
        assertEquals(1, clauses.size());
        assertEquals("+name:queryname~1", clauses.get(0).toString());
        assertTrue(clauses.get(0).getOccur().equals(BooleanClause.Occur.MUST));
    }

    @Test
    public void should_return_builder_when_call_getQueryAirportBuilder_and_params_isnull() {
        BooleanQuery.Builder builder = flightServiceImpl.getQueryAirportBuilder(null, null, null, null, null, null);
        assertNotNull(builder);
        assertEquals(0, builder.build().clauses().size());
    }

    @Test
    public void should_return_builder_when_call_getQueryAirportBuilder_and_params_is_not_null() {
        BooleanQuery.Builder builder = flightServiceImpl.getQueryAirportBuilder("queryName", "queryIATA", null, null, "queryCity", "queryCountry");
        assertNotNull(builder);
        List<BooleanClause> clauses = builder.build().clauses();
        assertEquals(4, clauses.size());
        assertEquals("+name:queryname~1", clauses.get(0).toString());
        assertTrue(clauses.get(1).getOccur().equals(BooleanClause.Occur.MUST));
        assertEquals("+city:querycity", clauses.get(2).toString());
        assertTrue(clauses.get(3).getOccur().equals(BooleanClause.Occur.MUST));
    }

    class FlightServiceImplMock extends FlightServiceImpl {
        @Override
        public List<Airport> getAirports(String name, String iATA, Double latitude, Double longitude, String city, String country) {
            return Lists.newArrayList(new Airport());
        }

        @Override
        Map<String, ArrayList<Route>> getAirlineMap(List<Airport> sourceCityAirports, List<Airport> destinationCityAirports) throws Exception {
            Map<String, ArrayList<Route>> routeMap = new HashMap<>();
            routeMap.put("key", new ArrayList<>());
            return routeMap;
        }

        @Override
        List<AirlineRoute> getAirlinesByAirlineIds(Map<String, ArrayList<Route>> airlineIdMap) throws Exception {
            return null;
        }
    }
}

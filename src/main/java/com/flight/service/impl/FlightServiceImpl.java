package com.flight.service.impl;

import com.flight.dto.Airline;
import com.flight.dto.AirlineRoute;
import com.flight.dto.Airport;
import com.flight.dto.Route;
import com.flight.service.ConvertService;
import com.flight.service.FlightService;
import com.flight.service.LuceneService;
import lombok.extern.slf4j.Slf4j;
import org.apache.lucene.document.Document;
import org.apache.lucene.index.IndexReader;
import org.apache.lucene.index.Term;
import org.apache.lucene.search.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.io.File;
import java.util.*;

@Service
@Slf4j
public class FlightServiceImpl implements FlightService {

    @Autowired
    private LuceneService luceneService;

    @Autowired
    private ConvertService convertService;

    public static final String airports_data_resource_name = "airports.dat";
    public static final String routes_data_resource_name = "routes.dat";
    public static final String airlines_data_resource_name = "airlines.dat";


    /**
     * @param name      查询机场名称，支持模糊查询
     * @param iATA      国际航空运输协会 ，支持精确查询
     * @param latitude  按纬度/经度查询机场，获取最近的机场。
     * @param longitude 按纬度/经度查询机场，获取最近的机场。
     * @param city      城市 ，支持精确查询
     * @param country   国家 ，支持精确查询
     * @return
     */
    @Override
    public List<Airport> getAirports(String name, String iATA, Double latitude, Double longitude, String city, String country) {
        List<Airport> airports = new ArrayList<>();
        try {
            luceneService.getIndexWriter();
            luceneService.deleteAll();
            //原始文件
            File airportsResource = luceneService.getFileByResourceName(airports_data_resource_name);
            List<Document> documents = convertService.convertAirportFile(airportsResource);
            if (null != documents && !documents.isEmpty()) {
                //创建索引，并写入索引库
                luceneService.addDocuments(documents);
                luceneService.commit();
            }
            BooleanQuery.Builder queryAirportBuilder = getQueryAirportBuilder(name, iATA, latitude, longitude, city, country);
            if (null != queryAirportBuilder) {
                // 读取索引库索引
                IndexReader reader = luceneService.openIndexReader();//读索引
                IndexSearcher indexSearcher = luceneService.getIndexSearcher(reader);
                BooleanQuery query = queryAirportBuilder.build();
                TopDocs topDocs = indexSearcher.search(query, Integer.MAX_VALUE);
                for (ScoreDoc scoreDoc : topDocs.scoreDocs) {
                    Airport airport = convertService.getAirport(indexSearcher.doc(scoreDoc.doc).get("lineData"));
                    airports.add(airport);
                }
                luceneService.closeIndexReader(reader);
            }
            luceneService.closeIndexWriter();
        } catch (Exception e) {
            throw new RuntimeException("查询机场数据失败");
        }
        return airports;
    }

    /**
     * @param sourceCity      源城市
     * @param destinationCity 目的地城市
     * @return
     */
    @Override
    public List<AirlineRoute> getAirlineRoutes(String sourceCity, String destinationCity) {
        List<AirlineRoute> airlineRoutes = new ArrayList<>();
        try {
            if (StringUtils.isEmpty(sourceCity) && StringUtils.isEmpty(destinationCity)) {
                return airlineRoutes;
            }
            List<Airport> sourceCityAirports = null;
            if (!StringUtils.isEmpty(sourceCity)) {
                sourceCityAirports = getAirports(null, null, null, null, sourceCity, null);
            }
            List<Airport> destinationCityAirports = null;
            if (!StringUtils.isEmpty(destinationCity)) {
                destinationCityAirports = getAirports(null, null, null, null, destinationCity, null);
            }
            if ((null == sourceCityAirports || sourceCityAirports.isEmpty()) && (null == destinationCityAirports || destinationCityAirports.isEmpty())) {
                return airlineRoutes;
            }
            luceneService.getIndexWriter();
            luceneService.deleteAll(); //原始文件
            Map<String, ArrayList<Route>> airlineIdMap = getAirlineMap(sourceCityAirports, destinationCityAirports);
            if (null != airlineIdMap && airlineIdMap.size() > 0) {
                luceneService.deleteAll();
                airlineRoutes = getAirlinesByAirlineIds(airlineIdMap);
            }
            luceneService.closeIndexWriter();
        } catch (Exception e) {
            throw new RuntimeException("查询航班数据失败");
        }
        return airlineRoutes;
    }

    /**
     * 根据源城市，目的地城市获取航班信息
     *
     * @param sourceCityAirports
     * @param destinationCityAirports
     * @return
     * @throws Exception
     */
    Map<String, ArrayList<Route>> getAirlineMap(List<Airport> sourceCityAirports, List<Airport> destinationCityAirports) throws Exception {
        File routesResource = luceneService.getFileByResourceName(routes_data_resource_name);
        List<Document> routeDocuments = convertService.convertRouteFile(routesResource);
        if (null != routeDocuments && routeDocuments.size() > 0) {
            BooleanQuery.Builder builder = new BooleanQuery.Builder();
            Map<String, ArrayList<Route>> airlineIdMap = new HashMap();
            //TODO 需要类似SQL中IN的语法
            if (null != sourceCityAirports && !sourceCityAirports.isEmpty()) {
                BooleanQuery.Builder sourceCityBuilder = new BooleanQuery.Builder();
                for (Airport sourceCityAirport : sourceCityAirports) {
                    TermQuery termQuery = new TermQuery(new Term("sourceAirportId", String.valueOf(sourceCityAirport.getAirportId())));
                    sourceCityBuilder.add(termQuery, BooleanClause.Occur.SHOULD);
                }
                builder.add(sourceCityBuilder.build(), BooleanClause.Occur.MUST);
            }
            //TODO 需要类似SQL中IN的语法
            if (null != destinationCityAirports && !destinationCityAirports.isEmpty()) {
                BooleanQuery.Builder destinationCityBuilder = new BooleanQuery.Builder();
                for (Airport destinationCityAirport : destinationCityAirports) {
                    TermQuery termQuery = new TermQuery(new Term("destinationAirportId", String.valueOf(destinationCityAirport.getAirportId())));
                    destinationCityBuilder.add(termQuery, BooleanClause.Occur.SHOULD);
                }
                builder.add(destinationCityBuilder.build(), BooleanClause.Occur.MUST);
            }
            //创建索引，并写入索引库
            luceneService.addDocuments(routeDocuments);
            luceneService.commit();
            // 读取索引库索引
            IndexReader reader = luceneService.openIndexReader();//读索引
            IndexSearcher indexSearcher = luceneService.getIndexSearcher(reader);
            BooleanQuery query = builder.build();
            TopDocs topDocs = indexSearcher.search(query, Integer.MAX_VALUE);
            for (ScoreDoc scoreDoc : topDocs.scoreDocs) {
                Route route = convertService.getRoute(indexSearcher.doc(scoreDoc.doc).get("lineData"));
                if (null != route) {
                    String airlineId = route.getAirlineId();
                    ArrayList<Route> routes = airlineIdMap.get(airlineId);
                    if (routes == null) {
                        routes = new ArrayList<>();
                    }
                    routes.add(route);
                    airlineIdMap.put(airlineId, routes);
                }
            }
            luceneService.closeIndexReader(reader);
            return airlineIdMap;
        }
        return null;
    }

    /**
     * 根据航空公司ID获取航空公司信息，并处理航空公司和航线信息
     *
     * @param airlineIdMap
     * @return
     * @throws Exception
     */
    List<AirlineRoute> getAirlinesByAirlineIds(Map<String, ArrayList<Route>> airlineIdMap) throws Exception {
        List<AirlineRoute> airlineRoutes = new ArrayList<>();
        Set<String> airlineIdKeys = null != airlineIdMap ? airlineIdMap.keySet() : null;
        if (null != airlineIdKeys && airlineIdKeys.size() > 0) {
            // 读取索引库索引
            IndexReader reader = luceneService.openIndexReader();//读索引
            //要查找的字符串数组
            //TODO 需要类似SQL中IN的语法
            BooleanQuery.Builder airlineBuilder = new BooleanQuery.Builder();
            for (String airlineIdKey : airlineIdKeys) {
                TermQuery termQuery = new TermQuery(new Term("airlineId", airlineIdKey));
                airlineBuilder.add(termQuery, BooleanClause.Occur.SHOULD);
            }
            File airlinesResource = luceneService.getFileByResourceName(airlines_data_resource_name);
            List<Document> airlinesDocuments = convertService.convertAirlineFile(airlinesResource);
            if (null != airlinesDocuments && !airlinesDocuments.isEmpty()) {
                //创建索引，并写入索引库
                luceneService.addDocuments(airlinesDocuments);
                luceneService.commit();
                reader = luceneService.openIndexReader();//读索引
            }
            BooleanQuery airlinesQuery = airlineBuilder.build();
            IndexSearcher indexAirlineSearcher = luceneService.getIndexSearcher(reader);
            TopDocs topAirlineDocs = indexAirlineSearcher.search(airlinesQuery, Integer.MAX_VALUE);
            Map<String, Airline> airlineMap = new HashMap();
            for (ScoreDoc scoreDoc : topAirlineDocs.scoreDocs) {
                Airline airline = convertService.getAirline(indexAirlineSearcher.doc(scoreDoc.doc).get("lineData"));
                if (null != airline) {
                    long airlineId = airline.getAirlineId();
                    airlineMap.put(String.valueOf(airlineId), airline);
                }
            }
            for (String airlineIdKey : airlineIdKeys) {
                AirlineRoute airlineRoute = new AirlineRoute();
                airlineRoute.setAirline(airlineMap.get(airlineIdKey));
                airlineRoute.setRoutes(airlineIdMap.get(airlineIdKey));
                airlineRoutes.add(airlineRoute);
            }
            luceneService.closeIndexReader(reader);
        }
        return airlineRoutes;
    }

    /**
     * 获取查询机场的builder
     *
     * @param name
     * @param iATA
     * @param latitude
     * @param longitude
     * @param city
     * @param country
     * @return
     */
    BooleanQuery.Builder getQueryAirportBuilder(String name, String iATA, Double latitude, Double longitude, String city, String country) {
        BooleanQuery.Builder builder = new BooleanQuery.Builder();
        if (!StringUtils.isEmpty(name)) {
            name = name.toLowerCase();
            FuzzyQuery nameQuery = new FuzzyQuery(new Term("name", name), 1);
            builder.add(nameQuery, BooleanClause.Occur.MUST);
        }
        if (!StringUtils.isEmpty(iATA)) {
            iATA = iATA.toLowerCase();
            TermQuery iATAQuery = new TermQuery(new Term("iATA", iATA));
            builder.add(iATAQuery, BooleanClause.Occur.MUST);
        }
        if (!StringUtils.isEmpty(city)) {
            city = city.toLowerCase();
            TermQuery cityQuery = new TermQuery(new Term("city", city));
            builder.add(cityQuery, BooleanClause.Occur.MUST);
        }
        if (!StringUtils.isEmpty(country)) {
            country = country.toLowerCase();
            TermQuery countryQuery = new TermQuery(new Term("country", country));
            builder.add(countryQuery, BooleanClause.Occur.MUST);
        }
        //todo 经纬度信息查询
        return builder;
    }

}

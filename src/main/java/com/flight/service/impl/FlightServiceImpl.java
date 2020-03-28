package com.flight.service.impl;

import com.flight.dto.Airline;
import com.flight.dto.AirlineRoute;
import com.flight.dto.Airport;
import com.flight.dto.Route;
import com.flight.service.FlightService;
import com.flight.service.LuceneService;
import lombok.extern.slf4j.Slf4j;
import org.apache.lucene.document.Document;
import org.apache.lucene.document.Field;
import org.apache.lucene.document.TextField;
import org.apache.lucene.index.DirectoryReader;
import org.apache.lucene.index.IndexReader;
import org.apache.lucene.index.Term;
import org.apache.lucene.search.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.*;

@Service
@Slf4j
public class FlightServiceImpl implements FlightService {

    @Autowired
    private LuceneService luceneService;


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
            File airportsResource = luceneService.getFileByResourceName("airports.dat");
            List<Document> documents = getDocumentFromFile(airportsResource);
            BooleanQuery.Builder b = new BooleanQuery.Builder();
            if (null != documents && !documents.isEmpty()) {
                //创建索引，并写入索引库
                luceneService.addDocuments(documents);
                luceneService.commit();
            }
            if (!StringUtils.isEmpty(name)) {
                name = name.toLowerCase();
                FuzzyQuery nameQuery = new FuzzyQuery(new Term("name", name), 1);
                b.add(nameQuery, BooleanClause.Occur.MUST);
            }
            if (!StringUtils.isEmpty(iATA)) {
                iATA = iATA.toLowerCase();
                TermQuery iATAQuery = new TermQuery(new Term("iATA", iATA));
                b.add(iATAQuery, BooleanClause.Occur.MUST);
            }
            if (!StringUtils.isEmpty(city)) {
                city = city.toLowerCase();
                TermQuery cityQuery = new TermQuery(new Term("city", city));
                b.add(cityQuery, BooleanClause.Occur.MUST);
            }
            if (!StringUtils.isEmpty(country)) {
                country = country.toLowerCase();
                TermQuery countryQuery = new TermQuery(new Term("country", country));
                b.add(countryQuery, BooleanClause.Occur.MUST);
            }

            // 读取索引库索引
            IndexReader reader = DirectoryReader.open(luceneService.getDirectory());//读索引
            IndexSearcher indexSearcher = new IndexSearcher(reader);
            BooleanQuery query = b.build();
            TopDocs topDocs = indexSearcher.search(query, Integer.MAX_VALUE);
            for (ScoreDoc scoreDoc : topDocs.scoreDocs) {
                Airport airport = getAirport(indexSearcher.doc(scoreDoc.doc).get("lineData"));
                airports.add(airport);
            }
            reader.close();
            luceneService.closeIndexWriter();
        } catch (IOException e) {
            e.printStackTrace();
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
                return null;
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
                return null;
            }
            luceneService.getIndexWriter();
            luceneService.deleteAll(); //原始文件
            File routesResource = luceneService.getFileByResourceName("routes.dat");
            List<Document> routeDocuments = getRouteDocument(routesResource);
            if (null != routeDocuments && routeDocuments.size() > 0) {
                BooleanQuery.Builder builder = new BooleanQuery.Builder();
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
                IndexReader reader = DirectoryReader.open(luceneService.getDirectory());//读索引
                IndexSearcher indexSearcher = new IndexSearcher(reader);
                BooleanQuery query = builder.build();
                TopDocs topDocs = indexSearcher.search(query, Integer.MAX_VALUE);
                Map<String, ArrayList<Route>> airlineIdMap = new HashMap();
                for (ScoreDoc scoreDoc : topDocs.scoreDocs) {
                    Route route = getRoute(indexSearcher.doc(scoreDoc.doc).get("lineData"));
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
                luceneService.deleteAll();
                Set<String> airlineIdKeys = airlineIdMap.keySet();
                if (null != airlineIdKeys && airlineIdKeys.size() > 0) {
                    //要查找的字符串数组
                    //TODO 需要类似SQL中IN的语法
                    BooleanQuery.Builder airlineBuilder = new BooleanQuery.Builder();
                    for (String airlineIdKey : airlineIdKeys) {
                        TermQuery termQuery = new TermQuery(new Term("airlineId", airlineIdKey));
                        airlineBuilder.add(termQuery, BooleanClause.Occur.SHOULD);
                    }
                    File airlinesResource = luceneService.getFileByResourceName("airlines.dat");
                    List<Document> airlinesDocuments = getAirlineDocument(airlinesResource);
                    if (null != airlinesDocuments && !airlinesDocuments.isEmpty()) {
                        //创建索引，并写入索引库
                        luceneService.addDocuments(airlinesDocuments);
                        luceneService.commit();
                        reader = DirectoryReader.open(luceneService.getDirectory());//读索引
                    }
                    BooleanQuery airlinesQuery = airlineBuilder.build();
                    IndexSearcher indexAirlineSearcher = new IndexSearcher(reader);
                    TopDocs topAirlineDocs = indexAirlineSearcher.search(airlinesQuery, Integer.MAX_VALUE);
                    Map<String, Airline> airlineMap = new HashMap();
                    for (ScoreDoc scoreDoc : topAirlineDocs.scoreDocs) {
                        Airline airline = getAirline(indexAirlineSearcher.doc(scoreDoc.doc).get("lineData"));
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
                }
                reader.close();
            }
            luceneService.closeIndexWriter();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return airlineRoutes;
    }

    List<Document> getDocumentFromFile(File fileResource) throws IOException {
        List<Document> documents = new ArrayList<>();
        if (null != fileResource && fileResource.exists()) {
            // 读取文件内容
            BufferedReader burReader = null;
            try {
                // BufferedReader 读取文件
                burReader = new BufferedReader(new FileReader(fileResource));
                String lineData = null;
                while ((lineData = burReader.readLine()) != null) {
                    Airport airport = getAirport(lineData);
                    if (null != airport) {
                        //域的名称 域的内容 是否存储
                        //创建Document 对象
                        Document document = new Document();
                        document.add(new TextField("lineData", lineData, Field.Store.YES));
                        document.add(new TextField("name", airport.getName().toLowerCase(), Field.Store.YES));
                        document.add(new TextField("city", airport.getCity().toLowerCase(), Field.Store.YES));
                        document.add(new TextField("country", airport.getCountry().toLowerCase(), Field.Store.YES));
                        document.add(new TextField("iATA", airport.getIATA().toLowerCase(), Field.Store.YES));
                        Field longitudeField = new TextField("longitude-latitude", airport.getLongitude() + " " + airport.getLatitude(), Field.Store.YES);
                        document.add(longitudeField);
                        documents.add(document);
                    } else {
                        // log.warn("有误数据,待处理：{}", lineData);
                    }
                }
            } catch (Exception e) {
                e.printStackTrace();
            } finally {
                if (null != burReader) {
                    burReader.close();
                }
            }
            return documents;
        }
        return documents;
    }

    List<Document> getRouteDocument(File fileResource) throws IOException {
        List<Document> documents = new ArrayList<>();
        if (null != fileResource && fileResource.exists()) {
            // 读取文件内容
            BufferedReader br = null;
            try {
                //构造一个BufferedReader类来读取文件
                br = new BufferedReader(new FileReader(fileResource));
                String lineData = null;
                while ((lineData = br.readLine()) != null) {
                    Route route = getRoute(lineData);
                    if (null != route) {
                        //域的名称 域的内容 是否存储
                        Field lineDataField = new TextField("lineData", lineData, Field.Store.YES);
                        Field airlineField = new TextField("airline", route.getAirline().toLowerCase(), Field.Store.YES);
                        Field airlineIdField = new TextField("airlineId", route.getAirlineId(), Field.Store.YES);
                        Field sourceAirportField = new TextField("sourceAirport", route.getSourceAirport().toLowerCase(), Field.Store.YES);
                        Field sourceAirportIdField = new TextField("sourceAirportId", route.getSourceAirportId(), Field.Store.YES);
                        Field destinationAirportField = new TextField("destinationAirport", route.getDestinationAirport().toLowerCase(), Field.Store.YES);
                        Field destinationAirportIdField = new TextField("destinationAirportId", route.getDestinationAirportId(), Field.Store.YES);
                        Field codeShareField = new TextField("codeShare", route.getCodeShare().toLowerCase(), Field.Store.YES);
                        //创建Document 对象
                        Document document = new Document();
                        document.add(lineDataField);
                        document.add(airlineField);
                        document.add(sourceAirportField);
                        document.add(destinationAirportField);
                        document.add(codeShareField);
                        document.add(airlineIdField);
                        document.add(sourceAirportIdField);
                        document.add(destinationAirportIdField);
                        documents.add(document);
                    } else {
                        // log.warn("有误数据,待处理：{}", lineData);
                    }
                }
            } catch (Exception e) {
                e.printStackTrace();
            } finally {
                if (null != br) {
                    br.close();
                }
            }
            return documents;
        }
        return documents;
    }

    List<Document> getAirlineDocument(File fileResource) throws IOException {
        List<Document> documents = new ArrayList<>();
        if (null != fileResource && fileResource.exists()) {
            // 读取文件内容
            BufferedReader br = null;
            try {
                //构造一个BufferedReader类来读取文件
                br = new BufferedReader(new FileReader(fileResource));
                String lineData = null;
                while ((lineData = br.readLine()) != null) {
                    Airline airline = getAirline(lineData);
                    if (null != airline) {
                        //域的名称 域的内容 是否存储
                        Field lineDataField = new TextField("lineData", lineData, Field.Store.YES);
                        Field airlineIdField = new TextField("airlineId", String.valueOf(airline.getAirlineId()), Field.Store.YES);
                        //创建Document 对象
                        Document document = new Document();
                        document.add(lineDataField);
                        document.add(airlineIdField);
                        documents.add(document);
                    } else {
                        log.warn("有误数据,待处理：{}", lineData);
                    }
                }
            } catch (Exception e) {
                e.printStackTrace();
            } finally {
                if (null != br) {
                    br.close();
                }
            }
            return documents;
        }
        return documents;
    }

    Airport getAirport(String lineData) {
        if (!StringUtils.isEmpty(lineData)) {
            String[] fieldsData = lineData.split(",");
            if (null != fieldsData && fieldsData.length == 14) {
                Airport airport = new Airport();
                airport.setAirportId(Long.valueOf(fieldsData[0]));
                airport.setName(fieldsData[1].replaceAll("\"", ""));
                airport.setCity(fieldsData[2].replaceAll("\"", ""));
                airport.setCountry(fieldsData[3].replaceAll("\"", ""));
                airport.setIATA(fieldsData[4].replaceAll("\"", ""));
                airport.setICAO(fieldsData[5].replaceAll("\"", ""));
                airport.setLatitude(Double.valueOf(fieldsData[6]));
                airport.setLongitude(Double.valueOf(fieldsData[7]));
                airport.setAltitude(fieldsData[8].replaceAll("\"", ""));
                airport.setTimezone(fieldsData[9].replaceAll("\"", ""));
                airport.setDST(fieldsData[10].replaceAll("\"", ""));
                airport.setTzDatabaseTimeZone(fieldsData[11].replaceAll("\"", ""));
                airport.setType(fieldsData[12].replaceAll("\"", ""));
                airport.setSource(fieldsData[13].replaceAll("\"", ""));
                return airport;
            }
        }
        return null;
    }

    Airline getAirline(String lineData) {
        if (!StringUtils.isEmpty(lineData)) {
            String[] fieldsData = lineData.split(",");
            if (null != fieldsData && fieldsData.length == 8) {
                Airline airline = new Airline();
                airline.setAirlineId(Long.valueOf(fieldsData[0]));
                airline.setName(fieldsData[1].replaceAll("\"", ""));
                airline.setAlias(fieldsData[2].replaceAll("\"", ""));
                airline.setIATA(fieldsData[3].replaceAll("\"", ""));
                airline.setICAO(fieldsData[4].replaceAll("\"", ""));
                airline.setCallSign(fieldsData[5].replaceAll("\"", ""));
                airline.setCountry(fieldsData[6].replaceAll("\"", ""));
                airline.setActive(fieldsData[7].replaceAll("\"", ""));
                return airline;
            }
        }
        return null;
    }

    Route getRoute(String lineData) {
        if (!StringUtils.isEmpty(lineData)) {
            String[] fieldsData = lineData.split(",");
            if (null != fieldsData && fieldsData.length == 9) {
                Route route = new Route();
                route.setAirline(fieldsData[0]);
                route.setAirlineId(fieldsData[1]);
                route.setSourceAirport(fieldsData[2]);
                route.setSourceAirportId(fieldsData[3]);
                route.setDestinationAirport(fieldsData[4]);
                route.setDestinationAirportId(fieldsData[5]);
                route.setCodeShare(fieldsData[6]);
                route.setStops(fieldsData[7]);
                route.setEquipment(fieldsData[8]);
                return route;
            }
        }
        return null;
    }

}

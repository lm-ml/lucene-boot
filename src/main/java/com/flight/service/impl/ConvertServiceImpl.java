package com.flight.service.impl;

import com.flight.dto.Airline;
import com.flight.dto.Airport;
import com.flight.dto.Route;
import com.flight.service.ConvertService;
import lombok.extern.slf4j.Slf4j;
import org.apache.lucene.document.Document;
import org.apache.lucene.document.Field;
import org.apache.lucene.document.TextField;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

@Slf4j
@Service
public class ConvertServiceImpl implements ConvertService {

    @Override
    public List<Document> convertAirportFile(File fileResource) throws IOException {
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

    @Override
    public List<Document> convertRouteFile(File fileResource) throws IOException {
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

    @Override
    public List<Document> convertAirlineFile(File fileResource) throws IOException {
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

    @Override
    public Airport getAirport(String lineData) {
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

    @Override
    public Airline getAirline(String lineData) {
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

    @Override
    public Route getRoute(String lineData) {
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

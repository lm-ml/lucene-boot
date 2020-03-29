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
import java.util.ArrayList;
import java.util.List;

@Slf4j
@Service
public class ConvertServiceImpl implements ConvertService {

    @Override
    public List<Document> convertAirportFile(File fileResource) throws Exception {
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
                        document.add(new TextField("name", toLowerCaseValue(airport.getName()), Field.Store.YES));
                        document.add(new TextField("city", toLowerCaseValue(airport.getCity()), Field.Store.YES));
                        document.add(new TextField("country", toLowerCaseValue(airport.getCountry()), Field.Store.YES));
                        document.add(new TextField("iATA", toLowerCaseValue(airport.getIATA()), Field.Store.YES));
                        Field longitudeField = new TextField("longitude-latitude", airport.getLongitude() + " " + airport.getLatitude(), Field.Store.YES);
                        document.add(longitudeField);
                        documents.add(document);
                    } else {
                        // log.warn("有误数据,待处理：{}", lineData);
                    }
                }
            } finally {
                burReader.close();
            }
        }
        return documents;
    }

    @Override
    public List<Document> convertRouteFile(File fileResource) throws Exception {
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
                        Field airlineField = new TextField("airline", toLowerCaseValue(route.getAirline()), Field.Store.YES);
                        Field airlineIdField = new TextField("airlineId", route.getAirlineId(), Field.Store.YES);
                        Field sourceAirportField = new TextField("sourceAirport", toLowerCaseValue(route.getSourceAirport()), Field.Store.YES);
                        Field sourceAirportIdField = new TextField("sourceAirportId", route.getSourceAirportId(), Field.Store.YES);
                        Field destinationAirportField = new TextField("destinationAirport", toLowerCaseValue(route.getDestinationAirport()), Field.Store.YES);
                        Field destinationAirportIdField = new TextField("destinationAirportId", route.getDestinationAirportId(), Field.Store.YES);
                        Field codeShareField = new TextField("codeShare", toLowerCaseValue(route.getCodeShare()), Field.Store.YES);
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
            } finally {
                br.close();
            }
        }
        return documents;
    }

    @Override
    public List<Document> convertAirlineFile(File fileResource) throws Exception {
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
                        //log.warn("有误数据,待处理：{}", lineData);
                    }
                }
            } finally {
                br.close();
            }
        }
        return documents;
    }

    @Override
    public Airport getAirport(String lineData) {
        if (!StringUtils.isEmpty(lineData)) {
            String[] fieldsData = lineData.split(",");
            if (null != fieldsData && fieldsData.length >= 14) {
                if (fieldsData.length > 8) {
                    String[] formatValue = getFieldsData(lineData);
                    if (null != formatValue && formatValue.length == 14) {
                        fieldsData = formatValue;
                    } else {
                        //问题数据
                        return null;
                    }
                }
                Airport airport = new Airport();
                airport.setAirportId(Long.valueOf(fieldsData[0]));
                airport.setName(formatValue(fieldsData[1]));
                airport.setCity(formatValue(fieldsData[2]));
                airport.setCountry(formatValue(fieldsData[3]));
                airport.setIATA(formatValue(fieldsData[4]));
                airport.setICAO(formatValue(fieldsData[5]));
                airport.setLatitude(Double.valueOf(fieldsData[6]));
                airport.setLongitude(Double.valueOf(fieldsData[7]));
                airport.setAltitude(formatValue(fieldsData[8]));
                airport.setTimezone(formatValue(fieldsData[9]));
                airport.setDST(formatValue(fieldsData[10]));
                airport.setTzDatabaseTimeZone(formatValue(fieldsData[11]));
                airport.setType(formatValue(fieldsData[12]));
                airport.setSource(formatValue(fieldsData[13]));
                return airport;
            } else {
                //问题数据
            }
        }
        return null;
    }

    @Override
    public Airline getAirline(String lineData) {
        if (!StringUtils.isEmpty(lineData)) {
            String[] fieldsData = lineData.split(",");
            if (null != fieldsData && fieldsData.length >= 8) {
                if (fieldsData.length > 8) {
                    String[] formatValue = getFieldsData(lineData);
                    if (null != formatValue && formatValue.length == 8) {
                        fieldsData = formatValue;
                    } else {
                        //问题数据
                        return null;
                    }
                }
                Airline airline = new Airline();
                airline.setAirlineId(Long.valueOf(fieldsData[0]));
                airline.setName(formatValue(fieldsData[1]));
                airline.setAlias(formatValue(fieldsData[2]));
                airline.setIATA(formatValue(fieldsData[3]));
                airline.setICAO(formatValue(fieldsData[4]));
                airline.setCallSign(formatValue(fieldsData[5]));
                airline.setCountry(formatValue(fieldsData[6]));
                airline.setActive(formatValue(fieldsData[7]));
                return airline;
            } else {
                //问题数据
            }
        }
        return null;
    }

    /**
     * 只处理用 " 或 ' 包含的数据
     *
     * @param lineData
     * @return
     */
    String[] getFieldsData(String lineData) {
        if (!StringUtils.isEmpty(lineData)) {
            String splitCode = ",";
            String doubleQuotes = "\"";
            String apostrophe = "'";
            List<String> fields = new ArrayList<>();
            while (lineData.length() > 0) {
                String start = lineData.substring(0, 1);
                int endIndex = lineData.indexOf(splitCode);
                if (start.equals(doubleQuotes)) {
                    int splitIndex = lineData.indexOf(doubleQuotes + splitCode);
                    endIndex = splitIndex > -1 ? splitIndex + 1 : endIndex;
                } else if (start.equals(apostrophe)) {
                    int splitIndex = lineData.indexOf(apostrophe + splitCode);
                    endIndex = splitIndex > -1 ? splitIndex + 1 : endIndex;
                }
                if (endIndex == -1) {
                    fields.add(lineData);
                    break;
                } else {
                    fields.add(lineData.substring(0, endIndex));
                    lineData = lineData.substring(endIndex + 1);
                }
            }
            return fields.toArray(new String[fields.size()]);
        }
        return null;
    }

    @Override
    public Route getRoute(String lineData) {
        if (!StringUtils.isEmpty(lineData)) {
            String[] fieldsData = lineData.split(",");
            if (null != fieldsData && fieldsData.length >= 9) {
                if (fieldsData.length > 8) {
                    String[] formatValue = getFieldsData(lineData);
                    if (null != formatValue && formatValue.length == 9) {
                        fieldsData = formatValue;
                    } else {
                        //问题数据
                        return null;
                    }
                }
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
            } else {
                //问题数据
                return null;
            }
        }
        return null;
    }

    String formatValue(String value) {
        return StringUtils.isEmpty(value) ? value : value.replaceAll("\"", "");
    }

    String toLowerCaseValue(String value) {
        return StringUtils.isEmpty(value) ? value : value.toLowerCase();
    }

}

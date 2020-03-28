package com.potato.service.impl;

import com.potato.dto.AirlineRoute;
import com.potato.dto.Airport;
import com.potato.service.PermissionService;
import lombok.extern.slf4j.Slf4j;
import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.standard.StandardAnalyzer;
import org.apache.lucene.document.Document;
import org.apache.lucene.document.Field;
import org.apache.lucene.document.TextField;
import org.apache.lucene.index.*;
import org.apache.lucene.search.*;
import org.apache.lucene.store.Directory;
import org.apache.lucene.store.RAMDirectory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.Resource;
import org.springframework.core.io.ResourceLoader;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import javax.annotation.PostConstruct;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

@Service
@Slf4j
public class PermissionServiceImpl implements PermissionService {

    @Autowired
    private ResourceLoader resourceLoader;
    static Directory directory;
    static IndexWriter indexWriter;

    @PostConstruct
    void init() throws IOException {
        try {
            directory = new RAMDirectory();
            // 标准分词器
            Analyzer analyzer = new StandardAnalyzer();
            IndexWriterConfig config = new IndexWriterConfig(analyzer);
            //1.创建IndexWriter
            indexWriter = new IndexWriter(directory, config);
        } finally {
            indexWriter.close();
        }
    }

    /**
     * 根据资源名称读取文件
     *
     * @param resourceName
     * @return
     * @throws IOException
     */
    File getFileByResourceName(String resourceName) throws IOException {
        if (StringUtils.isEmpty(resourceName)) {
            return null;
        }
        Resource resource = resourceLoader.getResource("classpath:data/" + resourceName);
        if (resource.exists()) {
            return resource.getFile();
        }
        return null;
    }

    List<Document> getDocumentFromFile(File fileResource) throws IOException {
        List<Document> documents = new ArrayList<>();
        if (null != fileResource && fileResource.exists()) {
            // 读取文件内容
            BufferedReader br = null;
            try {
                //构造一个BufferedReader类来读取文件
                br = new BufferedReader(new FileReader(fileResource));
                String lineData = null;
                while ((lineData = br.readLine()) != null) {
                    Airport airport = getAirport(lineData);
                    if (null != airport) {
                        //域的名称 域的内容 是否存储
                        Field lineDataField = new TextField("lineData", lineData, Field.Store.YES);
                        Field nameField = new TextField("name", airport.getName().toLowerCase(), Field.Store.YES);
                        Field cityField = new TextField("city", airport.getCity().toLowerCase(), Field.Store.YES);
                        Field countryField = new TextField("country", airport.getCountry().toLowerCase(), Field.Store.YES);
                        Field iATAField = new TextField("iATA", airport.getIATA().toLowerCase(), Field.Store.YES);
                        Field longitudeField = new TextField("longitude-latitude",
                                airport.getLongitude() + " " + airport.getLatitude(), Field.Store.YES);

                        //创建Document 对象
                        Document document = new Document();
                        document.add(lineDataField);
                        document.add(nameField);
                        document.add(cityField);
                        document.add(countryField);
                        document.add(iATAField);
                        document.add(longitudeField);
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

    Airport getAirport(String lineData) {
        if (!StringUtils.isEmpty(lineData)) {
            String[] fieldsData = lineData.split(",");
            if (null != fieldsData && fieldsData.length == 14) {
                Airport airport = new Airport();
                airport.setAirportID(Long.valueOf(fieldsData[0]));
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

    void getIndexWriter() throws IOException {
        if (!indexWriter.isOpen()) {
            Analyzer analyzer = new StandardAnalyzer();
            IndexWriterConfig config = new IndexWriterConfig(analyzer);
            indexWriter = new IndexWriter(directory, config);
        }
    }

    /**
     * 1，提供rest api来查询机场（响应应为json格式，包括机场的所有字段）：
     * a，按名称查询机场，支持模糊查询。
     * b，通过国际航空运输协会查询机场，支持精确查询。
     * c，按纬度/经度查询机场，获取最近的机场。
     * d，按城市或国家查询机场。
     *
     * @param name      按名称查询机场，支持模糊查询 WildcardQuery
     * @param iATA      通过国际航空运输协会查询机场，支持精确查询 TermQuery
     * @param latitude  按纬度/经度查询机场，获取最近的机场。 FuzzyQuery
     * @param longitude 按纬度/经度查询机场，获取最近的机场。 FuzzyQuery
     * @param city      按城市或国家查询机场。 TermQuery
     * @param country   按城市或国家查询机场。 TermQuery
     * @return
     */
    @Override
    public List<Airport> getAirports(String name, String iATA, Double latitude, Double longitude, String city, String country) {
        List<Airport> airports = new ArrayList<>();
        try {
            getIndexWriter();
            indexWriter.deleteAll(); //原始文件
            List<Document> documents = getDocumentFromFile(getFileByResourceName("airports.dat"));
            BooleanQuery.Builder b = new BooleanQuery.Builder();
            if (null != documents && !documents.isEmpty()) {
                //创建索引，并写入索引库
                indexWriter.addDocuments(documents);
                indexWriter.commit();
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
            IndexReader reader = DirectoryReader.open(directory);//读索引
            IndexSearcher indexSearcher = new IndexSearcher(reader);
            BooleanQuery query = b.build();
            TopDocs topDocs = indexSearcher.search(query, 100);
            System.out.println("匹配查询到" + topDocs.totalHits + "个记录");
            for (ScoreDoc scoreDoc : topDocs.scoreDocs) {
                Airport airport = getAirport(indexSearcher.doc(scoreDoc.doc).get("lineData"));
                airports.add(airport);
            }
            indexWriter.close();
            reader.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return airports;
    }

    @Override
    public List<AirlineRoute> getAirlineRoutes(String sourceAirport, String destinationAirport) {
        return null;
    }

}

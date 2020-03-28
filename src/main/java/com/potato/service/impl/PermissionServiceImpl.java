package com.potato.service.impl;

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
    void init() {
        try {
            directory = new RAMDirectory();
            Analyzer analyzer = new StandardAnalyzer(); // 标准分词器
            IndexWriterConfig config = new IndexWriterConfig(analyzer);
            //1.创建IndexWriter
            indexWriter = new IndexWriter(directory, config);

//            //原始文件
//            Document airlinesDocument = getDocumentFromFile(getFileByResourceName("airlines.dat"));
//            if (null != airlinesDocument) {
//                //创建索引，并写入索引库
//                indexWriter.addDocument(airlinesDocument);
//            }
//            Document airportsDocument = getDocumentFromFile(getFileByResourceName("airports.dat"));
//            if (null != airportsDocument) {
//                //创建索引，并写入索引库
//                indexWriter.addDocument(airportsDocument);
//            }
//            Document routesDocument = getDocumentFromFile(getFileByResourceName("routes.dat"));
//            if (null != routesDocument) {
//                //创建索引，并写入索引库
//                indexWriter.addDocument(routesDocument);
//            }
            // indexWriter.close();
        } catch (IOException e) {
            e.printStackTrace();
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
                        Field longitudeField = new TextField("longitude", String.valueOf(airport.getLongitude()), Field.Store.YES);
                        Field latitudeField = new TextField("latitude", String.valueOf(airport.getLatitude()), Field.Store.YES);

                        //创建Document 对象
                        Document document = new Document();
                        document.add(lineDataField);
                        document.add(nameField);
                        document.add(cityField);
                        document.add(countryField);
                        document.add(iATAField);
                        document.add(longitudeField);
                        document.add(latitudeField);
                        documents.add(document);
                    } else {
                        log.warn("有误数据：{}", lineData);
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

    @Override
    public void save() {
        getTest("Gisborne");
    }


    void getTest(String keyword) {
        try {
            indexWriter.deleteAll();
            //原始文件
            List<Document> documents = getDocumentFromFile(getFileByResourceName("airports.dat"));
            if (null != documents && !documents.isEmpty()) {
                //创建索引，并写入索引库
                indexWriter.addDocuments(documents);
                indexWriter.commit();
            }
            BooleanQuery.Builder b = new BooleanQuery.Builder();
            //b.add(new FuzzyQuery(new Term("name", keyword), 1), BooleanClause.Occur.MUST);
            b.add(new TermQuery(new Term("sex", "GID".toLowerCase())), BooleanClause.Occur.MUST);
            BooleanQuery query = b.build();
            excQuery(query);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    void excQuery(Query query) throws IOException {
        IndexReader reader = DirectoryReader.open(directory);//读索引
        IndexSearcher is = new IndexSearcher(reader);
        TopDocs hits = is.search(query, 10);//执行搜索
        System.out.println("匹配查询到" + hits.totalHits + "个记录");
        for (ScoreDoc scoreDoc : hits.scoreDocs) {
            Document doc = is.doc(scoreDoc.doc);
            String lineData = doc.get("lineData");
            Airport airport = getAirport(lineData);
            System.out.println(airport.toString());//打印Document的fileName属性
        }
        reader.close();
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
        try {
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
                System.out.println(airport.toString());//打印Document的fileName属性
            }
            reader.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }

}

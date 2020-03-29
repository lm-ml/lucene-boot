package com.flight.service.impl;

import com.flight.service.LuceneService;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;
import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.standard.StandardAnalyzer;
import org.apache.lucene.document.Document;
import org.apache.lucene.index.DirectoryReader;
import org.apache.lucene.index.IndexReader;
import org.apache.lucene.index.IndexWriter;
import org.apache.lucene.index.IndexWriterConfig;
import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.store.Directory;
import org.apache.lucene.store.RAMDirectory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.Resource;
import org.springframework.core.io.ResourceLoader;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import javax.annotation.PostConstruct;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.List;

@Slf4j
@Service
public class LuceneServiceImpl implements LuceneService {

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
            this.closeIndexWriter();
        }
    }

    @Override
    public Directory getDirectory() {
        return directory;
    }

    @Override
    public IndexWriter getIndexWriter() throws IOException {
        if (null == indexWriter || !indexWriter.isOpen()) {
            Analyzer analyzer = new StandardAnalyzer();
            IndexWriterConfig config = new IndexWriterConfig(analyzer);
            indexWriter = new IndexWriter(directory, config);
        }
        return indexWriter;
    }

    @Override
    public IndexReader openIndexReader() throws IOException {
        return DirectoryReader.open(getDirectory());
    }

    @Override
    public IndexSearcher getIndexSearcher(IndexReader indexReader) throws IOException {
        return new IndexSearcher(indexReader);
    }

    @Override
    public void closeIndexWriter() throws IOException {
        if (null != indexWriter && indexWriter.isOpen()) {
            indexWriter.close();
        }
    }

    @Override
    public void closeIndexReader(IndexReader indexReader) throws IOException {
        if (null != indexReader) {
            indexReader.close();
        }
    }

    /**
     * 根据资源名称读取文件
     *
     * @param resourceName
     * @return
     * @throws IOException
     */

    /**
     * cannot be resolved to absolute file path because it does not reside in the file.
     * @param resourceName
     * @return
     * @throws IOException
     */
    @Override
    public File getFileByResourceName(String resourceName) throws IOException {
        if (StringUtils.isEmpty(resourceName)) {
            return null;
        }
        Resource resource = resourceLoader.getResource("classpath:data/" + resourceName);
        if (null != resource && resource.exists()) {
            return resource.getFile();
        }
        return null;
    }

    @Override
    public File copyInputStreamToFileByResourceName(String resourceName) throws IOException {
        if (StringUtils.isEmpty(resourceName)) {
            return null;
        }
        ClassPathResource classPathResource = new ClassPathResource("data/" + resourceName);
        InputStream inputStream = classPathResource.getInputStream();
        File file = File.createTempFile("test", ".txt");
        try {
            FileUtils.copyInputStreamToFile(inputStream, file);
        } finally {
            IOUtils.closeQuietly(inputStream);
        }
        return file;
    }



    @Override
    public long deleteAll() throws IOException {
        if (null != indexWriter && indexWriter.isOpen()) {
            return indexWriter.deleteAll();
        }
        return 0;
    }

    @Override
    public long addDocuments(List<Document> documents) throws IOException {
        if (null != indexWriter && indexWriter.isOpen()) {
            return indexWriter.addDocuments(documents);
        }
        return 0;
    }

    @Override
    public long commit() throws IOException {
        if (null != indexWriter && indexWriter.isOpen()) {
            return indexWriter.commit();
        }
        return 0;
    }
}

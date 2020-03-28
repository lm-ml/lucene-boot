package com.flight.service.impl;

import com.flight.service.LuceneService;
import lombok.extern.slf4j.Slf4j;
import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.standard.StandardAnalyzer;
import org.apache.lucene.document.Document;
import org.apache.lucene.index.IndexWriter;
import org.apache.lucene.index.IndexWriterConfig;
import org.apache.lucene.store.Directory;
import org.apache.lucene.store.RAMDirectory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.Resource;
import org.springframework.core.io.ResourceLoader;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import javax.annotation.PostConstruct;
import java.io.File;
import java.io.IOException;
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
    public void closeIndexWriter() throws IOException {
        indexWriter.close();
    }

    /**
     * 根据资源名称读取文件
     *
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
        if (resource.exists()) {
            return resource.getFile();
        }
        return null;
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

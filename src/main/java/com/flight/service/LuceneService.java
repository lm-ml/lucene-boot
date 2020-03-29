package com.flight.service;

import org.apache.lucene.document.Document;
import org.apache.lucene.index.IndexReader;
import org.apache.lucene.index.IndexWriter;
import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.store.Directory;

import java.io.File;
import java.io.IOException;
import java.util.List;

/**
 * Lucene相关服务
 */
public interface LuceneService {

    Directory getDirectory();

    IndexWriter getIndexWriter() throws IOException;

    IndexReader openIndexReader() throws IOException;

    IndexSearcher getIndexSearcher(IndexReader indexReader) throws IOException;

    void closeIndexWriter() throws IOException;

    void closeIndexReader(IndexReader indexReader) throws IOException;

    File getFileByResourceName(String resourceName) throws IOException;

    long deleteAll() throws IOException;

    long addDocuments(List<Document> documents) throws IOException;

    long commit() throws IOException;
}

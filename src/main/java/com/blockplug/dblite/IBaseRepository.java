package com.blockplug.dblite;


import com.couchbase.lite.Database;
import com.couchbase.lite.Query;

import java.util.List;

public interface IBaseRepository<T extends DocumentEntity> {
    T findOneById(String documentId);
     void createIndex();
    com.couchbase.lite.Document findById(String id);
    List<T> findBy(Query query);
    boolean deleteByDocumentId(String documentId);
   T save(T entity);
    boolean isKnownType(Class<?> type);
    Database getDatabase();
    boolean delete();
    List<T> findAll();
    int count();
    List<T> pageOF(int offset,int limit);
    List<T> pageOFAcending(int offset,int limit);
    void close();
    String getTableName();

}

package com.blockplug.dblite;

import com.couchbase.lite.Database;
import com.couchbase.lite.Document;
import com.couchbase.lite.Query;
import org.dizitart.no2.Cursor;
import org.dizitart.no2.FindOptions;

import java.awt.*;
import java.util.List;
public  class DocumentRepository<T extends DocumentEntity> implements IBaseRepository<T> ,ICBRepository<T>,INitriteRepository<T>{

    IBaseRepository<T> baseRepository;
    public DocumentRepository(DBConfig config) {
        switch (config.getDbName()){
            case NITRITE:baseRepository= new CBDocumentRepository(config);break;
            case COUCHBASE:baseRepository= new NitriteDocumentRepository(config);break;
                default:throw new RuntimeException("Invalid database selection");
        }
    }

    @Override
    public T findOneById(String documentId) {
        if(baseRepository instanceof ICBRepository){
            ICBRepository<T> repository= (ICBRepository<T>) baseRepository;
          return repository.findOneById(documentId);
        }
        return baseRepository.findOneById(documentId);
    }

    @Override
    public void createIndex() {
        baseRepository.createIndex();
    }

    @Override
    public Document findById(String id) {
        if(baseRepository instanceof ICBRepository){
            ICBRepository<T> repository= (ICBRepository<T>) baseRepository;
            return repository.findById(id);
        }
        throw  new RuntimeException("Invalid operation");
    }

    @Override
    public List<T> findBy(Query query) {
        if(baseRepository instanceof ICBRepository){
            ICBRepository<T> repository= (ICBRepository<T>) baseRepository;
            return repository.findBy(query);
        }
        throw  new RuntimeException("Invalid operation");
    }

    @Override
    public boolean deleteByDocumentId(String documentId) {
        return baseRepository.deleteByDocumentId(documentId);
    }

    @Override
    public List<T> findBy(Cursor cursor) {
        if(baseRepository instanceof INitriteRepository){
            INitriteRepository<T> repository= (INitriteRepository<T>) baseRepository;
            return repository.findBy(cursor);
        }
        throw  new RuntimeException("Invalid operation");
    }

    @Override
    public T save(T entity) {
        return baseRepository.save(entity);
    }

    @Override
    public boolean isKnownType(Class<?> type) {
        return baseRepository.isKnownType(type);
    }

    @Override
    public Database getDatabase() {
        if(baseRepository instanceof ICBRepository){
            ICBRepository<T> repository= (ICBRepository<T>) baseRepository;
            return repository.getDatabase();
        }
        throw  new RuntimeException("Invalid operation");
    }

    @Override
    public boolean delete() {
        return baseRepository.delete();
    }

    @Override
    public List<T> findAll() {
        return baseRepository.findAll();
    }

    @Override
    public long count() {
        return baseRepository.count();
    }

    @Override
    public List<T> pageOF(int offset, int limit) {
        return baseRepository.pageOF(offset,limit);
    }

    @Override
    public List<T> pageOFAscending(int offset, int limit) {
        return baseRepository.pageOFAscending(offset,limit);
    }

    @Override
    public void close() {
        baseRepository.close();
    }

    @Override
    public String getCollectionName() {
        return baseRepository.getCollectionName();
    }

    @Override
    public Cursor find(FindOptions findOptions) {
        if(baseRepository instanceof INitriteRepository){
            INitriteRepository<T> repository= (INitriteRepository<T>) baseRepository;
            return repository.find(findOptions);
        }
        throw  new RuntimeException("Invalid operation");
    }
}

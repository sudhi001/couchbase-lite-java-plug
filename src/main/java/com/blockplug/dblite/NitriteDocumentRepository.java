package com.blockplug.dblite;

import com.couchbase.lite.internal.database.util.TextUtils;
import org.dizitart.no2.*;
import org.dizitart.no2.filters.Filters;

import java.io.File;
import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class NitriteDocumentRepository<T extends DocumentEntity> extends BaseDocumentRepository<T> implements INitriteRepository<T> {
    Nitrite database;
    NitriteCollection collection;
    String collectionName;

    public NitriteDocumentRepository(DBConfig config,OnDocumentLifeListener onDocumentLifeListener) {
        super(config,onDocumentLifeListener);
        init(config);
    }

    private void init(DBConfig config) {

        if (config.getCollectionName() == null) {
            if (config.getEntityType().isAnnotationPresent(DocumentNode.class)) {
                DocumentNode documentNode = (DocumentNode) config.getEntityType().getAnnotation(DocumentNode.class);
                this.collectionName = (documentNode.name() != null && documentNode.name().trim().length() > 0) ? documentNode.name() : config.getEntityType().getName().toLowerCase();
            } else {
                this.collectionName = config.getEntityType().getSimpleName().toLowerCase();
            }

        } else {
            this.collectionName = config.getCollectionName();
        }
        File file = new File(config.getDbPath() + File.separator);
        if (!file.exists() || !file.isDirectory()) {
            file.mkdirs();
        }
        database = Nitrite.builder()
                .compressed()
                .filePath(new File(config.getDbPath() + collectionName + ".db"))
                .openOrCreate(config.getDbUsername(), config.getDbPassword());
        collection = database.getCollection(collectionName);


    }

    @Override
    public T findOneById(String documentId) {
        try {
            T object = (T) getConfig().getEntityType().getDeclaredConstructor().newInstance();
            Document document = collection.getById(NitriteId.createId(Long.parseLong(documentId)));
            copyNitriteDocumentToEntity(document, object);
            return object;
        } catch (InstantiationException e) {
            e.printStackTrace();
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        } catch (InvocationTargetException e) {
            e.printStackTrace();
        } catch (NoSuchMethodException e) {
            e.printStackTrace();
        }

        return null;
    }

    @Override
    public void createIndex() {
        onDocumentLifeListener.createIndex();
    }

    @Override
    public NitriteCollection getCollection() {
        return collection;
    }

    @Override
    public boolean deleteByDocumentId(String documentId) {
        collection.remove(Filters.eq(DOCUMENT_ID, documentId));
        return true;
    }

    @Override
    public T save(T entity) {
        org.dizitart.no2.Document document = null;
        try {
            document = TextUtils.isEmpty(entity.getDocumentID()) ? null
                    : collection.getById(NitriteId.createId(Long.parseLong(entity.getDocumentID())));
        } catch (NumberFormatException e) {
        }
        Map properties = createMapFromEntity(entity);
        if (document == null) {
            document = new org.dizitart.no2.Document(properties);
            entity.setDocumentID(String.valueOf(document.getId().getIdValue()));
            collection.insert(document);
        } else {
            document.putAll(properties);
            collection.update(Filters.eq(DOCUMENT_ID, String.valueOf(document.getId().getIdValue())), document);
        }
        return entity;
    }


    @Override
    public boolean delete() {
        collection.drop();
        return true;
    }

    @Override
    public List<T> findAll() {
        Cursor cursor = collection.find();
        return findBy(cursor);
    }

    @Override
    public List<T> findBy(Cursor cursor) {
        List<T> data = new ArrayList<>();
        T object;
        for (Document document : cursor) {
            try {
                object = (T) getConfig().getEntityType().getDeclaredConstructor().newInstance();
                copyNitriteDocumentToEntity(document, object);
                data.add(object);
            } catch (InstantiationException e) {
                e.printStackTrace();
            } catch (IllegalAccessException e) {
                e.printStackTrace();
            } catch (InvocationTargetException e) {
                e.printStackTrace();
            } catch (NoSuchMethodException e) {
                e.printStackTrace();
            }

        }
        return data;
    }


    @Override
    public long count() {
        return collection.size();
    }

    @Override
    public List<T> pageOF(int offset, int limit) {
        Cursor cursor = collection.find(FindOptions.limit(offset, limit));
        return findBy(cursor);
    }

    @Override
    public List<T> find(Filter filter) {
        Cursor cursor = collection.find(filter);
        return findBy(cursor);
    }

    @Override
    public List<T> pageOFAscending(int offset, int limit) {
        Cursor cursor = collection.find(FindOptions.sort(CREATED_TIME, SortOrder.Ascending).limit(offset, limit));
        return findBy(cursor);
    }

    @Override
    public void close() {
        collection.close();
    }

    @Override
    public String getCollectionName() {
        return collectionName;
    }

    @Override
    public Cursor find(FindOptions findOptions) {
        return collection.find(findOptions);
    }

    @Override
    public Cursor find(Filter filter, FindOptions findOptions) {
        return collection.find(filter, findOptions);
    }
}

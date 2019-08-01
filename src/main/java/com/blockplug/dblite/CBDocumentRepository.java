/**
 *  Copyright 20199 Sudhi S sudhis@live.com / droidsworld@gmail.com
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *  http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */
package com.blockplug.dblite;

import com.couchbase.lite.*;
import com.couchbase.lite.internal.database.util.TextUtils;

import java.io.File;
import java.lang.reflect.InvocationTargetException;
import java.util.*;

public  class CBDocumentRepository<T extends DocumentEntity> extends BaseDocumentRepository<T> implements ICBRepository<T>{

    //this list Hold the methods from table to reduce the repeated reflection function and to speed up the process.

    private Database database;
    private Manager manager;
    private String collectionName;


    /**
     *
     * @param config = {@link DBConfig}
     */
    public CBDocumentRepository(DBConfig config) {
        super(config);
        init(config);
    }



    private void init(DBConfig config) {
        if(config.getCollectionName()==null){
            if(config.getEntityType().isAnnotationPresent(DocumentNode.class)){
                DocumentNode documentNode = (DocumentNode) config.getEntityType().getAnnotation(DocumentNode.class);
                this.collectionName = (documentNode.name()!=null&&documentNode.name().trim().length()>0)?documentNode.name():config.getEntityType().getName().toLowerCase();
            }else{
                this.collectionName = config.getEntityType().getName().toLowerCase();
            }

        }else{
            this.collectionName =config.getCollectionName();
        }

        openOrInitDatabase( );
    }
    private void openOrInitDatabase(  ){
        try {
            DatabaseOptions options = new DatabaseOptions();
            options.setCreate(true);
            if(getConfig().getDbPassword()!=null) {
                options.setEncryptionKey(getConfig().getDbPassword());
            }
            this.manager = new Manager(new JavaContext(){
                @Override
                public File getRootDirectory() {
                    return getConfig().getDbPath()!=null?new File(getConfig().getDbPath()):super.getRootDirectory();
                }

            }, Manager.DEFAULT_OPTIONS);
                database = manager.openDatabase(collectionName.toLowerCase(), options);
                createIndex();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * Create an index based on the document id
     */
    @Override
    public void createIndex() {
        View todoView = database.getView(collectionName);
        todoView.setMap((document, emitter) -> emitter.emit(document.get("_id"), document), "1");
    }



    /**
     * Find the document by id
     *
     */

    @Override
    public com.couchbase.lite.Document findById(String id) {
        return database.getDocument(id);
    }

    /**
     *
     */
    @Override
    public T findOneById(String id) {
        Document document = database.getDocument(id);
        if (document != null) {
            T object = null;
            try {
                object = (T) config.getEntityType().getDeclaredConstructor().newInstance();
                copyCouchBaseDocumentToEntity(document, object);
            } catch (InstantiationException e) {
                e.printStackTrace();
            } catch (IllegalAccessException e) {
                e.printStackTrace();
            } catch (InvocationTargetException e) {
                e.printStackTrace();
            } catch (NoSuchMethodException e) {
                e.printStackTrace();
            }
            return object;
        }
        return null;
    }
@Override
    public List<T> findBy(Query query) {
        try {
            List<T> dataSet = new LinkedList<>();
            QueryEnumerator result = query.run();
            T object;
            com.couchbase.lite.Document document = null;
            for (Iterator<QueryRow> it = result; it.hasNext(); ) {
                QueryRow row = it.next();
                document = row.getDocument();
                object = (T) config.getEntityType().getDeclaredConstructor().newInstance();
                copyCouchBaseDocumentToEntity(document, object);
                dataSet.add(object);
            }
            return dataSet;
        }catch (CouchbaseLiteException e){
            e.printStackTrace();
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        } catch (InstantiationException e) {
            e.printStackTrace();
        } catch (NoSuchMethodException e) {
            e.printStackTrace();
        } catch (InvocationTargetException e) {
            e.printStackTrace();
        }
        return Collections.emptyList();
    }

    /**
     * Delete DocumentColumn by id
     */
    @Override
    public boolean deleteByDocumentId(String documentId) {
        try {
            com.couchbase.lite.Document document = database.getDocument(documentId);
            if(document!=null){
                document.delete();
            }else{
                return false;
            }
            return true;
        } catch (CouchbaseLiteException e) {
            e.printStackTrace();
            return false;
        }
    }



    /**
     * Save or Update the entity
     *
     */
    @Override
    public  T save(T entity) {
        try {
            com.couchbase.lite.Document document = TextUtils.isEmpty(entity.getDocumentID()) ? null : database.getDocument(entity.getDocumentID());
            Map properties= createMapFromEntity(entity);
            if (document == null) {
                document =  this.database.createDocument();
                entity.setDocumentID(document.putProperties(properties).getDocument().getId());
            } else {
                document.update(newRevision -> {
                    Map oldProperties = newRevision.getUserProperties();
                    oldProperties.putAll(properties);
                    newRevision.setUserProperties(oldProperties);
                    return true;
                });
            }
            return entity;
        } catch (CouchbaseLiteException e) {
            e.printStackTrace();
            return null;
        }
    }



    /**
     * To check the Type of data is supported by the library of Couchbase Lite
     * @param type  {@link Class}
     * @return If true then the type of data is supported else not.
     */
    @Override
    public boolean isKnownType(Class<?> type) {
        return  knownItems.contains(type);
    }

    /**
     *  To get the Couchbase Database object
     */
    @Override
    public Database getDatabase() {
        return database;
    }

    /**
     * To delete the Database
     */
    @Override
    public boolean delete()  {
        try {
            database.delete();
            database.close();
            database=null;
        } catch (CouchbaseLiteException e) {
            e.printStackTrace();
            return false;
        }
        return true;
    }

    /**
     * @return List of saved entities
     */
    @Override
    public List<T> findAll() {
        return findBy(getDatabase().createAllDocumentsQuery());
    }

    /**
     * @return Total number of rows
     */
    @Override
    public long count() {
        return database.getDocumentCount();
    }

    /**
     * Pagination function
     */
    @Override
    public List<T> pageOF(int offset,int limit) {
        Query query= database.createAllDocumentsQuery();
        query.setLimit(limit);
        query.setSkip(offset);
        query.setDescending(true);
        return findBy(query);
    }
    /**
     * Pagination function
     * @param offset  Where the pagination stats
     * @param limit  Maximum number of items per page
     * @return List ot items
     */
    @Override
    public List<T> pageOFAscending(int offset, int limit) {
        Query query= database.createAllDocumentsQuery();
        query.setLimit(limit);
        query.setSkip(offset);
        return findBy(query);
    }
    /**
     * Close the database instance.
     * Better you should call this function before the application termination.
     */
    @Override
    public void close() {
        if (database != null) {
            database.close();
        }
    }

    /**
     *
     * @return Name of the current table
     */
    @Override
    public String getCollectionName() {
        return collectionName;
    }
}

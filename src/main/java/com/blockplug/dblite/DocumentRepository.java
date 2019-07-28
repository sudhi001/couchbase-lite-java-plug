package com.blockplug.dblite;
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

import com.couchbase.lite.*;
import com.couchbase.lite.internal.database.util.TextUtils;
import javafx.beans.property.ObjectProperty;

import java.io.File;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.sql.Array;
import java.sql.Blob;
import java.util.*;

public abstract class DocumentRepository<T extends DocumentEntity> {
    private final String rootDirectoryPath;
    private final String dbPath;
    //this list Hold the methods from table to reduce the repeated reflection function and to speed up the process.
    Map<Field, Method> mapOfFieldMethod = new LinkedHashMap<>();
    Map<Field, Method> mapOfFieldGetMethod = new LinkedHashMap<>();
    private Database database;
    private Class aClass;
    private Manager manager;
    private final String tableName;
    static List<Class> knownItems = new LinkedList<Class>(){
        {
            add(String.class);
            add(Integer.class);
            add(Float.class);
            add(Date.class);
            add(Array.class);
            add(Boolean.class);
            add(Blob.class);
            add(Number.class);
            add(Long.class);
            add(long.class);
            add(double.class);
            add(Dictionary.class);
        }
    };
    /**
     * Simple implementation
     * @param aClass
     * @param tableName
     */
    public DocumentRepository(Class aClass,String tableName) {
        this(aClass,tableName,null,null,null);
    }

    /**
     *
     * @param config = {@link DBConfig}
     */
    public DocumentRepository(DBConfig config) {
        this(config.getEntityType(),config.getDbPassword(),config.getRootFolderPath(),config.getDbPath());
    }
    /**
     *
     * @param aClass
     * @param password
     * @param rootDirectoryPath
     * @param dbPath
     */
    public DocumentRepository(Class aClass,String password, String rootDirectoryPath ,String dbPath) {
        this(aClass,null,password,rootDirectoryPath,dbPath);
    }
    /**
     *
     * @param aClass
     * @param tableName
     * @param password
     * @param rootDirectoryPath
     * @param dbPath
     */
    public DocumentRepository(Class aClass,String tableName,String password, String rootDirectoryPath ,String dbPath) {
        this.aClass=aClass;
        if(tableName==null){
            if(aClass.isAnnotationPresent(DocumentNode.class)){
                DocumentNode documentNode = (DocumentNode) aClass.getAnnotation(DocumentNode.class);
                this.tableName= (documentNode.name()!=null&&documentNode.name().trim().length()>0)?documentNode.name():aClass.getName().toLowerCase();
            }else{
                this.tableName= aClass.getName().toLowerCase();

            }

        }else{
            this.tableName=tableName;
        }

        this.rootDirectoryPath=rootDirectoryPath;
        this.dbPath=dbPath;
        openOrInitDatabase(password );
    }
    private void openOrInitDatabase(String password ){
        try {
            DatabaseOptions options = new DatabaseOptions();
            options.setCreate(true);
            if(password!=null) {
                options.setEncryptionKey(password);
            }
            this.manager = new Manager(new JavaContext(){
                @Override
                public File getRootDirectory() {
                    return (rootDirectoryPath!=null&&dbPath!=null)?new File(rootDirectoryPath, dbPath):super.getRootDirectory();
                }

            }, Manager.DEFAULT_OPTIONS);
                database = manager.openDatabase(tableName.toLowerCase(), options);
                createIndex();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * Create an index based on the document id
     */
    public void createIndex() {
        View todoView = database.getView(tableName);
        todoView.setMap((document, emitter) -> emitter.emit(document.get("_id"), document), "1");
    }



    /**
     * Find the document by id
     *
     * @param id
     * @return
     */

    public com.couchbase.lite.Document findById(String id) {
        return database.getDocument(id);
    }


    public List<T> findBy(Query query) {
        try {
            List<T> dataSet = new LinkedList<>();
            QueryEnumerator result = query.run();
            T object;
            com.couchbase.lite.Document document = null;
            for (Iterator<QueryRow> it = result; it.hasNext(); ) {
                QueryRow row = it.next();
                document = row.getDocument();
                object = (T) aClass.getDeclaredConstructor().newInstance();
                copyTo(document, object);
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
     * @param documentId
     * @return
     */
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
     * @param entity
     * @param <T>
     * @return
     */
    public <T extends DocumentEntity> T save(T entity) {
        try {
            com.couchbase.lite.Document document = TextUtils.isEmpty(entity.getDocumentID()) ? null : database.getDocument(entity.getDocumentID());
            Map properties= createDocument(entity);
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
     * A document is created using Registration object with the help of Reflections.
     * The getter field name is set as key the document attributes.
     * @param document
     * @param object
     * @return
     */

    /**
     * A document is created using Registration object with the help of Reflections.
     * The getter field name is set as key the document attributes.
     * @param document
     * @param object
     * @return
     */
    private Map createDocument(Map document, Object object) {
        Map<Field, Method> methodLinkedHashMap = allGetMethodsAndFields(object.getClass());
        for (Field field : methodLinkedHashMap.keySet()) {
            Method method = methodLinkedHashMap.get(field);
            try {
                Object value = method.invoke(object);
                if (value != null) {
                    if (value!=null&&value instanceof  KeyValue) {
                        KeyValue keyValue= (KeyValue) value;
                        document.put(field.getName(), keyValue.getKey()+":"+keyValue.getName());
                    }else if (isKnownType(field.getType()))
                        document.put(field.getName(), value);
                } else {
                    document.put(field.getName(), null);
                }
            } catch (IllegalAccessException e) {
                e.printStackTrace();
            } catch (InvocationTargetException e) {
                e.printStackTrace();
            }
        }
        return document;
    }



    private Map createDocument(Object object) {
        return createDocument(new HashMap(), object);
    }
    private List<Method> listGetMethods(Class clazz) {
        List<Method> methodsList = new ArrayList<>();
        while (clazz != null) {
            Method[] methods = clazz.getDeclaredMethods();
            for (Method method : methods) {
                if (method.getName().startsWith("get")) {
                    methodsList.add(method);
                }
            }
            clazz = clazz.getSuperclass();
        }
        return methodsList;
    }
    private List<Method> listSetMethods(Class clazz) {
        List<Method> methodsList = new ArrayList<>();
        while (clazz != null) {
            Method[] methods = clazz.getDeclaredMethods();
            for (Method method : methods) {
                if (method.getName().startsWith("set")) {
                    methodsList.add(method);
                }
            }
            clazz = clazz.getSuperclass();
        }
        return methodsList;
    }

    private Map<Field, Method> allGetMethodsAndFields(Class clazz) {
        if (!mapOfFieldGetMethod.isEmpty())
            return mapOfFieldGetMethod;
        Map<Field, Method> filedList = new LinkedHashMap<>();
        List<Method> setMethods = listGetMethods(clazz);
        for (Method method : setMethods) {
            String methodName = method.getName();
            String fieldName = String.valueOf(methodName.charAt(3)).toLowerCase() + methodName.substring(4);
            Field field = getField(clazz, fieldName);
            if (field != null) {
                field.setAccessible(true);
                DocumentColumn documentColumnProperty = field.getAnnotation(DocumentColumn.class);
                if (documentColumnProperty != null) filedList.put(field, method);
            }
        }
        mapOfFieldGetMethod.putAll(filedList);
        return filedList;
    }
    private Map<Field, Method> methodsAndFields(Class clazz) {
        if (!mapOfFieldMethod.isEmpty())
            return mapOfFieldMethod;
        Map<Field, Method> filedList = new LinkedHashMap<>();
        List<Method> setMethods = listSetMethods(clazz);
        for (Method method : setMethods) {
            String methodName = method.getName();
            String fieldName = String.valueOf(methodName.charAt(3)).toLowerCase() + methodName.substring(4);
            Field field = getField(clazz, fieldName);
            if (field != null) {
                field.setAccessible(true);
                DocumentColumn documentColumnProperty = field.getAnnotation(DocumentColumn.class);
                if (documentColumnProperty != null) filedList.put(field, method);
            }
        }
        mapOfFieldMethod.putAll(filedList);
        return filedList;
    }
    private Field getField(Class clazz, String fieldName) {
        if (clazz != null) try {
            return clazz.getDeclaredField(fieldName);
        } catch (NoSuchFieldException e) {
            return getField(clazz.getSuperclass(), fieldName);
        }
        return null;
    }
    private void copyTo(Document document, Object object) {
        T data = (T) object;
        Map<Field, Method> methodLinkedHashMap = methodsAndFields(data.getClass());
        for (Field field : methodLinkedHashMap.keySet()) {
            try {
                Method method = methodLinkedHashMap.get(field);
                field.setAccessible(true);
                if (document.getProperty(field.getName())!=null && method != null) {
                    if (field.getType() == Date.class) {
                        Object value = document.getProperty(field.getName());
                        if(value instanceof  Long) {
                            method.invoke(data, new Date((Long)value));
                        }else{
                            method.invoke(data,(Date)document.getProperty(field.getName()));
                        }
                    }else if(field.getType() ==  ObjectProperty.class){
                        Object value = document.getProperty(field.getName());
                        if(value!=null){
                            String[] datas = value.toString().split(":");
                            if(datas.length>1){
                                method.invoke(data, new KeyValue(datas[1],datas[0]));
                            }
                        }
                    }
                    else{
                        method.invoke(data, document.getProperty(field.getName()));
                    }

                }
            } catch (InvocationTargetException e) {
                e.printStackTrace();
            } catch (IllegalAccessException e) {
                e.printStackTrace();
            }

            data.setDocumentID(document.getId());
        }
    }
    /**
     * To check the Type of data is supported by the library of Couchbase Lite
     * @param type  {@link Class}
     * @return If true then the type of data is supported else not.
     */
    public boolean isKnownType(Class<?> type) {
        return  knownItems.contains(type);
    }

    /**
     *  To get the Couchbase Database object
     * @return
     */
    public Database getDatabase() {
        return database;
    }

    /**
     * To delete the Datbase
     * @return
     */
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
    public List<T> findAll() {
        return findBy(getDatabase().createAllDocumentsQuery());
    }

    /**
     * @return Total number of rows
     */
    public int count() {
        return database.getDocumentCount();
    }

    /**
     * Pagination function
     * @param offset
     * @param limit
     * @return
     */
    public List<T> pageOF(int offset,int limit) {
        Query query= database.createAllDocumentsQuery();
        query.setLimit(limit);
        query.setSkip(offset);
        return findBy(query);
    }
    /**
     * Close the database instance.
     * Better you should call this function before the application termination.
     */
    public void close() {
        if (database != null) {
            database.close();
        }
    }

    /**
     *
     * @return Name of the current table
     */
    public String getTableName() {
        return tableName;
    }
}

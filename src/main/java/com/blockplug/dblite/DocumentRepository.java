package com.blockplug.dblite;
/**
 * MIT License
 *
 * Copyright (c) 2019 Sudhi.s sudhis@live.com
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
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

public abstract class DocumentRepository<T extends Entity> {
    private final String rootDirectoryPath;
    private final String dbPath;
    //this list Hold the methods from table to reduce the repeated reflection function and to speed up the process.
    Map<Field, Method> mapOfFieldMethod = new LinkedHashMap<>();
    Map<Field, Method> mapOfFieldGetMethod = new LinkedHashMap<>();
    private Database database;
    private Class aClass;
    private Manager manager;
    final String tableName;
    public DocumentRepository(Class aClass,String tableName) {
        this(aClass,tableName,null,null,null);
    }
    public DocumentRepository(Class aClass,String tableName,String password, String rootDirectoryPath ,String dbPath) {
        this.aClass=aClass;
        this.tableName=tableName;
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

    public void createIndex() {
        View todoView = database.getView(tableName);
        todoView.setMap((document, emitter) -> emitter.emit(document.get("_id"), document), "1");
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
     * Delete Document by id
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
    public <T extends Entity> T save(T entity) {
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
                    }else if (field.getType() == String.class) {
                        document.put(field.getName(), value.toString());
                    } else if (field.getType() == Integer.class) {
                        document.put(field.getName(), (int) value);
                    } else if (field.getType() == Float.class) {
                        document.put(field.getName(), (float) value);
                    } else if (field.getType() == Date.class) {
                        document.put(field.getName(), (Date) value);
                    } else if (field.getType() == Array.class) {
                        document.put(field.getName(), (Array) value);
                    } else if (field.getType() == Boolean.class) {
                        document.put(field.getName(), value);
                    } else if (field.getType() == Blob.class) {
                        document.put(field.getName(), (Blob) value);
                    } else if (field.getType() == Number.class) {
                        document.put(field.getName(), (Number) value);
                    } else if (field.getType() == Long.class) {
                        document.put(field.getName(), (Long) value);
                    } else if (field.getType() == long.class) {
                        document.put(field.getName(), (long) value);
                    }else if (field.getType() == int.class) {
                        document.put(field.getName(), (int) value);
                    }else if (field.getType() == double.class) {
                        document.put(field.getName(), (double) value);
                    }else if (field.getType() == Dictionary.class)
                        document.put(field.getName(), (Dictionary) value);
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
                Document documentProperty = field.getAnnotation(Document.class);
                if (documentProperty != null) filedList.put(field, method);
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
                Document documentProperty = field.getAnnotation(Document.class);
                if (documentProperty != null) filedList.put(field, method);
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

    private void copyTo(com.couchbase.lite.Document document, Object object) {
        T data = (T) object;
        Map<Field, Method> methodLinkedHashMap = methodsAndFields(data.getClass());
        for (Field field : methodLinkedHashMap.keySet()) {
            try {
                Method method = methodLinkedHashMap.get(field);
                field.setAccessible(true);
                if (document.getProperty(field.getName())!=null && method != null) {
                    if (field.getType() == Date.class) {
                        Object value = document.getProperty(field.getName());
                        if(value instanceof Long) {
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

    public Database getDatabase() {
        return database;
    }

    public boolean delete() {
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

    public List<T> findAll() {
        return findBy(getDatabase().createAllDocumentsQuery());
    }

    public int count() {
        return database.getDocumentCount();
    }
    public List<T> pageOF(int offset, int limit) {
        Query query= database.createAllDocumentsQuery();
        query.setLimit(limit);
        query.setSkip(offset);
        return findBy(query);
    }


}

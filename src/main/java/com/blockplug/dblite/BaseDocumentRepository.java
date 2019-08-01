package com.blockplug.dblite;

import com.couchbase.lite.Document;
import javafx.beans.property.ObjectProperty;

import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.sql.Array;
import java.util.*;

public abstract class BaseDocumentRepository<T extends DocumentEntity> implements IBaseRepository<T> {
    Map<Field, Method> mapOfFieldMethod = new LinkedHashMap<>();
    Map<Field, Method> mapOfFieldGetMethod = new LinkedHashMap<>();
    DBConfig config;
    protected List<Class> knownItems = new LinkedList<Class>() {
        {
            add(String.class);
            add(Integer.class);
            add(Float.class);
            add(Date.class);
            add(Array.class);
            add(Boolean.class);
            add(byte[].class);
            add(Number.class);
            add(Long.class);
            add(long.class);
            add(double.class);
            add(Dictionary.class);
        }
    };

    public String documentID = "DOCUMENT_ID";
    public String createdTime = "CREATED_TIME";

    public BaseDocumentRepository(DBConfig config) {
        this.config = config;
    }

    public DBConfig getConfig() {
        return config;
    }

    protected void copyNitriteDocumentToEntity(org.dizitart.no2.Document document, Object entity) {
        T data = (T) entity;
        Map<Field, Method> methodLinkedHashMap = methodsAndFields(data.getClass());
        for (Field field : methodLinkedHashMap.keySet()) {
            try {
                DocumentColumn documentColumn = field.getAnnotation(DocumentColumn.class);
                if (document.containsKey(documentColumn.key())) {
                    Object value = document.get(documentColumn.key());
                    createObject(methodLinkedHashMap, field, value, entity);
                }
            } catch (InvocationTargetException e) {
                e.printStackTrace();
            } catch (IllegalAccessException e) {
                e.printStackTrace();
            }
        }
        data.setDocumentID(String.valueOf(document.getId().getIdValue()));
    }

    @Override
    public boolean isKnownType(Class<?> type) {
        return knownItems.contains(type);
    }

    protected void copyCouchBaseDocumentToEntity(Document document, Object entity) {
        T data = (T) entity;
        Map<Field, Method> methodLinkedHashMap = methodsAndFields(data.getClass());
        for (Field field : methodLinkedHashMap.keySet()) {
            try {
                DocumentColumn documentColumn = field.getAnnotation(DocumentColumn.class);
                if (document.getProperty(documentColumn.key()) != null) {
                    Object value = document.getProperty(documentColumn.key());
                    createObject(methodLinkedHashMap, field, value, entity);
                }
            } catch (InvocationTargetException e) {
                e.printStackTrace();
            } catch (IllegalAccessException e) {
                e.printStackTrace();
            }
        }
        data.setDocumentID(document.getId());
    }

    private void createObject(Map<Field, Method> methodLinkedHashMap, Field field, Object value, Object entity) throws InvocationTargetException, IllegalAccessException {
        Method method = methodLinkedHashMap.get(field);
        field.setAccessible(true);
        if (method != null && value != null) {
            if (field.getType() == Date.class) {
                if (value instanceof Long) {
                    method.invoke(entity, new Date((Long) value));
                } else {
                    method.invoke(entity, (Date) value);
                }
            } else if (field.getType() == ObjectProperty.class) {
                String[] datas = value.toString().split(":");
                if (datas.length > 1) {
                    method.invoke(entity, new KeyValue(datas[1], datas[0]));
                }
            } else if (field.getType() == byte[].class) {
                byte[] datas = value.toString().getBytes();
                method.invoke(entity, datas);
            } else {
                method.invoke(entity, value);
            }
        }
    }

    protected Map createMapFromEntity(T object) {
        return createMapFromEntity(new HashMap(), object);
    }
    /**
     * A document is created using Registration object with the help of Reflections.
     * The getter field name is set as key the document attributes.
     */

    /**
     * A document is created using Registration object with the help of Reflections.
     * The getter field name is set as key the document attributes.
     */
    private Map createMapFromEntity(Map document, Object object) {
        Map<Field, Method> methodLinkedHashMap = allGetMethodsAndFields(object.getClass());
        for (Field field : methodLinkedHashMap.keySet()) {
            Method method = methodLinkedHashMap.get(field);
            try {
                Object value = method.invoke(object);
                DocumentColumn documentColumn = field.getAnnotation(DocumentColumn.class);
                if (value != null) {
                    if (field.getType() == ObjectProperty.class) {
                        KeyValue keyValue = (KeyValue) value;
                        document.put(documentColumn.key(), keyValue.combilnedValue());
                    } else if (isKnownType(field.getType()))
                        document.put(documentColumn.key(), value);
                } else {
                    document.put(documentColumn.key(), null);
                }
            } catch (IllegalAccessException e) {
                e.printStackTrace();
            } catch (InvocationTargetException e) {
                e.printStackTrace();
            }
        }
        return document;
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
                if (field.isAnnotationPresent(DocumentColumn.class)) {
                    filedList.put(field, method);
                }
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
                if (field.isAnnotationPresent(DocumentColumn.class)) {
                    filedList.put(field, method);
                }
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
}

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

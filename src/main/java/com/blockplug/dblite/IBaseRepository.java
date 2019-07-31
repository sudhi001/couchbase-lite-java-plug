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



import java.util.List;

public interface IBaseRepository<T extends DocumentEntity> {

    public T findOneById(String documentId);
    public void createIndex();
    public boolean deleteByDocumentId(String documentId);
    public T save(T entity);
    public boolean isKnownType(Class<?> type);
    public boolean delete();
    public List<T> findAll();
    public long count();
    public List<T> pageOF(int offset,int limit);
    public List<T> pageOFAscending(int offset, int limit);
    public void close();
    public String getCollectionName();


}

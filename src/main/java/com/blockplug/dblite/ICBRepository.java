package com.blockplug.dblite;

import com.couchbase.lite.Database;
import com.couchbase.lite.Query;

import java.util.List;

public interface ICBRepository<T extends DocumentEntity> extends IBaseRepository<T> {
    com.couchbase.lite.Document findById(String id);

    Database getDatabase();

    List<T> findBy(Query query);
}

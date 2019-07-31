package com.blockplug.dblite;

import org.dizitart.no2.Cursor;
import org.dizitart.no2.Filter;
import org.dizitart.no2.FindOptions;

import java.util.List;

public interface INitriteRepository<T extends DocumentEntity> extends IBaseRepository<T>{

    public List<T> findBy(Cursor cursor);
    public  Cursor find(FindOptions findOptions);
    public List<T> find(Filter filter);
    public  Cursor find(Filter filter,FindOptions findOptions);
}

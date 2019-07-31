package com.blockplug.dblite;

import org.dizitart.no2.Cursor;

import java.util.List;

public interface INitriteRepository<T extends DocumentEntity> extends IBaseRepository<T>{

    List<T> findBy(Cursor cursor);
}

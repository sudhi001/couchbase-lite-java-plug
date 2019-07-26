package com.blockplug.dblite;

import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;


public class KeyValue {

    private StringProperty name = new SimpleStringProperty();
    private StringProperty key = new SimpleStringProperty();

    public KeyValue(String name, String key) {
        this.name.set(name);
        this.key.set(key);
    }

    @Override
    public String toString() {
        return name.get();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        KeyValue keyValue = (KeyValue) o;
        return getKey().equals(keyValue.getKey());
    }


    public String getName() {
        return name.get();
    }

    public StringProperty nameProperty() {
        return name;
    }

    public void setName(String name) {
        this.name.set(name);
    }

    public String getKey() {
        return key.get();
    }

    public StringProperty keyProperty() {
        return key;
    }

    public void setKey(String key) {
        this.key.set(key);
    }
}

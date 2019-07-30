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
 */package com.blockplug.dblite;

public class DBConfig {
    private final String rootFolderPath;
    private final String dbPath;
    private final String dbPassword;
    private final  Class entityType;

    private DBConfig(Builder builder) {
        this.rootFolderPath = builder.rootFolderPath;
        this.dbPath = builder.dbPath;
        this.dbPassword = builder.dbPassword;
        this.entityType=builder.entityType;
    }
    public String getRootFolderPath() {
        return rootFolderPath;
    }

    public String getDbPath() {
        return dbPath;
    }

    public String getDbPassword() {
        return dbPassword;
    }

    public Class getEntityType() {
        return entityType;
    }

    public static class Builder {
        private  String rootFolderPath;
        private  String dbPath;
        private  String dbPassword;
        private   Class entityType;
        public Builder setRootFolderPath(String rootFolderPath) {
            this.rootFolderPath = rootFolderPath;
            return this;
        }

        public Builder setEntityType(Class entityType) {
            this.entityType = entityType;
            return this;
        }

        public Builder setDbPath(String dbPath) {
            this.dbPath = dbPath;
            return this;
        }

        public Builder setDbPassword(String dbPassword) {
            this.dbPassword = dbPassword;
            return this;
        }
        public DBConfig buid(){
            return  new DBConfig(this);
        }
    }
}

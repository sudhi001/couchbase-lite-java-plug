package com.blockplug.dblite;

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

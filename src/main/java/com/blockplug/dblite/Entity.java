package com.blockplug.dblite;





import java.io.Serializable;
import java.util.Date;


public abstract class Entity implements Serializable {
    @NXTransactional(key = "CREATED_DATE")
    protected Date createdDate = new Date();
    @NXTransactional(key = "DOCUMENT_ID")
    protected String documentID;



    public Date getCreatedDate() {
        return createdDate;
    }

    public void setCreatedDate(Date createdDate) {
        this.createdDate = createdDate;
    }

    public String getDocumentID() {
        return documentID;
    }

    public void setDocumentID(String documentID) {
        this.documentID = documentID;
    }
}


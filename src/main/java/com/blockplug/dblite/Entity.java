package com.blockplug.dblite;


import java.io.Serializable;


public interface  Entity extends Serializable {
     String getDocumentID();

     void setDocumentID(String documentID);
}


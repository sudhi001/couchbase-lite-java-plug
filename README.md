# couchbase-lite-java-plug

This is base on couchbase lite 1.4.4  implementation. If you are an Android developer then Please don't prefer this.

## Intention to create this Library
Usually when you are planning to implement a lite Nosql embeded database , We may have differenty options, but those databases are keyvalue based databases that loose the ORM functionality. Here my solution is for the users those prefer to implement Couchbase lite as database.

### Sequence Diagram
                    
```seq
Contact->ContactRepository: Save
ContactRepository-->Contact: Save
```

# Model
```java

import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleObjectProperty;
import com.couchbase.lite.Document;
import java.util.Date;

public class Contact implements Entity {
    @Document(key = "DOCUMENT_ID")
    private String documentID;

    @Document(key = "CONTACT_NAME")
    private String name;
    @Document(key = "CONTACT_GENDER")
    private ObjectProperty<KeyValue> gender = new SimpleObjectProperty<>();
    @Document(key = "CONTACT_DATE_OF_BIRTH")
    private Date dateOfBirth;

    @Override
    public String getDocumentID() {
        return documentID;
    }

    @Override
    public void setDocumentID(String documentID) {
        this.documentID=documentID;

    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public KeyValue getGender() {
        return gender.get();
    }


    public void setGender(KeyValue gender) {
        this.gender.set(gender);
    }

    public Date getDateOfBirth() {
        return dateOfBirth;
    }

    public void setDateOfBirth(Date dateOfBirth) {
        this.dateOfBirth = dateOfBirth;
    }
}

```
# Repository
```java
public class ContactRepository extends DocumentRepository<Contact> {
    public ContactRepository( ) {
        super( Contact.class,Contact.class.getName().toLowerCase());

    }

    @Override
    public void createIndex() {
        super.createIndex();

}

}

```

 # Or Repository support AES encryption (Using SQLiteCipher)
   ```java

public class ContactRepository extends DocumentRepository<Contact> {
    public ContactRepository() {
        super( Contact.class,Contact.class.getName().toLowerCase(),"AADMIN123#",System.getProperty("user.home"),"example/data/");

    }

    @Override
    public void createIndex() {
        super.createIndex();

}

}
   ```
# Implementation

```java

  ContactRepository contactRepository= new ContactRepository();
        Contact contact= new Contact();
        contact.setState("Kerala");
        contact.setAddressLineOne("Trivandrum");
        contact.setCountry(new KeyValue("India","IN"));
        contactRepository.save(contact)
```

Copyright 2019 Sudhi S sudhis@live.com / droidsworld@gmail.com

Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except in compliance with the License. You may obtain a copy of the License at

http://www.apache.org/licenses/LICENSE-2.0

Unless required by applicable law or agreed to in writing, software distributed under the License is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the License for the specific language governing permissions and limitations under the License

package com.blockplug.dblite;

public class Contact implements Entity{
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

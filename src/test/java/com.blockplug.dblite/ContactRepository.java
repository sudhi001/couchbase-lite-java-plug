package com.blockplug.dblite;




public class ContactRepository extends DocumentRepository<Contact> {
    public ContactRepository() {
        super( Contact.class,Contact.class.getName().toLowerCase(),"AADMIN123#",System.getProperty("user.home"),"example/data/");

    }

    @Override
    public void createIndex() {
        super.createIndex();

}

}

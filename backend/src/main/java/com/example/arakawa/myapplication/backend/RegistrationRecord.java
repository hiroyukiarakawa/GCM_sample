package com.example.arakawa.myapplication.backend;

import com.googlecode.objectify.annotation.Entity;
import com.googlecode.objectify.annotation.Id;
import com.googlecode.objectify.annotation.Index;
import com.googlecode.objectify.annotation.Serialize;


/** The Objectify object model for device registrations we are persisting */
@Entity
public class RegistrationRecord {

    @Id
    Long id;

    @Index
    private String regId;
    // you can add more fields...

    @Serialize
    private String IMEI;


    public RegistrationRecord() {}

    public String getRegId() {
        return regId;
    }

    public void setRegId(String regId) {
        this.regId = regId;
    }

    public String getIMEI(){
        return IMEI;
    }

    public void setIMEI(String IMEI) {
        this.IMEI = IMEI;
    }

}
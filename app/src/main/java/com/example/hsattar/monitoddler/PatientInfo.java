package com.example.hsattar.monitoddler;

/**
 * Created by hsattar on 3/8/2016.
 */
public class PatientInfo {
    String Name;
    String Age;
    String Parents;
    String ParentsContact;

    String Doctor;
    String Case;
    String Address;
    String WardId;

    String HeartRate;
    String Temperature;
    String BloodPressure;
    String RespRate;

    String LastUpdated;


    String Critical;

    public PatientInfo(String name, String age, String doctor, String case_num, String critical, String hr, String temp, String rr) {
        Age = age;
        Name = name;
        Doctor = doctor;
        WardId = "-";
        Critical = critical;
        Case = case_num;
        Parents = "-";
        ParentsContact = "-";
        Address = "-";
        HeartRate = hr;
        Temperature = temp;
        BloodPressure = "-";
        RespRate = rr;
        LastUpdated = "-";
    }

    public PatientInfo(String name, String age, String doctor, String case_num, String critical) {
        Age = age;
        Name = name;
        Doctor = doctor;
        WardId = "-";
        Critical = critical;
        Case = case_num;
        Parents = "-";
        ParentsContact = "-";
        Address = "-";
        HeartRate = "-";
        Temperature = "-";
        BloodPressure = "-";
        RespRate = "-";
        LastUpdated = "-";
    }

    public PatientInfo(String name, String age, String doctor, String wardId, String aCase, String parents,  String address) {
        Address = address;
        Age = age;
        Case = aCase;
        Doctor = doctor;
        Name = name;
        Parents = parents;
        ParentsContact = "-";
        WardId = wardId;
        HeartRate = "-";
        Temperature = "-";
        BloodPressure = "-";
        RespRate = "-";
        LastUpdated = "-";
        Critical = "-";

    }

    public String getCritical() {
        return Critical;
    }

    public void setCritical(String critical) {
        Critical = critical;
    }

    public String getParents() {
        return Parents;
    }

    public void setParents(String parents) {
        Parents = parents;
    }

    public String getParentsContact() {
        return ParentsContact;
    }

    public void setParentsContact(String parentsContact) {
        ParentsContact = parentsContact;
    }

    public String getAddress() {
        return Address;
    }

    public void setAddress(String address) {
        Address = address;
    }

    public String getAge() {
        return Age;
    }

    public void setAge(String age) {
        Age = age;
    }

    public String getBloodPressure() {
        return BloodPressure;
    }

    public void setBloodPressure(String bloodPressure) {
        BloodPressure = bloodPressure;
    }

    public String getCase() {
        return Case;
    }

    public void setCase(String aCase) {
        Case = aCase;
    }

    public String getDoctor() {
        return Doctor;
    }

    public void setDoctor(String doctor) {
        Doctor = doctor;
    }

    public String getHeartRate() {
        return HeartRate;
    }

    public void setHeartRate(String heartRate) {
        HeartRate = heartRate;
    }

    public String getName() {
        return Name;
    }

    public void setName(String name) {
        Name = name;
    }

    public String getLastUpdated() {
        return LastUpdated;
    }

    public void setLastUpdated(String lastUpdated) {
        LastUpdated = lastUpdated;
    }

    public String getRespRate() {
        return RespRate;
    }

    public void setRespRate(String resp) {
        RespRate = resp;
    }

    public String getTemperature() {
        return Temperature;
    }

    public void setTemperature(String temperature) {
        Temperature = temperature;
    }

    public String getWardId() {
        return WardId;
    }

    public void setWardId(String wardId) {
        WardId = wardId;
    }


}




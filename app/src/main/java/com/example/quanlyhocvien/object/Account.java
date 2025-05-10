package com.example.quanlyhocvien.object;

import java.util.ArrayList;

public class Account implements java.io.Serializable{
    private String UID;
    private int age;
    private Boolean activeStatus;
    private int role;
    private int gender;
    private ArrayList<Log> log;
    private String displayName;
    private String email;
    private String phoneNumber;
    private String photoUrl;

    public Account(String UID, int age, Boolean activeStatus, int role, int gender, ArrayList<Log> log, String displayName, String email, String phoneNumber, String photoUrl) {
        this.UID = UID;
        this.displayName = displayName;
        this.age = age;
        this.activeStatus = activeStatus;
        this.role = role;
        this.gender = gender;
        this.log = log;
        this.email = email;
        this.phoneNumber = phoneNumber;
        this.photoUrl = photoUrl;
    }

    public Account(String UID, String displayName, String email, int role) {
        this.UID = UID;
        this.displayName = displayName;
        this.email = email;
        this.role = role;
    }

    public String getPhotoUrl() {
        return photoUrl;
    }

    public void setPhotoUrl(String photoUrl) {
        this.photoUrl = photoUrl;
    }

    public String getPhoneNumber() {
        return phoneNumber;
    }

    public void setPhoneNumber(String phoneNumber) {
        this.phoneNumber = phoneNumber;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public Account(){
        this.UID = null;
        this.displayName = null;
        this.age = -1;
        this.activeStatus = null;
        this.role = -1;
        this.gender = -1;
        this.log = null;
    }

    public String getDisplayName() {
        return displayName;
    }

    public void setDisplayName(String displayName) {
        this.displayName = displayName;
    }

    public String getUID() {
        return UID;
    }

    public void setUID(String UID) {
        this.UID = UID;
    }

    public int getAge() {
        return age;
    }

    public void setAge(int age) {
        this.age = age;
    }

    public Boolean getActiveStatus() {
        return activeStatus;
    }

    public void setActiveStatus(Boolean activeStatus) {
        this.activeStatus = activeStatus;
    }

    public int getRole() {
        return role;
    }

    public void setRole(int role) {
        this.role = role;
    }

    public int getGender() {
        return gender;
    }

    public void setGender(int gender) {
        this.gender = gender;
    }

    public ArrayList<Log> getLog() {
        return log;
    }

    public void setLog(ArrayList<Log> log) {
        this.log = log;
    }

    @Override
    public String toString() {
        return "account{" + "UID=" + UID + ", age=" + age + ", activeStatus=" + activeStatus + ", role=" + role + ", gender=" + gender + ", Total log=" + log.size() + '}';
    }

    public String genderToString() {
        switch (this.gender) {
            case 0:
                return "Nam";
            case 1:
                return "Nữ";
            case 2:
                return "Khác";
        }
        return "null";
    }

    public String roleToString() {
        switch (this.role) {
            case 0:
                return "Quản trị viên";
            case 1:
                return "Quản lý";
            case 2:
                return "Nhân viên";
        }
        return "null";
    }
}

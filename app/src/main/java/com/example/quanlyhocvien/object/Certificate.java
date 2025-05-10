package com.example.quanlyhocvien.object;

import android.icu.text.SimpleDateFormat;

import com.google.firebase.Timestamp;

public class Certificate implements java.io.Serializable{
    private String id;
    private String name;
    private String desc;
    private Timestamp issueDate;
    private Timestamp expirationDate;
    private int status;
    private String imageURL;
    private String note;

    public Certificate(String id, String name, String desc, Timestamp issueDate, Timestamp expirationDate, int status, String imageURL, String note) {
        this.id = id;
        this.name = name;
        this.desc = desc;
        this.issueDate = issueDate;
        this.expirationDate = expirationDate;
        this.status = status;
        this.imageURL = imageURL;
        this.note = note;
    }

    public Certificate() {
        this.id = "";
        this.name = "";
        this.desc = "";
        this.issueDate = null;
        this.expirationDate = null;
        this.status = 0;
        this.imageURL = "";
        this.note = "";
    }

    public Certificate(String id, String name, int status) {
        this.id = id;
        this.name = name;
        this.status = status;
    }

    public Certificate(String id, String name, Timestamp dateOfIssue, Timestamp expirationDate, String statusString, String description, String note) {
        this.id = id;
        this.name = name;
        this.issueDate = dateOfIssue;
        this.expirationDate = expirationDate;
        this.desc = description;
        this.note = note;
        if (statusString.equals("Chưa hoàn thành")) {
            this.status = 0;
        } else if (statusString.equals("Đã hoàn thành")) {
            this.status = 1;
        } else {
            this.status = 2;
        }
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getDesc() {
        return desc;
    }

    public void setDesc(String desc) {
        this.desc = desc;
    }

    public Timestamp getIssueDate() {
        return issueDate;
    }

    public void setIssueDate(Timestamp issueDate) {
        this.issueDate = issueDate;
    }

    public Timestamp getExpirationDate() {
        return expirationDate;
    }

    public void setExpirationDate(Timestamp expirationDate) {
        this.expirationDate = expirationDate;
    }

    public int getStatus() {
        return status;
    }

    public void setStatus(int status) {
        this.status = status;
    }

    public String getImageURL() {
        return imageURL;
    }

    public void setImageURL(String imageURL) {
        this.imageURL = imageURL;
    }

    public String getNote() {
        return note;
    }

    public void setNote(String note) {
        this.note = note;
    }

    @Override
    public String toString() {
        return "Certificate{" +
                "id='" + id + '\'' +
                ", name='" + name + '\'' +
                ", desc='" + desc + '\'' +
                ", issueDate=" + issueDate +
                ", expirationDate=" + expirationDate +
                ", status=" + status +
                ", imageURL='" + imageURL + '\'' +
                ", note='" + note + '\'' +
                '}';
    }

    public String statusToString(){
        if (status == 0) {
            return "Chưa hoàn thành";
        } else if (status == 1) {
            return "Đã hoàn thành";
        } else {
            return "Đang chờ";
        }
    }

    public String issueDateToString() {
        try {
            SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy");
            return sdf.format(issueDate.toDate());
        } catch (Exception e) {
            return "";
        }
    }

    public String expirationDateToString() {
        try {
            SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy");
            return sdf.format(expirationDate.toDate());
        } catch (Exception e) {
            return "";
        }
    }
}

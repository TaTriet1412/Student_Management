package com.example.quanlyhocvien.object;

import com.google.firebase.Timestamp;

import java.text.SimpleDateFormat;

public class Log implements java.io.Serializable{
    private Timestamp time;
    private String device;

    public Log(Timestamp time, String device) {
        this.time = time;
        this.device = device;
    }

    public Log() {

    }

    public String realTime() {
        SimpleDateFormat formatter = new SimpleDateFormat("dd/MM/yyyy HH:mm");
        String formattedDate = formatter.format(time.toDate());
        return formattedDate;
    }


    public Timestamp getTime() {
        return time;
    }

    public void setTime(Timestamp time) {
        this.time = time;
    }

    public String getDevice() {
        return device;
    }

    public void setDevice(String device) {
        this.device = device;
    }
}

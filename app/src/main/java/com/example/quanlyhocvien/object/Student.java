package com.example.quanlyhocvien.object;

import android.icu.text.SimpleDateFormat;
import android.util.Log;

import com.google.firebase.Timestamp;

import java.io.Serializable;
import java.time.LocalDate;
import java.time.Period;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.Calendar;

public class Student implements Serializable {
    private String id;
    private String name;
    private transient Timestamp dateOfBirth; // transient: không tham gia vào quá trình serialization, fix lỗi nhapaj file certificate
    private String phoneNumber;
    private String email;
    private String address;
    private int gender;
    private String avatarURL;
    private ArrayList<Certificate> certificate;

    public Student() {
    }

    public Student(String id, String name, String email) {
        this.id = id;
        this.name = name;
        this.email = email;
    }

    public Student(String id, String name, Timestamp dateOfBirth, String phoneNumber, String email, String address, int gender, String avatarUrl, ArrayList<Certificate> certificate) {
        this.id = id;
        this.name = name;
        this.dateOfBirth = dateOfBirth;
        this.phoneNumber = phoneNumber;
        this.email = email;
        this.address = address;
        this.gender = gender;
        this.avatarURL = avatarUrl;
        this.certificate = certificate;
    }

    public Student(String id, String name, Timestamp dateOfBirth, String phoneNumber, String email, String address, int gender) {
        this.id = id;
        this.name = name;
        this.dateOfBirth = dateOfBirth;
        this.phoneNumber = phoneNumber;
        this.email = email;
        this.address = address;
        this.gender = gender;
        this.avatarURL = null;
        this.certificate = null;
    }

    public Student(String id, String name, String email, int gender) {
        this.id = id;
        this.name = name;
        this.email = email;
        this.gender = gender;
    }

    public Student(String id, String name, String email, int gender, Timestamp dateOfBirth) {
        this.id = id;
        this.name = name;
        this.email = email;
        this.gender = gender;
        this.dateOfBirth = dateOfBirth;
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

    public Timestamp getDateOfBirth() {
        return dateOfBirth;
    }

    public String dateOfBirthString() {
        SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy");
        return sdf.format(dateOfBirth.toDate());
    }

    public void setDateOfBirth(Timestamp dateOfBirth) {
        this.dateOfBirth = dateOfBirth;
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

    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    public int getGender() {
        return gender;
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

    public void setGender(int gender) {
        this.gender = gender;
    }

    public String getAvatarURL() {
        return avatarURL;
    }

    public void setAvatarURL(String avatarUrl) {
        this.avatarURL = avatarUrl;
    }

    public ArrayList<Certificate> getCertificate() {
        return certificate;
    }

    public void setCertificate(ArrayList<Certificate> certificate) {
        this.certificate = certificate;
    }

    public void addCertificate(Certificate certificate) {
        this.certificate.add(certificate);
    }

    public void removeCertificate(Certificate certificate) {
        this.certificate.remove(certificate);
    }

    public void updateCertificate(Certificate certificate) {
        for (int i = 0; i < this.certificate.size(); i++) {
            if (this.certificate.get(i).getId().equals(certificate.getId())) {
                this.certificate.set(i, certificate);
                break;
            }
        }
    }

    public Certificate getCertificateById(String id) {
        for (Certificate certificate : this.certificate) {
            if (certificate.getId().equals(id)) {
                return certificate;
            }
        }
        return null;
    }

    public Certificate getCertificateByName(String name) {
        for (Certificate certificate : this.certificate) {
            if (certificate.getName().equals(name)) {
                return certificate;
            }
        }
        return null;
    }


    public Certificate getCertificateByIssueDate(Timestamp issueDate) {
        for (Certificate certificate : this.certificate) {
            if (certificate.getIssueDate().equals(issueDate)) {
                return certificate;
            }
        }
        return null;
    }

    public Certificate getCertificateByExpirationDate(Timestamp expirationDate) {
        for (Certificate certificate : this.certificate) {
            if (certificate.getExpirationDate().equals(expirationDate)) {
                return certificate;
            }
        }
        return null;
    }

    public Certificate getCertificateByStatus(int status) {
        for (Certificate certificate : this.certificate) {
            if (certificate.getStatus() == (status)) {
                return certificate;
            }
        }
        return null;
    }

    @Override
    public String toString() {
        return "Student{" +
                "id='" + id + '\'' +
                ", name='" + name + '\'' +
                ", dateOfBirth=" + dateOfBirth +
                ", phoneNumber='" + phoneNumber + '\'' +
                ", email='" + email + '\'' +
                ", address='" + address + '\'' +
                ", gender ='" + gender + '\'' +
                ", avatarURL='" + avatarURL + '\'' +
                ", Total Certificate=" + certificate.size() + '}';
    }

    public int toNowAge() {
        Calendar now = Calendar.getInstance();
        Calendar dob = Calendar.getInstance();
        dob.setTime(dateOfBirth.toDate());

        int age = now.get(Calendar.YEAR) - dob.get(Calendar.YEAR);

        if (now.get(Calendar.DAY_OF_YEAR) < dob.get(Calendar.DAY_OF_YEAR)) {
            age--;
        }

        return age;
    }
}

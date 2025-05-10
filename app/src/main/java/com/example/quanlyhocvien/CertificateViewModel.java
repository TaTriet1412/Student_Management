package com.example.quanlyhocvien;

import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.example.quanlyhocvien.object.Certificate;

import java.util.ArrayList;

public class CertificateViewModel extends ViewModel {
    private final MutableLiveData<ArrayList<Certificate>> certificatesLiveData = new MutableLiveData<>(new ArrayList<>());

    // Phương thức để lấy dữ liệu
    public MutableLiveData<ArrayList<Certificate>> getCertificatesLiveData() {
        return certificatesLiveData;
    }

    // Phương thức để cập nhật dữ liệu
    public void setCertificatesLiveData(ArrayList<Certificate> certificates) {
        certificatesLiveData.setValue(certificates);
    }

    // Phương thức để thêm chứng chỉ mới
    public void addCertificate(Certificate certificate) {
        ArrayList<Certificate> certificates = certificatesLiveData.getValue();
        assert certificates != null;
        certificates.add(certificate);
        certificatesLiveData.setValue(certificates);
    }

    // Phương thức để xóa một chứng chỉ
    public void removeCertificate(Certificate certificate) {
        ArrayList<Certificate> certificates = certificatesLiveData.getValue();
        assert certificates != null;
        for (int i = 0; i < certificates.size(); i++) {
            if (certificates.get(i).getId().equals(certificate.getId())) {
                certificates.remove(i);
                break;
            }
        }
        certificatesLiveData.setValue(certificates);
    }

    // Phương thức để cập nhật thông tin của một chứng chỉ
    public void updateCertificate(Certificate certificate) {
        ArrayList<Certificate> certificates = certificatesLiveData.getValue();
        assert certificates != null;
        for (int i = 0; i < certificates.size(); i++) {
            if (certificates.get(i).getId().equals(certificate.getId())) {
                certificates.set(i, certificate);
                break;
            }
        }
        certificatesLiveData.setValue(certificates);
    }

}

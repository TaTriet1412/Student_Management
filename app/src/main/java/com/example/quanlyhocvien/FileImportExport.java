package com.example.quanlyhocvien;

import com.example.quanlyhocvien.object.Certificate;
import com.example.quanlyhocvien.object.Student;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import com.google.firebase.Timestamp;
import com.opencsv.CSVReader;
import com.opencsv.exceptions.CsvValidationException;
public class FileImportExport {

    public interface StudentFileReaderCallback {
        void onCallback(ArrayList<Student> students);
    }

    public interface CertificateFileReaderCallback {
        void onCallback(ArrayList<Certificate> certificates);
    }

    private static ExecutorService executorService = Executors.newSingleThreadExecutor();

    // Đọc file CSV dữ liệu học viên
    public static void readCSV(InputStream inputStream, StudentFileReaderCallback callback) {
        executorService.submit(() -> {
            ArrayList<Student> students = new ArrayList<>();
            try (CSVReader reader = new CSVReader(new InputStreamReader(inputStream))) {
                reader.readNext(); // bỏ dòng đầu tiên
                String[] columns;
                while ((columns = reader.readNext()) != null) {
                    if (columns.length == 6) {
                        String id = generateId();
                        String name = columns[0];
                        String dateOfBirthString = columns[1];
                        String phoneNumber = columns[2];
                        String email = columns[3];
                        String address = columns[4].replaceAll("^\"|\"$", ""); // Bỏ dấu " ở hai đầu chuỗi
                        int gender = parseGender(columns[5]);
                        Timestamp dateOfBirth = convertStringToTimestamp(dateOfBirthString);

                        Student student = new Student(id, name, dateOfBirth, phoneNumber, email, address, gender);
                        students.add(student);
                    }
                }
            } catch (IOException | CsvValidationException e) {
                e.printStackTrace();
            }
            callback.onCallback(students);
        });
    }

    // Đọc file CSV dữ liệu certificate
    public static void readCertificateCSV(InputStream inputStream, CertificateFileReaderCallback callback) {
        executorService.submit(() -> {
            ArrayList<Certificate> certificates = new ArrayList<>();
            try (BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream))) {
                reader.readLine(); // bỏ dòng đầu tiên
                String line;
                while ((line = reader.readLine()) != null) {
                    String[] columns = line.split(",");
                    if (columns.length == 6) {
                        String id = generateIdCertificate();
                        String name = columns[0];
                        String dateOfIssueString = columns[1];
                        String expirationDateString = columns[2];
                        String statusString = columns[3];
                        String description = columns[4];
                        String note = columns[5];
                        Timestamp dateOfIssue = convertStringToTimestamp(dateOfIssueString);
                        Timestamp expirationDate = convertStringToTimestamp(expirationDateString);

                        Certificate certificate = new Certificate(id, name, dateOfIssue, expirationDate, statusString, description, note);
                        certificates.add(certificate);
                    }
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
            callback.onCallback(certificates);
        });
    }

    private static int parseGender(String gender) {
        switch (gender.toLowerCase()) {
            case "nam":
                return 0;
            case "nữ":
                return 1;
            case "khác":
                return 2;
            default:
                return -1;
        }
    }

    // Chuyển đổi chuỗi thành Timestamp
    public static Timestamp convertStringToTimestamp(String dateString) {
        SimpleDateFormat dateFormat = new SimpleDateFormat("dd/MM/yyyy");
        try {
            // Chuyển đổi chuỗi thành đối tượng Date
            Date date = dateFormat.parse(dateString);
            // Tạo đối tượng Timestamp từ Date
            return new Timestamp(date);
        } catch (ParseException e) {
            e.printStackTrace();
            return null;
        }
    }

    // Tạo id
    private static String generateId() {
        //delay 1 millisecond rồi mới return
        try {
            Thread.sleep(1);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        return "HV" + System.currentTimeMillis() + (int) (Math.random() * 10);
    }

    private static String generateIdCertificate() {
        //delay 1 millisecond rồi mới return
        try {
            Thread.sleep(1);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        return "CC" + System.currentTimeMillis() + (int) (Math.random() * 10);
    }
}

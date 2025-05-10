package com.example.quanlyhocvien;

import android.content.Context;
import android.util.Log;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.lifecycle.ViewModelProvider;
import androidx.lifecycle.ViewModelStoreOwner;

import com.example.quanlyhocvien.object.Account;
import com.example.quanlyhocvien.object.Certificate;
import com.example.quanlyhocvien.object.Student;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.android.gms.tasks.TaskCompletionSource;
import com.google.firebase.Timestamp;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentChange;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

public class Firestore {
    static FirebaseFirestore db = FirebaseFirestore.getInstance(); // Khởi tạo Firestore

    // Interface để truyền kết quả trả về từ Firestore (true: thành công, false: thất bại)
    public interface FirestoreAddCallback {
        void onCallback(boolean isSuccess);
    }

    // Thêm account
    public static void addAccount(Account account, FirestoreAddCallback callback) {
        Map<String, Object> data = new HashMap<>();
        data.put("displayName", account.getDisplayName());
        data.put("email", account.getEmail());
        data.put("age", account.getAge());
        data.put("phoneNumber", account.getPhoneNumber());
        data.put("activeStatus", account.getActiveStatus());
        data.put("role", account.getRole());
        data.put("gender", account.getGender());
        data.put("photoUrl", account.getPhotoUrl());

        db.collection("account").document(account.getUID()).set(data)
                .addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void aVoid) {
                        Log.d("Firestore", "Account added");
                        callback.onCallback(true);
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Log.e("Firestore", "Error adding account", e);
                        callback.onCallback(false);
                    }
                });
    }

    // Thêm học viên
    public static void addStudent(Student student, FirestoreAddCallback callback) {
        db.collection("student").document(student.getId()).set(student)
                .addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void aVoid) {
                        Log.d("Firestore", "Student added");
                        callback.onCallback(true);
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Log.e("Firestore", "Error adding student", e);
                        callback.onCallback(false);
                    }
                });
    }

    // Lấy thông tin học viên
    public interface FirestoreGetStudentCallback {
        void onCallback(Student student);
    }
    public static void getStudent(String studentID, FirestoreGetStudentCallback callback) {
        // input: studentID
        // output: student, null nếu không tìm thấy

        db.collection("student").document(studentID).get()
                .addOnSuccessListener(documentSnapshot -> {
                    if (documentSnapshot.exists()) {
                        Log.d("Firestore", "Student found");
                        Student student = documentSnapshot.toObject(Student.class);
                        callback.onCallback(student);
                    } else {
                        Log.d("Firestore", "No such student");
                        callback.onCallback(null);
                    }
                })
                .addOnFailureListener(e -> {
                    Log.e("Firestore", "Error getting student", e);
                    callback.onCallback(null);
                });
    }

    // Update photo URL cho học viên
    public static void updateStudentPhotoURL(String studentID, String avatarURL, FirestoreAddCallback callback) {
        Map<String, Object> data = new HashMap<>();
        data.put("avatarURL", avatarURL);
        db.collection("student").document(studentID).update(data)
                .addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void aVoid) {
                        Log.d("Firestore", "Student photo URL updated");
                        callback.onCallback(true);
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Log.e("Firestore", "Error updating student photo URL", e);
                        callback.onCallback(false);
                    }
                });
    }

    // Update photoURL cho account
    public static void updateAccountPhotoURL(String UID, String avatarURL, FirestoreAddCallback callback) {
        Map<String, Object> data = new HashMap<>();
        data.put("photoUrl", avatarURL);
        db.collection("account").document(UID).update(data)
                .addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void aVoid) {
                        Log.d("Firestore", "Account photo URL updated");
                        callback.onCallback(true);
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Log.e("Firestore", "Error updating account photo URL", e);
                        callback.onCallback(false);
                    }
                });
    }

    // Lấy thông tin account
    public interface FirestoreGetAccountCallback {
        void onCallback(Account account);
    }
    public static void getAccount(String UID, FirestoreGetAccountCallback callback) {
        // input: UID
        // output: account, null nếu không tìm thấy

        db.collection("account").document(UID).get()
                .addOnSuccessListener(documentSnapshot -> {
                    if (documentSnapshot.exists()) {
                        Log.d("Firestore", "Account found");
                        Account account = documentSnapshot.toObject(Account.class);
                        assert account != null;
                        account.setUID(documentSnapshot.getId());
                        callback.onCallback(account);
                    } else {
                        Log.d("Firestore", "No such account");
                        callback.onCallback(null);
                    }
                })
                .addOnFailureListener(e -> {
                    Log.e("Firestore", "Error getting account", e);
                    callback.onCallback(null);
                });
    }

    // Xóa học viên
    public static void deleteStudent(String studentID, FirestoreAddCallback callback) {
        db.collection("student").document(studentID).delete()
                .addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void aVoid) {
                        Log.d("Firestore", "Student deleted");
                        callback.onCallback(true);
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Log.e("Firestore", "Error deleting student", e);
                        callback.onCallback(false);
                    }
                });
    }

    // Xóa account
    public static void deleteAccount(String UID, FirestoreAddCallback callback) {
        db.collection("account").document(UID).delete()
                .addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void aVoid) {
                        Log.d("Firestore", "Account deleted");
                        callback.onCallback(true);
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Log.e("Firestore", "Error deleting account", e);
                        callback.onCallback(false);
                    }
                });
    }

    // update account activeStatus
    public static void updateAccountActiveStatus(String UID, boolean activeStatus, FirestoreAddCallback callback) {
        Map<String, Object> data = new HashMap<>();
        data.put("activeStatus", activeStatus);
        db.collection("account").document(UID).update(data)
                .addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void aVoid) {
                        Log.d("Firestore", "Account activeStatus updated");
                        callback.onCallback(true);
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Log.e("Firestore", "Error updating account activeStatus", e);
                        callback.onCallback(false);
                    }
                });
    }

    // Khởi tạo listener để lắng nghe sự thêm, xóa, sửa của collection "student", ở các trường name, email
    public static void initStudentSimpleDataListener(ViewModelStoreOwner owner) {
        StudentViewModel studentViewModel = new ViewModelProvider(owner).get(StudentViewModel.class);

        CollectionReference studentRef = db.collection("student");
        studentRef.addSnapshotListener((value, error) -> {
            if (error != null) {
                Log.e("Firestore", "Error listening to student collection", error);
                return;
            }

            if (value != null) {
                for (DocumentChange documentChange : value.getDocumentChanges()) {
                    Log.d("Firestore", "Student change detected");
                    // Lấy loại thay đổi (add, remove, modify)
                    DocumentChange.Type type = documentChange.getType();
                    DocumentSnapshot document = documentChange.getDocument();

                    // Lấy thông tin học viên
                    String id = document.getId();
                    String name = document.getString("name");
                    String email = document.getString("email");
                    int gender = Objects.requireNonNull(document.getLong("gender")).intValue();
                    Timestamp dateOfBirth = document.getTimestamp("dateOfBirth");
                    Student student = new Student(id, name, email, gender, dateOfBirth);

                    if (type == DocumentChange.Type.ADDED) {
                        // Sự kiên
                        Log.d("Firestore", "Student added");
                        studentViewModel.addStudent(student);
                    } else if (type == DocumentChange.Type.REMOVED) {
                        // Sự kiện xóa học viên
                        Log.d("Firestore", "Student removed");
                        studentViewModel.removeStudent(student);
                    } else if (type == DocumentChange.Type.MODIFIED) {
                        // Sự kiện sửa học viên
                        Log.d("Firestore", "Student modified");
                        studentViewModel.updateStudent(student);
                    }
                }
            }
        });
    }

    // Hủy lắng nghe sự thêm, xóa, sửa của collection "student", ở các trường name, email
    public static void removeStudentSimpleDataListener() {
        db.collection("student").addSnapshotListener((value, error) -> {
            // Do nothing
        }).remove();
    }

    // Khởi tạo listener để lắng nghe sự thêm, xóa, sửa của collection "account", ở các trường displayName, email, role
    public static void initAccountSimpleDataListener(ViewModelStoreOwner owner) {
        AccountViewModel accountViewModel = new ViewModelProvider(owner).get(AccountViewModel.class);

        CollectionReference accountRef = db.collection("account");
        accountRef.addSnapshotListener((value, error) -> {
            if (error != null) {
                Log.e("Firestore", "Error listening to account collection", error);
                return;
            }

            if (value != null) {
                for (DocumentChange documentChange : value.getDocumentChanges()) {
                    Log.d("Firestore", "Account change detected");
                    // Lấy loại thay đổi (add, remove, modify)
                    DocumentChange.Type type = documentChange.getType();
                    DocumentSnapshot document = documentChange.getDocument();

                    // Lấy thông tin account
                    String UID = document.getId();
                    String displayName = document.getString("displayName");
                    String email = document.getString("email");
                    int role = Objects.requireNonNull(document.getLong("role")).intValue();
                    Account account = new Account(UID, displayName, email, role);

                    if (type == DocumentChange.Type.ADDED) {
                        // Sự kiện thêm account
                        Log.d("Firestore", "Account added");
                        accountViewModel.addAccount(account);
                    } else if (type == DocumentChange.Type.REMOVED) {
                        // Sự kiện xóa account
                        Log.d("Firestore", "Account removed");
                        accountViewModel.removeAccount(account);
                    } else if (type == DocumentChange.Type.MODIFIED) {
                        // Sự kiện sửa account
                        Log.d("Firestore", "Account modified");
                        accountViewModel.updateAccount(account);
                    }
                }
            }
        });
    }

    // Hủy lắng nghe sự thêm, xóa, sửa của collection "account", ở các trường displayName, email, role
    public static void removeAccountSimpleDataListener() {
        db.collection("account").addSnapshotListener((value, error) -> {
            // Do nothing
        }).remove();
    }


    // Lấy thông tin tất cả học viên,ở các trường name, email, gender trả về dưới dạng ArrayList<Student>
    public interface FirestoreGetAllStudentSimpleDataCallback {
        void onCallback(ArrayList<Student> students);
    }
    public static void getAllStudentSimpleData(FirestoreGetAllStudentSimpleDataCallback callback) {
        db.collection("student").get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    ArrayList<Student> students = new ArrayList<>();
                    for (DocumentSnapshot document : queryDocumentSnapshots.getDocuments()) {
                        String id = document.getId();
                        String name = document.getString("name");
                        String email = document.getString("email");
                        int gender = Objects.requireNonNull(document.getLong("gender")).intValue();
                        Timestamp dateOfBirth = document.getTimestamp("dateOfBirth");
                        Student student = new Student(id, name, email, gender, dateOfBirth);
                        students.add(student);
                    }
                    callback.onCallback(students);
                })
                .addOnFailureListener(e -> {
                    Log.e("Firestore", "Error getting all students", e);
                    callback.onCallback(null);
                });
    }

    // Lấy thông tin tất cả account,ở các trường displayName, email, role trả về dưới dạng ArrayList<Account>
    public interface FirestoreGetAllAccountSimpleDataCallback {
        void onCallback(ArrayList<Account> accounts);
    }
    public static void getAllAccountSimpleData(FirestoreGetAllAccountSimpleDataCallback callback) {
        db.collection("account").get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    ArrayList<Account> accounts = new ArrayList<>();
                    for (DocumentSnapshot document : queryDocumentSnapshots.getDocuments()) {
                        String UID = document.getId();
                        String displayName = document.getString("displayName");
                        String email = document.getString("email");
                        int role = Objects.requireNonNull(document.getLong("role")).intValue();
                        Account account = new Account(UID, displayName, email, role);
                        accounts.add(account);
                    }
                    callback.onCallback(accounts);
                })
                .addOnFailureListener(e -> {
                    Log.e("Firestore", "Error getting all accounts", e);
                    callback.onCallback(null);
                });
    }

    // Thêm Certificate
    public static void addCertificate(String studentID, Certificate certificate, FirestoreAddCallback callback) {
        db.collection("student").document(studentID).collection("certificate").document(certificate.getId()).set(certificate)
                .addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void aVoid) {
                        Log.d("Firestore", "Certificate added");
                        callback.onCallback(true);
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Log.e("Firestore", "Error adding certificate", e);
                        callback.onCallback(false);
                    }
                });
    }

    // Cập nhật ảnh cho Certificate
    public static void updateCertificatePhotoURL(String studentID, Certificate certificate, FirestoreAddCallback callback) {
        Map<String, Object> data = new HashMap<>();
        data.put("imageURL", certificate.getImageURL());
        db.collection("student").document(studentID).collection("certificate").document(certificate.getId()).update(data)
                .addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void aVoid) {
                        Log.d("Firestore", "Certificate photo URL updated");
                        callback.onCallback(true);
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Log.e("Firestore", "Error updating certificate photo URL", e);
                        callback.onCallback(false);
                    }
                });
    }

    // Lấy thông tin Certificate
    public interface FirestoreGetCertificateCallback {
        void onCallback(Certificate certificate);
    }
    public static void getCertificate(String studentID, String certificateID, FirestoreGetCertificateCallback callback) {
        db.collection("student").document(studentID).collection("certificate").document(certificateID).get()
                .addOnSuccessListener(documentSnapshot -> {
                    if (documentSnapshot.exists()) {
                        Log.d("Firestore", "Certificate found");
                        Certificate certificate = documentSnapshot.toObject(Certificate.class);
                        callback.onCallback(certificate);
                    } else {
                        Log.d("Firestore", "No such certificate");
                        callback.onCallback(null);
                    }
                })
                .addOnFailureListener(e -> {
                    Log.e("Firestore", "Error getting certificate", e);
                    callback.onCallback(null);
                });
    }

    // Xóa Certificate
    public static void deleteCertificate(String studentID, String certificateID, FirestoreAddCallback callback) {
        db.collection("student").document(studentID).collection("certificate").document(certificateID).delete()
                .addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void aVoid) {
                        Log.d("Firestore", "Certificate deleted");
                        callback.onCallback(true);
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Log.e("Firestore", "Error deleting certificate", e);
                        callback.onCallback(false);
                    }
                });
    }

    // Lấy thông tin tất cả Certificate của học viên, trả về dưới dạng ArrayList<Certificate>, chỉ lấy id, name, status
    public interface FirestoreGetAllCertificateSimpleDataCallback {
        void onCallback(ArrayList<Certificate> certificates);
    }
    public static void getAllCertificateSimpleData(String studentID, FirestoreGetAllCertificateSimpleDataCallback callback) {
        db.collection("student").document(studentID).collection("certificate").get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    ArrayList<Certificate> certificates = new ArrayList<>();
                    for (DocumentSnapshot document : queryDocumentSnapshots.getDocuments()) {
                        String id = document.getId();
                        String name = document.getString("name");
                        int status = Objects.requireNonNull(document.getLong("status")).intValue();
                        Certificate certificate = new Certificate(id, name, status);
                        certificates.add(certificate);
                    }
                    callback.onCallback(certificates);
                })
                .addOnFailureListener(e -> {
                    Log.e("Firestore", "Error getting all certificates", e);
                    callback.onCallback(null);
                });
    }

    // Đăng ký trình lắng nghe sự thêm, xóa, sửa của collection "certificate" của một student cụ thể, ở các trường name, status
    public static void initCertificateSimpleDataListener(String studentID, ViewModelStoreOwner owner) {
        CertificateViewModel certificateViewModel = new ViewModelProvider(owner).get(CertificateViewModel.class);

        CollectionReference certificateRef = db.collection("student").document(studentID).collection("certificate");
        certificateRef.addSnapshotListener((value, error) -> {
            if (error != null) {
                Log.e("Firestore", "Error listening to certificate collection", error);
                return;
            }

            if (value != null) {
                for (DocumentChange documentChange : value.getDocumentChanges()) {
                    Log.d("Firestore", "Certificate change detected");
                    // Lấy loại thay đổi (add, remove, modify)
                    DocumentChange.Type type = documentChange.getType();
                    DocumentSnapshot document = documentChange.getDocument();

                    // Lấy thông tin Certificate
                    String id = document.getId();
                    String name = document.getString("name");
                    int status = Objects.requireNonNull(document.getLong("status")).intValue();
                    Certificate certificate = new Certificate(id, name, status);

                    if (type == DocumentChange.Type.ADDED) {
                        // Sự kiện thêm Certificate
                        Log.d("Firestore", "Certificate added");
                        certificateViewModel.addCertificate(certificate);
                    } else if (type == DocumentChange.Type.REMOVED) {
                        // Sự kiện xóa Certificate
                        Log.d("Firestore", "Certificate removed");
                        certificateViewModel.removeCertificate(certificate);
                    } else if (type == DocumentChange.Type.MODIFIED) {
                        // Sự kiện sửa Certificate
                        Log.d("Firestore", "Certificate modified");
                        certificateViewModel.updateCertificate(certificate);
                    }
                }
            }
        });
    }

    // Hủy lắng nghe sự thêm, xóa, sửa của collection "certificate" của một student cụ thể, ở các trường name, status
    public static void removeCertificateSimpleDataListener(String studentID) {
        db.collection("student").document(studentID).collection("certificate").addSnapshotListener((value, error) -> {
            // Do nothing
        }).remove();
    }

    // Thêm Log của một account cụ thể
    public static void addLog(String UID, com.example.quanlyhocvien.object.Log log, FirestoreAddCallback callback) {
        db.collection("account").document(UID).collection("log").add(log)
                .addOnSuccessListener(documentReference -> {
                    Log.d("Firestore", "Log added");
                    callback.onCallback(true);
                })
                .addOnFailureListener(e -> {
                    Log.e("Firestore", "Error adding log", e);
                    callback.onCallback(false);
                });
    }

    // Lấy thông tin tất cả Log của một account cụ thể, trả về dưới dạng ArrayList<Log>
    public interface FirestoreGetAllLogCallback {
        void onCallback(ArrayList<com.example.quanlyhocvien.object.Log> logs);
    }
    public static void getAllLog(String UID, FirestoreGetAllLogCallback callback) {
        db.collection("account").document(UID).collection("log").get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    ArrayList<com.example.quanlyhocvien.object.Log> logs = new ArrayList<>();
                    for (DocumentSnapshot document : queryDocumentSnapshots.getDocuments()) {
                        com.example.quanlyhocvien.object.Log log = document.toObject(com.example.quanlyhocvien.object.Log.class);
                        logs.add(log);
                    }
                    callback.onCallback(logs);
                })
                .addOnFailureListener(e -> {
                    Log.e("Firestore", "Error getting all logs", e);
                    callback.onCallback(null);
                });
    }

    // Đăng ký trình lắng nghe sự thêm, xóa, sửa của collection "log" của một account cụ thể
    public static void initLogListener(String UID, ViewModelStoreOwner owner) {
        LogViewModel logViewModel = new ViewModelProvider(owner).get(LogViewModel.class);

        CollectionReference logRef = db.collection("account").document(UID).collection("log");
        logRef.addSnapshotListener((value, error) -> {
            if (error != null) {
                Log.e("Firestore", "Error listening to log collection", error);
                return;
            }

            if (value != null) {
                for (DocumentChange documentChange : value.getDocumentChanges()) {
                    Log.d("Firestore", "Log change detected");
                    // Lấy loại thay đổi (add, remove, modify)
                    DocumentChange.Type type = documentChange.getType();
                    DocumentSnapshot document = documentChange.getDocument();

                    // Lấy thông tin Log
                    com.example.quanlyhocvien.object.Log log = document.toObject(com.example.quanlyhocvien.object.Log.class);

                    if (type == DocumentChange.Type.ADDED) {
                        // Sự kiện thêm Log
                        Log.d("Firestore", "Log added");
                        logViewModel.addLog(log);
                    } else if (type == DocumentChange.Type.REMOVED) {
                        // Sự kiện xóa Log
                        Log.d("Firestore", "Log removed");
                        logViewModel.removeLog(log);
                    } else if (type == DocumentChange.Type.MODIFIED) {
                        // Sự kiện sửa Log
                        Log.d("Firestore", "Log modified");
                        logViewModel.updateLog(log);
                    }
                }
            }
        });
    }

    // Hủy lắng nghe sự thêm, xóa, sửa của collection "log" của một account cụ thể
    public static void removeLogListener(String UID) {
        db.collection("account").document(UID).collection("log").addSnapshotListener((value, error) -> {
            // Do nothing
        }).remove();
    }

    // Thêm nhiều học viên bằng write batch
    public static void addManyStudent(ArrayList<Student> students, FirestoreAddCallback callback) {
        TaskCompletionSource<Boolean> taskCompletionSource = new TaskCompletionSource<>();
        Task<Boolean> task = taskCompletionSource.getTask();

        db.runTransaction(transaction -> {
            for (Student student : students) {
                transaction.set(db.collection("student").document(student.getId()), student);
            }
            return null;
        }).addOnSuccessListener(aVoid -> {
            Log.d("Firestore", "Many students added");
            taskCompletionSource.setResult(true);
        }).addOnFailureListener(e -> {
            Log.e("Firestore", "Error adding many students", e);
            taskCompletionSource.setResult(false);
        });

        task.addOnSuccessListener(callback::onCallback);
        task.addOnFailureListener(e -> callback.onCallback(false));
    }

    // Thêm nhiều certificate bằng write batch
    public static void addManyCertificate(String studentID, ArrayList<Certificate> certificates, FirestoreAddCallback callback) {
        TaskCompletionSource<Boolean> taskCompletionSource = new TaskCompletionSource<>();
        Task<Boolean> task = taskCompletionSource.getTask();

        db.runTransaction(transaction -> {
            for (Certificate certificate : certificates) {
                transaction.set(db.collection("student").document(studentID).collection("certificate").document(certificate.getId()), certificate);
            }
            return null;
        }).addOnSuccessListener(aVoid -> {
            Log.d("Firestore", "Many certificates added");
            taskCompletionSource.setResult(true);
        }).addOnFailureListener(e -> {
            Log.e("Firestore", "Error adding many certificates", e);
            taskCompletionSource.setResult(false);
        });

        task.addOnSuccessListener(callback::onCallback);
        task.addOnFailureListener(e -> callback.onCallback(false));
    }
}

package com.example.quanlyhocvien;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import com.example.quanlyhocvien.object.Account;
import com.google.android.gms.tasks.Task;
import com.google.firebase.FirebaseApp;

import org.junit.Test;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

public class FirestoreInstrumentedTest {

    // Test khởi tạo FirebaseApp
    @Test
    public void testFirebaseInitialization() {
        FirebaseApp firebaseApp = FirebaseApp.getInstance();
        assertNotNull(firebaseApp);
    }

    // Test thêm account
    @Test
    public void testAddAccount() throws InterruptedException {
        // Random UID để test
        String UID = "AutoTest" + System.currentTimeMillis();
        // Tạo account mới
        Account account = new Account(UID, 20, true, 1, 1, null, "displayName", "email", "phoneNumber", "photoUrl");

        CountDownLatch latch = new CountDownLatch(1);
        // Thêm account vào Firestore
        Firestore.addAccount(account, new Firestore.FirestoreAddCallback() {
            @Override
            public void onCallback(boolean isSuccess) {
                assertTrue(isSuccess);
                latch.countDown();
            }
        });
        latch.await(10, TimeUnit.SECONDS);
    }

//    // Test updateStudentAvatarURL
//    @Test
//    public void testUpdateStudentAvatarURL() throws InterruptedException {
//        // Random UID để test
//        String UID ="HV17306906602813";
//        String avatarName = "ava1.jpg";
//
//        CountDownLatch latch = new CountDownLatch(1);
//        Firestore.updateStudentPhotoURL(UID, avatarName, new Firestore.FirestoreAddCallback() {
//            @Override
//            public void onCallback(boolean isSuccess) {
//                assertTrue(isSuccess);
//                latch.countDown();
//            }
//        });
//        latch.await(10, TimeUnit.SECONDS);
//    }
}

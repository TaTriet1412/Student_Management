package com.example.quanlyhocvien;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import androidx.test.ext.junit.runners.AndroidJUnit4;

import com.google.android.gms.tasks.Task;
import com.google.firebase.FirebaseApp;

import org.junit.Test;
import org.junit.runner.RunWith;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

@RunWith(AndroidJUnit4.class)
public class FirebaseInstrumentedTest {
    int testSignUpSuccess_waitTime = 5; // Thời gian chờ cho testSignUpSuccess
    int testSignUpFail_waitTime = 5;
    int testSignInSuccess_waitTime = 5;
    int testSignInFail_waitTime = 5;
    int testSendResetPasswordEmailSuccess_waitTime = 5;
    int testSendResetPasswordEmailFail_waitTime = 5;

    // Test khởi tạo FirebaseApp
    @Test
    public void testFirebaseInitialization() {
        FirebaseApp firebaseApp = FirebaseApp.getInstance();
        assertNotNull(firebaseApp);
    }

    @Test // Test đăng ký tài khoản thành công
    public void testSignUpSuccess() throws InterruptedException {
        // Tạo email duy nhất cho mỗi lần kiểm thử
        String email = "test" + System.currentTimeMillis() + "@example.com";
        String password = "123456";

        // Sử dụng CountDownLatch để đợi task hoàn thành
        CountDownLatch latch = new CountDownLatch(1);

        // Gọi phương thức signUp
        Task<Boolean> resultTask = FirebaseAuth.signUp(email, password);

        // Đợi cho đến khi task hoàn thành
        resultTask.addOnCompleteListener(task -> {
            assertTrue(task.isSuccessful() && task.getResult());
            latch.countDown(); // Giảm số đếm c��a latch khi task hoàn thành
        });

        // Đợi tối đa 10 giây cho task hoàn thành
        latch.await(testSignUpSuccess_waitTime , TimeUnit.SECONDS);
    }

    @Test // Test đăng ký tài khoản thất bại với email đã tồn tại
    public void testSignUpFail() throws InterruptedException {
        String email = "dangvantrong@gmail.com";
        String password = "123456";

        CountDownLatch latch = new CountDownLatch(1);
        Task<Boolean> resultTask = FirebaseAuth.signUp(email, password);
        resultTask.addOnCompleteListener(task -> {
            assertFalse(task.isSuccessful() && task.getResult());
            latch.countDown();
        });
        latch.await(testSignUpFail_waitTime, TimeUnit.SECONDS);
    }

    @Test // Test đăng nhập tài khoản thành công
    public void testSignInSuccess() throws InterruptedException {
        String email = "dangvantrong@gmail.com";
        String password = "123456";

        CountDownLatch latch = new CountDownLatch(1);
        Task<Boolean> resultTask = FirebaseAuth.signIn(email, password);
        resultTask.addOnCompleteListener(task -> {
            assertTrue(task.isSuccessful() && task.getResult());
            latch.countDown();
        });
        latch.await(testSignInSuccess_waitTime, TimeUnit.SECONDS);
    }

    @Test // Test đăng nhập tài khoản thất bại với mật khẩu không đúng
    public void testSignInFail() throws InterruptedException {
        String email = "dangvantrong@gmail.com";
        String password = "1234567";

        CountDownLatch latch = new CountDownLatch(1);
        Task<Boolean> resultTask = FirebaseAuth.signIn(email, password);
        resultTask.addOnCompleteListener(task -> {
            assertFalse(task.isSuccessful() && task.getResult());
            latch.countDown();
        });
        latch.await(testSignInFail_waitTime, TimeUnit.SECONDS);
    }

    @Test // Test đăng nhập tài khoản thất bại với email không tồn tại
    public void testSignInFail2() throws InterruptedException {
        String email = "noemail@example.com";
        String password = "123456";

        CountDownLatch latch = new CountDownLatch(1);
        Task<Boolean> resultTask = FirebaseAuth.signIn(email, password);
        resultTask.addOnCompleteListener(task -> {
            assertFalse(task.isSuccessful() && task.getResult());
            latch.countDown();
        });
        latch.await(testSignInFail_waitTime, TimeUnit.SECONDS);
    }

    @Test // Test gửi email đặt lại mật khẩu thành công
    public void testSendResetPasswordEmailSuccess() throws InterruptedException {
        String email = "innovatechtdtu@gmail.com";

        CountDownLatch latch = new CountDownLatch(1);
        Task<Boolean> resultTask = FirebaseAuth.sendResetPasswordEmail(email);
        resultTask.addOnCompleteListener(task -> {
            assertTrue(task.isSuccessful() && task.getResult());
            latch.countDown();
        });
        latch.await(testSendResetPasswordEmailSuccess_waitTime, TimeUnit.SECONDS);
    }

    @Test // Test gửi email đặt lại mật khẩu thất bại, email không tồn tại
    public void testSendResetPasswordEmailFail() throws InterruptedException {
        String email = "noemail@example.com";

        CountDownLatch latch = new CountDownLatch(1);
        Task<Boolean> resultTask = FirebaseAuth.sendResetPasswordEmail(email);
        resultTask.addOnCompleteListener(task -> {
            assertFalse(task.isSuccessful() && task.getResult());
            latch.countDown();
        });
        latch.await(testSendResetPasswordEmailFail_waitTime, TimeUnit.SECONDS);
    }
}

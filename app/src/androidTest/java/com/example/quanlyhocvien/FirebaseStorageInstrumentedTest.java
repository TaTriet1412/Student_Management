package com.example.quanlyhocvien;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import android.content.Context;
import android.net.Uri;

import androidx.test.core.app.ApplicationProvider;
import androidx.test.ext.junit.runners.AndroidJUnit4;
import androidx.test.platform.app.InstrumentationRegistry;

import com.google.android.gms.tasks.Task;
import com.google.firebase.FirebaseApp;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

@RunWith(AndroidJUnit4.class)
public class FirebaseStorageInstrumentedTest {
    private Uri fileUri;
    private String folderPath;
    int testUploadFileSuccess_waitTime = 5; // Thời gian chờ cho testUploadFileSuccess
    int testCheckFile_waitTime = 5;
    int testDeleteFile_waitTime = 5;
    int testDownload_waitTime = 5;
    String fileName;
    String fileName_testDownload = "testDownload.jpg";
    String folderUri_testDownload;

    @Before
    public void setUp() {
        Context context = ApplicationProvider.getApplicationContext();
        fileUri = Uri.parse("android.resource://" + context.getPackageName() + "/" + R.drawable.img_for_test);
        folderPath = "testFolder/";
        fileName = "testFile" + System.currentTimeMillis();
        folderUri_testDownload = InstrumentationRegistry.getInstrumentation().getTargetContext().getFilesDir() + "thay_file_nay_thi_xoa_di.jpg"; // bộ nhớ trong của thiết bị
    }

    // Test khởi tạo FirebaseApp
    @Test
    public void testFirebaseInitialization() {
        FirebaseApp firebaseApp = FirebaseApp.getInstance();
        assertNotNull(firebaseApp);
    }

    @Test // Test upload file thành công
    public void testUploadFileSuccess() throws InterruptedException {
        CountDownLatch latch = new CountDownLatch(1);

        Task<Boolean> resultTask = FirebaseStorageManager.uploadFile(fileUri, fileName, folderPath, testUploadFileSuccess_waitTime - 1);
        resultTask.addOnCompleteListener(task -> {
            assertTrue(task.getResult());
            latch.countDown(); // Giảm số đếm của latch khi task hoàn thành
        });
        latch.await(testUploadFileSuccess_waitTime, TimeUnit.SECONDS);
    }

    @Test // Test upLoadByteArray thành công
    public void testUploadByteArraySuccess() throws InterruptedException {

        CountDownLatch latch = new CountDownLatch(1);

        Task<Boolean> resultTask = FirebaseStorageManager.upLoadByteArray(new byte[1], fileName, folderPath, testUploadFileSuccess_waitTime - 1);
        resultTask.addOnCompleteListener(task -> {
            assertTrue(task.getResult());
            latch.countDown(); // Giảm số đếm của latch khi task hoàn thành
        });
        latch.await(testUploadFileSuccess_waitTime, TimeUnit.SECONDS);
    }

    @Test // Test upload file thất bại vì file không tồn tại
    public void testUploadFileFail() throws InterruptedException {
        CountDownLatch latch = new CountDownLatch(1);

        Task<Boolean> resultTask = FirebaseStorageManager.uploadFile(Uri.parse("file:///nonexistent"), fileName, folderPath, testUploadFileSuccess_waitTime - 1);
        resultTask.addOnCompleteListener(task -> {
            assertFalse(task.getResult());
            latch.countDown(); // Giảm số đếm của latch khi task hoàn thành
        });
        latch.await(testUploadFileSuccess_waitTime, TimeUnit.SECONDS);
    }

    @Test // Test checkFileExist trả về true khi file tồn tại
    public void testCheckFileExistTrue() throws InterruptedException {
        CountDownLatch latch = new CountDownLatch(1);

        Task<Boolean> resultTask = FirebaseStorageManager.checkFileExist("testDownload.jpg", folderPath, testCheckFile_waitTime - 1);
        resultTask.addOnCompleteListener(task -> {
            assertTrue(task.getResult());
            latch.countDown(); // Giảm số đếm của latch khi task hoàn thành
        });
        latch.await(testCheckFile_waitTime, TimeUnit.SECONDS);
    }

    @Test // Test checkFileExist trả về false khi file không tồn tại
    public void testCheckFileExistFalse() throws InterruptedException {
        CountDownLatch latch = new CountDownLatch(1);

        Task<Boolean> resultTask = FirebaseStorageManager.checkFileExist("nonexistent", folderPath, testCheckFile_waitTime - 1);
        resultTask.addOnCompleteListener(task -> {
            assertFalse(task.getResult());
            latch.countDown(); // Giảm số đếm của latch khi task hoàn thành
        });
        latch.await(testCheckFile_waitTime, TimeUnit.SECONDS);
    }

    @Test // Test deleteFile thành công (trước khi test cần upload file lên Firebase Storage)
    public void testDeleteFileSuccess() throws InterruptedException {
        CountDownLatch latch = new CountDownLatch(1);
        String newFileName = "avata1.jpg";

        Task<Boolean> resultTask = FirebaseStorageManager.deleteFile(newFileName, folderPath, testDeleteFile_waitTime -1);
        resultTask.addOnCompleteListener(task -> {
            assertTrue(task.getResult());
            latch.countDown(); // Giảm số đếm của latch khi task hoàn thành
        });
        latch.await(testDeleteFile_waitTime, TimeUnit.SECONDS);
    }

    @Test // Test deleteFile thất bại vì file không tồn tại
    public void testDeleteFileFail() throws InterruptedException {
        CountDownLatch latch = new CountDownLatch(1);

        Task<Boolean> resultTask = FirebaseStorageManager.deleteFile("nonexistent", folderPath, testDeleteFile_waitTime - 1);
        resultTask.addOnCompleteListener(task -> {
            assertFalse(task.getResult());
            latch.countDown(); // Giảm số đếm của latch khi task hoàn thành
        });
        latch.await(testDeleteFile_waitTime, TimeUnit.SECONDS);
    }

    @Test // Test downloadFile thành công
    public void testDownloadFileSuccess() throws InterruptedException {
        CountDownLatch latch = new CountDownLatch(1);

        FirebaseStorageManager.downloadFile(fileName_testDownload, folderPath, testDownload_waitTime - 1, DownloadFileCallback -> {
            assertNotNull(DownloadFileCallback);
            latch.countDown(); // Giảm số đếm của latch khi task hoàn thành
        });
        latch.await(testCheckFile_waitTime, TimeUnit.SECONDS);
    }

    // Test downloadFile thất bại vì file không tồn tại
    @Test
    public void testDownloadFileFail() throws InterruptedException {
        CountDownLatch latch = new CountDownLatch(1);

        FirebaseStorageManager.downloadFile("nonexistent", folderPath, testDownload_waitTime - 1, DownloadFileCallback -> {
            assertNull(DownloadFileCallback);
            latch.countDown(); // Giảm số đếm của latch khi task hoàn thành
        });
        latch.await(testDownload_waitTime, TimeUnit.SECONDS);
    }

    // Test downloadFileToPhysicalMemory thành công
    @Test
    public void testDownloadFileToPhysicalMemorySuccess() throws InterruptedException {
        CountDownLatch latch = new CountDownLatch(1);

        Task<Boolean> resultTask = FirebaseStorageManager.downloadFileToPhysicalMemory(fileName_testDownload, folderPath, folderUri_testDownload , testDownload_waitTime- 1);
        resultTask.addOnCompleteListener(task -> {
            assertTrue(task.getResult());
            latch.countDown(); // Giảm số đếm của latch khi task hoàn thành
        });
        latch.await(testDownload_waitTime, TimeUnit.SECONDS);
    }

    // Test downloadFileToPhysicalMemory thất bại vì file không tồn tại ở Firebase Storage
    @Test
    public void testDownloadFileToPhysicalMemoryFail() throws InterruptedException {
        CountDownLatch latch = new CountDownLatch(1);

        Task<Boolean> resultTask = FirebaseStorageManager.downloadFileToPhysicalMemory("nonexistent", folderPath, folderUri_testDownload, testDownload_waitTime - 1);
        resultTask.addOnCompleteListener(task -> {
            assertFalse(task.getResult());
            latch.countDown(); // Giảm số đếm của latch khi task hoàn thành
        });
        latch.await(testDownload_waitTime, TimeUnit.SECONDS);
    }
}

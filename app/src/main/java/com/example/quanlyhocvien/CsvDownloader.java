package com.example.quanlyhocvien;

import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.content.Context;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Build;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.core.app.ActivityCompat;
import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;

import java.io.DataOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

public class CsvDownloader {
    private static final String CHANNEL_ID = "CSV_DOWNLOAD_CHANNEL";
    private static final String TAG = "CsvDownloader";
    private static final String BASE_API_URL = "https://android-midterm-essay-api.onrender.com/";
    private static final String API_KEY = "05badc8fe5c64c0d96c2ec54d970d2dc";
    private final Context context;
    private String apiUrl;
    private Uri destinationUri;
    private NotificationCompat.Builder notificationBuilder;
    private NotificationManagerCompat notificationManager;

    public CsvDownloader(Context context, String studentID, Uri destinationUri) {
        this.context = context;
        this.destinationUri = destinationUri;
        if (studentID != null) {
            this.apiUrl = BASE_API_URL + "export_certificates_csv/" + studentID;
        } else {
            this.apiUrl = BASE_API_URL + "export_students_csv";
        }
        createNotificationChannel(); // Tạo kênh thông báo
        notificationManager = NotificationManagerCompat.from(context);
    }

    public void downloadCsvFile() {
        OkHttpClient client = new OkHttpClient();

        // Tạo request với header API key
        Request request = new Request.Builder()
                .url(apiUrl)
                .addHeader("x-api-key", API_KEY)
                .build();

        // Hiển thị thông báo khi bắt đầu tải xuống
        showNotification("Đang tải xuống", "Đang tải file CSV...");

        client.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(@NonNull Call call, @NonNull IOException e) {
                Log.e(TAG, "API Request Failed: " + e.getMessage());
                updateNotification("Tải xuống thất bại", "Không thể tải xuống file CSV");
            }

            @Override
            public void onResponse(@NonNull Call call, @NonNull Response response) throws IOException {
                Log.d(TAG, "API Response Code: " + response.code());

                if (response.isSuccessful() && response.body() != null) {
                    // Lưu nội dung CSV vào file từ response body
                    saveCsvToFile(response);
                } else {
                    Log.e(TAG, "Failed to download CSV: " + response.message());
                    updateNotification("Tải xuống thất bại", "Tải xuống CSV thất bại: " + response.message());
                }
            }
        });
    }

    private void saveCsvToFile(Response response) {
        try {
            // Mở file output stream tới destinationUri
            File file = new File(destinationUri.getPath());
            try (DataOutputStream fos = new DataOutputStream(context.getContentResolver().openOutputStream(destinationUri))) {
                fos.write(response.body().bytes());
                fos.flush();
                Log.d(TAG, "CSV File saved to: " + destinationUri.getPath());
                // Thông báo tải thành công
                updateNotification("Tải xuống hoàn tất", "File CSV đã được lưu vào thiết bị");
            }
        } catch (IOException e) {
            Log.e(TAG, "Error saving CSV to file: " + e.getMessage());
            updateNotification("Tải xuống thất bại", "Lỗi lưu file CSV: " + e.getMessage());
        }
    }

    // Tạo thông báo
    private void showNotification(String title, String message) {
        notificationBuilder = new NotificationCompat.Builder(context, CHANNEL_ID)
                .setSmallIcon(android.R.drawable.stat_sys_download)
                .setContentTitle(title)
                .setContentText(message)
                .setPriority(NotificationCompat.PRIORITY_HIGH)
                .setOngoing(true); // Thông báo không thể bị loại bỏ

        if (ActivityCompat.checkSelfPermission(context, android.Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED) {
            return;
        }
        notificationManager.notify(1, notificationBuilder.build());
    }

    // Cập nhật thông báo
    private void updateNotification(String title, String message) {
        notificationBuilder.setContentTitle(title)
                .setContentText(message)
                .setSmallIcon(android.R.drawable.stat_sys_download_done)
                .setOngoing(false); // Thông báo có thể bị loại bỏ

        if (ActivityCompat.checkSelfPermission(context, android.Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED) {
            return;
        }
        notificationManager.notify(1, notificationBuilder.build());
    }

    // Tạo kênh thông báo (Notification Channel)
    private void createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            CharSequence name = "CSV Download Channel";
            String description = "Channel for CSV download notifications";
            int importance = NotificationManager.IMPORTANCE_HIGH;
            NotificationChannel channel = new NotificationChannel(CHANNEL_ID, name, importance);
            channel.setDescription(description);

            NotificationManager notificationManager = context.getSystemService(NotificationManager.class);
            if (notificationManager != null) {
                notificationManager.createNotificationChannel(channel);
            }
        }
    }
}
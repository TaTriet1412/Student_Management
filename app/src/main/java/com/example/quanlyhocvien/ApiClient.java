package com.example.quanlyhocvien;
import android.os.Handler;
import android.os.Looper;
import android.widget.Toast;
import androidx.annotation.NonNull;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.concurrent.TimeUnit;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

public class ApiClient {

    // Khai báo hằng số
    private static final String BASE_URL = "https://android-midterm-essay-api.onrender.com";
    private static final String API_KEY = "05badc8fe5c64c0d96c2ec54d970d2dc";
    private static final MediaType JSON = MediaType.get("application/json; charset=utf-8");

    // Tạo một OkHttpClient với timeout tùy chỉnh
    private static final OkHttpClient client = new OkHttpClient.Builder()
            .connectTimeout(90, TimeUnit.SECONDS)
            .writeTimeout(90, TimeUnit.SECONDS)
            .readTimeout(90, TimeUnit.SECONDS)
            .build();

    // Sử dụng Handler để đảm bảo callback chạy trên Main Thread
    private static final Handler mainHandler = new Handler(Looper.getMainLooper());

    // Interface cho callback
    public interface ApiResponseCallback {
        void onSuccess(String response);
        void onError(String errorMessage);
    }

    // Phương thức để gọi API tạo người dùng
    public static void createUser(String email, String password, String displayName, ApiResponseCallback callback) {
        String jsonBody = "{\"email\":\"" + email + "\",\"password\":\"" + password + "\",\"display_name\":\"" + displayName + "\"}";
        RequestBody body = RequestBody.create(jsonBody, JSON);

        Request request = new Request.Builder()
                .url(BASE_URL + "/create_user/")
                .header("Content-Type", "application/json")
                .header("x-api-key", API_KEY)
                .post(body)
                .build();

        client.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(@NonNull Call call, @NonNull IOException e) {
                mainHandler.post(() -> callback.onError("Failed to create user: " + e.getMessage()));
            }

            @Override
            public void onResponse(@NonNull Call call, @NonNull Response response) throws IOException {
                if (response.isSuccessful() && response.body() != null) {
                    String responseData = response.body().string();
                    mainHandler.post(() -> callback.onSuccess(responseData));
                } else {
                    String errorResponse = response.body() != null ? response.body().string() : "Unknown error";
                    mainHandler.post(() -> callback.onError("Error: " + response.code() + " - " + errorResponse));
                }
            }
        });
    }

    // Phương thức để kiểm tra kết nối API
    public static void checkConnection(ApiResponseCallback callback) {
        Request request = new Request.Builder()
                .url(BASE_URL + "/connect_check/")
                .header("x-api-key", API_KEY)
                .get()
                .build();

        client.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(@NonNull Call call, @NonNull IOException e) {
                mainHandler.post(() -> callback.onError("Connection failed: " + e.getMessage()));
            }

            @Override
            public void onResponse(@NonNull Call call, @NonNull Response response) throws IOException {
                if (response.isSuccessful() && response.body() != null) {
                    String responseData = response.body().string();
                    mainHandler.post(() -> callback.onSuccess(responseData));
                } else {
                    mainHandler.post(() -> callback.onError("Connection error: " + response.code()));
                }
            }
        });
    }

    // Phương thức để cập nhật tài khoản người dùng
    public static void updateUser(String uid, String email, String displayName, ApiResponseCallback callback) {
        String jsonBody = "{\"uid\":\"" + uid + "\",\"email\":\"" + email + "\",\"display_name\":\"" + displayName + "\"}";
        RequestBody body = RequestBody.create(jsonBody, JSON);

        Request request = new Request.Builder()
                .url(BASE_URL + "/update_account/")
                .header("Content-Type", "application/json")
                .header("x-api-key", API_KEY)
                .put(body)
                .build();

        client.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(@NonNull Call call, @NonNull IOException e) {
                mainHandler.post(() -> callback.onError("Failed to update user: " + e.getMessage()));
            }

            @Override
            public void onResponse(@NonNull Call call, @NonNull Response response) throws IOException {
                if (response.isSuccessful() && response.body() != null) {
                    String responseData = response.body().string();
                    mainHandler.post(() -> callback.onSuccess(responseData));
                } else {
                    String errorResponse = response.body() != null ? response.body().string() : "Unknown error";
                    mainHandler.post(() -> callback.onError("Error: " + response.code() + " - " + errorResponse));
                }
            }
        });
    }

    // Phương thức để xóa tài khoản người dùng
    public static void deleteUser(String uid, ApiResponseCallback callback) {
        String jsonBody = "{\"uid\":\"" + uid + "\"}";
        RequestBody body = RequestBody.create(jsonBody, JSON);

        Request request = new Request.Builder()
                .url(BASE_URL + "/delete_account/")
                .header("Content-Type", "application/json")
                .header("x-api-key", API_KEY)
                .delete(body)
                .build();

        client.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(@NonNull Call call, @NonNull IOException e) {
                mainHandler.post(() -> callback.onError("Failed to delete user: " + e.getMessage()));
            }

            @Override
            public void onResponse(@NonNull Call call, @NonNull Response response) throws IOException {
                if (response.isSuccessful() && response.body() != null) {
                    String responseData = response.body().string();
                    mainHandler.post(() -> callback.onSuccess(responseData));
                } else {
                    String errorResponse = response.body() != null ? response.body().string() : "Unknown error";
                    mainHandler.post(() -> callback.onError("Error: " + response.code() + " - " + errorResponse));
                }
            }
        });
    }

    public static void disableUser(String uid, boolean disabled, ApiResponseCallback callback) {
        String url = BASE_URL + "/disable_account/";

        // Tạo JSON body cho yêu cầu
        JSONObject jsonObject = new JSONObject();
        try {
            jsonObject.put("uid", uid);
            jsonObject.put("disabled", disabled);
        } catch (JSONException e) {
            callback.onError("Failed to create JSON body: " + e.getMessage());
            return;
        }

        // Tạo RequestBody
        RequestBody body = RequestBody.create(jsonObject.toString(), JSON);

        // Tạo request
        Request request = new Request.Builder()
                .url(url)
                .put(body)
                .addHeader("x-api-key", API_KEY) // Đảm bảo thêm API key cho xác thực
                .build();

        // Thực hiện request bất đồng bộ
        client.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(@NonNull Call call, @NonNull IOException e) {
                mainHandler.post(() -> callback.onError("Failed to disable user: " + e.getMessage()));
            }

            @Override
            public void onResponse(@NonNull Call call, @NonNull Response response) throws IOException {
                if (response.isSuccessful()) {
                    String responseData = response.body() != null ? response.body().string() : "";
                    mainHandler.post(() -> callback.onSuccess(responseData));
                } else {
                    String errorResponse = response.body() != null ? response.body().string() : "Unknown error";
                    mainHandler.post(() -> callback.onError("Error: " + response.code() + " - " + errorResponse));
                }
            }
        });
    }

}



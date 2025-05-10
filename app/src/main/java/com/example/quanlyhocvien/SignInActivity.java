package com.example.quanlyhocvien;

import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.app.AppCompatDelegate;

import com.example.quanlyhocvien.object.Account;
import com.example.quanlyhocvien.object.Log;
import com.example.quanlyhocvien.utils.GlobalVariables;
import com.example.quanlyhocvien.utils.SecurePreferencesUtil;
import com.google.android.material.textfield.TextInputEditText;
import com.google.firebase.Timestamp;
import com.google.firebase.auth.FirebaseAuthInvalidCredentialsException;
import com.google.firebase.auth.FirebaseAuthInvalidUserException;

import java.util.Objects;

public class SignInActivity extends AppCompatActivity {
    TextInputEditText  edtPassword, edtEmaill;
    Button btnSignIn;
    TextView tvError, tvForgotPassword;
    FrameLayout progressOverlay;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO);
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sign_in);
        btnSignIn = findViewById(R.id.btnSignIn);
        edtPassword = findViewById(R.id.txtPass);
        edtEmaill = findViewById(R.id.txtEmail);
        tvError = findViewById(R.id.lblError);
        tvForgotPassword = findViewById(R.id.lblForgotPass);
        progressOverlay = findViewById(R.id.progress_overlay);

        // Đánh thức API Server
        wakeUpAPIServer();

        // Set email from SignHomeActivity
        String emailFromHome = getIntent().getStringExtra("email");
        if (emailFromHome != null) {
            edtEmaill.setText(emailFromHome);
            edtPassword.requestFocus();
        } else {
            // Set email from SharedPreferences
            try {
                String emailFromShf = SecurePreferencesUtil.getEmail(this);
                if (emailFromShf != null) {
                    edtEmaill.setText(emailFromShf);
                    edtPassword.requestFocus();
                } else {
                    edtEmaill.requestFocus();
                }
            } catch (Exception e) {
                edtEmaill.requestFocus();
            }
        }

//       Default focus on email field
        edtEmaill.requestFocus();

//      handle click event Login Button
        btnSignIn.setOnClickListener(view -> {
            String email = edtEmaill.getText().toString().trim();
            String password = edtPassword.getText().toString().trim();

            // Kiểm tra email và password có rỗng không
            if(email.isEmpty()){
                edtEmaill.setBackgroundResource(R.drawable.sign_in_frame_error_view);
                tvError.setVisibility(View.VISIBLE);
                edtEmaill.requestFocus();
                Toast.makeText(this, "Email không được để trống", Toast.LENGTH_SHORT).show();
                return;
            }
            if(password.isEmpty()){
                edtPassword.setBackgroundResource(R.drawable.sign_in_frame_error_view);
                tvError.setVisibility(View.VISIBLE);
                edtPassword.requestFocus();
                Toast.makeText(this, "Mật khẩu không được để trống", Toast.LENGTH_SHORT).show();
                return;
            }
            // Gọi hàm signIn và xử lý kết quả
            progressOverlay.setVisibility(View.VISIBLE); // Hiển thị loading overlay
            FirebaseAuth.signIn(email, password)
                    .addOnCompleteListener(task -> {
                        if (task.isSuccessful()) {
                            // Đăng nhập thành công
                            Boolean isSuccess = task.getResult();
                            if (isSuccess != null && isSuccess) {
                                // Lấy thông tin người dùng từ Firestore
                                Firestore.getAccount(FirebaseAuth.getUID(), new Firestore.FirestoreGetAccountCallback() {
                                    @Override
                                    public void onCallback(Account account) {
                                        if (account != null) {
                                            // Thành công
                                            GlobalVariables globalVariables = GlobalVariables.getInstance();
                                            globalVariables.setCurrentAccount(account); // Lưu thông tin người dùng vào biến toàn cục
                                            saveLog(); // Lưu log

                                            try {
                                                SecurePreferencesUtil.saveEmail(SignInActivity.this, email); // Lưu email vào SharedPreferences
                                            } catch (Exception e) {}
                                            progressOverlay.setVisibility(View.GONE); // Ẩn loading overlay
                                            Intent intent = new Intent(SignInActivity.this, MainActivity.class);
                                            startActivity(intent);

                                        } else {
                                            // Thông tin người dùng không có trong firestore hoặc lỗi đọc firestore
                                            FirebaseAuth.signOut();
                                            progressOverlay.setVisibility(View.GONE); // Ẩn loading overlay
                                            Toast.makeText(SignInActivity.this, "Không thể lấy thông tin tài khoản", Toast.LENGTH_SHORT).show();
                                        }
                                    }
                                });
                            }
                        }
                    })
                    .addOnFailureListener(e -> {
                        progressOverlay.setVisibility(View.GONE); // Ẩn loading overlay
                        // Xử lý lỗi
                        if (e instanceof FirebaseAuthInvalidUserException || e instanceof FirebaseAuthInvalidCredentialsException) {
                            edtEmaill.setBackgroundResource(R.drawable.sign_in_frame_error_view);
                            edtPassword.setBackgroundResource(R.drawable.sign_in_frame_error_view);
                            tvError.setVisibility(View.VISIBLE);
                            edtEmaill.requestFocus();
                            return;
                        }
                        else {
                            Toast.makeText(SignInActivity.this, "Đã có lỗi xảy ra, vui lòng thử lại", Toast.LENGTH_SHORT).show();
                            edtEmaill.requestFocus();
                            return;
                        }
                    });
        });



        // handle change text of email field
        edtEmaill.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                edtEmaill.setBackgroundResource(R.drawable.sign_in_frame_view);
                tvError.setVisibility(View.GONE);
            }

            @Override
            public void afterTextChanged(Editable s) {

            }
        });

        // handle change text of password field
        edtPassword.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                edtPassword.setBackgroundResource(R.drawable.sign_in_frame_view);
                tvError.setVisibility(View.GONE);
            }

            @Override
            public void afterTextChanged(Editable s) {

            }
        });

        // handle click event Forgot Password TextView
        tvForgotPassword.setOnClickListener(view -> {
            Intent intent = new Intent(this, ForgotPasswordActivity.class);
            intent.putExtra("email", Objects.requireNonNull(edtEmaill.getText()).toString());
            startActivity(intent);
        });

    }

    private void wakeUpAPIServer() {
        ApiClient.checkConnection(new ApiClient.ApiResponseCallback() {
            @Override
            public void onSuccess(String response) {
                // Thực thi nếu kết nối thành công
            }

            @Override
            public void onError(String errorMessage) {
                // Xử lý nếu kết nối thất bại
                Toast.makeText(getApplicationContext(), "Không thể kết nối tới API: " + errorMessage, Toast.LENGTH_LONG).show();
            }
        });

    }

    private void getCurrentAccount() {
        GlobalVariables globalVariables = GlobalVariables.getInstance();
        Firestore.getAccount(FirebaseAuth.getUID(), new Firestore.FirestoreGetAccountCallback() {
            @Override
            public void onCallback(Account account) {
                if (account != null) {
                    globalVariables.setCurrentAccount(account);
                } else {
                    // Thông tin người dùng không có trong firestore hoặc lỗi
                    Toast.makeText(SignInActivity.this, "Không thể lấy thông tin tài khoản", Toast.LENGTH_SHORT).show();
                }
            }
        });
    }

    private  void saveLog() {
        Log log = new Log();
        log.setTime(Timestamp.now());
        String manufacturer = Build.MANUFACTURER; // Nhà sản xuất (Samsung, Xiaomi, v.v.)
        String model = Build.MODEL; // Model của thiết bị (Galaxy S10, Redmi Note 9, v.v.)
        if (model.startsWith(manufacturer)) {
            log.setDevice(model);
        } else {
            log.setDevice(manufacturer + " " + model);
        }

        // Lưu log vào Firestore
        Firestore.addLog(FirebaseAuth.getUID(), log, new Firestore.FirestoreAddCallback() {
            @Override
            public void onCallback(boolean isSuccess) {
                // Không quan trọng
            }
        });
    }
}



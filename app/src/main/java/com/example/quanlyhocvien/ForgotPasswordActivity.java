package com.example.quanlyhocvien;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.ViewTreeObserver;
import android.widget.FrameLayout;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.AppCompatButton;
import androidx.appcompat.widget.AppCompatImageView;
import androidx.appcompat.widget.AppCompatTextView;
import androidx.core.content.ContextCompat;

import com.google.android.material.textfield.TextInputEditText;
import com.google.firebase.auth.FirebaseAuthInvalidCredentialsException;
import com.google.firebase.auth.FirebaseAuthInvalidUserException;

import java.util.Objects;

public class ForgotPasswordActivity extends AppCompatActivity implements View.OnClickListener {

    private final String EMAIL_NULL_ERR = "Vui lòng nhập e-mail khôi phục";
    private final String EMAIL_WRONG_FORMAT_ERR = "Email không đúng định dạng";
    private final String OTHER_ERR = "Đã có lỗi xảy ra, vui lòng thử lại sau!";
    private final String EMAIL_NOT_FOUND_ERR = "Tài khoản không tồn tại";

    private AppCompatImageView imgBack;
    private TextInputEditText txtEmail;
    private AppCompatTextView lblEmailValidErr;
    private AppCompatButton btnSubmit;

    FrameLayout progressOverlay;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_forgot_password);
        initViews();
        initListeners();

    }

    @Override
    protected void onStart() {
        super.onStart();
        fillEmailFromLastActivity();
        txtEmail.requestFocus();
    }

    private void initViews() {
        imgBack = findViewById(R.id.imgBack);
        txtEmail = findViewById(R.id.txtEmail);
        lblEmailValidErr = findViewById(R.id.lblEmailValidErr);
        btnSubmit = findViewById(R.id.btnSubmit);
        progressOverlay = findViewById(R.id.progress_overlay);
    }

    private void initListeners() {
        imgBack.setOnClickListener(this);
        btnSubmit.setOnClickListener(this);
    }


    @Override
    public void onClick(View v) {
        // Nhấn nút back
        // -> Quay về Activity trước đó trong stackđịnh
        if (v.getId() == R.id.imgBack){
            Intent intent = new Intent(ForgotPasswordActivity.this, SignInActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TOP);
            startActivity(intent);
        }
        // Nhấn nút xác thực
        // -> Xác minh email để gửi yêu cầu đặt lại mật khẩu
        if (v.getId() == R.id.btnSubmit){
            String email = Objects.requireNonNull(txtEmail.getText()).toString();
            if (email.isEmpty() || email.isBlank()){
                setError(EMAIL_NULL_ERR);
                return;
            }
            checkEmailValid(email);
        }
    }

    private void checkEmailValid(String email) {
        progressOverlay.setVisibility(View.VISIBLE); // Hiển thị loading overlay
        FirebaseAuth.sendResetPasswordEmail(email).addOnCompleteListener(task -> {
            if (task.isSuccessful() && task.getResult()) {
                progressOverlay.setVisibility(View.GONE); // Ẩn loading overlay
                // Xác thực thành công, ẩn thông báo lỗi và chuyển activity
                hideError();
                startResetPasswordActivity();
            } else {
                progressOverlay.setVisibility(View.GONE); // Ẩn loading overlay
                if (task.getException() instanceof FirebaseAuthInvalidUserException) {
                    setError(EMAIL_NOT_FOUND_ERR);
                } else if (task.getException() instanceof FirebaseAuthInvalidCredentialsException) {
                    setError(EMAIL_WRONG_FORMAT_ERR);
                } else {
                    setError(OTHER_ERR);
                }
            }
        });
    }

    // Chuyển hướng đến activity thông báo gửi mail đổi mk thành công
    // Finish activity hiện tại mà không đưa vào trong stack
    private void startResetPasswordActivity(){
        Intent intent = new Intent(this, ResetPasswordActivity.class);
        intent.putExtra("email", Objects.requireNonNull(txtEmail.getText()).toString());
        startActivity(intent);
        finish();
    }

    private void fillEmailFromLastActivity(){
        Intent intent = getIntent();
        String email = intent.getStringExtra("email");
        if (email != null) txtEmail.setText(email);
    }

    private void setError(String errorMessage){
        txtEmail.setBackgroundDrawable(ContextCompat.getDrawable(this, R.drawable.bg_rounded_view_error));
        lblEmailValidErr.setVisibility(View.VISIBLE);
        lblEmailValidErr.setText(errorMessage);
    }

    private void hideError(){
        txtEmail.setBackgroundDrawable(ContextCompat.getDrawable(this, R.drawable.sign_in_frame_view));
        lblEmailValidErr.setVisibility(View.INVISIBLE);
    }

}
package com.example.quanlyhocvien;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.AppCompatButton;
import androidx.appcompat.widget.AppCompatImageView;
import androidx.appcompat.widget.AppCompatTextView;

import com.example.quanlyhocvien.utils.GlobalVariables;

public class ResetPasswordActivity extends AppCompatActivity implements View.OnClickListener {

    private AppCompatImageView imgBack;
    private AppCompatButton btnResend;
    private AppCompatTextView appCompatTextView2;

    private final static String TOAST_NOTIFICATION = "Vui lòng kiểm tra lại email";
    private String email;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_reset_password);
        initViews();
        initListeners();
        loadEmailFromIntent();
        checkRole();
    }

    private void initViews(){
        imgBack = findViewById(R.id.imgBack);
        btnResend = findViewById(R.id.btnResend);
        appCompatTextView2 = findViewById(R.id.appCompatTextView2);
    }

    private void initListeners(){
        imgBack.setOnClickListener(this);
        btnResend.setOnClickListener(this);
    }

    private void loadEmailFromIntent(){
        Intent intent = getIntent();
        email = intent.getStringExtra("email");
    }

    @Override
    public void onClick(View v) {
        if (v.getId() == R.id.imgBack) {
            // Finish activity, chuyển hướng về activity gần nhất trong stack
            finish();
        } else if (v.getId() == R.id.btnResend) {
            if (btnResend.getText().toString().equals("Trở về")) {
                // Admin
                finish();
                return;
            }

            // User
            // Chuyển hướng đến activity ForgotPassword để người dùng kiểm tra lại email
            Intent intent = new Intent(this, ForgotPasswordActivity.class);
            intent.putExtra("email", email);
            Toast.makeText(ResetPasswordActivity.this, TOAST_NOTIFICATION, Toast.LENGTH_SHORT).show();
            startActivity(intent);
            finish();
        }
    }

    private void checkRole(){
        int role = GlobalVariables.getInstance().getCurrentAccount().getRole();
        if(role == 0){
            // Cấp lại
            String str = getString(R.string.pass_reissue_hint_admin);
            appCompatTextView2.setText(str);
            btnResend.setText("Trở về");
        }
    }
}
package com.example.quanlyhocvien.utils;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.DatePickerDialog;
import android.content.Context;
import android.content.pm.PackageManager;
import android.os.Build;
import android.view.inputmethod.InputMethodManager;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;

import androidx.appcompat.app.AlertDialog;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;

import com.example.quanlyhocvien.R;
import com.google.android.material.textfield.TextInputEditText;

import java.util.Calendar;
import java.util.Objects;

public class ActionUtils {
    public static final String[] genders = {"Nam", "Nữ", "Khác"};
    public static final String[] roles = {"Nhân viên", "Quản lý"};
    public static final String[] statuses = {"Chưa hoàn thành", "Đã hoàn thành", "Đang chờ"};

    public static void setupPicker(Fragment fragment, AutoCompleteTextView autoGender, AutoCompleteTextView autoRole, AutoCompleteTextView autoStatus) {
        // Thiết lập cho chọn giới tính
        try {
            ArrayAdapter<String> adapter1 = new ArrayAdapter<>(fragment.requireContext(), android.R.layout.simple_list_item_1, genders);
            autoGender.setAdapter(adapter1);
            // Hiển thị danh sách khi người dùng nhấp vào trường
            autoGender.setDropDownBackgroundDrawable(ContextCompat.getDrawable(fragment.requireContext(), R.drawable.bg_dropdown));
            autoGender.setOnClickListener(v -> autoGender.showDropDown());
        } catch (Exception e) {
            // Fragment không có trường chọn giới tính
        }

        // Thiết lập cho chọn vai trò
        try {
            ArrayAdapter<String> adapter2 = new ArrayAdapter<>(fragment.requireContext(), android.R.layout.simple_list_item_1, roles);
            autoRole.setAdapter(adapter2);
            // Hiển thị danh sách khi người dùng nhấp vào trường
            autoRole.setDropDownBackgroundDrawable(ContextCompat.getDrawable(fragment.requireContext(), R.drawable.bg_dropdown));
            autoRole.setOnClickListener(v -> autoRole.showDropDown());
        } catch (Exception e) {
            // Fragment không có trường chọn vai trò
        }

        // Thiết lập cho chọn trạng thái (Certificate)
        try {
            ArrayAdapter<String> adapter3 = new ArrayAdapter<>(fragment.requireContext(), android.R.layout.simple_list_item_1, statuses);
            autoStatus.setAdapter(adapter3);
            // Hiển thị danh sách khi người dùng nhấp vào trường
            autoStatus.setDropDownBackgroundDrawable(ContextCompat.getDrawable(fragment.requireContext(), R.drawable.bg_dropdown));
            autoStatus.setOnClickListener(v -> autoStatus.showDropDown());
        } catch (Exception e) {
            // Fragment không có trường chọn trạng thái
        }
    }

    // Thiết lập cho tùy chọn cho bất kỳ AutoCompleteTextView nào, cần truyền vào context
    public static void setupPickerForAutoCompleteTextView(Context context, AutoCompleteTextView view, String[] options) {
        // Thiết lập cho chọn tùy chọn
        ArrayAdapter<String> adapter = new ArrayAdapter<>(context, android.R.layout.simple_list_item_1, options);
        view.setAdapter(adapter);
        // Hiển thị danh sách khi người dùng nhấp vào trường
        view.setDropDownBackgroundDrawable(ContextCompat.getDrawable(context, R.drawable.bg_dropdown));
        view.setOnClickListener(v -> view.showDropDown());
    }

    // Các phiên bản Android đời cũ cần quyền ghi bộ nhớ ngoài
    public static boolean checkWriteExternalPermission(Context context, Activity activity){
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            return true;
        }
        if (ActivityCompat.checkSelfPermission(context, Manifest.permission.WRITE_EXTERNAL_STORAGE)
                == PackageManager.PERMISSION_GRANTED) {
            return true;
        } else {
            new AlertDialog.Builder(context)
                    .setTitle("Quyền truy cập bộ nhớ ngoài")
                    .setMessage("Ứng dụng cần quyền truy cập bộ nhớ để xuất dữ liệu\n" +
                            "Nhấn OK để ứng dụng yêu cầu quyền truy cập bộ nhớ\n" +
                            "Nếu thông báo yêu cầu không hiện lên, vui lòng cấp quyền thủ công trong cài đặt!")
                    .setPositiveButton("OK", (dialog, which) -> {
                        ActivityCompat.requestPermissions(activity,
                                new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE}, 123);
                    })
                    .setNegativeButton("Hủy", (dialog, which) -> {
                    })
                    .create()
                    .show();
            return false;
        }
    }

}

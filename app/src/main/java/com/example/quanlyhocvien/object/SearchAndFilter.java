package com.example.quanlyhocvien.object;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.res.ColorStateList;
import android.graphics.Color;
import android.graphics.PixelFormat;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.AttributeSet;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.WindowManager;
import android.view.inputmethod.InputMethodManager;
import android.widget.AutoCompleteTextView;
import android.widget.LinearLayout;
import android.widget.PopupWindow;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.widget.AppCompatButton;
import androidx.appcompat.widget.AppCompatImageButton;
import androidx.appcompat.widget.AppCompatImageView;
import androidx.appcompat.widget.AppCompatTextView;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.core.content.res.ResourcesCompat;

import com.example.quanlyhocvien.R;
import com.example.quanlyhocvien.utils.ActionUtils;
import com.example.quanlyhocvien.utils.SearchAndFilterListener;
import com.google.android.material.textfield.TextInputEditText;

import java.util.Objects;

public class SearchAndFilter extends ConstraintLayout {

    private final static String[] genderList = {"nam", "nữ", "khác", "tất cả"};
    private final Context context;
    private SearchAndFilterListener listener;

    private TextInputEditText edtContent;
    private AppCompatImageButton btnFilter;
    private AppCompatImageButton optSearch;

    private boolean sortName, sortAge, isShown;
    private int genderIndex;

    public SearchAndFilter(@NonNull Context context) {
        super(context);
        this.context = context;
        inflate(context, R.layout.layout_search_and_filter, this);
    }

    public SearchAndFilter(@NonNull Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        this.context = context;
        inflate(context, R.layout.layout_search_and_filter, this);
    }

    public SearchAndFilter(@NonNull Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        this.context = context;
        inflate(context, R.layout.layout_search_and_filter, this);
    }

    @Override
    public void onViewAdded(View view) {
        super.onViewAdded(view);
        initView(view);
    }

    private void initView(View view) {
        Log.d("SearchAndFilter", "Init");
        edtContent = view.findViewById(R.id.edtContent);
        btnFilter = view.findViewById(R.id.btnFilter);
        optSearch = view.findViewById(R.id.opt_search);
        setDefault();
        initListeners();
    }

    private void initListeners(){
        Log.d("SearchAndFilter", "Listeners");
        btnFilter.setOnClickListener(view -> {
            showFilterPopUpMenu(btnFilter);
            edtContent.clearFocus();
        });
        optSearch.setOnClickListener(view -> {
            edtContent.clearFocus();
            edtContent.setText(edtContent.getText());
            ((InputMethodManager) context.getSystemService(Context.INPUT_METHOD_SERVICE))
                .hideSoftInputFromWindow(edtContent.getWindowToken(), 0);
            addSearchCalledListener();
        });
        // Thêm TextWatcher để lắng nghe sự thay đổi của văn bản tìm kiếm
        edtContent.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
                // Không cần xử lý
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                // Cập nhật danh sách học viên khi văn bản thay đổi
                addSearchCalledListener();
            }

            @Override
            public void afterTextChanged(Editable s) {
                // Không cần xử lý
            }
        });
    }

    private void showFilterPopUpMenu(View anchor_view){
        // Nếu PopupWindow đã hiển thị thì không hiển thị nữa
        if (isShown) return;
        else isShown = true;

        @SuppressLint("InflateParams")
        View popUpView = LayoutInflater.from(context).inflate(R.layout.layout_filter_popup, null);

        PopupWindow popupWindow = new PopupWindow (
                popUpView,
                LinearLayout.LayoutParams.WRAP_CONTENT,
                LinearLayout.LayoutParams.WRAP_CONTENT,
                true
        );

        // Khởi tạo optName
        AppCompatImageView optName = popUpView.findViewById(R.id.opt_name);
        // Thiết lập hình ảnh cho optName
        optName.setImageDrawable( ResourcesCompat.getDrawable(
                getResources() , sortName ? R.drawable.ic_sort_az : R.drawable.ic_sort_za , null
        ));
        // Thiết lập sự kiện click cho optName - Ô chọn tên
        optName.setOnClickListener(v -> {
            optName.setImageTintList(ColorStateList.valueOf(Color.parseColor("#000000")));
            sortName = !sortName;
            optName.setImageDrawable( ResourcesCompat.getDrawable(
                    getResources() , sortName ? R.drawable.ic_sort_az : R.drawable.ic_sort_za , null
            ));
        });

        // Khởi tạo optAge - Ô chọn tuổi
        AppCompatImageView optAge = popUpView.findViewById(R.id.opt_age);
        optAge.setImageDrawable( ResourcesCompat.getDrawable(
                getResources() , sortAge ? R.drawable.ic_sort_19 : R.drawable.ic_sort_91 , null
        ));
        // Thiết lập sự kiện click cho optAge
        optAge.setOnClickListener(v -> {
            optAge.setImageTintList(ColorStateList.valueOf(Color.parseColor("#000000")));
            sortAge = !sortAge;
            optAge.setImageDrawable( ResourcesCompat.getDrawable(
                    getResources() , sortAge ? R.drawable.ic_sort_19 : R.drawable.ic_sort_91 , null
            ));
        });

        // Khởi tạo optGender - Ô chọn giới tính
        AutoCompleteTextView optGender = popUpView.findViewById(R.id.opt_gender);
        optGender.setText(genderList[genderIndex]);
        // Thiết lập Adapter cho optGender
        ActionUtils.setupPickerForAutoCompleteTextView(context, optGender, genderList);
        optGender.setOnItemClickListener((parent, view, position, id) -> {
            optGender.setTextColor(Color.parseColor("#000000"));
            genderIndex = position;
        });

        // Khởi tạo optReset - Nút đặt filter lại mặc định
        AppCompatTextView optReset = popUpView.findViewById(R.id.opt_reset);
        // Thiết lập sự kiện click cho optReset
        optReset.setOnClickListener(v -> {
            setDefault();
            addFilterResetListener();
            popupWindow.dismiss();
        });

        // Khởi tạo optApply - Nút áp dụng thay đổi cho filter
        AppCompatButton optApply = popUpView.findViewById(R.id.opt_apply);
        // Thiết lập sự kiện click cho optApply
        optApply.setOnClickListener(v -> {
            addFilterChangedListener();
            popupWindow.dismiss();
        });

        // Nếu filter không mặc định thì thiết lập màu đậm
        if ( !isDefault() ){
            optAge.setImageTintList(ColorStateList.valueOf(Color.parseColor("#000000")));
            optName.setImageTintList(ColorStateList.valueOf(Color.parseColor("#000000")));
            optGender.setTextColor(Color.parseColor("#000000"));
        }

        // Tạo View nền mờ phía sau PopupWindow
        View dimView = new View(context);
        dimView.setLayoutParams(new WindowManager.LayoutParams(
                WindowManager.LayoutParams.MATCH_PARENT,
                WindowManager.LayoutParams.MATCH_PARENT
        ));
        dimView.setBackgroundColor(Color.parseColor("#BF000000"));

        // Thêm dimView vào màn hình phía sau PopupWindow
        WindowManager wm = (WindowManager) context.getSystemService(Context.WINDOW_SERVICE);
        WindowManager.LayoutParams dimParams = new WindowManager.LayoutParams(
                WindowManager.LayoutParams.MATCH_PARENT,
                WindowManager.LayoutParams.MATCH_PARENT,
                WindowManager.LayoutParams.TYPE_APPLICATION_PANEL,  // Sử dụng TYPE_APPLICATION_PANEL thay vì TYPE_APPLICATION_OVERLAY
                WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE | WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE,
                PixelFormat.TRANSLUCENT
        );
        wm.addView(dimView, dimParams);

        // Hiển thị PopupWindow
        popupWindow.showAsDropDown(anchor_view, -470, 70);

        // Loại bỏ dimView khi PopupWindow đóng
        popupWindow.setOnDismissListener(() -> wm.removeView(dimView));

        // Bắt sự kiện khi PopupWindow đóng
        popupWindow.setOnDismissListener(() -> {
            // Khi PopupWindow đóng thì isShown = false
            isShown = false;
            // Loại bỏ dimView
            wm.removeView(dimView);
            // Nếu filter có trạng thái không phải true&true&3 thì có thêm chấm xanh
            btnFilter.setImageDrawable( ResourcesCompat.getDrawable(
                    getResources(),
                    isDefault() ? R.drawable.ic_filter_default : R.drawable.ic_filter_active ,
                    null
            ));
        });
    }

    // Thiết lập mặc định
    public void setDefault() {
        sortName = sortAge = true;
        genderIndex = 3;
    }

    // Kiểm tra xem dữ liệu hiện tại có mặc định hay không
    public boolean isDefault() {
        return sortName && sortAge && genderIndex == 3;
    }

    // Hàm bắt những sự kiện được triển khai trong Interface
    public void addSearchAndFilterListeners(SearchAndFilterListener listener){
        this.listener = listener;
    }

    // Hàm truyền dữ liệu và gọi listener cho sự kiện nhấn nút tìm kiếm
    private void addSearchCalledListener(){
        listener.onSearchTriggered(
                Objects.requireNonNull(edtContent.getText()).toString(),
                sortName,
                sortAge,
                genderIndex);
    }

    // Hàm truyền dữ liệu và gọi listener cho sự kiện thay đổi filter
    private void addFilterChangedListener(){
        listener.onFilterChanged(
                Objects.requireNonNull(edtContent.getText()).toString(),
                sortName,
                sortAge,
                genderIndex);
    }

    // Hàm truyền dữ liệu và gọi listener cho sự kiện đặt lại filter
    private void addFilterResetListener(){
        listener.onFilterReset(
                Objects.requireNonNull(edtContent.getText()).toString(),
                sortName,
                sortAge,
                genderIndex);
    }

    // Getter
    public boolean isSortName() {
        return sortName;
    }
    public boolean isSortAge() {
        return sortAge;
    }
    public int getGenderIndex() {
        return genderIndex;
    }
    public String getContent() {
        return Objects.requireNonNull(edtContent.getText()).toString();
    }
}
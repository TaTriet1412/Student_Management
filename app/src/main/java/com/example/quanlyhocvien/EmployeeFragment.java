package com.example.quanlyhocvien;

import android.content.Intent;
import android.content.res.ColorStateList;
import android.graphics.Color;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.Fragment;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.quanlyhocvien.Adapter.EmployeeRecyclerViewAdapter;
import com.example.quanlyhocvien.Fragment.LoginHistoryFragment;
import com.example.quanlyhocvien.object.Account;
import com.example.quanlyhocvien.object.SearchAndFilter;
import com.example.quanlyhocvien.object.Student;
import com.example.quanlyhocvien.utils.GlobalVariables;
import com.example.quanlyhocvien.utils.SearchAndFilterListener;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

public class EmployeeFragment extends Fragment {
    // chỉnh cái biến này để thay đổi vị trí hiện tại tính từ 1
    int positionToShowButton = 2;
    List<Account> employees = new LinkedList<>();
    Button btnHome;
    EmployeeRecyclerViewAdapter adapter;
    int role;
    private AccountViewModel accountViewModel;
    FrameLayout progressOverlay;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.fragment_employee, container, false);
        RecyclerView recyclerView = view.findViewById(R.id.EmployeeRecyclerView);
        progressOverlay = requireActivity().findViewById(R.id.progress_overlay);
        btnHome = view.findViewById(R.id.btnHome);
        btnHome.setOnClickListener(v -> {
            recyclerView.scrollToPosition(0);
        });

        role = GlobalVariables.getInstance().getCurrentAccount().getRole();

        recyclerView.addOnScrollListener(new RecyclerView.OnScrollListener() {
            @Override
            public void onScrolled(@NonNull RecyclerView recyclerView, int dx, int dy) {
                super.onScrolled(recyclerView, dx, dy);

                // Kiểm tra vị trí cuộn
                if (recyclerView.computeVerticalScrollOffset() > (positionToShowButton)*500) {
                    btnHome.setVisibility(View.VISIBLE);
                } else {
                    btnHome.setVisibility(View.GONE);
                }
            }
        });
        adapter = new EmployeeRecyclerViewAdapter(employees, this, role);
        recyclerView.setAdapter(adapter);
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        return view;
    }

    public boolean onContextItemSelected(MenuItem item) {
        // Lấy thông tin của item được chọn
        Account employee = employees.get(adapter.positionSelected);
        if (item.getItemId() == R.id.info_employee) {
            // Xử lý khi chọn Option 1
            MyHandleClick();
            return true;
        } else if (item.getItemId() == R.id.history) {
            // Xử lý khi chọn Option 2
            Fragment fragment = new LoginHistoryFragment();
            Bundle bundle = new Bundle();
            bundle.putString("studentId", employee.getUID());
            fragment.setArguments(bundle);
            ((MainActivity) requireActivity()).loadFragment(fragment);
            return true;
        } else {
            AlertDialog.Builder builder = new AlertDialog.Builder(requireContext());
            builder.setTitle("Cảnh báo");
            builder.setMessage("Vô hiệu hóa tài khoản sẽ khiến người dùng không thể đăng nhập.\n" +
                    "Bạn có chắc chắn muốn vô hiệu hóa tài khoản này?");
            builder.setPositiveButton("Vô hiệu hóa", (dialog, which) -> {
                // Vô hiệu hóa tài khoản
                enable_disable_account(false, employee);
            });
            builder.setNegativeButton("Hủy", (dialog, which) -> {
                dialog.dismiss();
            });
            builder.show();

            return super.onContextItemSelected(item);
        }
    }


    //chỗ này xử lí sự kiện click vào menu
    public void MyHandleClick() {
        Account employee = employees.get(adapter.positionSelected);
        Fragment fragment = new EditAccountFragment();
        Bundle bundle = new Bundle();
        bundle.putString("uid", employee.getUID());
        fragment.setArguments(bundle);
        ((MainActivity) requireActivity()).loadFragment(fragment);

    }

    // xử lí sự kiện click ở đây
    public void MyHandleClick(Account employee) {
        Fragment fragment = new EditAccountFragment();
        Bundle bundle = new Bundle();
        bundle.putString("uid", employee.getUID());
        fragment.setArguments(bundle);
        ((MainActivity) requireActivity()).loadFragment(fragment);
    }
    public void addClick () {
            Fragment fragment = new AddAccountFragment();
            ((MainActivity) requireActivity()).loadFragment(fragment);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        // Đăng ký trình lắng nghe sự kiện ở Firestore
        Firestore.initAccountSimpleDataListener(requireActivity());

        // Khởi tạo viewmodel
        accountViewModel = new ViewModelProvider(requireActivity()).get(AccountViewModel.class);

        // Lắng nghe sự thay đổi trên accountsLiveData
        accountViewModel.getAccountsLiveData().observe(getViewLifecycleOwner(), accounts -> {
            EmployeeFragment.this.employees = accounts;
            adapter.setEmployees(employees);
            adapter.notifyDataSetChanged();
        });

        // Lấy dữ liệu từ Firestore
        progressOverlay.setVisibility(View.VISIBLE); // Hiện loading overlay
        Firestore.getAllAccountSimpleData(new Firestore.FirestoreGetAllAccountSimpleDataCallback() {
            @Override
            public void onCallback(ArrayList<Account> accounts) {
                if (accounts != null) {
                    accountViewModel.setAccountsLiveData(accounts);
                    progressOverlay.setVisibility(View.GONE); // Ẩn loading overlay
                } else {
                    progressOverlay.setVisibility(View.GONE); // Ẩn loading overlay
                    Toast.makeText(getContext(), "Không tải được dữ liệu", Toast.LENGTH_SHORT).show();
                }
            }
        });
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        Firestore.removeAccountSimpleDataListener(); // Hủy đăng ký trình lắng nghe sự kiện ở Firestore
    }

    private void enable_disable_account(boolean activeStatus, Account currentAccount) {
        // Không cho vô hiệu hóa tài khoản admin
        if (currentAccount.getRole() == 0) {
            Toast.makeText(requireContext(), "Không thể vô hiệu hóa tài khoản admin", Toast.LENGTH_LONG).show();
            return;
        }

        progressOverlay.setVisibility(View.VISIBLE); // Hiển thị loading

        ApiClient.disableUser(currentAccount.getUID(), !activeStatus, new ApiClient.ApiResponseCallback() {
            @Override
            public void onSuccess(String response) {
                // Xử lý khi yêu cầu thành công
                // Cập nhật trạng thái tài khoản ở Firestore, không quan trọng kết quả
                currentAccount.setActiveStatus(activeStatus);
                Firestore.updateAccountActiveStatus(currentAccount.getUID(), currentAccount.getActiveStatus(), callback -> {});
                progressOverlay.setVisibility(View.GONE); // Ẩn loading
            }

            @Override
            public void onError(String errorMessage) {
                // Xử lý khi yêu cầu thất bại
                progressOverlay.setVisibility(View.GONE); // Ẩn loading
                Toast.makeText(requireContext(), "Đã xảy ra lỗi" + errorMessage, Toast.LENGTH_LONG).show();
            }
        });
    }
}
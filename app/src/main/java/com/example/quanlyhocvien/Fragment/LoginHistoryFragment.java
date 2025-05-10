package com.example.quanlyhocvien.Fragment;

import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.example.quanlyhocvien.Adapter.EmployeeRecyclerViewAdapter;
import com.example.quanlyhocvien.Adapter.LoginHistoryRecyclerViewAdapter;
import com.example.quanlyhocvien.CertificateViewModel;
import com.example.quanlyhocvien.Firestore;
import com.example.quanlyhocvien.LogViewModel;
import com.example.quanlyhocvien.R;
import com.example.quanlyhocvien.object.Log;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;


public class LoginHistoryFragment extends Fragment {
    List<Log> logs = new LinkedList<>();
    Button btnHome;
    int positionToShowButton = 2;
    private LogViewModel logViewModel;
    private String studentId;
    LoginHistoryRecyclerViewAdapter adapter;
    FrameLayout progressOverlay;
    TextView tvMessage;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        for (int i = 0; i < 15; i++) {
            Log log = new Log();
            log.setTime(com.google.firebase.Timestamp.now());
            log.setDevice("Device " + i);
            logs.add(log);
        }

        View view = inflater.inflate(R.layout.fragment_login_history, container, false);
        RecyclerView recyclerView = view.findViewById(R.id.EmployeeRecyclerView);
        progressOverlay = requireActivity().findViewById(R.id.progress_overlay);
        tvMessage = view.findViewById(R.id.tvMessage);
        btnHome = view.findViewById(R.id.btnHome);
        btnHome.setOnClickListener(v -> {
            recyclerView.scrollToPosition(0);
        });


        adapter = new LoginHistoryRecyclerViewAdapter(logs);
        recyclerView.setAdapter(adapter);
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
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

        // Nhận dữ liệu từ fragment trước
        assert getArguments() != null;
        studentId = getArguments().getString("studentId");

        return view;
    }

    @Override
    public void onViewCreated(@NonNull View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        logViewModel = new LogViewModel();

        // Đăng ký trình lắng nghe sự kiện thay đổi dữ liệu
        Firestore.initLogListener(studentId, requireActivity());

        // Khởi tạo view model
        logViewModel = new ViewModelProvider(requireActivity()).get(LogViewModel.class);

        // Lắng nghe sự kiện thay đổi dữ liệu
        logViewModel.getLogsLiveData().observe(getViewLifecycleOwner(), logs -> {
            // Cập nhật dữ liệu mới
            this.logs = logs;
            // Sắp xếp dữ liệu theo thời gian mới nhất
            this.logs.sort((o1, o2) -> o2.getTime().compareTo(o1.getTime()));
            // Cập nhật adapter
            if (logs.isEmpty()) {
                tvMessage.setVisibility(View.VISIBLE);
            } else {
                tvMessage.setVisibility(View.GONE);
            }
            adapter.setLogs(logs);
            adapter.notifyDataSetChanged();
        });

        // Lấy dữ liệu từ Firestore
        progressOverlay.setVisibility(View.VISIBLE); // Hiện loading overlay
        Firestore.getAllLog(studentId, new Firestore.FirestoreGetAllLogCallback() {
            @Override
            public void onCallback(ArrayList<Log> logs) {
                if (logs != null) {
                    logViewModel.setLogsLiveData(logs);
                    progressOverlay.setVisibility(View.GONE); // Ẩn loading overlay
                } else {
                    progressOverlay.setVisibility(View.GONE); // Ẩn loading overlay
                    Toast.makeText(requireActivity(), "Không tải được dữ liệu", Toast.LENGTH_SHORT).show();
                }
            }
        });
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        Firestore.removeLogListener(studentId);
    }
}
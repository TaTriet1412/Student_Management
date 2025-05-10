package com.example.quanlyhocvien.Fragment;

import static android.app.Activity.RESULT_OK;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.example.quanlyhocvien.Adapter.CertificateRecyclerViewAdapter;
import com.example.quanlyhocvien.Adapter.StudentRecyclerViewAdapter;
import com.example.quanlyhocvien.AddCertificateFragment;
import com.example.quanlyhocvien.AddStudentFragment;
import com.example.quanlyhocvien.CertificateViewModel;
import com.example.quanlyhocvien.CsvDownloader;
import com.example.quanlyhocvien.EditCertificateFragment;
import com.example.quanlyhocvien.FileImportExport;
import com.example.quanlyhocvien.FirebaseStorageManager;
import com.example.quanlyhocvien.Firestore;
import com.example.quanlyhocvien.MainActivity;
import com.example.quanlyhocvien.R;
import com.example.quanlyhocvien.StudentViewModel;
import com.example.quanlyhocvien.object.Certificate;
import com.example.quanlyhocvien.object.SearchAndFilter;
import com.example.quanlyhocvien.object.Student;
import com.example.quanlyhocvien.utils.ActionUtils;
import com.example.quanlyhocvien.utils.GlobalVariables;

import java.io.File;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;


public class CertificateFragment extends Fragment {

    int positionToShowButton = 2;
    List<Certificate> certificates = new LinkedList<>();
    Button btnHome;
    private RecyclerView recyclerView;
    private CertificateRecyclerViewAdapter adapter;
    SearchAndFilter searchAndFilter;
    FrameLayout progressOverlay;
    int role;
    private CertificateViewModel certificateViewModel;
    Student student;
    TextView tvMessage;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_certificate, container, false);
        progressOverlay = requireActivity().findViewById(R.id.progress_overlay);
        recyclerView = view.findViewById(R.id.EmployeeRecyclerView);
        tvMessage = view.findViewById(R.id.tvMessage);
        //searchAndFilter = view.findViewById(R.id.searchAndFilter);


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
        adapter = new CertificateRecyclerViewAdapter(certificates, this, role);
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

        // Nhận thông tin chứng chỉ từ fragment trước
        Bundle bundle = getArguments();
        if (bundle != null) {
            student = (Student) bundle.getSerializable("student");
            if (student == null) {
                // không có thông tin sinh viên
                Toast.makeText(requireContext(), "Không có thông tin học viên", Toast.LENGTH_SHORT).show();
                // Quay lại
                requireActivity().getSupportFragmentManager().popBackStack();
            }
        }

        return view;
    }

    @Override
    public void onViewCreated(@NonNull View view, @NonNull Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        // Đăng ký trình lắng nghe sự kiện ở Firestore
        Firestore.initCertificateSimpleDataListener(student.getId(), requireActivity());

        // Khởi tạo viewmodel
        certificateViewModel = new ViewModelProvider(requireActivity()).get(CertificateViewModel.class);

        // Lắng nghe sự thay đổi trên certificatesLiveData
        certificateViewModel.getCertificatesLiveData().observe(getViewLifecycleOwner(), new Observer<ArrayList<Certificate>>() {
            @Override
            public void onChanged(ArrayList<Certificate> certificates) {
                CertificateFragment.this.certificates = certificates;
                if (certificates.isEmpty()) {
                    // Học viên chưa có chứng chỉ
                    tvMessage.setVisibility(View.VISIBLE);
                } else {
                    // Hiển thị danh sách chứng chỉ
                    tvMessage.setVisibility(View.GONE);
                }
                adapter.setCertificates(certificates);
                adapter.notifyDataSetChanged();
            }
        });

        // Lấy dữ liệu từ Firestore
        progressOverlay.setVisibility(View.VISIBLE); // Hiện loading overlay
        Firestore.getAllCertificateSimpleData(student.getId(), new Firestore.FirestoreGetAllCertificateSimpleDataCallback() {
            @Override
            public void onCallback(ArrayList<Certificate> certificates) {
                if (certificates != null) {
                    certificateViewModel.setCertificatesLiveData(certificates);
                    progressOverlay.setVisibility(View.GONE); // Ẩn loading overlay
                } else {
                    progressOverlay.setVisibility(View.GONE); // Ẩn loading overlay
                    Toast.makeText(getContext(), "Không tải được dữ liệu", Toast.LENGTH_SHORT).show();
                }
            }
        });
    }

    public boolean onContextItemSelected(MenuItem item) {
        // Lấy thông tin của item được chọn
        AdapterView.AdapterContextMenuInfo info = (AdapterView.AdapterContextMenuInfo) item.getMenuInfo();
        Certificate certificate = certificates.get(adapter.positionSelected);
        if (item.getItemId() == R.id.info_certificate) {
            MyHandleClick(certificate);
            return true;
        } else if (item.getItemId() == R.id.delete) {
            AlertDialog.Builder builder = new AlertDialog.Builder(requireContext());
            builder.setTitle("Xác nhận xóa");
            builder.setMessage("Bạn có chắc chắn muốn xóa chứng chỉ này?");
            builder.setPositiveButton("Có", (dialog, which) -> {
                deleteCertificate(student.getId(), certificate);
            });
            builder.setNegativeButton("Không", (dialog, which) -> {
                dialog.dismiss();
            });
            builder.show();
            return true;
        }
        return super.onContextItemSelected(item);
    }



    public void MyHandleClick(Certificate certificate1) {
        Fragment fragment = new EditCertificateFragment();
        Bundle bundle = new Bundle();
        bundle.putSerializable("id", certificate1.getId());
        bundle.putSerializable("student", student);
        fragment.setArguments(bundle);
        ((MainActivity) requireActivity()).loadFragment(fragment);
    }

    public void addClick() {
        Fragment fragment = new AddCertificateFragment();
        Bundle bundle = new Bundle();
        bundle.putString("studentId", student.getId());
        fragment.setArguments(bundle);
        ((MainActivity) requireActivity()).loadFragment(fragment);
    }

    public void changeClick() {
        // Hiển thị dialog chọn thao tác
        AlertDialog.Builder builder = new AlertDialog.Builder(requireContext());
        builder.setItems(new String[]{"Thêm từ tệp (.csv)", "Xuất dữ liệu (.csv)"}, (dialog, which) -> {
            if (which == 0) {
                // Chọn tệp để nhập dữ liệu
                openFilePicker();
            } else if (which == 1) {
                // Chọn thư mục để xuất dữ liệu
                if (ActionUtils.checkWriteExternalPermission(requireContext(), requireActivity())) {
                    mSetContent.launch("certificates_" + student.getId() + ".csv");
                }
            }
        }).create();
        builder.show();
    }

    // xóa chứng chỉ
    private void deleteCertificate(String studentId, Certificate certificate) {
        progressOverlay.setVisibility(View.VISIBLE); // Hiển thi loading
        // Xóa chứng chỉ
        Firestore.deleteCertificate(studentId, certificate.getId(), new Firestore.FirestoreAddCallback() {
            @Override
            public void onCallback(boolean isSuccess) {
                if (isSuccess) {
                    // Xóa thành công
                    // Nếu có ảnh thì xóa ảnh (thất bại hay thaành công không quan trọng)
                    if (certificate.getImageURL() != null && !certificate.getImageURL().isEmpty()) {
                        FirebaseStorageManager.deleteFile(certificate.getImageURL(), "certificateImage/", 10);
                    }
                    // Cập nhật giao diện

                    progressOverlay.setVisibility(View.GONE); // Ẩn loading overlay
                    Toast.makeText(requireContext(), "Đã xóa chứng chỉ", Toast.LENGTH_SHORT).show();
                } else {
                    // Xóa thất bại
                    progressOverlay.setVisibility(View.GONE); // Ẩn loading overlay
                    Toast.makeText(requireContext(), "Có lỗi xảy ra", Toast.LENGTH_SHORT).show();
                }
            }
        });
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        Firestore.removeCertificateSimpleDataListener(student.getId());
    }

    private void openFilePicker() {
        Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
        intent.setType("text/comma-separated-values"); // Chỉ chấp nhận file CSV
        intent.addCategory(Intent.CATEGORY_OPENABLE);
        filePickerLauncher.launch(Intent.createChooser(intent, "Chọn tệp để nhập dữ liệu"));
    }


    private ActivityResultLauncher<Intent> filePickerLauncher = registerForActivityResult(
            new ActivityResultContracts.StartActivityForResult(),
            result -> {
                if (result.getResultCode() == RESULT_OK && result.getData() != null) {
                    Uri fileUri = result.getData().getData();
                    assert fileUri != null;
                    Log.d("FilePicker", "File URI: " + fileUri.toString());
                    // Đọc dữ liệu từ file
                    importCertificate(fileUri);
                }
            }
    );

    private void importCertificate(Uri fileUri) {
        InputStream inputStream = null;
        try {
            inputStream = requireActivity().getContentResolver().openInputStream(fileUri);
            if (inputStream != null) {
                progressOverlay.setVisibility(View.VISIBLE); // Hiện loading overlay
                FileImportExport.readCertificateCSV(inputStream, new FileImportExport.CertificateFileReaderCallback() {
                    @Override
                    public void onCallback(ArrayList<Certificate> certificates) {
                        if (certificates != null) {
                            int size = certificates.size();
                            // Thêm chứng chỉ vào Firestore
                            Firestore.addManyCertificate(student.getId(), certificates, new Firestore.FirestoreAddCallback() {
                                @Override
                                public void onCallback(boolean isSuccess) {
                                    if (isSuccess) {
                                        // Thêm thành công
                                        progressOverlay.setVisibility(View.GONE); // Ẩn loading overlay
                                        String message = "Đã thêm " + certificates.size() + " chứng chỉ";
                                        Toast.makeText(getContext(), message, Toast.LENGTH_SHORT).show();
                                    } else {
                                        // Thêm thất bại
                                        progressOverlay.setVisibility(View.GONE); // Ẩn loading overlay
                                        Toast.makeText(getContext(), "Có lỗi xảy ra", Toast.LENGTH_SHORT).show();
                                    }
                                }
                            });
                        } else {
                            // Thêm thất bại
                            progressOverlay.setVisibility(View.GONE); // Ẩn loading overlay
                            Toast.makeText(getContext(), "Có lỗi xảy ra", Toast.LENGTH_SHORT).show();
                        }
                    }
                });
            } else {
                Toast.makeText(getContext(), "Không thể mở tệp", Toast.LENGTH_SHORT).show();
            }
        } catch (Exception e) {
            Toast.makeText(getContext(), "Có lỗi xảy ra", Toast.LENGTH_SHORT).show();
        }
    }

    private ActivityResultLauncher<String> mSetContent = registerForActivityResult(
            new ActivityResultContracts.CreateDocument(),
            this::exportCertificate);

    private void exportCertificate(Uri uri) {
        if (uri == null) return;    // Trường hợp người dùng nhấn nút hủy
        CsvDownloader csvDownloader1 = new CsvDownloader(requireContext(), student.getId(), uri);
        csvDownloader1.downloadCsvFile();
    }

}
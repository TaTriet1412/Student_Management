package com.example.quanlyhocvien;

import static android.app.Activity.RESULT_OK;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.PickVisualMediaRequest;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AlertDialog;
import androidx.core.app.ActivityCompat;
import androidx.fragment.app.Fragment;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.quanlyhocvien.Adapter.StudentRecyclerViewAdapter;
import com.example.quanlyhocvien.Fragment.CertificateFragment;
import com.example.quanlyhocvien.object.SearchAndFilter;
import com.example.quanlyhocvien.object.Student;
import com.example.quanlyhocvien.utils.ActionUtils;
import com.example.quanlyhocvien.utils.GlobalVariables;
import com.example.quanlyhocvien.utils.SearchAndFilterListener;

import java.io.File;
import java.io.InputStream;
import java.io.OutputStreamWriter;
import java.security.Permission;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;


public class StudentFragment extends Fragment  {
    private static final int REQUEST_CODE_READ_EXTERNAL_STORAGE = 1;
    // chỉnh cái biến này để thay đổi vị trí hiện tại tính từ 1
    int positionToShowButton = 2;
    List<Student> students = new LinkedList<>();
    Button btnHome;
    ArrayList<Student> filteredStudents;
    RecyclerView recyclerView;
    StudentRecyclerViewAdapter adapter;
    SearchAndFilter searchAndFilter;
    FrameLayout progressOverlay;
    int role;
    private StudentViewModel studentViewModel;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.fragment_student, container, false);
        progressOverlay = requireActivity().findViewById(R.id.progress_overlay);
        recyclerView = view.findViewById(R.id.EmployeeRecyclerView);
        searchAndFilter = view.findViewById(R.id.searchAndFilter);
        btnHome = view.findViewById(R.id.btnHome);
        btnHome.setOnClickListener(v -> {
            recyclerView.scrollToPosition(0);
        });

        role = GlobalVariables.getInstance().getCurrentAccount().getRole();
        // Xử lý sự kiện khi nhấn vào nút tìm kiếm
        searchAndFilter.addSearchAndFilterListeners(new SearchAndFilterListener() {
            @Override
            public void onSearchTriggered(String content, boolean sortName, boolean sortAge, int gender) {
                search_filter();
            }

            @Override
            public void onFilterChanged(String content, boolean sortName, boolean sortAge, int gender) {
                search_filter();
            }

            @Override
            public void onFilterReset(String content, boolean sortName, boolean sortAge, int gender) {
                search_filter();
            }
        });

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

        adapter = new StudentRecyclerViewAdapter(students, this, role);
        recyclerView.setAdapter(adapter);
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        return view;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        //Dăng ký trình lắng nghe sự kiện ở Firestore
        Firestore.initStudentSimpleDataListener(requireActivity());

        // Khởi tạo viewmodel
        studentViewModel = new ViewModelProvider(requireActivity()).get(StudentViewModel.class);

        // Lắng nghe sự thay đổi trên studentsLiveData
        studentViewModel.getStudentsLiveData().observe(getViewLifecycleOwner(), new Observer<ArrayList<Student>>() {
            @Override
            public void onChanged(ArrayList<Student> students) {
                StudentFragment.this.students = students;
                search_filter();
            }
        });

        // Lấy dữ liệu từ Firestore
        progressOverlay.setVisibility(View.VISIBLE); // Hiện loading overlay
        Firestore.getAllStudentSimpleData(new Firestore.FirestoreGetAllStudentSimpleDataCallback() {
            @Override
            public void onCallback(ArrayList<Student> students) {
                if (students != null) {
                    studentViewModel.setStudentsLiveData(students);
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
        Student student = filteredStudents.get(adapter.positionSelected);
        if (item.getItemId() == R.id.infoStudent) {
            MyHandleClick();
            return true;
        } else if (item.getItemId() == R.id.certificate) {
            Fragment fragment = new CertificateFragment();
            Bundle bundle = new Bundle();
            bundle.putSerializable("student", student);
            fragment.setArguments(bundle);
            ((MainActivity) requireActivity()).loadFragment(fragment);
            return true;
        } else {
            // Xóa học viên
            // Hiện dialog xác nhận xóa
            AlertDialog.Builder builder = new AlertDialog.Builder(requireContext());
            builder.setTitle("Xác nhận xóa");
            builder.setMessage("Bạn có chắc chắn muốn xóa học viên này không?");
            builder.setPositiveButton("Có", (dialog, which) -> {
                // Xóa học viên
                progressOverlay.setVisibility(View.VISIBLE);// Hiện loading overlay
                Firestore.deleteStudent(student.getId(), new Firestore.FirestoreAddCallback() {
                    @Override
                    public void onCallback(boolean isSuccess) {
                        if (isSuccess) {
                            // Xóa thành công
                            // Nếu có ảnh thì xóa ảnh (thất bại hay thaành công không quan trọng)
                            if (student.getAvatarURL() != null && !student.getAvatarURL().isEmpty()) {
                                FirebaseStorageManager.deleteFile(student.getAvatarURL(), "studentAvatar/", 10);
                            }
                            // Cập nhật giao diện

                            progressOverlay.setVisibility(View.GONE); // Ẩn loading overlay
                            Toast.makeText(getContext(), "Đã xóa học viên", Toast.LENGTH_SHORT).show();
                        } else {
                            // Xóa thất bại
                            progressOverlay.setVisibility(View.GONE); // Ẩn loading overlay
                            Toast.makeText(getContext(), "Có lỗi xảy ra", Toast.LENGTH_SHORT).show();
                        }
                    }
                }
                );
            });
            builder.setNegativeButton("Không", (dialog, which) -> {
                // Không làm gì cả
            });
            builder.show();
            return true;
        }
    }

    // chỗ này xử lí cho sự kiện click vào item trong menu
    public void MyHandleClick() {
        Student student = filteredStudents.get(adapter.positionSelected);
        Fragment fragment = new EditStudentFragment();
        Bundle bundle = new Bundle();
        bundle.putString("id", student.getId());
        fragment.setArguments(bundle);
        ((MainActivity) requireActivity()).loadFragment(fragment);

    }

    //xử lí sự kiện click ở đây
    public void MyHandleClick(Student student) {
        Fragment fragment = new EditStudentFragment();
        Bundle bundle = new Bundle();
        bundle.putString("id", student.getId());
        fragment.setArguments(bundle);
        ((MainActivity) requireActivity()).loadFragment(fragment);
    }

    public void addClick() {
       Fragment fragment = new AddStudentFragment();
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
                if (ActionUtils.checkWriteExternalPermission(requireContext(), requireActivity())){
                    mSetContent.launch("students.csv");
                }
            }
        }).create();
        builder.show();
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        // Xóa trình lắng nghe sự kiện ở Firestore
        Firestore.removeStudentSimpleDataListener();
        Firestore.removeAccountSimpleDataListener();
    }

    // Tìm kiếm học viên, sắp xếp và lọc trong một luồng riêng
    //	Khi Tuổi đang ở cờ giảm dần và Tên đang ở cờ mặc định (a-z) thì sẽ ưu tiên sắp xếp theo tuổi trước.
    //	Các trường hợp còn lại đều sẽ ưu tiên sắp xếp theo tên trước.

    //nếu sortName là true (a-z) còn sortAge là false (9-1) thì ưu tiên sắp xếp theo tuổi trước,
    //nếu sortName là true (a-z) còn sortAge là true (1-9) thì ưu tiên sắp xếp theo tên trước,
    //nếu sortName là false (z-a) còn sortAge là false (9-1) thì ưu tiên sắp xếp theo tên trước
    //nếu sortName là false (z-a) còn sortAge là true (1-9) thì ưu tiên sắp xếp theo tên trước
    private ExecutorService executorService = Executors.newSingleThreadExecutor();
    private void search_filter() {
        String content = searchAndFilter.getContent();
        boolean sortName = searchAndFilter.isSortName();
        boolean sortAge = searchAndFilter.isSortAge();
        int gender = searchAndFilter.getGenderIndex();

        executorService.submit(() -> {
            filteredStudents = new ArrayList<>();
            for (Student student : students) {
                boolean matchesGender = (gender == 3) || (student.getGender() == gender);
                boolean matchesContent = content.isEmpty() || student.getName().toLowerCase().contains(content.toLowerCase()) ||
                        student.getId().toLowerCase().contains(content.toLowerCase());
                if (matchesGender && matchesContent) {
                    filteredStudents.add(student);
                }
            }

            // Sắp xếp danh sách học viên
            filteredStudents.sort((s1, s2) -> {
                if (sortName && !sortAge) {
                    // Ưu tiên sắp xếp theo tuổi trước (9-1)
                    int ageCompare = Integer.compare(s2.toNowAge(), s1.toNowAge());
                    if (ageCompare != 0) {
                        return ageCompare;
                    }
                    return s1.getName().compareToIgnoreCase(s2.getName());
                } else if (sortName && sortAge) {
                    // Ưu tiên sắp xếp theo tên trước (a-z)
                    int nameCompare = s1.getName().compareToIgnoreCase(s2.getName());
                    if (nameCompare != 0) {
                        return nameCompare;
                    }
                    return Integer.compare(s1.toNowAge(), s2.toNowAge());
                } else if (!sortName && !sortAge) {
                    // Ưu tiên sắp xếp theo tên trước (z-a)
                    int nameCompare = s2.getName().compareToIgnoreCase(s1.getName());
                    if (nameCompare != 0) {
                        return nameCompare;
                    }
                    return Integer.compare(s2.toNowAge(), s1.toNowAge());
                } else {
                    // Ưu tiên sắp xếp theo tên trước (z-a)
                    int nameCompare = s2.getName().compareToIgnoreCase(s1.getName());
                    if (nameCompare != 0) {
                        return nameCompare;
                    }
                    return Integer.compare(s1.toNowAge(), s2.toNowAge());
                }
            });

            requireActivity().runOnUiThread(() -> {
                adapter.setStudents(filteredStudents);
                //adapter.notifyDataSetChanged();
//                adapter = new StudentRecyclerViewAdapter(filteredStudents, this, role);
//                recyclerView.setAdapter(adapter);
//                recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
            });
        });
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
                    importStudent(fileUri);
                }
            }
    );

    private void importStudent(Uri fileUri) {
        InputStream inputStream = null;
        try {
            inputStream = requireActivity().getContentResolver().openInputStream(fileUri);
            if (inputStream != null) {
                progressOverlay.setVisibility(View.VISIBLE); // Hiện loading overlay
                FileImportExport.readCSV(inputStream, new FileImportExport.StudentFileReaderCallback() {
                    @Override
                    public void onCallback(ArrayList<Student> students) {
                        Log.d("FileImportExport", "Read " + students.size() + " students");
                        // Thêm học viên vào Firestore
                        Firestore.addManyStudent(students, new Firestore.FirestoreAddCallback() {
                            @Override
                            public void onCallback(boolean isSuccess) {
                                if (isSuccess) {
                                    // Thêm thành công
                                    progressOverlay.setVisibility(View.GONE); // Ẩn loading overlay
                                    String message = "Đã thêm " + students.size() + " học viên";
                                    Toast.makeText(getContext(), message, Toast.LENGTH_SHORT).show();
                                } else {
                                    // Thêm thất bại
                                    progressOverlay.setVisibility(View.GONE); // Ẩn loading overlay
                                    Toast.makeText(getContext(), "Có lỗi xảy ra", Toast.LENGTH_SHORT).show();
                                }
                            }
                        });
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
            this::exportStudent);

    private void exportStudent(Uri uri) {
        if (uri == null) return;    // Trường hợp người dùng nhấn nút hủy
        CsvDownloader csvDownloader1 = new CsvDownloader(requireContext(), null, uri);
        csvDownloader1.downloadCsvFile();
    }
}
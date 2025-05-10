package com.example.quanlyhocvien;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AutoCompleteTextView;
import android.widget.FrameLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.result.ActivityResult;
import androidx.activity.result.ActivityResultCallback;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.PickVisualMediaRequest;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.widget.AppCompatButton;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.example.quanlyhocvien.Fragment.CertificateFragment;
import com.example.quanlyhocvien.object.Student;
import com.example.quanlyhocvien.utils.ActionUtils;
import com.example.quanlyhocvien.utils.GlobalVariables;
import com.google.android.gms.tasks.Task;
import com.google.android.gms.tasks.TaskCompletionSource;
import com.google.android.gms.tasks.Tasks;
import com.google.android.material.imageview.ShapeableImageView;
import com.google.android.material.textfield.TextInputEditText;
import com.google.firebase.Timestamp;

public class EditStudentFragment extends Fragment implements View.OnClickListener {
    FrameLayout progressOverlay;
    ShapeableImageView imgAvatar;
    TextInputEditText edtDisplayName, edtDateOfBirth, edtEmail, edtPhoneNumber, edtAddress, edtID;
    AutoCompleteTextView autoGender;
    AppCompatButton btnSave, btnCertification, btnCancel;
    TextView tvDelete;
    Student currentStudent;
    Boolean imageChanged = false;
    byte[] imageByte = null; // Chỉ dùng khi đổi ảnh
    Uri imageUri; // Chỉ dùng khi đổi ảnh

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_edit_student, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        progressOverlay = requireActivity().findViewById(R.id.progress_overlay);
        imgAvatar = view.findViewById(R.id.imgAvatar);
        autoGender = view.findViewById(R.id.autoGender);
        edtID = view.findViewById(R.id.edtID);
        edtDisplayName = view.findViewById(R.id.edtDisplayName);
        edtDateOfBirth = view.findViewById(R.id.edtDateOfBirth);
        edtEmail = view.findViewById(R.id.edtEmail);
        edtPhoneNumber = view.findViewById(R.id.edtPhoneNumber);
        edtAddress = view.findViewById(R.id.edtAddress);
        btnSave = view.findViewById(R.id.btnSave);
        btnCertification = view.findViewById(R.id.btnCertification);
        btnCancel = view.findViewById(R.id.btnCancel);
        tvDelete = view.findViewById(R.id.tvDelete);
        edtID.setEnabled(false);
        imgAvatar.setOnClickListener(this);
        btnSave.setOnClickListener(this);
        tvDelete.setOnClickListener(this);
        btnCancel.setOnClickListener(this);
        btnCertification.setOnClickListener(this);
        ActionUtils.setupPicker(this, autoGender, new AutoCompleteTextView(getContext()), new AutoCompleteTextView(requireContext()));
        edtDisplayName.requestFocus();
        currentStudent = new Student();

        // Đặt icon cho button chứng chỉ
        Drawable icon = ContextCompat.getDrawable(requireContext(), R.drawable.ic_chevron_right);
        btnCertification.setCompoundDrawablesWithIntrinsicBounds(null, null, icon, null);

        progressOverlay.setVisibility(View.VISIBLE); // Hiển thị loading overlay

        // Nhận dữ liệu từ fragment StudentFragment
        Bundle bundle = getArguments();
        if (bundle != null) {
            currentStudent.setId(bundle.getString("id"));
            if (currentStudent.getId() != null) {
                // Lấy thông tin học viên từ database
                loadStudentData();
            }
        }

        checkRole();
    }

    @Override
    public void onClick(View view) {
        if (view == imgAvatar) {
            // Chọn ảnh đại diện
            // Hiển thị context menu chọn ảnh từ thư viện hoặc chụp ảnh
            AlertDialog.Builder builder = new AlertDialog.Builder(requireContext());
            builder.setItems(new String[]{"Xem ảnh", "Chọn ảnh từ thư viện", "Chụp ảnh"}, (dialog, which) -> {
                if (which == 0) {
                    // Xem ảnh
//                            Intent intent = new Intent(getActivity(), ViewImageActivity.class);
//                            intent.putExtra("image", imgAvatar.getDrawable());
//                            startActivity(intent);
                } else if (which == 1) {
                    // Chọn ảnh từ thư viện
                    pickMedia.launch(new PickVisualMediaRequest.Builder()
                            .setMediaType(ActivityResultContracts.PickVisualMedia.ImageOnly.INSTANCE)
                            .build());
                } else {
                    // Chụp ảnh
                    Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
                    myResultLuncherCamera.launch(intent);
                }
            }).create();
            builder.show();
        } else if (view == btnSave) {
            // Lưu thông tin học viên
            saveStudent();
        } else if (view == btnCertification) {
            // Chuyển sang fragment chứng chỉ
            Fragment fragment = new CertificateFragment();
            Bundle bundle = new Bundle();
            bundle.putSerializable("student", currentStudent);
            fragment.setArguments(bundle);
            ((MainActivity) requireActivity()).loadFragment(fragment);
        } else if (view == btnCancel) {
            // Quay lại StudentFragment
            ((MainActivity) requireActivity()).loadFragment(new StudentFragment());
        } else if (view == tvDelete) {
            // Xóa học viên
            AlertDialog.Builder builder = new AlertDialog.Builder(requireContext());
            builder.setTitle("Xác nhận xóa");
            builder.setMessage("Bạn có chắc chắn muốn xóa học viên này không?");
            builder.setPositiveButton("Có", (dialog, which) -> {
                // Xóa học viên
                progressOverlay.setVisibility(View.VISIBLE); // Hiện loading overlay
                Firestore.deleteStudent(currentStudent.getId(), new Firestore.FirestoreAddCallback() {
                            @Override
                            public void onCallback(boolean isSuccess) {
                                if (isSuccess) {
                                    // Xóa thành công
                                    // Nếu có ảnh thì xóa ảnh (thất bại hay thaành công không quan trọng)
                                    if (currentStudent.getAvatarURL() != null && !currentStudent.getAvatarURL().isEmpty()) {
                                        FirebaseStorageManager.deleteFile(currentStudent.getAvatarURL(), "studentAvatar/", 10);
                                    }
                                    progressOverlay.setVisibility(View.GONE); // Ẩn loading overlay
                                    // Xóa fragment hiện tại và quay lại fragment trước đó
                                    requireActivity().getSupportFragmentManager().popBackStack();
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
        }
    }

    private void checkRole() {
        int role = GlobalVariables.getInstance().getCurrentAccount().getRole();
        if (role == 2) {
            // Employee
            edtDisplayName.setEnabled(false);
            edtDateOfBirth.setEnabled(false);
            edtEmail.setEnabled(false);
            edtPhoneNumber.setEnabled(false);
            autoGender.setEnabled(false);
            edtAddress.setEnabled(false);
            btnSave.setVisibility(View.GONE);
            btnCancel.setVisibility(View.GONE);
            tvDelete.setVisibility(View.GONE);
            imgAvatar.setEnabled(false);
        }
    }

    // Lấy thông tin học viên từ database
    private void loadStudentData() {
        Firestore.getStudent(currentStudent.getId(), new Firestore.FirestoreGetStudentCallback() {
            @Override
            public void onCallback(Student student) {
                if (student != null) {
                    // Thành công
                    student.setId(currentStudent.getId());
                    currentStudent = student;
                    edtID.setText(student.getId());
                    edtDisplayName.setText(student.getName());
                    edtDateOfBirth.setText(student.dateOfBirthString());
                    edtEmail.setText(student.getEmail());
                    edtPhoneNumber.setText(student.getPhoneNumber());
                    edtAddress.setText(student.getAddress());
                    autoGender.setText(student.genderToString(), false);

                    if (student.getAvatarURL() == null || student.getAvatarURL().isEmpty()) {
                        // Không có ảnh đại diện
                        progressOverlay.setVisibility(View.GONE); // Ẩn loading overlay
                        return;
                    } else {
                        // Lấy ảnh đại diện từ firebase storage
                        FirebaseStorageManager.downloadFile(currentStudent.getAvatarURL(), "studentAvatar/", 10, new FirebaseStorageManager.DownloadFileCallback() {
                            @Override
                            public void onCallback(byte[] bytes) {
                                if (bytes != null) {
                                    // Hiển thị ảnh
                                    Bitmap bitmap = BitmapFactory.decodeByteArray(bytes, 0, bytes.length);
                                    imgAvatar.setImageBitmap(bitmap);
                                    progressOverlay.setVisibility(View.GONE); // Ẩn loading overlay
                                } else {
                                    // Lỗi
                                    progressOverlay.setVisibility(View.GONE); // Ẩn loading overlay
                                    Toast.makeText(requireContext(), "Không tải được ảnh", Toast.LENGTH_SHORT).show();
                                }
                            }
                        });
                    }
                } else {
                    // Lỗi
                    progressOverlay.setVisibility(View.GONE); // Ẩn loading overlay
                    Toast.makeText(requireContext(), "Có lỗi xảy ra", Toast.LENGTH_SHORT).show();
                    // Quay lại StudentFragment
                    ((MainActivity) requireActivity()).loadFragment(new StudentFragment());
                }
            }
        });
    }

    private void saveStudent() {
        // Lấy thông tin học viên từ ui
        String name = edtDisplayName.getText().toString();
        String dateOfBirth = edtDateOfBirth.getText().toString();
        String email = edtEmail.getText().toString();
        String phoneNumber = edtPhoneNumber.getText().toString();
        String address = edtAddress.getText().toString();
        String gender = autoGender.getText().toString();

        // Kiểm tra có trường nào trống không
        if (name.isEmpty()) {
            edtDisplayName.setError(requireContext().getString(R.string.display_name_empty));
            edtDisplayName.requestFocus();
            return;
        } else if (dateOfBirth.isEmpty()) {
            edtDateOfBirth.setError(requireContext().getString(R.string.date_of_birth_empty));
            edtDateOfBirth.requestFocus();
            return;
        } else if (email.isEmpty()) {
            edtEmail.setError(requireContext().getString(R.string.email_empty));
            edtEmail.requestFocus();
            return;
        } else if (!isValidEmail(email)) {
            edtEmail.setError(requireContext().getString(R.string.email_wrong_format));
            edtEmail.requestFocus();
            return;
        } else if (phoneNumber.isEmpty()) {
            edtPhoneNumber.setError(requireContext().getString(R.string.phone_empty));
            edtPhoneNumber.requestFocus();
            return;
        } else if (address.isEmpty()) {
            edtAddress.setError(requireContext().getString(R.string.address_empty));
            edtAddress.requestFocus();
            return;
        }

        // Kiểm tra định dạng ngày sinh thành Timestamp
        Timestamp dateOfBirthTimestamp = AddStudentFragment.convertStringToTimestamp(dateOfBirth);
        if (dateOfBirthTimestamp == null) {
            edtDateOfBirth.setError(requireContext().getString(R.string.date_wrong_format));
            edtDateOfBirth.requestFocus();
            return;
        }

        // Chuyển đổi giới tính
        int genderInt;
        if (gender.equals(requireContext().getString(R.string.male))) {
            genderInt = 0;
        } else if (gender.equals(requireContext().getString(R.string.female))) {
            genderInt = 1;
        } else {
            genderInt = 2;
        }

        // Kiểm tra có thay đổi dữ liệu không
        if (
                name.equals(currentStudent.getName())
                && dateOfBirthTimestamp.equals(currentStudent.getDateOfBirth())
                && email.equals(currentStudent.getEmail())
                && phoneNumber.equals(currentStudent.getPhoneNumber())
                && address.equals(currentStudent.getAddress())
                && genderInt == currentStudent.getGender()) {
            if (!imageChanged) {
                // Cũng không thay đổi ảnh
                // Quay lại StudentFragment
                ((MainActivity) requireActivity()).loadFragment(new StudentFragment());
                return;
            } else {
                // Chỉ thay đổi ảnh
                progressOverlay.setVisibility(View.VISIBLE); // Hiển thị loading overlay
                updateAvatar();
            }
        } else {
            // Có thay đổi
            currentStudent.setName(name);
            currentStudent.setDateOfBirth(dateOfBirthTimestamp);
            currentStudent.setEmail(email);
            currentStudent.setPhoneNumber(phoneNumber);
            currentStudent.setAddress(address);
            currentStudent.setGender(genderInt);

            progressOverlay.setVisibility(View.VISIBLE); // Hiển thị loading overlay
            updateStudentAndAvatar();
        }
    }

    // Dùng khi thay đổi thông tin học viên và ảnh
    private void updateStudentAndAvatar() {
        TaskCompletionSource<Void> updateStudentTask = new TaskCompletionSource<>();
        TaskCompletionSource<Void> updateAvatarTask = new TaskCompletionSource<>();

        // Cập nhật thông tin học viên
        Firestore.addStudent(currentStudent, new Firestore.FirestoreAddCallback() {
            @Override
            public void onCallback(boolean isSuccess) {
                if (isSuccess) {
                    updateStudentTask.setResult(null);
                } else {
                    updateStudentTask.setException(new Exception("Update student failed"));
                }
            }
        });

        // Cập nhật ảnh đại diện
        String fileName;
        if (currentStudent.getAvatarURL() == null || currentStudent.getAvatarURL().isEmpty()) {
            // Chưa có ảnh đại diện
            fileName = "img" + System.currentTimeMillis() + (int) (Math.random() * 100000);
        } else {
            fileName = currentStudent.getAvatarURL().substring(currentStudent.getAvatarURL().lastIndexOf("/") + 1);
        }

        if (!imageChanged) {
            // Không thay đổi ảnh
            updateAvatarTask.setResult(null);
        } else if (imageByte != null) {
            // Thay đổi ảnh từ camera
            FirebaseStorageManager.upLoadByteArray(imageByte, fileName, "studentAvatar/", 10).addOnCompleteListener(task -> {
                if (task.isSuccessful() && task.getResult()) {
                    if (currentStudent.getAvatarURL() == null || currentStudent.getAvatarURL().isEmpty()) {
                        // Cập nhật URL ảnh đại diện vào firestore
                        Firestore.updateStudentPhotoURL(currentStudent.getId(), fileName, new Firestore.FirestoreAddCallback() {
                            @Override
                            public void onCallback(boolean isSuccess) {
                                if (isSuccess) {
                                    // Cập nhật ảnh thành công
                                    updateAvatarTask.setResult(null);
                                } else {
                                    // Lỗi
                                    updateAvatarTask.setException(new Exception("Update avatar failed"));
                                }
                            }
                        });
                    } else {
                        updateAvatarTask.setResult(null);
                    }
                } else {
                    updateAvatarTask.setException(new Exception("Update avatar failed"));
                }
            });
        } else {
            // Thay đổi ảnh từ thư viện
            FirebaseStorageManager.uploadFile(imageUri, fileName, "studentAvatar/", 10).addOnCompleteListener(task -> {
                if (task.isSuccessful() && task.getResult()) {
                    // Cập nhật URL ảnh đại diện vào firestore
                    Firestore.updateStudentPhotoURL(currentStudent.getId(), fileName, new Firestore.FirestoreAddCallback() {
                        @Override
                        public void onCallback(boolean isSuccess) {
                            if (isSuccess) {
                                // Cập nhật ảnh thành công
                                updateAvatarTask.setResult(null);
                            } else {
                                // Lỗi
                                updateAvatarTask.setException(new Exception("Update avatar failed"));
                            }
                        }
                    });
                } else {
                    updateAvatarTask.setException(new Exception("Update avatar failed"));
                }
            });
        }

        // Kết hợp hai Task và xử lý kết quả
        Tasks.whenAllComplete(updateStudentTask.getTask(), updateAvatarTask.getTask()).addOnCompleteListener(task -> {
            progressOverlay.setVisibility(View.GONE); // Ẩn loading overlay
            boolean isStudentUpdateSuccessful = updateStudentTask.getTask().isSuccessful();
            boolean isAvatarUpdateSuccessful = updateAvatarTask.getTask().isSuccessful();

            if (isStudentUpdateSuccessful && isAvatarUpdateSuccessful) {
                Toast.makeText(requireContext(), "Cập nhật thành công", Toast.LENGTH_SHORT).show();
            } else {
                String message = "";
                if (!isStudentUpdateSuccessful) {
                    message += "cập nhật thông tin (không phải ảnh) thất bại\n";
                    message += "câp nhật ảnh thành công\n";
                }
                if (!isAvatarUpdateSuccessful) {
                    message += "cập nhật ảnh thất bại\n";
                    message += "cập nhật thông tin (không phải ảnh) thành công\n";
                }

                AlertDialog.Builder builder = new AlertDialog.Builder(requireContext());
                builder.setTitle("Lỗi")
                        .setMessage(message)
                        .setPositiveButton("OK", null)
                        .show();
            }
        });
    }

    // Dùng khi chỉ thay đổi ảnh
    private void updateAvatar() {
        String fileName;
        if (currentStudent.getAvatarURL() == null || currentStudent.getAvatarURL().isEmpty()) {
            // Chưa có ảnh đại diện
            fileName = "img" + System.currentTimeMillis() + (int) (Math.random() * 100000);
        } else {
            fileName = currentStudent.getAvatarURL().substring(currentStudent.getAvatarURL().lastIndexOf("/") + 1);
        }

        Task<Boolean> uploadTask;

        if (imageByte != null) {
            uploadTask = FirebaseStorageManager.upLoadByteArray(imageByte, fileName, "studentAvatar/", 10);
        } else {
            uploadTask = FirebaseStorageManager.uploadFile(imageUri, fileName, "studentAvatar/", 10);
        }

        uploadTask.addOnCompleteListener(task -> {
            if (task.isSuccessful() && task.getResult()) {
                if (currentStudent.getAvatarURL() == null || currentStudent.getAvatarURL().isEmpty()) {
                    // Cập nhật URL ảnh đại diện vào firestore
                    aupdateAvaterURLToFirestore(fileName);
                } else {
                    progressOverlay.setVisibility(View.GONE); // Ẩn loading overlay
                    Toast.makeText(requireContext(), "Cập nhật ảnh thành công", Toast.LENGTH_SHORT).show();
                }
            } else {
                progressOverlay.setVisibility(View.GONE); // Ẩn loading overlay
                Toast.makeText(requireContext(), "Cập nhật ảnh thất bại", Toast.LENGTH_SHORT).show();
            }
        });
    }

    // Cập nhật URL ảnh đại diện vào firestore
    private void aupdateAvaterURLToFirestore(String fileName) {
        Firestore.updateStudentPhotoURL(currentStudent.getId(), fileName, new Firestore.FirestoreAddCallback() {
            @Override
            public void onCallback(boolean isSuccess) {
                if (isSuccess) {
                    // Cập nhật ảnh thành công
                    progressOverlay.setVisibility(View.GONE); // Ẩn loading overlay
                    Toast.makeText(requireContext(), "Cập nhật ảnh thành công", Toast.LENGTH_SHORT).show();
                } else {
                    progressOverlay.setVisibility(View.GONE); // Ẩn loading overlay
                    Toast.makeText(requireContext(), "Cập nhật ảnh thất bại", Toast.LENGTH_SHORT).show();
                }
            }
        });
    }

    // Chọn ảnh từ thư viện (không cần cấp quyền)
    ActivityResultLauncher<PickVisualMediaRequest> pickMedia =
            registerForActivityResult( new ActivityResultContracts.PickVisualMedia(), uri -> {
                // Gọi lại được gọi sau khi người dùng chọn một mục phương tiện hoặc đóng
                // trình chọn ảnh.
                if (uri != null ) {
                    Log.d( "PhotoPicker" , "URI đã chọn: " + uri);
                    imageUri = uri;
                    imgAvatar.setImageURI(uri);
                    imageChanged = true;
                    imageByte = null;
                } else {
                    Log.d( "PhotoPicker" , "Không có phương tiện nào được chọn" );
                }
            });

    // Chụp ảnh từ camera
    private ActivityResultLauncher<Intent> myResultLuncherCamera = registerForActivityResult(
            new ActivityResultContracts.StartActivityForResult(),
            new ActivityResultCallback<ActivityResult>() {
                @Override
                public void onActivityResult(ActivityResult result) {
                    if (result.getResultCode() == Activity.RESULT_OK && result.getData() != null) {
                        // nhận thumbnail ảnh từ camera
                        Bundle extras = result.getData().getExtras();
                        // Hiển thị ảnh thumbnail
                        if (extras != null && extras.get("data") != null) {
                            Bitmap imageBitmap = (Bitmap) extras.get("data");
                            imgAvatar.setImageBitmap(imageBitmap);

                            // chuyển thumbnail sang byte[]
                            imageByte = FirebaseStorageManager.bitmapToByteArray((Bitmap) extras.get("data"));
                            imageChanged = true;
                        }
                    }
                }
            }
    );

    // Kiểm tra định dạng email
    private boolean isValidEmail(String email) {
        return android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches();
    }
}
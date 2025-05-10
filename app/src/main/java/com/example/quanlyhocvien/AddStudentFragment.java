package com.example.quanlyhocvien;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
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

import com.example.quanlyhocvien.object.Student;
import com.example.quanlyhocvien.utils.ActionUtils;
import com.google.android.material.imageview.ShapeableImageView;
import com.google.android.material.textfield.TextInputEditText;

import java.util.Objects;
import com.google.firebase.Timestamp;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Random;

public class AddStudentFragment extends Fragment implements View.OnClickListener {
    FrameLayout progressOverlay;
    ShapeableImageView imgAvatar;
    TextInputEditText edtDisplayName, edtDateOfBirth, edtEmail, edtPhoneNumber, edtAddress, edtID;
    AutoCompleteTextView autoGender;
    AppCompatButton btnSave;
    Boolean imageChanged = false;
    byte[] imageByte = null;
    Uri imageUri;
    Student student;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_add_student, container, false);
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
        edtID.setEnabled(false);
        imgAvatar.setOnClickListener(this);
        btnSave.setOnClickListener(this);
        ActionUtils.setupPicker(this, autoGender, new AutoCompleteTextView(getContext()), new AutoCompleteTextView(getContext()));
        autoGender.setText(ActionUtils.genders[0], false); // Đặt giá trị mặc định
        edtDisplayName.requestFocus();
        edtID.setText(generateId());
    }

    @Override
    public void onClick(View view) {
        if (view.getId() == R.id.btnSave) {
            // Save student
            String id = Objects.requireNonNull(edtID.getText()).toString();
            String name = Objects.requireNonNull(edtDisplayName.getText()).toString();
            String dateOfBirth = Objects.requireNonNull(edtDateOfBirth.getText()).toString();
            String email = Objects.requireNonNull(edtEmail.getText()).toString();
            String phoneNumber = Objects.requireNonNull(edtPhoneNumber.getText()).toString();
            String address = Objects.requireNonNull(edtAddress.getText()).toString();
            String gender = autoGender.getText().toString();
            int genderInt;
            if (gender.equals(requireContext().getString(R.string.male))) {
                genderInt = 0;
            } else if (gender.equals(requireContext().getString(R.string.female))) {
                genderInt = 1;
            } else {
                genderInt = 2;
            }

            // Chuyển đổi chuỗi ngày sinh thành Timestamp
            if (dateOfBirth.isEmpty()) {
                edtDateOfBirth.setError(requireContext().getString(R.string.date_of_birth_empty));
                edtDateOfBirth.requestFocus();
                return;
            }
            Timestamp dateOfBirthTimestamp = convertStringToTimestamp(dateOfBirth);
            if (dateOfBirthTimestamp == null) {
                edtDateOfBirth.setError(requireContext().getString(R.string.date_wrong_format));
                edtDateOfBirth.requestFocus();
                return;
            }

            // Kiểm tra dữ liệu
            if (name.isEmpty()) {
                edtDisplayName.setError(requireContext().getString(R.string.display_name_empty));
                edtDisplayName.requestFocus();
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

            student = new Student(id, name, dateOfBirthTimestamp, phoneNumber, email, address, genderInt);
            progressOverlay.setVisibility(View.VISIBLE); // Hiển thị loading overlay
            addStudent();
        } else if (view.getId() == R.id.imgAvatar) {
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
        }
    }

    // Tạo id
    private String generateId() {
        return "HV" + System.currentTimeMillis() + (int) (Math.random() * 10);
    }

    // Kiểm tra định dạng email
    private boolean isValidEmail(String email) {
        return android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches();
    }

    // DÙNG Ở NHỀU MƠI
    // Chuyển đổi chuỗi thành Timestamp
    public static Timestamp convertStringToTimestamp(String dateString) {
        SimpleDateFormat dateFormat = new SimpleDateFormat("dd/MM/yyyy");
        try {
            // Chuyển đổi chuỗi thành đối tượng Date
            Date date = dateFormat.parse(dateString);
            // Tạo đối tượng Timestamp từ Date
            return new Timestamp(date);
        } catch (ParseException e) {
            e.printStackTrace();
            return null;
        }
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

    private void addStudent() {
        // Thêm học viên vào Firestore
        Firestore.addStudent(student, new Firestore.FirestoreAddCallback() {
            @Override
            public void onCallback(boolean isSuccess) {
                if (isSuccess) {
                    // Clear data
                    edtID.setText(generateId());
                    edtDisplayName.setText("");
                    edtDateOfBirth.setText("");
                    edtEmail.setText("");
                    edtPhoneNumber.setText("");
                    edtAddress.setText("");
                    autoGender.setText(ActionUtils.genders[0], false);
                    imgAvatar.setImageResource(R.drawable.ic_avatar);
                    // Cập nhật ảnh
                    if (imageChanged) {
                        updateAvatar();
                    } else {
                        progressOverlay.setVisibility(View.GONE); // Ẩn loading overlay
                    }
                    Toast.makeText(requireContext(), "Đã thêm học viên", Toast.LENGTH_SHORT).show();
                } else {
                    progressOverlay.setVisibility(View.GONE); // Ẩn loading overlay
                    Toast.makeText(requireContext(), "Có lỗi xảy ra, vui lòng thử lại", Toast.LENGTH_SHORT).show();
                }
            }
        });
    }

    private void updateAvatar() {
        // Upload ảnh lên Firebase Storage và cập nhật link ảnh vào Firestore
        String fileName = "img" + System.currentTimeMillis() + (int) (Math.random() * 100000);
        if (imageByte != null) {
            // Upload byte[] từ camera lên Firebase Storage
            FirebaseStorageManager.upLoadByteArray(imageByte, fileName, "studentAvatar/", 10).addOnCompleteListener(task -> {
                if (task.isSuccessful() && task.getResult()) {
                    // Upload ảnh thành công
                    // Cập nhật link ảnh vào Firestore
                    aupdateAvaterURLToFirestore(fileName);
                    progressOverlay.setVisibility(View.GONE); // Ẩn loading overlay
                } else {
                    progressOverlay.setVisibility(View.GONE); // Ẩn loading overlay
                    Toast.makeText(requireContext(), "Lưu ảnh thất bại", Toast.LENGTH_SHORT).show();
                }
            });
        } else {
            // Upload ảnh từ Uri lên Firebase Storage
            FirebaseStorageManager.uploadFile(imageUri, fileName, "studentAvatar/", 10).addOnCompleteListener(task -> {
                if (task.isSuccessful() && task.getResult()) {
                    // Upload ảnh thành công
                    // Cập nhật link ảnh vào Firestore
                    aupdateAvaterURLToFirestore(fileName);
                    progressOverlay.setVisibility(View.GONE); // Ẩn loading overlay
                } else {
                    progressOverlay.setVisibility(View.GONE); // Ẩn loading overlay
                    Toast.makeText(requireContext(), "Lưu ảnh thất bại", Toast.LENGTH_SHORT).show();
                }
            });
        }
    }

    private void aupdateAvaterURLToFirestore(String fileName) {
        Firestore.updateStudentPhotoURL(student.getId(), fileName, new Firestore.FirestoreAddCallback() {
            @Override
            public void onCallback(boolean isSuccess) {
                if (isSuccess) {
                    // Cập nhật ảnh thành công
                } else {
                    progressOverlay.setVisibility(View.GONE); // Ẩn loading overlay
                    Toast.makeText(requireContext(), "Cập nhật ảnh thất bại", Toast.LENGTH_SHORT).show();
                }
            }
        });
    }

}
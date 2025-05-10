package com.example.quanlyhocvien;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Bitmap;

import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;

import androidx.activity.result.ActivityResult;
import androidx.activity.result.ActivityResultCallback;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.PickVisualMediaRequest;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.widget.AppCompatButton;
import androidx.appcompat.widget.AppCompatTextView;
import androidx.fragment.app.Fragment;

import android.provider.MediaStore;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AutoCompleteTextView;
import android.widget.FrameLayout;
import android.widget.Toast;

import com.example.quanlyhocvien.Fragment.CertificateFragment;
import com.example.quanlyhocvien.object.Certificate;
import com.example.quanlyhocvien.object.Student;
import com.example.quanlyhocvien.utils.ActionUtils;
import com.example.quanlyhocvien.utils.GlobalVariables;
import com.google.android.gms.tasks.Task;
import com.google.android.gms.tasks.TaskCompletionSource;
import com.google.android.gms.tasks.Tasks;
import com.google.android.material.imageview.ShapeableImageView;
import com.google.android.material.textfield.TextInputEditText;
import com.google.firebase.Timestamp;

public class EditCertificateFragment extends Fragment implements View.OnClickListener {

    private FrameLayout progressOverlay;
    private TextInputEditText edtID, edtName, edtIssueDate, edtExpiryDate, edtDescription, edtNote;
    private AutoCompleteTextView autoStatus;
    private ShapeableImageView imgCertificate;
    private AppCompatButton btnSave, btnCancel;
    private AppCompatTextView tvDelete;
    private boolean imageChanged = false;
    private byte[] imageByte = null;
    private Uri imageUri;
    private Certificate certificate;
    private Student student;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_edit_certificate, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        initViews(view);
        initListeners();

        progressOverlay.setVisibility(View.VISIBLE); // Hiển thị loading overlay

        // Nhận thông tin chứng chỉ từ fragment trước
        certificate = new Certificate();
        Bundle bundle = getArguments();
        if (bundle != null) {
            certificate.setId(bundle.getString("id"));
            student = (Student) bundle.getSerializable("student");
            if (certificate.getId() != null && student != null) {
                // Load thông tin chứng chỉ
                loadData();
            }
            checkRole();
        }
    }

    private void initViews(View view){
        progressOverlay = requireActivity().findViewById(R.id.progress_overlay);
        edtID = view.findViewById(R.id.edtID);
        edtName = view.findViewById(R.id.edtName);
        edtIssueDate = view.findViewById(R.id.edtIssueDate);
        edtExpiryDate = view.findViewById(R.id.edtExpiryDate);
        edtDescription = view.findViewById(R.id.edtDescription);
        edtNote = view.findViewById(R.id.edtNote);
        autoStatus = view.findViewById(R.id.autoStatus);
        imgCertificate = view.findViewById(R.id.imgCertificate);
        btnSave = view.findViewById(R.id.btnSave);
        btnCancel = view.findViewById(R.id.btnCancel);
        tvDelete = view.findViewById(R.id.tvDelete);
        ActionUtils.setupPicker(this, new AutoCompleteTextView(requireContext()), new AutoCompleteTextView(requireContext()), autoStatus);
    }

    private void initListeners(){
        btnSave.setOnClickListener(this);
        btnCancel.setOnClickListener(this);
        tvDelete.setOnClickListener(this);
        imgCertificate.setOnClickListener(this);
    }

    @Override
    public void onClick(View v) {
        if (v.getId() == R.id.btnSave){
            save();
        } else if (v.getId() == R.id.btnCancel){
            // Quay lại
            requireActivity().getSupportFragmentManager().popBackStack();
        } else if (v.getId() == R.id.tvDelete){
            // Xóa chứng chỉ
            AlertDialog.Builder builder = new AlertDialog.Builder(requireContext());
            builder.setTitle("Xác nhận xóa");
            builder.setMessage("Bạn có chắc chắn muốn xóa chứng chỉ này không?");
            builder.setPositiveButton("Có", (dialog, which) -> {
                // Xóa chứng chỉ
                progressOverlay.setVisibility(View.VISIBLE); // Hiện loading overlay
                Firestore.deleteCertificate(student.getId(), certificate.getId(), new Firestore.FirestoreAddCallback() {
                    @Override
                    public void onCallback(boolean isSuccess) {
                        progressOverlay.setVisibility(View.GONE); // Ẩn loading overlay
                        if (isSuccess) {
                            // Xóa thành công
                            // Nếu có ảnh thì xóa ảnh (thất bại hay thaành công không quan trọng)
                            if (certificate.getImageURL() != null && !certificate.getImageURL().isEmpty()) {
                                FirebaseStorageManager.deleteFile(certificate.getImageURL(), "certificateImage/", 10);
                            }
                            // Quay lại
                            progressOverlay.setVisibility(View.GONE); // Ẩn loading overlay
                            requireActivity().getSupportFragmentManager().popBackStack();
                            Toast.makeText(requireContext(), "Đã xóa chứng chỉ", Toast.LENGTH_SHORT).show();
                        } else {
                            // Xóa thất bại
                            progressOverlay.setVisibility(View.GONE); // Ẩn loading overlay
                            Toast.makeText(requireContext(), "Có lỗi xảy ra", Toast.LENGTH_SHORT).show();
                        }
                    }
                });
            });
            builder.setNegativeButton("Không", (dialog, which) -> {
                // Không làm gì cả
            });
            builder.show();
        } else if (v.getId() == R.id.imgCertificate){
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

    private void save() {
        Timestamp expiryDateTimestamp;
        Timestamp issueDateTimestamp;
        // Lấy thông tin từ form
        String id = edtID.getText().toString();
        String name = edtName.getText().toString();
        String issueDate = edtIssueDate.getText().toString();
        String expiryDate = edtExpiryDate.getText().toString();
        String description = edtDescription.getText().toString();
        String note = edtNote.getText().toString();

        // Chuyển status từ string sang int
        int status;
        if (autoStatus.getText().toString().equals("Chưa hoàn thành")) {
            status = 0;
        } else if (autoStatus.getText().toString().equals("Đã hoàn thành")) {
            status = 1;
        } else {
            status = 2;
        }

        // Kiểm tra thông tin nhập vào
        if (name.isEmpty()){
            edtName.setError(requireContext().getString(R.string.certificate_name_empty));
            edtName.requestFocus();
            return;
        }

        // ===================== Nếu ngày hết hạn không rỗng thì ngày cấp cũng khoogn được rỗng, ngày cấp <= ngày hết hạn =====================
        if (!expiryDate.isEmpty()){
            // Nếu ngày hết hạn không rỗng
            // Chuyển đổi ngày tháng năm từ string sang timestamp
            expiryDateTimestamp = AddStudentFragment.convertStringToTimestamp(expiryDate);
            if (expiryDateTimestamp == null) {
                // Ngày hết hạn không đúng định dạng
                edtExpiryDate.setError(requireContext().getString(R.string.date_wrong_format));
                edtExpiryDate.requestFocus();
                return;
            } else {
                // Ngày hết hạn đúng định dạng
                if (!issueDate.isEmpty()){
                    // Nếu ngày cấp không rỗng
                    // Chuyển đổi ngày tháng năm từ string sang timestamp
                    issueDateTimestamp = AddStudentFragment.convertStringToTimestamp(issueDate);
                    if (issueDateTimestamp == null) {
                        // Ngày cấp không đúng định dạng
                        edtIssueDate.setError(requireContext().getString(R.string.date_wrong_format));
                        edtIssueDate.requestFocus();
                        return;
                    } else {
                        // Ngày cấp đúng định dạng
                        // Ngày cấp phải nhỏ hơn ngày hết hạn
                        if (issueDateTimestamp.compareTo(expiryDateTimestamp) > 0) {
                            edtIssueDate.setError("Ngày cấp phải nhỏ hơn ngày hết hạn");
                            edtIssueDate.requestFocus();
                            return;
                        }
                    }
                } else {
                    // Ngày cấp rỗng
                    edtIssueDate.setError(requireContext().getString(R.string.certificate_issueDate_empty));
                    edtIssueDate.requestFocus();
                    return;
                }
            }
        } else {
            // Ngày hết hạn rỗng
            expiryDateTimestamp = null;
            if (!issueDate.isEmpty()) {
                // Ngày cấp không rỗng
                // Chuyển đổi ngày tháng năm từ string sang timestamp
                issueDateTimestamp = AddStudentFragment.convertStringToTimestamp(issueDate);
                if (issueDateTimestamp == null) {
                    // Ngày cấp không đúng định dạng
                    edtIssueDate.setError(requireContext().getString(R.string.date_wrong_format));
                    edtIssueDate.requestFocus();
                    return;
                }
            } else {
                // Ngày cấp rỗng
                issueDateTimestamp = null;
            }
        }

        // Kểm tra xem có thay đổi thông tin không
        boolean dataNoChange = name.equals(certificate.getName())
                && ((issueDateTimestamp != null && certificate.getIssueDate() != null
                && issueDateTimestamp.equals(certificate.getIssueDate()))
                || (issueDateTimestamp == null && certificate.getIssueDate() == null))
                && ((expiryDateTimestamp != null && certificate.getExpirationDate() != null
                && expiryDateTimestamp.equals(certificate.getExpirationDate()))
                || (expiryDateTimestamp == null && certificate.getExpirationDate() == null))
                && description.equals(certificate.getDesc())
                && note.equals(certificate.getNote())
                && status == certificate.getStatus();

        if (dataNoChange) {
            // Không có thay đổi
            if (imageChanged) {
                // Chỉ có thay đổi ảnh
                // Lưu ảnh
                updateAvatar();
            } else {
                // Không có thay đổi ảnh
                ((MainActivity) requireActivity()).loadFragment(new CertificateFragment());
            }
        } else {
            // Có thay đổi
            // Cập nhật thông tin chứng chỉ
            certificate.setName(name);
            certificate.setIssueDate(issueDateTimestamp);
            certificate.setExpirationDate(expiryDateTimestamp);
            certificate.setDesc(description);
            certificate.setNote(note);
            certificate.setStatus(status);

            // Cập nhật thông tin chứng chỉ
            updateCertificateAndImage();
        }

    }

    // Dùng khi cập nhật thông tin chứng chỉ và ảnh
    private void updateCertificateAndImage() {
        progressOverlay.setVisibility(View.VISIBLE); // Hiển thị loading overlay
        TaskCompletionSource<Void> updateCertificateTask = new TaskCompletionSource<>();
        TaskCompletionSource<Void> updateCertificateImageTask = new TaskCompletionSource<>();

        // Cập nhật thông tin học viên
        Firestore.addCertificate(student.getId(), certificate, new Firestore.FirestoreAddCallback() {
            @Override
            public void onCallback(boolean isSuccess) {
                if (isSuccess) {
                    // Cập nhật thông tin thành công
                    updateCertificateTask.setResult(null);
                } else {
                    // Lỗi
                    updateCertificateTask.setException(new Exception("Update certificate failed"));
                }
            }
        });

        // Cập nhật ảnh đại diện
        String fileName;
        if (certificate.getImageURL() == null || certificate.getImageURL().isEmpty()) {
            // Chưa có ảnh đại diện
            fileName = "img" + System.currentTimeMillis() + (int) (Math.random() * 100000);
        } else {
            fileName = certificate.getImageURL().substring(certificate.getImageURL().lastIndexOf("/") + 1);
        }

        if (!imageChanged) {
            // Không thay đổi ảnh
            updateCertificateImageTask.setResult(null);
        } else if (imageByte != null) {
            // Thay đổi ảnh từ camera
            FirebaseStorageManager.upLoadByteArray(imageByte, fileName, "certificateImage/", 10).addOnCompleteListener(task -> {
                if (task.isSuccessful() && task.getResult()) {
                    if (certificate.getImageURL() == null || certificate.getImageURL().isEmpty()) {
                        // Cập nhật URL ảnh đại diện vào firestore
                        Firestore.updateCertificatePhotoURL(student.getId(), certificate, new Firestore.FirestoreAddCallback() {
                            @Override
                            public void onCallback(boolean isSuccess) {
                                if (isSuccess) {
                                    // Cập nhật ảnh thành công
                                    updateCertificateImageTask.setResult(null);
                                } else {
                                    // Lỗi
                                    updateCertificateImageTask.setException(new Exception("Update avatar failed"));
                                }
                            }
                        });
                    } else {
                        updateCertificateImageTask.setResult(null);
                    }
                } else {
                    updateCertificateImageTask.setException(new Exception("Update avatar failed"));
                }
            });
        } else {
            // Thay đổi ảnh từ thư viện
            FirebaseStorageManager.uploadFile(imageUri, fileName, "certificateImage/", 10).addOnCompleteListener(task -> {
                if (task.isSuccessful() && task.getResult()) {
                    // Cập nhật URL ảnh đại diện vào firestore
                    Firestore.updateCertificatePhotoURL(student.getId(), certificate, new Firestore.FirestoreAddCallback() {
                        @Override
                        public void onCallback(boolean isSuccess) {
                            if (isSuccess) {
                                // Cập nhật ảnh thành công
                                updateCertificateImageTask.setResult(null);
                            } else {
                                // Lỗi
                                updateCertificateImageTask.setException(new Exception("Update avatar failed"));
                            }
                        }
                    });
                } else {
                    updateCertificateImageTask.setException(new Exception("Update avatar failed"));
                }
            });
        }

        // Kết hợp hai Task và xử lý kết quả
        Tasks.whenAllComplete(updateCertificateTask.getTask(), updateCertificateImageTask.getTask()).addOnCompleteListener(task -> {
            progressOverlay.setVisibility(View.GONE); // Ẩn loading overlay
            boolean isStudentUpdateSuccessful = updateCertificateTask.getTask().isSuccessful();
            boolean isAvatarUpdateSuccessful = updateCertificateImageTask.getTask().isSuccessful();

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
        progressOverlay.setVisibility(View.VISIBLE); // Hiển thị loading overlay
        String fileName;
        if (certificate.getImageURL() == null ||certificate.getImageURL().isEmpty()) {
            // Chưa có ảnh đại diện
            fileName = "img" + System.currentTimeMillis() + (int) (Math.random() * 100000);
        } else {
            fileName = certificate.getImageURL().substring(certificate.getImageURL().lastIndexOf("/") + 1);
        }

        Task<Boolean> uploadTask;

        if (imageByte != null) {
            uploadTask = FirebaseStorageManager.upLoadByteArray(imageByte, fileName, "certificateImage/", 10);
        } else {
            uploadTask = FirebaseStorageManager.uploadFile(imageUri, fileName, "certificateImage/", 10);
        }

        uploadTask.addOnCompleteListener(task -> {
            if (task.isSuccessful() && task.getResult()) {
                if (certificate.getImageURL() == null || certificate.getImageURL().isEmpty()) {
                    // Cập nhật URL ảnh đại diện vào firestore
                    certificate.setImageURL(fileName);
                    updateImageToFirestore();
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

    // Cập nhật ảnh vào storage
    private void updateImageToFirestore() {
        Firestore.updateCertificatePhotoURL(student.getId(), certificate, new Firestore.FirestoreAddCallback() {
            @Override
            public void onCallback(boolean isSuccess) {
                progressOverlay.setVisibility(View.GONE); // Ẩn loading overlay
                if (isSuccess) {
                    // Cập nhật ảnh thành công
                    progressOverlay.setVisibility(View.GONE); // Ẩn loading overlay
                    Toast.makeText(requireContext(), "Cập nhật ảnh thành công", Toast.LENGTH_SHORT).show();
                } else {
                    certificate.setImageURL(null);
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
                    imgCertificate.setImageURI(uri);
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
                            imgCertificate.setImageBitmap(imageBitmap);

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

    private void loadData() {
        Firestore.getCertificate(student.getId(), certificate.getId(), new Firestore.FirestoreGetCertificateCallback() {
            @Override
            public void onCallback(Certificate certificate) {
                if (certificate != null) {
                    // Thành công
                    EditCertificateFragment.this.certificate = certificate;
                    edtID.setText(certificate.getId());
                    edtName.setText(certificate.getName());
                    edtDescription.setText(certificate.getDesc());
                    edtNote.setText(certificate.getNote());
                    edtIssueDate.setText(certificate.issueDateToString());
                    edtExpiryDate.setText(certificate.expirationDateToString());
                    autoStatus.setText(certificate.statusToString(), false);

                    // Lấy ảnh
                    if (certificate.getImageURL() == null || certificate.getImageURL().isEmpty()) {
                        // Không có ảnh
                        progressOverlay.setVisibility(View.GONE); // Ẩn loading overlay
                        return;
                    } else {
                        // Có ảnh
                        // Lấy ảnh từ firebase storage
                        FirebaseStorageManager.downloadFile(certificate.getImageURL(), "certificateImage/", 10, new FirebaseStorageManager.DownloadFileCallback() {
                            @Override
                            public void onCallback(byte[] bytes) {
                                if (bytes != null) {
                                    // Hiển thị ảnh
                                    Bitmap bitmap = BitmapFactory.decodeByteArray(bytes, 0, bytes.length);
                                    imgCertificate.setImageBitmap(bitmap);
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
                    // Quay lại
                    ((MainActivity) requireActivity()).loadFragment(new CertificateFragment());
                }
            }
        });
    }

    private void checkRole() {
        int role = GlobalVariables.getInstance().getCurrentAccount().getRole();
        if (role == 2) {
            // Nhân viên
            tvDelete.setVisibility(View.GONE);
            btnSave.setVisibility(View.GONE);
            btnCancel.setVisibility(View.GONE);
            edtID.setEnabled(false);
            edtName.setEnabled(false);
            edtIssueDate.setEnabled(false);
            edtExpiryDate.setEnabled(false);
            edtDescription.setEnabled(false);
            edtNote.setEnabled(false);
            autoStatus.setEnabled(false);
            imgCertificate.setEnabled(false);
        }
    }
}
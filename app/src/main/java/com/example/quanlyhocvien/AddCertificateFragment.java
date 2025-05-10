package com.example.quanlyhocvien;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Bitmap;
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
import com.example.quanlyhocvien.object.SearchAndFilter;
import com.example.quanlyhocvien.object.Student;
import com.example.quanlyhocvien.utils.ActionUtils;
import com.google.android.gms.tasks.Task;
import com.google.android.material.imageview.ShapeableImageView;
import com.google.android.material.textfield.TextInputEditText;
import com.google.firebase.Timestamp;

import java.util.Objects;

public class AddCertificateFragment extends Fragment implements View.OnClickListener {
    private FrameLayout progressOverlay;
    private TextInputEditText edtID, edtName, edtIssueDate, edtExpiryDate, edtDescription, edtNote;
    private AutoCompleteTextView autoStatus;
    private ShapeableImageView imgCertificate;
    private AppCompatButton btnSave, btnCancel;
    private boolean imageChanged = false;
    private byte[] imageByte = null;
    private Uri imageUri;
    private Certificate certificate;
    private String studentId;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_add_certificate, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        // Nhận dữ liệu từ fragment trước
        Bundle bundle = getArguments();
        studentId = Objects.requireNonNull(bundle).getString("studentId");

        initViews(view);
        initListeners();
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
        edtName.requestFocus();
        autoStatus.setText(ActionUtils.statuses[0], false); // Đặt giá trị mặc định
        edtID.setText(generateId()); // Tạo id
        ActionUtils.setupPicker(this, new AutoCompleteTextView(requireContext()), new AutoCompleteTextView(requireContext()), autoStatus);
    }

    private void initListeners(){
        btnSave.setOnClickListener(this);
        btnCancel.setOnClickListener(this);
        imgCertificate.setOnClickListener(this);
    }

    @Override
    public void onClick(View v) {
        if (v.getId() == R.id.btnSave){
            saveStudent();
        } else if (v.getId() == R.id.btnCancel){
            ((MainActivity) requireActivity()).loadFragment(new CertificateFragment());
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

    // Tạo id
    private String generateId() {
        return "CC" + System.currentTimeMillis() + (int) (Math.random() * 10);
    }

    private void saveStudent(){
        Timestamp expiryDateTimestamp;
        Timestamp issueDateTimestamp;
        if (edtName.getText().toString().isEmpty()){
            edtName.setError(requireContext().getString(R.string.certificate_name_empty));
            edtName.requestFocus();
            return;
        }

        // ===================== Nếu ngày hết hạn không rỗng thì ngày cấp cũng khoogn được rỗng, ngày cấp <= ngày hết hạn =====================
        if (!edtExpiryDate.getText().toString().isEmpty()){
            // Nếu ngày hết hạn không rỗng
            // Chuyển đổi ngày tháng năm từ string sang timestamp
            expiryDateTimestamp = AddStudentFragment.convertStringToTimestamp(edtExpiryDate.getText().toString());
            if (expiryDateTimestamp == null) {
                // Ngày hết hạn không đúng định dạng
                edtExpiryDate.setError(requireContext().getString(R.string.date_wrong_format));
                edtExpiryDate.requestFocus();
                return;
            } else {
                // Ngày hết hạn đúng định dạng
                if (!edtIssueDate.getText().toString().isEmpty()){
                    // Nếu ngày cấp không rỗng
                    // Chuyển đổi ngày tháng năm từ string sang timestamp
                    issueDateTimestamp = AddStudentFragment.convertStringToTimestamp(edtIssueDate.getText().toString());
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
            if (!edtIssueDate.getText().toString().isEmpty()) {
                // Ngày cấp không rỗng
                // Chuyển đổi ngày tháng năm từ string sang timestamp
                issueDateTimestamp = AddStudentFragment.convertStringToTimestamp(edtIssueDate.getText().toString());
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

        // Chuyển status từ string sang int
        int status;
        if (autoStatus.getText().toString().equals("Chưa hoàn thành")) {
            status = 0;
        } else if (autoStatus.getText().toString().equals("Đã hoàn thành")) {
            status = 1;
        } else {
            status = 2;
        }

        certificate = new Certificate(edtID.getText().toString(),
                edtName.getText().toString(),
                edtDescription.getText().toString(),
                issueDateTimestamp,
                expiryDateTimestamp,
                status,
                "",
                edtNote.getText().toString());

        addCertificate();
    }

    private void addCertificate(){
        progressOverlay.setVisibility(View.VISIBLE); // Hiển thi loading
        // Thêm chứng chỉ vào firestore
        Firestore.addCertificate(studentId, certificate, new Firestore.FirestoreAddCallback() {
            @Override
            public void onCallback(boolean isSuccess) {
                if (isSuccess) {
                    // Clear form
                    edtID.setText(generateId());
                    edtName.setText("");
                    edtIssueDate.setText("");
                    edtExpiryDate.setText("");
                    edtDescription.setText("");
                    edtNote.setText("");
                    autoStatus.setText(ActionUtils.statuses[0], false);
                    imgCertificate.setImageResource(R.drawable.ic_image_null);
                    edtName.requestFocus();

                    // Thêm ảnh
                    if (imageChanged) {
                        addImage();
                        Toast.makeText(requireContext(), "Thêm chứng chỉ thành công", Toast.LENGTH_SHORT).show();
                    } else {
                        progressOverlay.setVisibility(View.GONE); // Ẩn loading
                        Toast.makeText(requireContext(), "Thêm chứng chỉ thành công", Toast.LENGTH_SHORT).show();
                    }

                } else {
                    progressOverlay.setVisibility(View.GONE); // Ẩn loading
                    Toast.makeText(requireContext(), "Thêm chứng chỉ thất bại", Toast.LENGTH_SHORT).show();
                }
            }
        });
    }

    // Thêm ảnh vào storage và firestore
    private void addImage(){
        // Thêm ảnh vào storage
        String fileName = "img" + System.currentTimeMillis() + (int) (Math.random() * 100000);
        Task<Boolean> uploadTask;

        if (imageByte != null) {
            uploadTask = FirebaseStorageManager.upLoadByteArray(imageByte, fileName, "certificateImage/", 10);
        } else {
            uploadTask = FirebaseStorageManager.uploadFile(imageUri, fileName, "certificateImage/", 10);
        }

        uploadTask.addOnCompleteListener(task -> {
            if (task.isSuccessful() && task.getResult()) {
                // Thành công
                imageByte = null;
                imageUri = null;
                imageChanged = false;
                certificate.setImageURL(fileName);
                // Cập nhật ảnh vào firestore
                updateImageToFirestore();
            } else {
                progressOverlay.setVisibility(View.GONE); // Ẩn loading overlay
                Toast.makeText(requireContext(), "Cập nhật ảnh thất bại", Toast.LENGTH_SHORT).show();
            }
        });
    }

    // Cập nhật ảnh vào storage
    private void updateImageToFirestore() {
        Firestore.updateCertificatePhotoURL(studentId, certificate, new Firestore.FirestoreAddCallback() {
            @Override
            public void onCallback(boolean isSuccess) {
                progressOverlay.setVisibility(View.GONE); // Ẩn loading overlay
                if (isSuccess) {
                    // Thành công, không cần thoogn baos
                } else {
                    Toast.makeText(requireContext(), "Thêm chứng chỉ thất bại", Toast.LENGTH_SHORT).show();
                }
            }
        });
    }
}
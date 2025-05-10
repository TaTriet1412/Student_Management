package com.example.quanlyhocvien;

import android.app.Activity;
import android.content.Intent;
import android.content.res.ColorStateList;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
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

import com.example.quanlyhocvien.object.Account;
import com.example.quanlyhocvien.utils.ActionUtils;
import com.google.android.gms.tasks.Task;
import com.google.android.material.imageview.ShapeableImageView;
import com.google.android.material.textfield.TextInputEditText;

import java.util.Objects;

public class MyProfileFragment extends Fragment implements View.OnClickListener {
    AppCompatButton btnLogout;
    FrameLayout progressOverlay;
    ShapeableImageView imgAvatar;
    TextInputEditText edtDisplayName, edtAge, edtEmail, edtPhoneNumber;
    AutoCompleteTextView autoGender, autoRole;
    AppCompatButton btnSave, btnResetPassword;
    Account myAccount;
    Boolean imageChanged = false;
    byte[] imageByte = null; // Chỉ dùng khi đổi ảnh
    Uri imageUri; // Chỉ dùng khi đổi ảnh

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_my_profile, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        btnLogout = view.findViewById(R.id.btnLogout);
        btnLogout.setOnClickListener(this);
        progressOverlay = requireActivity().findViewById(R.id.progress_overlay);
        imgAvatar = view.findViewById(R.id.imgAvatar);
        autoGender = view.findViewById(R.id.autoGender);
        edtDisplayName = view.findViewById(R.id.edtDisplayName);
        edtAge = view.findViewById(R.id.edtAge);
        edtEmail = view.findViewById(R.id.edtEmail);
        edtPhoneNumber = view.findViewById(R.id.edtPhoneNumber);
        autoRole = view.findViewById(R.id.autoRole);
        btnSave = view.findViewById(R.id.btnSave);
        btnResetPassword = view.findViewById(R.id.btnResetPassword);
        btnSave.setOnClickListener(this);
        btnResetPassword.setOnClickListener(this);
        imgAvatar.setOnClickListener(this);
        ActionUtils.setupPicker(this, autoGender, autoRole, new AutoCompleteTextView(requireContext()));

        // Đặt icon cho button reset mật khẩu và button logout
        Drawable icon = ContextCompat.getDrawable(requireContext(), R.drawable.ic_chevron_right);
        btnResetPassword.setCompoundDrawablesWithIntrinsicBounds(null, null, icon, null);
        btnLogout.setCompoundDrawablesWithIntrinsicBounds(null, null, icon, null);

        // Vô hiệu hóa các trường nhập thông tin
        edtDisplayName.setEnabled(false);
        edtAge.setEnabled(false);
        edtEmail.setEnabled(false);
        edtPhoneNumber.setEnabled(false);
        autoGender.setEnabled(false);
        autoRole.setEnabled(false);
        btnSave.setEnabled(false);

        // Sẽ lấy từ database
        myAccount = new Account();
        myAccount.setUID(FirebaseAuth.getUID());

        progressOverlay.setVisibility(View.VISIBLE); // Hiển thị loading overlay

        // Lấy thông tin tài khoản
        Firestore.getAccount(FirebaseAuth.getUID(), account -> {
            if (account != null) {
                myAccount = account;
                edtDisplayName.setText(myAccount.getDisplayName());
                edtEmail.setText(FirebaseAuth.getEmail());
                autoGender.setText(myAccount.genderToString(), false);
                edtAge.setText(String.valueOf(myAccount.getAge()));
                edtPhoneNumber.setText(myAccount.getPhoneNumber());
                autoRole.setText(myAccount.roleToString(), false);

                // Lấy ảnh đại diện
                if (myAccount.getPhotoUrl() == null || myAccount.getPhotoUrl().isEmpty()) {
                    progressOverlay.setVisibility(View.GONE); // Ẩn loading overlay
                } else {
                    getAvatar();
                }
            } else {
                // Lỗi
                progressOverlay.setVisibility(View.GONE); // Ẩn loading overlay
                Toast.makeText(requireContext(), "Có lỗi xảy ra", Toast.LENGTH_SHORT).show();
                // Quay lại Fragment trước
                requireActivity().getSupportFragmentManager().popBackStack();
            }
        });
    }

    @Override
    public void onClick(View view) {
        if (view.getId() == R.id.btnLogout) {
            // Đăng xuất
            String email = FirebaseAuth.getEmail();
            FirebaseAuth.signOut();
            Intent intent = new Intent(getContext(), SignInActivity.class);
            intent.putExtra("email", email);
            startActivity(intent);
            getActivity().finish();
        } else if (view.getId() == R.id.tvDelete) {
            // Xóa tài khoản
        } else if (view.getId() == R.id.btnResetPassword) {
            // Reset mật khẩu
            String email = FirebaseAuth.getEmail();
            sendEmail(this, progressOverlay, email);
        } else if (view.getId() == R.id.imgAvatar) {
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
        } else if (view.getId() == R.id.btnSave) {
            // Nếu có thay đổi ảnh thì lưu
            if (imageChanged) {
                progressOverlay.setVisibility(View.VISIBLE); // Hiển thị loading overlay
                updateAvatar();
            } else {
                Toast.makeText(requireContext(), "Không có thay đổi nào", Toast.LENGTH_SHORT).show();
            }
        }
    }

    // HÀM ĐƯỢC DÙNG Ở NHIỀU FRAGMENT
    public static void sendEmail(Fragment fragment, FrameLayout progressOverlay, String email) {
        progressOverlay.setVisibility(View.VISIBLE); // Hiển thị loading overlay
        FirebaseAuth.sendResetPasswordEmail(email).addOnCompleteListener(task -> {
            if (task.isSuccessful() && task.getResult()) {
                progressOverlay.setVisibility(View.GONE); // Ẩn loading overlay
                // Xác thực thành công
                Intent intent = new Intent(fragment.requireContext(), ResetPasswordActivity.class);
                fragment.startActivity(intent);
            } else {
                progressOverlay.setVisibility(View.GONE); // Ẩn loading overlay
                // Xác thực thất bại
                AlertDialog.Builder builder = new AlertDialog.Builder(fragment.requireContext());
                builder.setTitle("Lỗi");
                builder.setMessage("Đã xảy ra lỗi khi thực hiện thao tác này. Vui lòng thử lại sau.");
                builder.setPositiveButton("OK", (dialog, which) -> dialog.dismiss());
                builder.show();
            }
        });
    }

    private void updateAvatar() {
        String fileName;
        if (myAccount.getPhotoUrl() == null || myAccount.getPhotoUrl().isEmpty()) {
            // Chưa có ảnh đại diện
            fileName = "img" + System.currentTimeMillis() + (int) (Math.random() * 100000);
        } else {
            fileName = myAccount.getPhotoUrl().substring(myAccount.getPhotoUrl().lastIndexOf("/") + 1);
        }

        Task<Boolean> uploadTask;

        if (imageByte != null) {
            uploadTask = FirebaseStorageManager.upLoadByteArray(imageByte, fileName, "userAvatar/", 10);
        } else {
            uploadTask = FirebaseStorageManager.uploadFile(imageUri, fileName, "userAvatar/", 10);
        }

        uploadTask.addOnCompleteListener(task -> {
            if (task.isSuccessful() && task.getResult()) {
                btnSave.setEnabled(false);
                if (myAccount.getPhotoUrl() == null || myAccount.getPhotoUrl().isEmpty()) {
                    // Cập nhật link ảnh vào Firestore
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

    // Cập nhật url ảnh vào firestore
    private void aupdateAvaterURLToFirestore(String fileName) {
        Firestore.updateAccountPhotoURL(myAccount.getUID(), fileName, new Firestore.FirestoreAddCallback() {
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
                    btnSave.setEnabled(true);
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
                            imageUri = null;
                            btnSave.setEnabled(true);
                        }
                    }
                }
            }
    );

    private void getAvatar() {
        // Lấy ảnh đại diện từ firebase storage
        FirebaseStorageManager.downloadFile(myAccount.getPhotoUrl(), "userAvatar/", 10, new FirebaseStorageManager.DownloadFileCallback() {
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
}
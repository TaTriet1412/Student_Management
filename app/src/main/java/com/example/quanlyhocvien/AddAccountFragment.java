package com.example.quanlyhocvien;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Bitmap;
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
import androidx.fragment.app.Fragment;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.example.quanlyhocvien.object.Account;
import com.example.quanlyhocvien.object.Student;
import com.example.quanlyhocvien.utils.ActionUtils;
import com.google.android.material.imageview.ShapeableImageView;
import com.google.android.material.textfield.TextInputEditText;
import com.google.firebase.FirebaseException;

import java.io.IOException;
import java.util.Objects;
import org.json.JSONObject;

import okhttp3.Callback;
import okhttp3.Call;
import okhttp3.Response;

public class AddAccountFragment extends Fragment implements View.OnClickListener {
    FrameLayout progressOverlay;
    ShapeableImageView imgAvatar;
    TextInputEditText edtDisplayName, edtAge, edtEmail, edtPhoneNumber;
    AutoCompleteTextView autoGender, autoRole;
    AppCompatButton btnAddAccount;
    byte[] imageByte = null;
    Uri imageUri;
    Student student;
    Boolean imageChanged = false;
    Account newAccount;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_add_account, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        progressOverlay = requireActivity().findViewById(R.id.progress_overlay);
        imgAvatar = view.findViewById(R.id.imgAvatar);
        autoGender = view.findViewById(R.id.autoGender);
        edtDisplayName = view.findViewById(R.id.edtDisplayName);
        edtAge = view.findViewById(R.id.edtAge);
        edtEmail = view.findViewById(R.id.edtEmail);
        edtPhoneNumber = view.findViewById(R.id.edtPhoneNumber);
        autoRole = view.findViewById(R.id.autoRole);
        btnAddAccount = view.findViewById(R.id.btnAddAccount);
        ActionUtils.setupPicker(this, autoGender, autoRole, new AutoCompleteTextView(requireContext()));
        edtDisplayName.requestFocus();
        autoRole.setText(ActionUtils.roles[0], false); // Đặt giá trị mặc định
        autoGender.setText(ActionUtils.genders[0], false); // Đặt giá trị mặc định
        btnAddAccount.setOnClickListener(this);
        newAccount = new Account();
        imgAvatar.setOnClickListener(this);
    }

    @Override
    public void onClick(View view) {
        if (view.getId() == R.id.btnAddAccount) {
            String displayName = Objects.requireNonNull(edtDisplayName.getText()).toString();
            String age = Objects.requireNonNull(edtAge.getText()).toString();
            String email = Objects.requireNonNull(edtEmail.getText()).toString();
            String phoneNumber = Objects.requireNonNull(edtPhoneNumber.getText()).toString();
            String gender = Objects.requireNonNull(autoGender.getText()).toString();
            String role = Objects.requireNonNull(autoRole.getText()).toString();

            // Kiểm tra dữ liệu nhập vào
            if (displayName.isEmpty()) {
                edtDisplayName.setError(requireContext().getString(R.string.display_name_empty));
                edtDisplayName.requestFocus();
                return;
            }
            if (age.isEmpty()) {
                edtAge.setError(requireContext().getString(R.string.age_empty));
                edtAge.requestFocus();
                return;
            }
            if (email.isEmpty()) {
                edtEmail.setError(requireContext().getString(R.string.email_empty));
                edtEmail.requestFocus();
                return;
            } else if (!isValidEmail(email)) {
                edtEmail.setError(requireContext().getString(R.string.email_wrong_format));
                edtEmail.requestFocus();
                return;
            }
            if (phoneNumber.isEmpty()) {
                edtPhoneNumber.setError(requireContext().getString(R.string.phone_empty));
                edtPhoneNumber.requestFocus();
                return;
            }

            // Tạo đối tượng Account
            int roleInt, genderInt;
            if (role.equals(getString(R.string.manager))) {
                roleInt = 1;
            } else {
                roleInt = 2;
            }
            if (gender.equals(requireContext().getString(R.string.male))) {
                genderInt = 0;
            } else if (gender.equals(requireContext().getString(R.string.female))) {
                genderInt = 1;
            } else {
                genderInt = 2;
            }

            newAccount.setDisplayName(displayName);
            newAccount.setEmail(Objects.requireNonNull(edtEmail.getText()).toString());
            newAccount.setPhoneNumber(phoneNumber);
            newAccount.setAge(Integer.parseInt(age));
            newAccount.setActiveStatus(true);
            newAccount.setRole(roleInt);
            newAccount.setGender(genderInt);

            // Hiển thị loading overlay
            progressOverlay.setVisibility(View.VISIBLE);

            // Gọi API để tạo người dùng
            ApiClient.createUser(newAccount.getEmail(), randomPassword(), newAccount.getDisplayName(), new ApiClient.ApiResponseCallback() {
                @Override
                public void onSuccess(String response) {
                    // Thành công
                    // Lấy UID từ response
                    try {
                        JSONObject jsonObject = new JSONObject(response);
                        String uid = jsonObject.getString("uid");
                        newAccount.setUID(uid);
                        // Thêm tài khoản vào firestore
                        Firestore.addAccount(newAccount, new Firestore.FirestoreAddCallback() {
                            @Override
                            public void onCallback(boolean isSuccess) {
                                if (isSuccess) {
                                    //Clear form
                                    edtDisplayName.setText("");
                                    edtAge.setText("");
                                    edtEmail.setText("");
                                    edtPhoneNumber.setText("");
                                    edtDisplayName.requestFocus();
                                    autoRole.setText(ActionUtils.roles[0], false); // Đặt giá trị mặc định
                                    autoGender.setText(ActionUtils.genders[0], false); // Đặt giá trị mặc định
                                    imgAvatar.setImageResource(R.drawable.ic_avatar);
                                    // Cập nhật ảnh
                                    if (imageChanged) {
                                        updateAvatar();
                                    } else {
                                        progressOverlay.setVisibility(View.GONE); // Ẩn loading overlay
                                    }
                                    // Hiện dialog thông báo
                                    AlertDialog.Builder builder = new AlertDialog.Builder(requireContext());
                                    builder.setTitle("Thông báo");
                                    builder.setMessage("Thêm tài khoản thành công, mật khẩu mặc định là 123456");
                                    builder.setPositiveButton("OK", (dialog, which) -> dialog.dismiss());
                                    builder.create().show();
                                } else {
                                    // Lỗi tài khoản đã được thêm vào Firebase Auth nhưng không thêm vào Firestore
                                }
                            }
                        });
                    } catch (Exception e) {
                        // Lỗi khi lấy UID
                        progressOverlay.setVisibility(View.GONE); // Ẩn loading overlay
                    }
                }

                @Override
                public void onError(String errorMessage) {
                    // Thất bại
                    if (errorMessage.contains("EMAIL_EXISTS")) {
                        // Lỗi email đã tồn tại
                        edtEmail.setError(requireContext().getString(R.string.account_exist));
                        edtEmail.requestFocus();
                    } else {
                        // Lỗi khác
                        Log.e("AddAccountFragment", "Error: " + errorMessage);
                        Toast.makeText(requireContext(), "Có lỗi xảy ra", Toast.LENGTH_SHORT).show();
                    }
                    progressOverlay.setVisibility(View.GONE); // Ẩn loading overlay
                }
            });
        }
        else if (view.getId() == R.id.imgAvatar) {
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

    // Hàm tạo mật khẩu ngẫu nhiên
    public static String randomPassword() {
//        String SALTCHARS = "ABCDEFGHIJKLMNOPQRSTUVWXYZ1234567890";
//        StringBuilder salt = new StringBuilder();
//        while (salt.length() < 8) {
//            int index = (int) (Math.random() * SALTCHARS.length());
//            salt.append(SALTCHARS.charAt(index));
//        }
//        return salt.toString();
        return "123456";
    }

    // Kiểm tra định dạng email
    private boolean isValidEmail(String email) {
        return android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches();
    }

    private void updateAvatar() {
        // Upload ảnh lên Firebase Storage và cập nhật link ảnh vào Firestore
        String fileName = "img" + System.currentTimeMillis() + (int) (Math.random() * 100000);
        if (imageByte != null) {
            // Upload byte[] từ camera lên Firebase Storage
            FirebaseStorageManager.upLoadByteArray(imageByte, fileName, "userAvatar/", 10).addOnCompleteListener(task -> {
                if (task.isSuccessful() && task.getResult()) {
                    // Upload ảnh thành công
                    // Cập nhật link ảnh vào Firestore
                    aupdateAvaterURLToFirestore(fileName);
                } else {
                    progressOverlay.setVisibility(View.GONE); // Ẩn loading overlay
                    Toast.makeText(requireContext(), "Lưu ảnh thất bại", Toast.LENGTH_SHORT).show();
                }
            });
        } else {
            // Upload ảnh từ Uri lên Firebase Storage
            FirebaseStorageManager.uploadFile(imageUri, fileName, "userAvatar/", 10).addOnCompleteListener(task -> {
                if (task.isSuccessful() && task.getResult()) {
                    // Upload ảnh thành công
                    // Cập nhật link ảnh vào Firestore
                    aupdateAvaterURLToFirestore(fileName);
                } else {
                    progressOverlay.setVisibility(View.GONE); // Ẩn loading overlay
                    Toast.makeText(requireContext(), "Lưu ảnh thất bại", Toast.LENGTH_SHORT).show();
                }
            });
        }
    }

    private void aupdateAvaterURLToFirestore(String fileName) {
        Firestore.updateAccountPhotoURL(newAccount.getUID(), fileName, new Firestore.FirestoreAddCallback() {
            @Override
            public void onCallback(boolean isSuccess) {
                if (isSuccess) {
                    // Cập nhật ảnh thành công
                    progressOverlay.setVisibility(View.GONE); // Ẩn loading overlay
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
}
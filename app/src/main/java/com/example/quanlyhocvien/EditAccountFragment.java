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

import com.example.quanlyhocvien.Fragment.LoginHistoryFragment;
import com.example.quanlyhocvien.object.Account;
import com.example.quanlyhocvien.utils.ActionUtils;
import com.google.android.gms.tasks.Task;
import com.google.android.gms.tasks.TaskCompletionSource;
import com.google.android.gms.tasks.Tasks;
import com.google.android.material.imageview.ShapeableImageView;
import com.google.android.material.textfield.TextInputEditText;
import com.google.firebase.Timestamp;

import java.util.Objects;
import java.util.concurrent.atomic.AtomicReference;

public class EditAccountFragment extends Fragment implements View.OnClickListener {
    FrameLayout progressOverlay;
    ShapeableImageView imgAvatar;
    TextInputEditText edtDisplayName, edtAge, edtEmail, edtPhoneNumber, edtStatus;
    AutoCompleteTextView autoGender, autoRole;
    AppCompatButton btnSave, btnCancel, btnResetPassword, btnLog;
    TextView tvDelete;
    Account currentAccount;
    Boolean imageChanged = false;
    byte[] imageByte = null; // Chỉ dùng khi đổi ảnh
    Uri imageUri; // Chỉ dùng khi đổi ảnh

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_edit_account, container, false);
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
        btnSave = view.findViewById(R.id.btnSave);
        btnCancel = view.findViewById(R.id.btnCancel);
        tvDelete = view.findViewById(R.id.tvDelete);
        btnResetPassword = view.findViewById(R.id.btnResetPassword);
        edtStatus = view.findViewById(R.id.edtStatus);
        edtStatus.setOnClickListener(this);
        btnSave.setOnClickListener(this);
        btnCancel.setOnClickListener(this);
        tvDelete.setOnClickListener(this);
        btnResetPassword.setOnClickListener(this);
        imgAvatar.setOnClickListener(this);
        ActionUtils.setupPicker(this, autoGender, autoRole, new AutoCompleteTextView(requireContext()));
        btnLog = view.findViewById(R.id.btnLog);
        btnLog.setOnClickListener(this);

        // Đặt icon cho button reset mật khẩu và lịch sử đăng nhập
        Drawable icon = ContextCompat.getDrawable(requireContext(), R.drawable.ic_chevron_right);
        btnResetPassword.setCompoundDrawablesWithIntrinsicBounds(null, null, icon, null);
        btnLog.setCompoundDrawablesWithIntrinsicBounds(null, null, icon, null);

        // Nhận dữ liệu từ fragment EmployeeFragment
        progressOverlay.setVisibility(View.VISIBLE); // Hiển thị loading
        currentAccount = new Account();
        Bundle bundle = getArguments();
        if (bundle != null) {
            currentAccount.setUID(bundle.getString("uid"));
            if (currentAccount.getUID() != null) {
                // Lấy thông tin tài khoản từ database
                loadAccountData();
            }
        }
    }

    @Override
    public void onClick(View view) {
        if (view.getId() == R.id.btnSave) {
            // Lưu thông tin
            saveAccount();
        } else if (view.getId() == R.id.btnCancel) {
            // Hủy bỏ, quay lại EmployeeFragment
            ((MainActivity) requireActivity()).loadFragment(new EmployeeFragment());
        } else if (view.getId() == R.id.tvDelete) {
            // Xóa tài khoản
            AlertDialog.Builder builder = new AlertDialog.Builder(requireContext());
            builder.setTitle("Xác nhận xóa");
            builder.setMessage("Bạn có chắc chắn muốn xóa tài khoản này?");
            builder.setPositiveButton("Xóa", (dialog, which) -> {
                dialog.dismiss();
                // Xóa tài khoản
                progressOverlay.setVisibility(View.VISIBLE); // Hiển thị loading
                ApiClient.deleteUser(currentAccount.getUID(), new ApiClient.ApiResponseCallback() {
                    @Override
                    public void onSuccess(String response) {
                        // Thực thi nếu xóa người dùng thành công
                        Firestore.deleteAccount(currentAccount.getUID(), callback -> {}); // Xóa account ở Firestore, không quan trọng kết quả
                        if (currentAccount.getPhotoUrl() != null && !currentAccount.getPhotoUrl().isEmpty()) {
                            // Xóa ảnh đại diện, không quan trọng kết quả
                            FirebaseStorageManager.deleteFile(currentAccount.getPhotoUrl(), "userAvatar/", 10);
                        }
                        progressOverlay.setVisibility(View.GONE); // Ẩn loading
                        ((MainActivity) requireActivity()).loadFragment(new EmployeeFragment());
                        Toast.makeText(requireContext(), "Đã xóa tài khoản", Toast.LENGTH_LONG).show();
                    }

                    @Override
                    public void onError(String errorMessage) {
                        // Xử lý nếu yêu cầu xóa người dùng thất bại
                        progressOverlay.setVisibility(View.GONE); // Ẩn loading
                        Toast.makeText(requireContext(), "Có lỗi xảy ra", Toast.LENGTH_LONG).show();
                    }
                });
            });
            builder.setNegativeButton("Hủy", (dialog, which) -> {
                dialog.dismiss();
            });
            builder.show();
        } else if (view.getId() == R.id.edtStatus) {
            // Chỉnh sửa trạng thái tài khoản
            if (Objects.requireNonNull(edtStatus.getText()).toString().equals("Đang hoạt động")) {
                AlertDialog.Builder builder = new AlertDialog.Builder(requireContext());
                builder.setTitle("Cảnh báo");
                builder.setMessage("Vô hiệu hóa tài khoản sẽ khiến người dùng không thể đăng nhập.\n" +
                        "Bạn có chắc chắn muốn vô hiệu hóa tài khoản này?");
                builder.setPositiveButton("Vô hiệu hóa", (dialog, which) -> {
                    // Vô hiệu hóa tài khoản
                    enable_disable_account(false);
                });
                builder.setNegativeButton("Hủy", (dialog, which) -> {
                    dialog.dismiss();
                });
                builder.show();
            } else {
                AlertDialog.Builder builder = new AlertDialog.Builder(requireContext());
                builder.setTitle("Cảnh báo");
                builder.setMessage("Kích hoạt tài khoản sẽ khiến người dùng có thể đăng nhập.\n" +
                        "Bạn có chắc chắn muốn kích hoạt tài khoản này?");
                builder.setPositiveButton("Kích hoạt", (dialog, which) -> {
                    // Kích hoạt tài khoản
                    enable_disable_account(true);
                });
                builder.setNegativeButton("Hủy", (dialog, which) -> {
                    dialog.dismiss();
                });
                builder.show();
            }
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
        } else if (view.getId() == R.id.btnResetPassword) {
            // Reset mật khẩu
            String email = currentAccount.getEmail();

            // Kiểm tra trạng thái tài khoản
            Boolean activeStatus = Objects.requireNonNull(edtStatus.getText()).toString().equals("Đang hoạt động");
            if (!activeStatus) {
                AlertDialog.Builder builder = new AlertDialog.Builder(requireContext());
                builder.setTitle("Cảnh báo");
                builder.setMessage("Tài khoản này đã bị vô hiệu hóa.\n" +
                        "\nNgười dùng vẫn không thể đăng nhập." +
                        "Bạn có chắc chắn muốn cấp lại mật khẩu?");
                builder.setPositiveButton("Cấp lại", (dialog, which) -> {
                    dialog.dismiss();
                    MyProfileFragment.sendEmail(this, progressOverlay, email);
                });
                builder.setNegativeButton("Hủy", (dialog, which) -> {
                    dialog.dismiss();
                });
                builder.show();
            } else {
                MyProfileFragment.sendEmail(this, progressOverlay, email);
            }
        } else if (view.getId() == R.id.btnLog) {
            // Xem lịch sử đăng nhập
            Fragment fragment = new LoginHistoryFragment();
            Bundle bundle = new Bundle();
            bundle.putString("studentId", currentAccount.getUID());
            fragment.setArguments(bundle);
            ((MainActivity) requireActivity()).loadFragment(fragment);
        }
    }

    private void loadAccountData() {
        Firestore.getAccount(currentAccount.getUID(), account -> {
            if (account != null) {
                currentAccount = account;
                edtDisplayName.setText(account.getDisplayName());
                edtAge.setText(String.valueOf(account.getAge()));
                edtEmail.setText(account.getEmail());
                edtPhoneNumber.setText(account.getPhoneNumber());
                autoGender.setText(account.genderToString(), false);
                autoRole.setText(account.roleToString(), false);
                progressOverlay.setVisibility(View.GONE); // Ẩn loading
                edtStatus.setText(account.getActiveStatus() ? "Đang hoạt động" : "Đã vô hiệu hóa");

                // Nếu role là admin thì không cho phép thay đổi
                if (account.getRole() == 0) {
                    autoRole.setEnabled(false);
                }

                // Đổi màu nền edtStatus
                ColorStateList colorStateList = account.getActiveStatus() ?
                        ColorStateList.valueOf(Color.parseColor("#67EB4F")) : // color primary
                        ColorStateList.valueOf(Color.parseColor("#F5004F")); // color faills
                edtStatus.setBackgroundTintList(colorStateList);

                // Tải ảnh
                if (currentAccount.getPhotoUrl() == null || currentAccount.getPhotoUrl().isEmpty()) {
                    // Không có ảnh đại diện
                    progressOverlay.setVisibility(View.GONE); // Ẩn loading overlay
                    return;
                } else {
                    // Lấy ảnh đại diện từ firebase storage
                    FirebaseStorageManager.downloadFile(currentAccount.getPhotoUrl(), "userAvatar/", 10, new FirebaseStorageManager.DownloadFileCallback() {
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
                progressOverlay.setVisibility(View.GONE); // Ẩn loading
                Toast.makeText(requireContext(), "Có lỗi xảy ra", Toast.LENGTH_SHORT).show();
                ((MainActivity) requireActivity()).loadFragment(new EmployeeFragment());
            }
        });
    }

    private void saveAccount() {
        // Lấy thông tin từ các trường
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

        // Chuyển đổi giới tính, vai trò
        int roleInt, genderInt;
        if (role.equals("Quản lý")) {
            roleInt = 1;
        } else if (role.equals("Nhân viên")) {
            roleInt = 2;
        } else {
            roleInt = 0;
        }

        if (gender.equals(requireContext().getString(R.string.male))) {
            genderInt = 0;
        } else if (gender.equals(requireContext().getString(R.string.female))) {
            genderInt = 1;
        } else {
            genderInt = 2;
        }


        // Kiểm tra có thay đổi dữ liệu không
        if (
                displayName.equals(currentAccount.getDisplayName()) &&
                        age.equals(String.valueOf(currentAccount.getAge())) &&
                        email.equals(currentAccount.getEmail()) &&
                        phoneNumber.equals(currentAccount.getPhoneNumber()) &&
                        genderInt == currentAccount.getGender() &&
                        roleInt == currentAccount.getRole() &&
                        Objects.requireNonNull(edtStatus.getText()).toString().equals(currentAccount.getActiveStatus() ? "Đang hoạt động" : "Đã vô hiệu hóa")) {
            if (!imageChanged) {
                // Cũng không thay đổi ảnh
                // Quay lại EmployeeFragment
                ((MainActivity) requireActivity()).loadFragment(new EmployeeFragment());
                return;
            } else {
                // Chỉ thay đổi ảnh
                progressOverlay.setVisibility(View.VISIBLE); // Hiển thị loading overlay
                updateAvatar();
            }
        } else {
            // Có thay đổi
            String oldEmail = currentAccount.getEmail();
            String oldDisplayName = currentAccount.getDisplayName();
            currentAccount.setDisplayName(displayName);
            currentAccount.setAge(Integer.parseInt(age));
            currentAccount.setEmail(email);
            currentAccount.setPhoneNumber(phoneNumber);
            currentAccount.setGender(genderInt);
            currentAccount.setRole(roleInt);
            currentAccount.setActiveStatus(Objects.requireNonNull(edtStatus.getText()).toString().equals("Đang hoạt động"));

            // Nếu có thay đổi email hoặc displayname
            if (!oldEmail.equals(email) || !oldDisplayName.equals(displayName)) {
                progressOverlay.setVisibility(View.VISIBLE); // Hiển thị loading overlay
                // Cập nhật ở authentication
                ApiClient.updateUser(currentAccount.getUID(), currentAccount.getEmail(), currentAccount.getDisplayName(), new ApiClient.ApiResponseCallback() {
                    @Override
                    public void onSuccess(String response) {
                        // Thực thi nếu cập nhật người dùng thành công
                        updateAccountAndAvatar();
                    }

                    @Override
                    public void onError(String errorMessage) {
                        // Xử lý nếu yêu cầu cập nhật người dùng thất bại
                        progressOverlay.setVisibility(View.GONE); // Ẩn loading
                        Toast.makeText(requireContext(), "Có lỗi xảy ra", Toast.LENGTH_LONG).show();
                    }
                });
            } else {
                progressOverlay.setVisibility(View.VISIBLE); // Hiển thị loading overlay
                updateAccountAndAvatar();
            }
        }
    }

    // Dùng khi thay đổi thông tin học viên và ảnh
    private void updateAccountAndAvatar() {
        TaskCompletionSource<Void> updateAccountTask = new TaskCompletionSource<>();
        TaskCompletionSource<Void> updateAvatarTask = new TaskCompletionSource<>();

        // Cập nhật thông tin học viên
        Firestore.addAccount(currentAccount, new Firestore.FirestoreAddCallback() {
            @Override
            public void onCallback(boolean isSuccess) {
                if (isSuccess) {
                    // Cập nhật thông tin thành công
                    updateAccountTask.setResult(null);
                } else {
                    // Lỗi
                    updateAccountTask.setException(new Exception("Update account failed"));
                }
            }
        });

        // Cập nhật ảnh đại diện
        String fileName;
        if (currentAccount.getPhotoUrl() == null || currentAccount.getPhotoUrl().isEmpty()) {
            // Chưa có ảnh đại diện
            fileName = "img" + System.currentTimeMillis() + (int) (Math.random() * 100000);
        } else {
            fileName = currentAccount.getPhotoUrl().substring(currentAccount.getPhotoUrl().lastIndexOf("/") + 1);
        }

        if (!imageChanged) {
            // Không thay đổi ảnh
            updateAvatarTask.setResult(null);
        } else if (imageByte != null) {
            // Thay đổi ảnh từ camera
            FirebaseStorageManager.upLoadByteArray(imageByte, fileName, "userAvatar/", 10).addOnCompleteListener(task -> {
                if (task.isSuccessful() && task.getResult()) {
                    if (currentAccount.getPhotoUrl() == null || currentAccount.getPhotoUrl().isEmpty()) {
                        // Cập nhật URL ảnh đại diện vào firestore
                        Firestore.updateAccountPhotoURL(currentAccount.getUID(), fileName, new Firestore.FirestoreAddCallback() {
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
            FirebaseStorageManager.uploadFile(imageUri, fileName, "userAvatar/", 10).addOnCompleteListener(task -> {
                if (task.isSuccessful() && task.getResult()) {
                    // Cập nhật URL ảnh đại diện vào firestore
                    Firestore.updateAccountPhotoURL(currentAccount.getUID(), fileName, new Firestore.FirestoreAddCallback() {
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
        Tasks.whenAllComplete(updateAccountTask.getTask(), updateAvatarTask.getTask()).addOnCompleteListener(task -> {
            progressOverlay.setVisibility(View.GONE); // Ẩn loading overlay
            boolean isStudentUpdateSuccessful = updateAccountTask.getTask().isSuccessful();
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
        if (currentAccount.getPhotoUrl() == null || currentAccount.getPhotoUrl().isEmpty()) {
            // Chưa có ảnh đại diện
            fileName = "img" + System.currentTimeMillis() + (int) (Math.random() * 100000);
        } else {
            fileName = currentAccount.getPhotoUrl().substring(currentAccount.getPhotoUrl().lastIndexOf("/") + 1);
        }

        Task<Boolean> uploadTask;

        if (imageByte != null) {
            uploadTask = FirebaseStorageManager.upLoadByteArray(imageByte, fileName, "userAvatar/", 10);
        } else {
            uploadTask = FirebaseStorageManager.uploadFile(imageUri, fileName, "userAvatar/", 10);
        }

        uploadTask.addOnCompleteListener(task -> {
            if (task.isSuccessful() && task.getResult()) {
                if (currentAccount.getPhotoUrl() == null || currentAccount.getPhotoUrl().isEmpty()) {
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

    // Cập nhật url ảnh vào firestore
    private void aupdateAvaterURLToFirestore(String fileName) {
        Firestore.updateAccountPhotoURL(currentAccount.getUID(), fileName, new Firestore.FirestoreAddCallback() {
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

    private void enable_disable_account(boolean activeStatus) {
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

                // Cập nhật trạng thái trên giao diện
                edtStatus.setText(currentAccount.getActiveStatus() ? "Đang hoạt động" : "Đã vô hiệu hóa");
                ColorStateList colorStateList = currentAccount.getActiveStatus() ?
                        ColorStateList.valueOf(Color.parseColor("#67EB4F")) : // color primary
                        ColorStateList.valueOf(Color.parseColor("#F5004F")); // color faills
                edtStatus.setBackgroundTintList(colorStateList);
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
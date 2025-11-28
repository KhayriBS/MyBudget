package tn.esprit.mybudget.ui.auth;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.ViewModelProvider;

import com.google.android.material.chip.Chip;
import com.google.android.material.switchmaterial.SwitchMaterial;
import com.google.android.material.textfield.TextInputEditText;

import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;

import tn.esprit.mybudget.R;
import tn.esprit.mybudget.data.entity.User;
import tn.esprit.mybudget.util.BiometricHelper;
import tn.esprit.mybudget.util.SessionManager;
import tn.esprit.mybudget.util.ValidationHelper;

public class ProfileActivity extends AppCompatActivity {

    private AuthViewModel viewModel;
    private TextView tvUsername, tvEmail;
    private SwitchMaterial switchBiometric;
    private Chip chipVerified;
    private ImageView ivProfileAvatar;
    private User currentUser;
    private ActivityResultLauncher<String> imagePickerLauncher;
    private SessionManager sessionManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile);

        androidx.appcompat.widget.Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setTitle("My Profile");
        }

        viewModel = new ViewModelProvider(this).get(AuthViewModel.class);

        imagePickerLauncher = registerForActivityResult(
                new ActivityResultContracts.GetContent(),
                uri -> {
                    if (uri != null && currentUser != null) {
                        saveProfilePicture(uri);
                    }
                });

        tvUsername = findViewById(R.id.tvProfileUsername);
        tvEmail = findViewById(R.id.tvProfileEmail);
        switchBiometric = findViewById(R.id.switchBiometric);
        chipVerified = findViewById(R.id.chipVerified);
        ivProfileAvatar = findViewById(R.id.ivProfileAvatar);
        Button btnLogout = findViewById(R.id.btnLogout);
        TextView btnChangePassword = findViewById(R.id.btnChangePassword);

        ivProfileAvatar.setOnClickListener(v -> showImagePickerDialog());

        if (!BiometricHelper.isBiometricAvailable(this)) {
            switchBiometric.setEnabled(false);
            switchBiometric.setText("Biometric login not available");
            Toast.makeText(this, BiometricHelper.getBiometricStatus(this), Toast.LENGTH_LONG).show();
        }

        viewModel.getCurrentUser().observe(this, user -> {
            if (user != null) {
                currentUser = user;
                updateUI(user);
            } else {
                startActivity(new Intent(this, LoginActivity.class));
                finishAffinity();
            }
        });

        sessionManager = new SessionManager(this);

        switchBiometric.setOnCheckedChangeListener((buttonView, isChecked) -> {
            if (currentUser != null && buttonView.isPressed()) {
                if (isChecked) {
                    BiometricHelper.authenticate(this, new BiometricHelper.BiometricCallback() {
                        @Override
                        public void onSuccess() {
                            viewModel.updateBiometricStatus(currentUser, true);
                            // Save this user as the last biometric user for quick login
                            sessionManager.saveLastBiometricUserId(currentUser.uid);
                            Toast.makeText(ProfileActivity.this, "Biometric login enabled", Toast.LENGTH_SHORT).show();
                        }

                        @Override
                        public void onError(String error) {
                            switchBiometric.setChecked(false);
                            Toast.makeText(ProfileActivity.this, "Authentication failed: " + error, Toast.LENGTH_SHORT)
                                    .show();
                        }

                        @Override
                        public void onCanceled() {
                            switchBiometric.setChecked(false);
                        }
                    });
                } else {
                    viewModel.updateBiometricStatus(currentUser, false);
                    // Clear the biometric user ID when disabled
                    sessionManager.clearLastBiometricUserId();
                }
            }
        });

        btnChangePassword.setOnClickListener(v -> showChangePasswordDialog());

        btnLogout.setOnClickListener(v -> {
            viewModel.logout();
            startActivity(new Intent(this, LoginActivity.class));
            finishAffinity();
        });
    }

    private void showChangePasswordDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        View view = LayoutInflater.from(this).inflate(R.layout.dialog_change_password, null);
        builder.setView(view);
        builder.setTitle("Change Password");

        com.google.android.material.textfield.TextInputLayout tilCurrentPass = view
                .findViewById(R.id.tilCurrentPassword);
        com.google.android.material.textfield.TextInputLayout tilNewPass = view.findViewById(R.id.tilNewPassword);
        com.google.android.material.textfield.TextInputLayout tilConfirmPass = view
                .findViewById(R.id.tilConfirmPassword);

        TextInputEditText etCurrentPass = view.findViewById(R.id.etCurrentPassword);
        TextInputEditText etNewPass = view.findViewById(R.id.etNewPassword);
        TextInputEditText etConfirmPass = view.findViewById(R.id.etConfirmPassword);

        builder.setPositiveButton("Change", null);
        builder.setNegativeButton("Cancel", (dialog, which) -> dialog.dismiss());

        AlertDialog dialog = builder.create();
        dialog.show();

        dialog.getButton(AlertDialog.BUTTON_POSITIVE).setOnClickListener(v -> {
            tilCurrentPass.setError(null);
            tilNewPass.setError(null);
            tilConfirmPass.setError(null);

            String currentPass = etCurrentPass.getText().toString();
            String newPass = etNewPass.getText().toString();
            String confirmPass = etConfirmPass.getText().toString();

            if (currentPass.isEmpty() || newPass.isEmpty() || confirmPass.isEmpty()) {
                Toast.makeText(this, "Please fill all fields", Toast.LENGTH_SHORT).show();
                return;
            }

            User user = viewModel.getCurrentUser().getValue();

            if (user == null) {
                Toast.makeText(this, "User session expired. Please login again.", Toast.LENGTH_SHORT).show();
                startActivity(new Intent(this, LoginActivity.class));
                finishAffinity();
                return;
            }

            if (!newPass.equals(confirmPass)) {
                tilConfirmPass.setError("Passwords do not match");
                return;
            }

            if (!ValidationHelper.isValidPassword(newPass)) {
                tilNewPass.setError("Password too weak (min 6 chars)");
                return;
            }

            // Observe for success/error to dismiss dialog
            viewModel.getMessage().observe(this, msg -> {
                if (msg != null && msg.equals("Password updated successfully")) {
                    dialog.dismiss();
                }
            });

            viewModel.changePassword(currentPass, newPass);
        });
    }

    private void updateUI(User user) {
        tvUsername.setText(user.username);
        tvEmail.setText(user.email);
        switchBiometric.setChecked(user.hasBiometricEnabled);

        // Handle profile picture
        if (user.profilePicturePath != null && !user.profilePicturePath.isEmpty()) {
            File imageFile = new File(user.profilePicturePath);
            if (imageFile.exists()) {
                // Display real photo without tint
                ivProfileAvatar.setImageURI(Uri.fromFile(imageFile));
                ivProfileAvatar.setColorFilter(null);
                ivProfileAvatar.setScaleType(ImageView.ScaleType.CENTER_CROP);
            } else {
                // Fallback to default avatar with tint
                ivProfileAvatar.setImageResource(R.drawable.ic_member_avatar);
                ivProfileAvatar.setColorFilter(getResources().getColor(R.color.primaryColor));
                ivProfileAvatar.setScaleType(ImageView.ScaleType.FIT_CENTER);
            }
        } else {
            // Default avatar with tint
            ivProfileAvatar.setImageResource(R.drawable.ic_member_avatar);
            ivProfileAvatar.setColorFilter(getResources().getColor(R.color.primaryColor));
            ivProfileAvatar.setScaleType(ImageView.ScaleType.FIT_CENTER);
        }

        if (user.isEmailVerified) {
            chipVerified.setVisibility(View.VISIBLE);
        } else {
            chipVerified.setVisibility(View.GONE);
        }
    }

    private void showImagePickerDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Change Profile Picture");
        builder.setItems(new String[] { "Choose from Gallery", "Use Default Avatar" }, (dialog, which) -> {
            if (which == 0) {
                imagePickerLauncher.launch("image/*");
            } else {
                if (currentUser != null) {
                    viewModel.updateProfilePicture(currentUser, null);
                    Toast.makeText(this, "Using default avatar", Toast.LENGTH_SHORT).show();
                }
            }
        });
        builder.show();
    }

    private void saveProfilePicture(Uri imageUri) {
        try {
            File profileDir = new File(getFilesDir(), "profiles");
            if (!profileDir.exists()) {
                profileDir.mkdirs();
            }

            String fileName = "profile_" + currentUser.uid + ".jpg";
            File imageFile = new File(profileDir, fileName);

            InputStream inputStream = getContentResolver().openInputStream(imageUri);
            FileOutputStream outputStream = new FileOutputStream(imageFile);

            byte[] buffer = new byte[1024];
            int length;
            while ((length = inputStream.read(buffer)) > 0) {
                outputStream.write(buffer, 0, length);
            }

            outputStream.close();
            inputStream.close();

            viewModel.updateProfilePicture(currentUser, imageFile.getAbsolutePath());
            Toast.makeText(this, "Profile picture updated", Toast.LENGTH_SHORT).show();

        } catch (Exception e) {
            e.printStackTrace();
            Toast.makeText(this, "Failed to save profile picture", Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    public boolean onSupportNavigateUp() {
        finish();
        return true;
    }
}

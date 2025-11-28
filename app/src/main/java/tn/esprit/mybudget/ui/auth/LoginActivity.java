package tn.esprit.mybudget.ui.auth;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.ViewModelProvider;

import com.google.android.material.textfield.TextInputEditText;

import tn.esprit.mybudget.R;
import tn.esprit.mybudget.data.entity.User;
import tn.esprit.mybudget.ui.main.MainActivity;
import tn.esprit.mybudget.util.BiometricHelper;
import tn.esprit.mybudget.util.SessionManager;

public class LoginActivity extends AppCompatActivity {
    private static final String PREFS_NAME = "UserPrefs";
    private static final String KEY_USER_ID = "userId";
    private static final String KEY_BIOMETRIC_ENABLED = "biometricEnabled";

    private AuthViewModel viewModel;
    private TextInputEditText etUsername, etPassword;
    private ImageButton btnBiometric;
    private LinearLayout layoutBiometric;
    private boolean isCheckingSession = true;
    private SessionManager sessionManager;
    private User savedUser = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        viewModel = new ViewModelProvider(this).get(AuthViewModel.class);
        sessionManager = new SessionManager(this);

        etUsername = findViewById(R.id.etUsername);
        etPassword = findViewById(R.id.etPassword);
        Button btnLogin = findViewById(R.id.btnLogin);
        TextView tvRegister = findViewById(R.id.tvRegister);
        TextView tvForgotPassword = findViewById(R.id.tvForgotPassword);
        btnBiometric = findViewById(R.id.btnBiometric);
        layoutBiometric = findViewById(R.id.layoutBiometric);

        // Check if user was previously logged in
        if (sessionManager.isLoggedIn()) {
            viewModel.getCurrentUser().observe(this, user -> {
                if (user != null && isCheckingSession) {
                    savedUser = user;
                    if (user.hasBiometricEnabled && BiometricHelper.isBiometricAvailable(this)) {
                        layoutBiometric.setVisibility(View.VISIBLE);
                        promptBiometricLogin(user);
                    } else {
                        hideBiometricOption();
                        // Auto login if no biometric but session exists?
                        // Usually we want them to re-enter password if no biometric,
                        // or just go to main if session is valid.
                        // For now, let's just pre-fill email
                        etUsername.setText(user.email);
                    }
                }
            });
        } else {
            hideBiometricOption();
            isCheckingSession = false;
        }

        btnLogin.setOnClickListener(v -> {
            String email = etUsername.getText().toString();
            String password = etPassword.getText().toString();

            if (email.isEmpty() || password.isEmpty()) {
                Toast.makeText(this, "Please fill all fields", Toast.LENGTH_SHORT).show();
                return;
            }

            viewModel.login(email, password);
        });

        tvRegister.setOnClickListener(v -> {
            startActivity(new Intent(this, RegisterActivity.class));
        });

        tvForgotPassword.setOnClickListener(v -> {
            showForgotPasswordDialog();
        });

        btnBiometric.setOnClickListener(v -> {
            if (savedUser != null) {
                promptBiometricLogin(savedUser);
            }
        });

        viewModel.getCurrentUser().observe(this, user -> {
            if (user != null && !isCheckingSession) {
                navigateToMain(user);
            }
        });

        viewModel.getError().observe(this, error -> {
            if (error != null) {
                Toast.makeText(this, error, Toast.LENGTH_SHORT).show();
            }
        });

        viewModel.getMessage().observe(this, message -> {
            if (message != null) {
                Toast.makeText(this, message, Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void showForgotPasswordDialog() {
        androidx.appcompat.app.AlertDialog.Builder builder = new androidx.appcompat.app.AlertDialog.Builder(this);
        View view = getLayoutInflater().inflate(R.layout.dialog_forgot_password, null);
        builder.setView(view);

        TextInputEditText etEmail = view.findViewById(R.id.etResetEmail);

        builder.setPositiveButton("Send Reset Link", (dialog, which) -> {
            String email = etEmail.getText().toString();
            if (!email.isEmpty()) {
                viewModel.forgotPassword(email);
            } else {
                Toast.makeText(this, "Please enter your email", Toast.LENGTH_SHORT).show();
            }
        });

        builder.setNegativeButton("Cancel", (dialog, which) -> dialog.dismiss());
        builder.show();
    }

    private void hideBiometricOption() {
        if (layoutBiometric != null) {
            layoutBiometric.setVisibility(View.GONE);
        }
    }

    private void promptBiometricLogin(User user) {
        BiometricHelper.authenticate(this, new BiometricHelper.BiometricCallback() {
            @Override
            public void onSuccess() {
                Toast.makeText(LoginActivity.this, "Biometric Login Success", Toast.LENGTH_SHORT).show();
                navigateToMain(user);
            }

            @Override
            public void onError(String error) {
                // Biometric failed, user can try again or login manually
                Toast.makeText(LoginActivity.this, "Biometric authentication failed: " + error, Toast.LENGTH_SHORT)
                        .show();
                isCheckingSession = false;
            }

            @Override
            public void onCanceled() {
                // User canceled biometric, allow manual login
                Toast.makeText(LoginActivity.this, "Biometric canceled. Please login manually.", Toast.LENGTH_SHORT)
                        .show();
                isCheckingSession = false;
            }
        });
    }

    private void navigateToMain(User user) {
        Toast.makeText(this, "Welcome " + user.username, Toast.LENGTH_SHORT).show();
        startActivity(new Intent(this, MainActivity.class));
        finish();
    }
}
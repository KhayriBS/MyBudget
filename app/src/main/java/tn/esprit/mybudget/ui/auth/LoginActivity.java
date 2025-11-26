package tn.esprit.mybudget.ui.auth;

import android.content.Intent;
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
        btnBiometric = findViewById(R.id.btnBiometric);
        layoutBiometric = findViewById(R.id.layoutBiometric);

        // Check if user is already logged in from session
        viewModel.getCurrentUser().observe(this, user -> {
            if (user != null) {
                savedUser = user;

                if (isCheckingSession) {
                    // Check if user has biometric enabled
                    if (user.hasBiometricEnabled && BiometricHelper.isBiometricAvailable(this)) {
                        // Show biometric button and prompt automatically
                        showBiometricOption();
                        promptBiometricLogin(user);
                    } else {
                        // Auto-login from saved session (no biometric)
                        navigateToMain(user);
                    }
                } else {
                    // Manual login success
                    navigateToMain(user);
                }
            } else {
                // Not logged in, allow manual login
                isCheckingSession = false;
                savedUser = null;
                hideBiometricOption();
            }
        });

        btnLogin.setOnClickListener(v -> {
            String username = etUsername.getText().toString();
            String password = etPassword.getText().toString();
            if (username.isEmpty() || password.isEmpty()) {
                Toast.makeText(this, "Please fill all fields", Toast.LENGTH_SHORT).show();
                return;
            }
            isCheckingSession = false;
            viewModel.login(username, password);
        });

        btnBiometric.setOnClickListener(v -> {
            if (savedUser != null) {
                promptBiometricLogin(savedUser);
            }
        });

        tvRegister.setOnClickListener(v -> {
            startActivity(new Intent(this, RegisterActivity.class));
        });

        viewModel.getError().observe(this, error -> {
            if (error != null) {
                Toast.makeText(this, error, Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void showBiometricOption() {
        layoutBiometric.setVisibility(View.VISIBLE);
    }

    private void hideBiometricOption() {
        layoutBiometric.setVisibility(View.GONE);
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
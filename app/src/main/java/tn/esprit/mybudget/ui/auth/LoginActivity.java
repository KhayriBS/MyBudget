package tn.esprit.mybudget.ui.auth;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.ViewModelProvider;

import com.google.android.material.textfield.TextInputEditText;

import tn.esprit.mybudget.R;
import tn.esprit.mybudget.ui.main.MainActivity;
import tn.esprit.mybudget.util.BiometricHelper;

public class LoginActivity extends AppCompatActivity {
    private AuthViewModel viewModel;
    private TextInputEditText etUsername, etPassword;
    private boolean isCheckingSession = true;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        viewModel = new ViewModelProvider(this).get(AuthViewModel.class);

        etUsername = findViewById(R.id.etUsername);
        etPassword = findViewById(R.id.etPassword);
        Button btnLogin = findViewById(R.id.btnLogin);
        TextView tvRegister = findViewById(R.id.tvRegister);

        // Check if user is already logged in from session
        viewModel.getCurrentUser().observe(this, user -> {
            if (user != null) {
                if (isCheckingSession) {
                    // Auto-login from saved session
                    startActivity(new Intent(this, MainActivity.class));
                    finish();
                } else {
                    // Manual login success
                    Toast.makeText(this, "Welcome " + user.username, Toast.LENGTH_SHORT).show();
                    startActivity(new Intent(this, MainActivity.class));
                    finish();
                }
            } else {
                // Not logged in, allow manual login
                isCheckingSession = false;
            }
        });

        // Check for Biometric
        if (BiometricHelper.isBiometricAvailable(this)) {
            // In a real app, check if user has enabled it in settings
            // For demo, we try to authenticate immediately if available
            BiometricHelper.authenticate(this, new BiometricHelper.BiometricCallback() {
                @Override
                public void onSuccess() {
                    Toast.makeText(LoginActivity.this, "Biometric Login Success", Toast.LENGTH_SHORT).show();
                    startActivity(new Intent(LoginActivity.this, MainActivity.class));
                    finish();
                }

                @Override
                public void onError(String error) {
                    // Fallback to password
                }

                @Override
                public void onCanceled() {
                    // User canceled, do nothing
                }
            });
        }

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

        tvRegister.setOnClickListener(v -> {
            startActivity(new Intent(this, RegisterActivity.class));
        });

        viewModel.getError().observe(this, error -> {
            if (error != null) {
                Toast.makeText(this, error, Toast.LENGTH_SHORT).show();
            }
        });
    }
}
package tn.esprit.mybudget.ui.auth;

import android.content.Intent;
import android.content.SharedPreferences;
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
    private static final String PREFS_NAME = "UserPrefs";
    private static final String KEY_USER_ID = "userId";
    private static final String KEY_BIOMETRIC_ENABLED = "biometricEnabled";

    private AuthViewModel viewModel;
    private TextInputEditText etUsername, etPassword;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        viewModel = new ViewModelProvider(this).get(AuthViewModel.class);

        etUsername = findViewById(R.id.etUsername);
        etPassword = findViewById(R.id.etPassword);
        Button btnLogin = findViewById(R.id.btnLogin);
        TextView tvRegister = findViewById(R.id.tvRegister);

        // Check for Biometric - only if user has previously logged in AND enabled
        // biometric
        SharedPreferences prefs = getSharedPreferences(PREFS_NAME, MODE_PRIVATE);
        int savedUserId = prefs.getInt(KEY_USER_ID, -1);
        boolean biometricEnabled = prefs.getBoolean(KEY_BIOMETRIC_ENABLED, false);

        if (savedUserId != -1 && biometricEnabled && BiometricHelper.isBiometricAvailable(this)) {
            BiometricHelper.authenticate(this, new BiometricHelper.BiometricCallback() {
                @Override
                public void onSuccess() {
                    Toast.makeText(LoginActivity.this, "Biometric Login Success", Toast.LENGTH_SHORT).show();
                    startActivity(new Intent(LoginActivity.this, MainActivity.class));
                    finish();
                }

                @Override
                public void onError(String error) {
                    // Fallback to password - user can still login manually
                    Toast.makeText(LoginActivity.this, "Use password to login", Toast.LENGTH_SHORT).show();
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
            viewModel.login(username, password);
        });

        tvRegister.setOnClickListener(v -> {
            startActivity(new Intent(this, RegisterActivity.class));
        });

        viewModel.getCurrentUser().observe(this, user -> {
            if (user != null) {
                // Login success - save user ID for biometric login next time
                SharedPreferences.Editor editor = getSharedPreferences(PREFS_NAME, MODE_PRIVATE).edit();
                editor.putInt(KEY_USER_ID, user.uid);
                editor.apply();

                Toast.makeText(this, "Welcome " + user.username, Toast.LENGTH_SHORT).show();
                startActivity(new Intent(this, MainActivity.class));
                finish();
            }
        });

        viewModel.getError().observe(this, error -> {
            if (error != null) {
                Toast.makeText(this, error, Toast.LENGTH_SHORT).show();
            }
        });
    }
}
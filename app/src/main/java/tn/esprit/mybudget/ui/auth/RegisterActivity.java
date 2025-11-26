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
import tn.esprit.mybudget.util.ValidationHelper;

public class RegisterActivity extends AppCompatActivity {

    private AuthViewModel viewModel;
    private TextInputEditText etUsername, etEmail, etPassword;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);

        androidx.appcompat.widget.Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setTitle("Register");
        }

        viewModel = new ViewModelProvider(this).get(AuthViewModel.class);

        etUsername = findViewById(R.id.etRegUsername);
        etEmail = findViewById(R.id.etRegEmail);
        etPassword = findViewById(R.id.etRegPassword);
        Button btnRegister = findViewById(R.id.btnRegister);
        TextView tvLogin = findViewById(R.id.tvLogin);

        btnRegister.setOnClickListener(v -> {
            String username = etUsername.getText().toString();
            String email = etEmail.getText().toString();
            String password = etPassword.getText().toString();

            // Validate username
            if (!ValidationHelper.isValidUsername(username)) {
                etUsername.setError("Username must be at least 3 characters (letters, numbers, underscore only)");
                etUsername.requestFocus();
                return;
            }

            // Validate email
            if (!ValidationHelper.isValidEmail(email)) {
                etEmail.setError("Please enter a valid email address");
                etEmail.requestFocus();
                return;
            }

            // Validate password
            if (!ValidationHelper.isValidPassword(password)) {
                etPassword.setError("Password must be at least 6 characters");
                etPassword.requestFocus();
                return;
            }

            // Show password strength
            String strength = ValidationHelper.getPasswordStrength(password);
            Toast.makeText(this, "Password strength: " + strength, Toast.LENGTH_SHORT).show();

            viewModel.register(username, password, email);
        });

        tvLogin.setOnClickListener(v -> {
            finish(); // Go back to Login
        });

        viewModel.getCurrentUser().observe(this, user -> {
            if (user != null) {
                // Register success, auto login
                Toast.makeText(this, "Account created! Please verify your email.", Toast.LENGTH_LONG).show();
                startActivity(new Intent(this, MainActivity.class));
                finishAffinity(); // Clear back stack
            }
        });

        viewModel.getError().observe(this, error -> {
            if (error != null) {
                Toast.makeText(this, error, Toast.LENGTH_SHORT).show();
            }
        });
    }

    @Override
    public boolean onSupportNavigateUp() {
        finish();
        return true;
    }
}
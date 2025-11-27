package tn.esprit.mybudget.ui.auth;

import android.content.Intent;
import android.os.Bundle;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.ViewModelProvider;

import com.google.android.material.button.MaterialButton;

import tn.esprit.mybudget.R;
import tn.esprit.mybudget.ui.main.MainActivity;

public class VerificationActivity extends AppCompatActivity {

    private AuthViewModel viewModel;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_verification);

        viewModel = new ViewModelProvider(this).get(AuthViewModel.class);

        TextView tvVerificationMessage = findViewById(R.id.tvVerificationMessage);
        MaterialButton btnCheckVerification = findViewById(R.id.btnCheckVerification);
        MaterialButton btnResendEmail = findViewById(R.id.btnResendEmail);
        TextView tvBackToLogin = findViewById(R.id.tvBackToLogin);

        // Set email in message if available
        if (viewModel.getCurrentUser().getValue() != null) {
            String email = viewModel.getCurrentUser().getValue().email;
            tvVerificationMessage.setText("We have sent a verification email to " + email
                    + ". Please check your inbox and verify your account.");
        }

        btnCheckVerification.setOnClickListener(v -> {
            viewModel.checkEmailVerification();
        });

        btnResendEmail.setOnClickListener(v -> {
            viewModel.resendVerificationEmail();
        });

        tvBackToLogin.setOnClickListener(v -> {
            viewModel.logout();
            startActivity(new Intent(this, LoginActivity.class));
            finish();
        });

        viewModel.getCurrentUser().observe(this, user -> {
            if (user != null && user.isEmailVerified) {
                // Only navigate if verified
                startActivity(new Intent(this, MainActivity.class));
                finishAffinity();
            }
        });

        viewModel.getMessage().observe(this, message -> {
            if (message != null) {
                Toast.makeText(this, message, Toast.LENGTH_SHORT).show();
            }
        });

        viewModel.getError().observe(this, error -> {
            if (error != null) {
                Toast.makeText(this, error, Toast.LENGTH_SHORT).show();
            }
        });
    }
}

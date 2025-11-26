package tn.esprit.mybudget.ui.auth;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.ViewModelProvider;

import com.google.android.material.chip.Chip;
import com.google.android.material.switchmaterial.SwitchMaterial;

import tn.esprit.mybudget.R;
import tn.esprit.mybudget.data.entity.User;
import tn.esprit.mybudget.util.BiometricHelper;

public class ProfileActivity extends AppCompatActivity {

    private AuthViewModel viewModel;
    private TextView tvUsername, tvEmail;
    private SwitchMaterial switchBiometric;
    private Chip chipVerified;
    private User currentUser;

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

        tvUsername = findViewById(R.id.tvProfileUsername);
        tvEmail = findViewById(R.id.tvProfileEmail);
        switchBiometric = findViewById(R.id.switchBiometric);
        chipVerified = findViewById(R.id.chipVerified);
        Button btnLogout = findViewById(R.id.btnLogout);

        // Check if biometric is available on device
        if (!BiometricHelper.isBiometricAvailable(this)) {
            switchBiometric.setEnabled(false);
            switchBiometric.setText("Biometric login not available");
        }

        viewModel.getCurrentUser().observe(this, user -> {
            if (user != null) {
                currentUser = user;
                updateUI(user);
            } else {
                // Not logged in, go to login
                startActivity(new Intent(this, LoginActivity.class));
                finishAffinity();
            }
        });

        switchBiometric.setOnCheckedChangeListener((buttonView, isChecked) -> {
            if (currentUser != null && buttonView.isPressed()) { // Only if user pressed it
                if (isChecked) {
                    // Verify biometric before enabling
                    BiometricHelper.authenticate(this, new BiometricHelper.BiometricCallback() {
                        @Override
                        public void onSuccess() {
                            viewModel.updateBiometricStatus(currentUser, true);
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
                }
            }
        });

        btnLogout.setOnClickListener(v -> {
            viewModel.logout();
            startActivity(new Intent(this, LoginActivity.class));
            finishAffinity();
        });
    }

    private void updateUI(User user) {
        tvUsername.setText(user.username);
        tvEmail.setText(user.email);
        switchBiometric.setChecked(user.hasBiometricEnabled);

        if (user.isEmailVerified) {
            chipVerified.setVisibility(View.VISIBLE);
        } else {
            chipVerified.setVisibility(View.GONE);
        }
    }

    @Override
    public boolean onSupportNavigateUp() {
        finish();
        return true;
    }
}

package com.smartexpensepro.activities;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;

import androidx.appcompat.app.AppCompatActivity;

import com.smartexpensepro.database.DatabaseHelper;
import com.smartexpensepro.databinding.ActivityRegisterBinding;
import com.smartexpensepro.models.User;
import com.smartexpensepro.utils.SessionManager;

public class RegisterActivity extends AppCompatActivity {

    private ActivityRegisterBinding binding;
    private DatabaseHelper dbHelper;
    private SessionManager sessionManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityRegisterBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        dbHelper = DatabaseHelper.getInstance(this);
        sessionManager = new SessionManager(this);

        binding.btnRegister.setOnClickListener(v -> attemptRegister());

        binding.tvLogin.setOnClickListener(v -> finish());
    }

    private void attemptRegister() {
        String name = binding.etName.getText().toString().trim();
        String email = binding.etEmail.getText().toString().trim();
        String password = binding.etPassword.getText().toString().trim();
        String confirmPassword = binding.etConfirmPassword.getText().toString().trim();

        binding.tilName.setError(null);
        binding.tilEmail.setError(null);
        binding.tilPassword.setError(null);
        binding.tilConfirmPassword.setError(null);

        boolean hasError = false;

        if (TextUtils.isEmpty(name)) {
            binding.tilName.setError("Name is required");
            hasError = true;
        }

        if (TextUtils.isEmpty(email)) {
            binding.tilEmail.setError("Email is required");
            hasError = true;
        } else if (!android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            binding.tilEmail.setError("Enter a valid email");
            hasError = true;
        }

        if (TextUtils.isEmpty(password)) {
            binding.tilPassword.setError("Password is required");
            hasError = true;
        } else if (password.length() < 6) {
            binding.tilPassword.setError("Password must be at least 6 characters");
            hasError = true;
        }

        if (!password.equals(confirmPassword)) {
            binding.tilConfirmPassword.setError("Passwords do not match");
            hasError = true;
        }

        if (hasError) return;

        if (dbHelper.isEmailExists(email)) {
            binding.tilEmail.setError("Email already registered");
            return;
        }

        binding.btnRegister.setEnabled(false);
        binding.progressBar.setVisibility(View.VISIBLE);

        User user = new User(email, password, name);
        long userId = dbHelper.registerUser(user);

        binding.btnRegister.setEnabled(true);
        binding.progressBar.setVisibility(View.GONE);

        if (userId != -1) {
            sessionManager.createSession((int) userId, email, name);
            Intent intent = new Intent(RegisterActivity.this, DashboardActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            startActivity(intent);
            finish();
        } else {
            binding.tilEmail.setError("Registration failed. Please try again.");
        }
    }
}

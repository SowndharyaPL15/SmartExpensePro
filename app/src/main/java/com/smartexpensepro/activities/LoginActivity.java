package com.smartexpensepro.activities;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;

import androidx.appcompat.app.AppCompatActivity;

import com.smartexpensepro.database.DatabaseHelper;
import com.smartexpensepro.databinding.ActivityLoginBinding;
import com.smartexpensepro.models.User;
import com.smartexpensepro.utils.SessionManager;

public class LoginActivity extends AppCompatActivity {

    private ActivityLoginBinding binding;
    private DatabaseHelper dbHelper;
    private SessionManager sessionManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityLoginBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        dbHelper = DatabaseHelper.getInstance(this);
        sessionManager = new SessionManager(this);

        binding.btnLogin.setOnClickListener(v -> attemptLogin());

        binding.tvRegister.setOnClickListener(v -> {
            startActivity(new Intent(LoginActivity.this, RegisterActivity.class));
        });
    }

    private void attemptLogin() {
        String email = binding.etEmail.getText().toString().trim();
        String password = binding.etPassword.getText().toString().trim();

        binding.tilEmail.setError(null);
        binding.tilPassword.setError(null);

        boolean hasError = false;

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
        }

        if (hasError) return;

        binding.btnLogin.setEnabled(false);
        binding.progressBar.setVisibility(View.VISIBLE);

        User user = dbHelper.loginUser(email, password);

        binding.btnLogin.setEnabled(true);
        binding.progressBar.setVisibility(View.GONE);

        if (user != null) {
            sessionManager.createSession(user.getId(), user.getEmail(), user.getName());
            Intent intent = new Intent(LoginActivity.this, DashboardActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            startActivity(intent);
            finish();
        } else {
            binding.tilPassword.setError("Invalid email or password");
        }
    }
}

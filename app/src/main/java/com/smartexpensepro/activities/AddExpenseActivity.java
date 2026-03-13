package com.smartexpensepro.activities;

import android.os.Bundle;
import android.text.TextUtils;
import android.widget.ArrayAdapter;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.smartexpensepro.database.DatabaseHelper;
import com.smartexpensepro.databinding.ActivityAddExpenseBinding;
import com.smartexpensepro.models.Transaction;
import com.smartexpensepro.utils.SessionManager;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class AddExpenseActivity extends AppCompatActivity {

    private ActivityAddExpenseBinding binding;
    private DatabaseHelper dbHelper;
    private SessionManager sessionManager;

    private final String[] CATEGORIES = {"Food", "Travel", "Bills", "Shopping", "Health", "Entertainment", "Education", "Others"};

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityAddExpenseBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        setSupportActionBar(binding.toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setTitle("Add Expense");
        }

        dbHelper = DatabaseHelper.getInstance(this);
        sessionManager = new SessionManager(this);

        setupCategorySpinner();

        binding.btnSave.setOnClickListener(v -> saveExpense());
        binding.btnCancel.setOnClickListener(v -> finish());
    }

    private void setupCategorySpinner() {
        ArrayAdapter<String> adapter = new ArrayAdapter<>(
                this, android.R.layout.simple_dropdown_item_1line, CATEGORIES);
        binding.actvCategory.setAdapter(adapter);
        binding.actvCategory.setText(CATEGORIES[0], false);
    }

    private void saveExpense() {
        String amountStr = binding.etAmount.getText().toString().trim();
        String category = binding.actvCategory.getText().toString().trim();
        String description = binding.etDescription.getText().toString().trim();

        binding.tilAmount.setError(null);
        binding.tilCategory.setError(null);

        boolean hasError = false;

        if (TextUtils.isEmpty(amountStr)) {
            binding.tilAmount.setError("Amount is required");
            hasError = true;
        } else {
            try {
                double amount = Double.parseDouble(amountStr);
                if (amount <= 0) {
                    binding.tilAmount.setError("Amount must be greater than 0");
                    hasError = true;
                }
            } catch (NumberFormatException e) {
                binding.tilAmount.setError("Enter a valid amount");
                hasError = true;
            }
        }

        if (TextUtils.isEmpty(category)) {
            binding.tilCategory.setError("Please select a category");
            hasError = true;
        }

        if (hasError) return;

        double amount = Double.parseDouble(amountStr);
        String date = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault()).format(new Date());

        Transaction transaction = new Transaction(
                amount, category, description, date, sessionManager.getUserId(), false);

        long result = dbHelper.addTransaction(transaction);
        if (result != -1) {
            Toast.makeText(this, "Expense saved successfully!", Toast.LENGTH_SHORT).show();
            finish();
        } else {
            Toast.makeText(this, "Failed to save expense. Please try again.", Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    public boolean onSupportNavigateUp() {
        finish();
        return true;
    }
}

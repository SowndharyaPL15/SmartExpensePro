package com.smartexpensepro.activities;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.smartexpensepro.R;
import com.smartexpensepro.adapters.TransactionAdapter;
import com.smartexpensepro.database.DatabaseHelper;
import com.smartexpensepro.databinding.ActivityDashboardBinding;
import com.smartexpensepro.models.Transaction;
import com.smartexpensepro.utils.ExcelExporter;
import com.smartexpensepro.utils.SessionManager;

import java.util.ArrayList;
import java.util.List;

public class DashboardActivity extends AppCompatActivity {

    private ActivityDashboardBinding binding;
    private DatabaseHelper dbHelper;
    private SessionManager sessionManager;
    private TransactionAdapter adapter;
    private List<Transaction> transactionList = new ArrayList<>();
    private int userId;

    private final ActivityResultLauncher<String[]> permissionLauncher =
            registerForActivityResult(new ActivityResultContracts.RequestMultiplePermissions(), result -> {
                // Handle permission results silently
            });

    private final ActivityResultLauncher<String> storagePermLauncher =
            registerForActivityResult(new ActivityResultContracts.RequestPermission(), granted -> {
                if (granted) {
                    exportToExcel();
                } else {
                    Toast.makeText(this, "Storage permission needed to export", Toast.LENGTH_SHORT).show();
                }
            });

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityDashboardBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        setSupportActionBar(binding.toolbar);

        dbHelper = DatabaseHelper.getInstance(this);
        sessionManager = new SessionManager(this);
        userId = sessionManager.getUserId();

        setupRecyclerView();
        setupClickListeners();
        requestSmsPermissions();
        loadDashboard();
    }

    @Override
    protected void onResume() {
        super.onResume();
        loadDashboard();
    }

    private void setupRecyclerView() {
        adapter = new TransactionAdapter(this, transactionList);
        binding.rvTransactions.setLayoutManager(new LinearLayoutManager(this));
        binding.rvTransactions.setAdapter(adapter);

        adapter.setOnItemLongClickListener((transaction, position) -> {
            new AlertDialog.Builder(this)
                    .setTitle("Delete Transaction")
                    .setMessage("Delete this ₹" + String.format("%.2f", transaction.getAmount()) + " expense?")
                    .setPositiveButton("Delete", (dialog, which) -> {
                        dbHelper.deleteTransaction(transaction.getId());
                        transactionList.remove(position);
                        adapter.notifyItemRemoved(position);
                        updateSummaryCard();
                        Toast.makeText(this, "Transaction deleted", Toast.LENGTH_SHORT).show();
                    })
                    .setNegativeButton("Cancel", null)
                    .show();
        });
    }

    private void setupClickListeners() {
        binding.fabAddExpense.setOnClickListener(v ->
                startActivity(new Intent(this, AddExpenseActivity.class)));

        binding.btnAnalytics.setOnClickListener(v ->
                startActivity(new Intent(this, AnalyticsActivity.class)));

        binding.btnExport.setOnClickListener(v -> checkStoragePermAndExport());
    }

    private void loadDashboard() {
        transactionList = dbHelper.getAllTransactions(userId);
        adapter.updateData(transactionList);
        updateSummaryCard();

        if (transactionList.isEmpty()) {
            binding.rvTransactions.setVisibility(View.GONE);
            binding.layoutEmpty.setVisibility(View.VISIBLE);
        } else {
            binding.rvTransactions.setVisibility(View.VISIBLE);
            binding.layoutEmpty.setVisibility(View.GONE);
        }

        String userName = sessionManager.getUserName();
        binding.toolbar.setTitle("Hi, " + (userName.isEmpty() ? "User" : userName.split(" ")[0]) + " 👋");
    }

    private void updateSummaryCard() {
        double total = dbHelper.getTotalExpense(userId);
        binding.tvTotalAmount.setText(String.format("₹ %.2f", total));
        binding.tvTransactionCount.setText(transactionList.size() + " transactions");
    }

    private void requestSmsPermissions() {
        List<String> permissions = new ArrayList<>();
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.RECEIVE_SMS)
                != PackageManager.PERMISSION_GRANTED) {
            permissions.add(Manifest.permission.RECEIVE_SMS);
        }
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.READ_SMS)
                != PackageManager.PERMISSION_GRANTED) {
            permissions.add(Manifest.permission.READ_SMS);
        }
        if (!permissions.isEmpty()) {
            permissionLauncher.launch(permissions.toArray(new String[0]));
        }
    }

    private void checkStoragePermAndExport() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            exportToExcel();
        } else {
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE)
                    == PackageManager.PERMISSION_GRANTED) {
                exportToExcel();
            } else {
                storagePermLauncher.launch(Manifest.permission.WRITE_EXTERNAL_STORAGE);
            }
        }
    }

    private void exportToExcel() {
        if (transactionList.isEmpty()) {
            Toast.makeText(this, "No transactions to export", Toast.LENGTH_SHORT).show();
            return;
        }

        Toast.makeText(this, "Exporting...", Toast.LENGTH_SHORT).show();

        ExcelExporter.exportToExcel(this, transactionList, new ExcelExporter.ExportCallback() {
            @Override
            public void onSuccess(String filePath) {
                runOnUiThread(() ->
                        Toast.makeText(DashboardActivity.this,
                                "Exported to Downloads/SmartExpenseReport.csv",
                                Toast.LENGTH_LONG).show());
            }

            @Override
            public void onError(String message) {
                runOnUiThread(() ->
                        Toast.makeText(DashboardActivity.this, "Export failed: " + message, Toast.LENGTH_LONG).show());
            }
        });
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.dashboard_menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == R.id.action_logout) {
            new AlertDialog.Builder(this)
                    .setTitle("Logout")
                    .setMessage("Are you sure you want to logout?")
                    .setPositiveButton("Logout", (dialog, which) -> {
                        sessionManager.logout();
                        Intent intent = new Intent(this, LoginActivity.class);
                        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                        startActivity(intent);
                        finish();
                    })
                    .setNegativeButton("Cancel", null)
                    .show();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }
}

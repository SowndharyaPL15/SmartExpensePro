package com.smartexpensepro.utils;

import android.content.Context;
import android.os.Environment;
import android.util.Log;

import com.smartexpensepro.models.Transaction;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.List;

public class ExcelExporter {

    private static final String TAG = "ExcelExporter";

    public interface ExportCallback {
        void onSuccess(String filePath);
        void onError(String message);
    }

    public static void exportToExcel(Context context, List<Transaction> transactions, ExportCallback callback) {
        new Thread(() -> {
            try {
                File downloadsDir = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS);
                if (!downloadsDir.exists()) downloadsDir.mkdirs();

                File file = new File(downloadsDir, "SmartExpenseReport.csv");
                FileWriter writer = new FileWriter(file);

                // Write header
                writer.append("#,Amount (Rs),Category,Description,Date,Source\n");

                double total = 0;
                for (int i = 0; i < transactions.size(); i++) {
                    Transaction t = transactions.get(i);
                    String desc = t.getDescription() != null ? t.getDescription().replace(",", ";") : "";
                    writer.append(String.valueOf(i + 1)).append(",");
                    writer.append(String.format("%.2f", t.getAmount())).append(",");
                    writer.append(t.getCategory()).append(",");
                    writer.append(desc).append(",");
                    writer.append(t.getDate()).append(",");
                    writer.append(t.isAutoDetected() ? "Auto (SMS)" : "Manual").append("\n");
                    total += t.getAmount();
                }

                // Write total
                writer.append("\nTOTAL,").append(String.format("%.2f", total)).append("\n");

                writer.flush();
                writer.close();

                callback.onSuccess(file.getAbsolutePath());

            } catch (IOException e) {
                Log.e(TAG, "Export error: " + e.getMessage());
                callback.onError("Export failed: " + e.getMessage());
            }
        }).start();
    }
}

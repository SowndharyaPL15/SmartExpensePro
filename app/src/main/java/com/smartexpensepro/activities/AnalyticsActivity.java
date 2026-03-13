package com.smartexpensepro.activities;

import android.graphics.Color;
import android.os.Bundle;
import android.view.View;

import androidx.appcompat.app.AppCompatActivity;

import com.github.mikephil.charting.animation.Easing;
import com.github.mikephil.charting.components.Legend;
import com.github.mikephil.charting.data.PieData;
import com.github.mikephil.charting.data.PieDataSet;
import com.github.mikephil.charting.data.PieEntry;
import com.github.mikephil.charting.formatter.PercentFormatter;
import com.smartexpensepro.database.DatabaseHelper;
import com.smartexpensepro.databinding.ActivityAnalyticsBinding;
import com.smartexpensepro.utils.SessionManager;

import java.util.ArrayList;
import java.util.List;

public class AnalyticsActivity extends AppCompatActivity {

    private ActivityAnalyticsBinding binding;
    private DatabaseHelper dbHelper;
    private SessionManager sessionManager;

    private static final String[] CATEGORIES = {
            "Food", "Travel", "Bills", "Shopping",
            "Health", "Entertainment", "Education", "Others"
    };

    private static final int[] CATEGORY_COLORS = {
            0xFFFF6B6B,   // Food        - red
            0xFF4ECDC4,   // Travel      - teal
            0xFF45B7D1,   // Bills       - blue
            0xFFFFA07A,   // Shopping    - salmon
            0xFF98D8C8,   // Health      - mint
            0xFFFFD700,   // Entertainment - gold
            0xFF9B59B6,   // Education   - purple
            0xFFBDC3C7    // Others      - grey
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityAnalyticsBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        setSupportActionBar(binding.toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setTitle("Analytics");
        }

        dbHelper = DatabaseHelper.getInstance(this);
        sessionManager = new SessionManager(this);

        setupPieChart();
        loadAnalyticsData();
    }

    private void setupPieChart() {
        binding.pieChart.setUsePercentValues(true);
        binding.pieChart.getDescription().setEnabled(false);
        binding.pieChart.setExtraOffsets(5, 10, 5, 5);
        binding.pieChart.setDragDecelerationFrictionCoef(0.95f);
        binding.pieChart.setDrawHoleEnabled(true);
        binding.pieChart.setHoleColor(Color.TRANSPARENT);
        binding.pieChart.setHoleRadius(55f);
        binding.pieChart.setTransparentCircleRadius(61f);
        binding.pieChart.setDrawCenterText(true);
        binding.pieChart.setCenterText("Expenses\nby Category");
        binding.pieChart.setCenterTextSize(14f);
        binding.pieChart.setRotationAngle(0);
        binding.pieChart.setRotationEnabled(true);
        binding.pieChart.setHighlightPerTapEnabled(true);

        Legend legend = binding.pieChart.getLegend();
        legend.setVerticalAlignment(Legend.LegendVerticalAlignment.BOTTOM);
        legend.setHorizontalAlignment(Legend.LegendHorizontalAlignment.CENTER);
        legend.setOrientation(Legend.LegendOrientation.HORIZONTAL);
        legend.setDrawInside(false);
        legend.setXEntrySpace(7f);
        legend.setYEntrySpace(0f);
        legend.setYOffset(0f);
        legend.setTextSize(11f);
        legend.setWordWrapEnabled(true);
    }

    private void loadAnalyticsData() {
        int userId = sessionManager.getUserId();
        double total = dbHelper.getTotalExpense(userId);

        double[] totals = new double[CATEGORIES.length];
        for (int i = 0; i < CATEGORIES.length; i++) {
            totals[i] = dbHelper.getCategoryTotal(userId, CATEGORIES[i]);
        }

        // Build pie entries only for categories that have data
        List<PieEntry> entries = new ArrayList<>();
        List<Integer> colors = new ArrayList<>();

        for (int i = 0; i < CATEGORIES.length; i++) {
            if (totals[i] > 0) {
                entries.add(new PieEntry((float) totals[i], CATEGORIES[i]));
                colors.add(CATEGORY_COLORS[i]);
            }
        }

        if (entries.isEmpty()) {
            binding.tvNoData.setVisibility(View.VISIBLE);
            binding.pieChart.setVisibility(View.GONE);
            binding.cardSummary.setVisibility(View.GONE);
            return;
        }

        // Update summary card
        binding.tvTotalExpense.setText(String.format("₹ %.2f", total));
        binding.tvFoodAmount.setText(String.format("₹ %.2f", totals[0]));
        binding.tvTravelAmount.setText(String.format("₹ %.2f", totals[1]));
        binding.tvBillsAmount.setText(String.format("₹ %.2f", totals[2]));
        binding.tvShoppingAmount.setText(String.format("₹ %.2f", totals[3]));
        binding.tvHealthAmount.setText(String.format("₹ %.2f", totals[4]));
        binding.tvEntertainmentAmount.setText(String.format("₹ %.2f", totals[5]));
        binding.tvEducationAmount.setText(String.format("₹ %.2f", totals[6]));
        binding.tvOthersAmount.setText(String.format("₹ %.2f", totals[7]));

        // Build chart
        PieDataSet dataSet = new PieDataSet(entries, "");
        dataSet.setSliceSpace(3f);
        dataSet.setSelectionShift(5f);
        dataSet.setColors(colors);
        dataSet.setValueLinePart1OffsetPercentage(80f);
        dataSet.setValueLinePart1Length(0.2f);
        dataSet.setValueLinePart2Length(0.4f);
        dataSet.setYValuePosition(PieDataSet.ValuePosition.OUTSIDE_SLICE);

        PieData data = new PieData(dataSet);
        data.setValueFormatter(new PercentFormatter(binding.pieChart));
        data.setValueTextSize(11f);
        data.setValueTextColor(Color.BLACK);

        binding.pieChart.setData(data);
        binding.pieChart.highlightValues(null);
        binding.pieChart.invalidate();
        binding.pieChart.animateY(1400, Easing.EaseInOutQuad);
    }

    @Override
    public boolean onSupportNavigateUp() {
        finish();
        return true;
    }
}

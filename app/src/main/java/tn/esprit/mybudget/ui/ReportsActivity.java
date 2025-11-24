package tn.esprit.mybudget.ui;

import android.os.Bundle;
import android.widget.Button;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.ViewModelProvider;
import tn.esprit.mybudget.R;
import tn.esprit.mybudget.ui.viewmodel.ReportsViewModel;
import com.github.mikephil.charting.charts.PieChart;
import com.github.mikephil.charting.data.PieData;
import com.github.mikephil.charting.data.PieDataSet;
import com.github.mikephil.charting.data.PieEntry;
import com.github.mikephil.charting.utils.ColorTemplate;
import java.util.List;

public class ReportsActivity extends AppCompatActivity {

    private ReportsViewModel viewModel;
    private PieChart pieChart;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_reports);

        androidx.appcompat.widget.Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setTitle("Reports");
        }

        pieChart = findViewById(R.id.pieChart);
        Button btnExport = findViewById(R.id.btnExport);

        viewModel = new ViewModelProvider(this).get(ReportsViewModel.class);
        viewModel.loadReportData(1); // Hardcoded User ID

        viewModel.getCategoryData().observe(this, entries -> {
            setupPieChart(entries);
        });

        btnExport.setOnClickListener(v -> {
            // TODO: Implement CSV Export
            Toast.makeText(this, "Exporting...", Toast.LENGTH_SHORT).show();
        });
    }

    private void setupPieChart(List<PieEntry> entries) {
        PieDataSet dataSet = new PieDataSet(entries, "Spending");
        dataSet.setColors(ColorTemplate.MATERIAL_COLORS);
        dataSet.setValueTextSize(12f);

        PieData data = new PieData(dataSet);
        pieChart.setData(data);
        pieChart.getDescription().setEnabled(false);
        pieChart.invalidate(); // Refresh
    }

    @Override
    public boolean onSupportNavigateUp() {
        finish();
        return true;
    }
}

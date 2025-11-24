package tn.esprit.mybudget.ui;

import android.content.Intent;
import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.floatingactionbutton.FloatingActionButton;

import tn.esprit.mybudget.R;
import tn.esprit.mybudget.ui.viewmodel.TransactionViewModel;

public class MainActivity extends AppCompatActivity {

    private TransactionViewModel viewModel;
    private TransactionAdapter adapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        RecyclerView rvTransactions = findViewById(R.id.rvTransactions);
        FloatingActionButton fabAdd = findViewById(R.id.fabAdd);

        adapter = new TransactionAdapter();
        rvTransactions.setLayoutManager(new LinearLayoutManager(this));
        rvTransactions.setAdapter(adapter);

        viewModel = new ViewModelProvider(this).get(TransactionViewModel.class);

        // Load transactions for user 1 (Hardcoded for MVP)
        viewModel.loadTransactions(1);

        viewModel.getTransactions().observe(this, transactions -> {
            adapter.setTransactions(transactions);
        });

        fabAdd.setOnClickListener(v -> {
            startActivity(new Intent(this, AddTransactionActivity.class));
        });

        findViewById(R.id.btnReports).setOnClickListener(v -> {
            startActivity(new Intent(this, ReportsActivity.class));
        });
    }

    @Override
    protected void onResume() {
        super.onResume();
        // Reload data when returning from Add Activity
        viewModel.loadTransactions(1);
    }
}
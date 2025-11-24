package tn.esprit.mybudget.ui;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.floatingactionbutton.FloatingActionButton;

import tn.esprit.mybudget.R;
import tn.esprit.mybudget.ui.viewmodel.TransactionViewModel;

public class WalletFragment extends Fragment {

    private TransactionViewModel viewModel;
    private TransactionAdapter adapter;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
            @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_wallet, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        RecyclerView rvTransactions = view.findViewById(R.id.rvTransactions);
        FloatingActionButton fabAdd = view.findViewById(R.id.fabAdd);
        View btnReports = view.findViewById(R.id.btnReports);

        adapter = new TransactionAdapter();
        rvTransactions.setLayoutManager(new LinearLayoutManager(getContext()));
        rvTransactions.setAdapter(adapter);

        viewModel = new ViewModelProvider(this).get(TransactionViewModel.class);

        // Load transactions for user 1 (Hardcoded for MVP)
        viewModel.loadTransactions(1);

        viewModel.getTransactions().observe(getViewLifecycleOwner(), transactions -> {
            adapter.setTransactions(transactions);
        });

        fabAdd.setOnClickListener(v -> {
            startActivity(new Intent(getContext(), AddTransactionActivity.class));
        });

        btnReports.setOnClickListener(v -> {
            startActivity(new Intent(getContext(), ReportsActivity.class));
        });
    }

    @Override
    public void onResume() {
        super.onResume();
        // Reload data when returning from Add Activity
        if (viewModel != null) {
            viewModel.loadTransactions(1);
        }
    }
}

package tn.esprit.mybudget.ui.transaction;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
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
import tn.esprit.mybudget.ui.report.ReportsActivity;

public class WalletFragment extends Fragment {

    private static final String PREFS_NAME = "UserPrefs";
    private static final String KEY_USER_ID = "userId";

    private TransactionViewModel viewModel;
    private TransactionAdapter adapter;
    private int currentUserId = 1; // Default fallback

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
            @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_wallet, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        // Get the logged-in user's ID
        if (getContext() != null) {
            SharedPreferences prefs = getContext().getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
            currentUserId = prefs.getInt(KEY_USER_ID, 1);
        }

        RecyclerView rvTransactions = view.findViewById(R.id.rvTransactions);
        FloatingActionButton fabAdd = view.findViewById(R.id.fabAdd);
        View btnReports = view.findViewById(R.id.btnReports);

        adapter = new TransactionAdapter();
        rvTransactions.setLayoutManager(new LinearLayoutManager(getContext()));
        rvTransactions.setAdapter(adapter);

        viewModel = new ViewModelProvider(this).get(TransactionViewModel.class);

        // Load transactions for the current user
        viewModel.loadTransactions(currentUserId);

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
            viewModel.loadTransactions(currentUserId);
        }
    }
}

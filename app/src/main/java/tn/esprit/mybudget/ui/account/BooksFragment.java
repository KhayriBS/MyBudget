package tn.esprit.mybudget.ui.account;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.textfield.TextInputEditText;

import tn.esprit.mybudget.R;
import tn.esprit.mybudget.data.entity.Account;

public class BooksFragment extends Fragment {

    private AccountViewModel viewModel;
    private AccountAdapter adapter;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
            @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_books, container, false);

        RecyclerView rvAccounts = view.findViewById(R.id.rvAccounts);
        FloatingActionButton fabAdd = view.findViewById(R.id.fabAddAccount);

        // Setup RecyclerView if it exists in layout (need to update layout first)
        // For now, assuming layout needs update.

        return view;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        RecyclerView rvAccounts = view.findViewById(R.id.rvAccounts);
        FloatingActionButton fabAdd = view.findViewById(R.id.fabAddAccount);

        adapter = new AccountAdapter();
        rvAccounts.setLayoutManager(new LinearLayoutManager(getContext()));
        rvAccounts.setAdapter(adapter);

        viewModel = new ViewModelProvider(this).get(AccountViewModel.class);
        viewModel.getAllAccounts().observe(getViewLifecycleOwner(), accounts -> {
            adapter.setAccounts(accounts);
        });

        fabAdd.setOnClickListener(v -> showAddAccountDialog());
    }

    private void showAddAccountDialog() {
        View view = LayoutInflater.from(getContext()).inflate(R.layout.dialog_add_account, null);
        TextInputEditText etName = view.findViewById(R.id.etAccountName);
        TextInputEditText etBalance = view.findViewById(R.id.etInitialBalance);
        RadioGroup rgType = view.findViewById(R.id.rgType); // Note: ID in layout is rgAccountType
        RadioButton rbCash = view.findViewById(R.id.rbCash);
        RadioButton rbCard = view.findViewById(R.id.rbCard);

        new AlertDialog.Builder(getContext())
                .setTitle("Add Account")
                .setView(view)
                .setPositiveButton("Add", (dialog, which) -> {
                    String name = etName.getText().toString();
                    String balanceStr = etBalance.getText().toString();

                    if (name.isEmpty()) {
                        Toast.makeText(getContext(), "Name required", Toast.LENGTH_SHORT).show();
                        return;
                    }

                    double balance = 0;
                    if (!balanceStr.isEmpty()) {
                        balance = Double.parseDouble(balanceStr);
                    }

                    String type = "CASH";
                    if (rbCard.isChecked())
                        type = "CARD";
                    else if (!rbCash.isChecked())
                        type = "SAVINGS"; // Assuming 3rd option

                    Account account = new Account(name, type, balance, "TND");
                    viewModel.insert(account);
                })
                .setNegativeButton("Cancel", null)
                .show();
    }
}

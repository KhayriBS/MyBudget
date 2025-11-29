package tn.esprit.mybudget.ui.transaction;

import android.app.DatePickerDialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.Spinner;
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

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;

import tn.esprit.mybudget.R;
import tn.esprit.mybudget.data.entity.Account;
import tn.esprit.mybudget.data.entity.Category;
import tn.esprit.mybudget.data.entity.Transaction;
import tn.esprit.mybudget.data.entity.TransactionWithAccount;
import tn.esprit.mybudget.ui.account.AccountViewModel;
import tn.esprit.mybudget.ui.category.CategoryViewModel;
import tn.esprit.mybudget.ui.report.ReportsActivity;

public class WalletFragment extends Fragment {

    private static final String PREFS_NAME = "UserPrefs";
    private static final String KEY_USER_ID = "userId";

    private TransactionViewModel viewModel;
    private CategoryViewModel categoryViewModel;
    private AccountViewModel accountViewModel;
    private TransactionAdapter adapter;
    private int currentUserId = 1; // Default fallback

    private List<Category> categoryList = new ArrayList<>();
    private List<Account> accountList = new ArrayList<>();

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
        categoryViewModel = new ViewModelProvider(this).get(CategoryViewModel.class);
        accountViewModel = new ViewModelProvider(this).get(AccountViewModel.class);

        // Load transactions for the current user
        viewModel.loadTransactions(currentUserId);

        // Load categories
        categoryViewModel.loadCategories();
        categoryViewModel.getCategories().observe(getViewLifecycleOwner(), categories -> {
            categoryList = categories;
        });

        // Load accounts
        accountViewModel.getAllAccounts().observe(getViewLifecycleOwner(), accounts -> {
            accountList = accounts;
        });

        viewModel.getTransactionsWithAccount().observe(getViewLifecycleOwner(), transactions -> {
            adapter.setTransactions(transactions);
        });

        // Click to edit transaction
        adapter.setOnTransactionClickListener(this::showEditTransactionDialog);

        // Delete transaction
        adapter.setOnTransactionDeleteListener(this::showDeleteConfirmation);

        fabAdd.setOnClickListener(v -> {
            startActivity(new Intent(getContext(), AddTransactionActivity.class));
        });

        btnReports.setOnClickListener(v -> {
            startActivity(new Intent(getContext(), ReportsActivity.class));
        });
    }

    private void showEditTransactionDialog(TransactionWithAccount transactionWithAccount) {
        if (getContext() == null)
            return;

        Transaction transaction = transactionWithAccount.transaction;
        final double oldAmount = transaction.amount;
        final int oldAccountId = transaction.accountId;

        View dialogView = LayoutInflater.from(getContext()).inflate(R.layout.dialog_edit_transaction, null);
        TextInputEditText etAmount = dialogView.findViewById(R.id.etEditAmount);
        TextInputEditText etNote = dialogView.findViewById(R.id.etEditNote);
        Spinner spinnerCategory = dialogView.findViewById(R.id.spinnerEditCategory);
        Spinner spinnerAccount = dialogView.findViewById(R.id.spinnerEditAccount);
        Button btnSelectDate = dialogView.findViewById(R.id.btnSelectDate);

        // Pre-fill current values
        etAmount.setText(String.valueOf(transaction.amount));
        etNote.setText(transaction.note);

        // Setup Category Spinner
        List<String> categoryNames = new ArrayList<>();
        int selectedCategoryIndex = 0;
        for (int i = 0; i < categoryList.size(); i++) {
            Category cat = categoryList.get(i);
            categoryNames.add(cat.name);
            if (cat.id == transaction.categoryId) {
                selectedCategoryIndex = i;
            }
        }
        ArrayAdapter<String> categoryAdapter = new ArrayAdapter<>(getContext(),
                android.R.layout.simple_spinner_item, categoryNames);
        categoryAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerCategory.setAdapter(categoryAdapter);
        spinnerCategory.setSelection(selectedCategoryIndex);

        // Setup Account Spinner
        List<String> accountNames = new ArrayList<>();
        int selectedAccountIndex = 0;
        for (int i = 0; i < accountList.size(); i++) {
            Account acc = accountList.get(i);
            accountNames.add(acc.name);
            if (acc.id == transaction.accountId) {
                selectedAccountIndex = i;
            }
        }
        ArrayAdapter<String> accountAdapter = new ArrayAdapter<>(getContext(),
                android.R.layout.simple_spinner_item, accountNames);
        accountAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerAccount.setAdapter(accountAdapter);
        spinnerAccount.setSelection(selectedAccountIndex);

        // Setup Date
        final long[] selectedDate = { transaction.date };
        SimpleDateFormat sdf = new SimpleDateFormat("MMM dd, yyyy", Locale.getDefault());
        btnSelectDate.setText(sdf.format(new Date(selectedDate[0])));

        btnSelectDate.setOnClickListener(v -> {
            Calendar cal = Calendar.getInstance();
            cal.setTimeInMillis(selectedDate[0]);

            DatePickerDialog datePickerDialog = new DatePickerDialog(getContext(),
                    (view, year, month, dayOfMonth) -> {
                        Calendar newCal = Calendar.getInstance();
                        newCal.set(year, month, dayOfMonth);
                        selectedDate[0] = newCal.getTimeInMillis();
                        btnSelectDate.setText(sdf.format(new Date(selectedDate[0])));
                    },
                    cal.get(Calendar.YEAR),
                    cal.get(Calendar.MONTH),
                    cal.get(Calendar.DAY_OF_MONTH));
            datePickerDialog.show();
        });

        new AlertDialog.Builder(getContext())
                .setTitle("Edit Transaction")
                .setView(dialogView)
                .setPositiveButton("Save", (dialog, which) -> {
                    String amountStr = etAmount.getText().toString();
                    String note = etNote.getText().toString();

                    if (amountStr.isEmpty()) {
                        Toast.makeText(getContext(), "Amount required", Toast.LENGTH_SHORT).show();
                        return;
                    }

                    if (categoryList.isEmpty()) {
                        Toast.makeText(getContext(), "No categories available", Toast.LENGTH_SHORT).show();
                        return;
                    }

                    if (accountList.isEmpty()) {
                        Toast.makeText(getContext(), "No accounts available", Toast.LENGTH_SHORT).show();
                        return;
                    }

                    // Get selected category
                    int selectedCatPos = spinnerCategory.getSelectedItemPosition();
                    if (selectedCatPos >= 0 && selectedCatPos < categoryList.size()) {
                        transaction.categoryId = categoryList.get(selectedCatPos).id;
                    }

                    // Get selected account
                    int selectedAccPos = spinnerAccount.getSelectedItemPosition();
                    int newAccountId = oldAccountId;
                    if (selectedAccPos >= 0 && selectedAccPos < accountList.size()) {
                        newAccountId = accountList.get(selectedAccPos).id;
                        transaction.accountId = newAccountId;
                    }

                    transaction.amount = Double.parseDouble(amountStr);
                    transaction.note = note;
                    transaction.date = selectedDate[0];

                    viewModel.updateTransaction(transaction, oldAmount, oldAccountId);
                    Toast.makeText(getContext(), "Transaction updated", Toast.LENGTH_SHORT).show();
                })
                .setNegativeButton("Cancel", null)
                .show();
    }

    private void showDeleteConfirmation(TransactionWithAccount transactionWithAccount) {
        if (getContext() == null)
            return;

        new AlertDialog.Builder(getContext())
                .setTitle("Delete Transaction")
                .setMessage(
                        "Are you sure you want to delete this transaction? The amount will be restored to your account.")
                .setPositiveButton("Delete", (dialog, which) -> {
                    viewModel.deleteTransaction(transactionWithAccount.transaction);
                    Toast.makeText(getContext(), "Transaction deleted", Toast.LENGTH_SHORT).show();
                })
                .setNegativeButton("Cancel", null)
                .show();
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

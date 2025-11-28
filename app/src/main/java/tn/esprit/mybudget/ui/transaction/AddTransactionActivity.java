package tn.esprit.mybudget.ui.transaction;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.widget.Button;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.ViewModelProvider;
import tn.esprit.mybudget.R;
import tn.esprit.mybudget.data.entity.Transaction;
import tn.esprit.mybudget.ui.category.CategoryViewModel;
import tn.esprit.mybudget.ui.account.AccountViewModel;

import com.google.android.material.textfield.TextInputEditText;

public class AddTransactionActivity extends AppCompatActivity {

    private static final String PREFS_NAME = "UserPrefs";
    private static final String KEY_USER_ID = "userId";

    private CategoryViewModel categoryViewModel;
    private AccountViewModel accountViewModel;
    private android.widget.Spinner spinnerCategory;
    private android.widget.Spinner spinnerAccount;
    private java.util.List<tn.esprit.mybudget.data.entity.Category> categoryList = new java.util.ArrayList<>();
    private java.util.List<tn.esprit.mybudget.data.entity.Account> accountList = new java.util.ArrayList<>();

    private TransactionViewModel viewModel;
    private TextInputEditText etAmount, etNote;
    private int selectedCategoryId = 1;
    private int selectedAccountId = 1;
    private int currentUserId = 1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_transaction);

        // Get the logged-in user's ID
        SharedPreferences prefs = getSharedPreferences(PREFS_NAME, MODE_PRIVATE);
        currentUserId = prefs.getInt(KEY_USER_ID, 1);

        androidx.appcompat.widget.Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setTitle("Add Transaction");
        }

        viewModel = new ViewModelProvider(this).get(TransactionViewModel.class);
        categoryViewModel = new ViewModelProvider(this).get(CategoryViewModel.class);
        accountViewModel = new ViewModelProvider(this).get(AccountViewModel.class);

        etAmount = findViewById(R.id.etAmount);
        etNote = findViewById(R.id.etNote);
        spinnerCategory = findViewById(R.id.spinnerCategory);
        spinnerAccount = findViewById(R.id.spinnerAccount);
        Button btnSave = findViewById(R.id.btnSaveTransaction);

        // Load categories
        categoryViewModel.loadCategories();
        categoryViewModel.getCategories().observe(this, categories -> {
            categoryList = categories;
            java.util.List<String> names = new java.util.ArrayList<>();
            for (tn.esprit.mybudget.data.entity.Category c : categories) {
                names.add(c.name);
            }
            android.widget.ArrayAdapter<String> adapter = new android.widget.ArrayAdapter<>(this,
                    android.R.layout.simple_spinner_item, names);
            adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
            spinnerCategory.setAdapter(adapter);
        });

        // Load accounts
        accountViewModel.getAllAccounts().observe(this, accounts -> {
            accountList = accounts;
            java.util.List<String> names = new java.util.ArrayList<>();
            for (tn.esprit.mybudget.data.entity.Account a : accounts) {
                names.add(a.name);
            }
            android.widget.ArrayAdapter<String> adapter = new android.widget.ArrayAdapter<>(this,
                    android.R.layout.simple_spinner_item, names);
            adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
            spinnerAccount.setAdapter(adapter);
        });

        btnSave.setOnClickListener(v -> {
            String amountStr = etAmount.getText().toString();
            String note = etNote.getText().toString();

            if (amountStr.isEmpty()) {
                Toast.makeText(this, "Enter amount", Toast.LENGTH_SHORT).show();
                return;
            }

            if (categoryList.isEmpty()) {
                Toast.makeText(this, "No categories available", Toast.LENGTH_SHORT).show();
                return;
            }

            if (accountList.isEmpty()) {
                Toast.makeText(this, "No accounts available", Toast.LENGTH_SHORT).show();
                return;
            }

            double amount = Double.parseDouble(amountStr);
            long date = System.currentTimeMillis();

            int selectedCatPos = spinnerCategory.getSelectedItemPosition();
            if (selectedCatPos >= 0 && selectedCatPos < categoryList.size()) {
                selectedCategoryId = categoryList.get(selectedCatPos).id;
            }

            int selectedAccPos = spinnerAccount.getSelectedItemPosition();
            tn.esprit.mybudget.data.entity.Account selectedAccount = null;
            if (selectedAccPos >= 0 && selectedAccPos < accountList.size()) {
                selectedAccount = accountList.get(selectedAccPos);
                selectedAccountId = selectedAccount.id;
            }

            Transaction transaction = new Transaction(currentUserId, selectedCategoryId, selectedAccountId, amount,
                    date, note);
            viewModel.addTransaction(transaction);

            // Deduct amount from the selected account balance
            if (selectedAccount != null) {
                selectedAccount.balance -= amount;
                accountViewModel.update(selectedAccount);
            }

            Toast.makeText(this, "Transaction Added", Toast.LENGTH_SHORT).show();
            finish();
        });
    }

    @Override
    public boolean onSupportNavigateUp() {
        finish();
        return true;
    }
}

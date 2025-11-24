package tn.esprit.mybudget.ui;

import android.os.Bundle;
import android.widget.Button;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.ViewModelProvider;
import tn.esprit.mybudget.R;
import tn.esprit.mybudget.data.entity.Transaction;
import tn.esprit.mybudget.ui.viewmodel.TransactionViewModel;

import com.google.android.material.textfield.TextInputEditText;

public class AddTransactionActivity extends AppCompatActivity {

    private tn.esprit.mybudget.ui.viewmodel.CategoryViewModel categoryViewModel;
    private android.widget.Spinner spinnerCategory;
    private java.util.List<tn.esprit.mybudget.data.entity.Category> categoryList = new java.util.ArrayList<>();

    private TransactionViewModel viewModel;
    private TextInputEditText etAmount, etNote;
    private int selectedCategoryId = 1;
    private int currentUserId = 1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_transaction);

        androidx.appcompat.widget.Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setTitle("Add Transaction");
        }

        viewModel = new ViewModelProvider(this).get(TransactionViewModel.class);
        categoryViewModel = new ViewModelProvider(this).get(tn.esprit.mybudget.ui.viewmodel.CategoryViewModel.class);

        etAmount = findViewById(R.id.etAmount);
        etNote = findViewById(R.id.etNote);
        spinnerCategory = findViewById(R.id.spinnerCategory);
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

            double amount = Double.parseDouble(amountStr);
            long date = System.currentTimeMillis();

            int selectedPosition = spinnerCategory.getSelectedItemPosition();
            if (selectedPosition >= 0 && selectedPosition < categoryList.size()) {
                selectedCategoryId = categoryList.get(selectedPosition).id;
            }

            Transaction transaction = new Transaction(currentUserId, selectedCategoryId, amount, date, note);
            viewModel.addTransaction(transaction);

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

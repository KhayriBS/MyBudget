package tn.esprit.mybudget.ui;

import android.os.Bundle;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.textfield.TextInputEditText;

import java.util.ArrayList;
import java.util.List;

import tn.esprit.mybudget.R;
import tn.esprit.mybudget.data.entity.Budget;
import tn.esprit.mybudget.data.entity.Category;
import tn.esprit.mybudget.ui.viewmodel.BudgetViewModel;
import tn.esprit.mybudget.ui.viewmodel.CategoryViewModel;

public class BudgetActivity extends AppCompatActivity {

    private BudgetViewModel budgetViewModel;
    private CategoryViewModel categoryViewModel;
    private BudgetAdapter adapter;
    private List<Category> categoryList = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_budget);

        androidx.appcompat.widget.Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setTitle("Budget");
        }

        RecyclerView rvBudgets = findViewById(R.id.rvBudgets);
        FloatingActionButton fabAdd = findViewById(R.id.fabAddBudget);

        adapter = new BudgetAdapter();
        rvBudgets.setLayoutManager(new LinearLayoutManager(this));
        rvBudgets.setAdapter(adapter);

        budgetViewModel = new ViewModelProvider(this).get(BudgetViewModel.class);
        categoryViewModel = new ViewModelProvider(this).get(CategoryViewModel.class);

        // Load Categories for the Add Dialog
        categoryViewModel.loadCategories();
        categoryViewModel.getCategories().observe(this, categories -> {
            categoryList = categories;
        });

        budgetViewModel.getAllBudgets().observe(this, budgets -> {
            adapter.setBudgets(budgets);
        });

        fabAdd.setOnClickListener(v -> showAddBudgetDialog());
    }

    private void showAddBudgetDialog() {
        if (categoryList.isEmpty()) {
            Toast.makeText(this, "No categories available", Toast.LENGTH_SHORT).show();
            return;
        }

        String[] categoryNames = new String[categoryList.size()];
        for (int i = 0; i < categoryList.size(); i++) {
            categoryNames[i] = categoryList.get(i).name;
        }

        final int[] selectedCategoryIndex = { 0 };

        android.view.View view = android.view.LayoutInflater.from(this).inflate(R.layout.dialog_add_account, null);
        // Reusing dialog_add_account layout for simplicity, but ideally should have its
        // own.
        // Let's create a simple input dialog programmatically or use a custom layout if
        // I had one.
        // Since I don't have dialog_add_budget.xml, I'll build a simple view here.

        android.widget.LinearLayout layout = new android.widget.LinearLayout(this);
        layout.setOrientation(android.widget.LinearLayout.VERTICAL);
        layout.setPadding(50, 40, 50, 10);

        final android.widget.Spinner spinner = new android.widget.Spinner(this);
        android.widget.ArrayAdapter<String> spinnerAdapter = new android.widget.ArrayAdapter<>(this,
                android.R.layout.simple_spinner_item, categoryNames);
        spinnerAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinner.setAdapter(spinnerAdapter);
        layout.addView(spinner);

        final android.widget.EditText etLimit = new android.widget.EditText(this);
        etLimit.setHint("Limit Amount");
        etLimit.setInputType(
                android.text.InputType.TYPE_CLASS_NUMBER | android.text.InputType.TYPE_NUMBER_FLAG_DECIMAL);
        layout.addView(etLimit);

        new AlertDialog.Builder(this)
                .setTitle("Set Budget Limit")
                .setView(layout)
                .setPositiveButton("Save", (dialog, which) -> {
                    String limitStr = etLimit.getText().toString();
                    if (limitStr.isEmpty())
                        return;

                    double limit = Double.parseDouble(limitStr);
                    int categoryId = categoryList.get(spinner.getSelectedItemPosition()).id;

                    Budget budget = new Budget(1, categoryId, limit, "MONTHLY"); // Hardcoded UserID
                    budgetViewModel.insert(budget);
                })
                .setNegativeButton("Cancel", null)
                .show();
    }

    @Override
    public boolean onSupportNavigateUp() {
        finish();
        return true;
    }
}

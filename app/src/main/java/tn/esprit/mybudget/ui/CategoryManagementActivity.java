package tn.esprit.mybudget.ui;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.textfield.TextInputEditText;

import tn.esprit.mybudget.R;
import tn.esprit.mybudget.data.entity.Category;
import tn.esprit.mybudget.ui.viewmodel.CategoryViewModel;

public class CategoryManagementActivity extends AppCompatActivity {

    private CategoryViewModel viewModel;
    private CategoryAdapter adapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_category_management);

        androidx.appcompat.widget.Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setTitle("Gestion des catégories");
        }

        RecyclerView rvCategories = findViewById(R.id.rvCategories);
        FloatingActionButton fabAdd = findViewById(R.id.fabAddCategory);

        adapter = new CategoryAdapter();
        rvCategories.setLayoutManager(new LinearLayoutManager(this));
        rvCategories.setAdapter(adapter);

        viewModel = new ViewModelProvider(this).get(CategoryViewModel.class);
        viewModel.loadCategories();

        viewModel.getCategories().observe(this, categories -> {
            adapter.setCategories(categories);
        });

        fabAdd.setOnClickListener(v -> showAddCategoryDialog());

        adapter.setOnDeleteClickListener(category -> {
            new AlertDialog.Builder(this)
                    .setTitle("Delete Category")
                    .setMessage("Are you sure you want to delete " + category.name + "?")
                    .setPositiveButton("Yes", (dialog, which) -> viewModel.deleteCategory(category))
                    .setNegativeButton("No", null)
                    .show();
        });
    }

    private void showAddCategoryDialog() {
        View view = LayoutInflater.from(this).inflate(R.layout.dialog_add_category, null);
        TextInputEditText etName = view.findViewById(R.id.etCategoryName);
        RadioGroup rgType = view.findViewById(R.id.rgType);
        RadioButton rbExpense = view.findViewById(R.id.rbExpense);

        new AlertDialog.Builder(this)
                .setTitle("Add Category")
                .setView(view)
                .setPositiveButton("Add", (dialog, which) -> {
                    String name = etName.getText().toString();
                    if (name.isEmpty()) {
                        Toast.makeText(this, "Name required", Toast.LENGTH_SHORT).show();
                        return;
                    }
                    String type = rbExpense.isChecked() ? "EXPENSE" : "INCOME";
                    // Default icon and color for now
                    Category category = new Category(name, type, "default_icon", "#000000");
                    viewModel.addCategory(category);
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

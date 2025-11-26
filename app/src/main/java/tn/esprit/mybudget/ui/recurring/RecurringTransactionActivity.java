package tn.esprit.mybudget.ui.recurring;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.floatingactionbutton.FloatingActionButton;

import java.util.ArrayList;
import java.util.List;

import tn.esprit.mybudget.R;
import tn.esprit.mybudget.data.entity.RecurringTransaction;

public class RecurringTransactionActivity extends AppCompatActivity {

    private RecurringTransactionViewModel viewModel;
    private RecurringAdapter adapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_recurring_transaction);

        androidx.appcompat.widget.Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setTitle("Recurring Transactions");
        }

        viewModel = new ViewModelProvider(this).get(RecurringTransactionViewModel.class);

        RecyclerView rvRecurring = findViewById(R.id.rvRecurringTransactions);
        rvRecurring.setLayoutManager(new LinearLayoutManager(this));
        adapter = new RecurringAdapter();
        rvRecurring.setAdapter(adapter);

        FloatingActionButton fabAdd = findViewById(R.id.fabAddRecurring);
        fabAdd.setOnClickListener(v -> showAddDialog());

        viewModel.getAllRecurringTransactions().observe(this, transactions -> {
            adapter.setTransactions(transactions);
        });
    }

    private void showAddDialog() {
        android.widget.LinearLayout layout = new android.widget.LinearLayout(this);
        layout.setOrientation(android.widget.LinearLayout.VERTICAL);
        layout.setPadding(50, 40, 50, 10);

        final EditText etNote = new EditText(this);
        etNote.setHint("Note (e.g., Rent)");
        layout.addView(etNote);

        final EditText etAmount = new EditText(this);
        etAmount.setHint("Amount");
        etAmount.setInputType(
                android.text.InputType.TYPE_CLASS_NUMBER | android.text.InputType.TYPE_NUMBER_FLAG_DECIMAL);
        layout.addView(etAmount);

        final Spinner spFrequency = new Spinner(this);
        ArrayAdapter<String> adapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item,
                new String[] { "Daily", "Weekly", "Monthly" });
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spFrequency.setAdapter(adapter);
        layout.addView(spFrequency);

        new AlertDialog.Builder(this)
                .setTitle("Add Recurring Transaction")
                .setView(layout)
                .setPositiveButton("Save", (dialog, which) -> {
                    String note = etNote.getText().toString();
                    String amountStr = etAmount.getText().toString();
                    String frequency = spFrequency.getSelectedItem().toString();

                    if (note.isEmpty() || amountStr.isEmpty()) {
                        Toast.makeText(this, "Please fill all fields", Toast.LENGTH_SHORT).show();
                        return;
                    }

                    double amount = Double.parseDouble(amountStr);
                    // Hardcoded categoryId=1, accountId=1 for simplicity in this step
                    RecurringTransaction transaction = new RecurringTransaction(1, note, amount, "Expense", 1, 1,
                            frequency, System.currentTimeMillis());
                    viewModel.insert(transaction);
                })
                .setNegativeButton("Cancel", null)
                .show();
    }

    @Override
    public boolean onSupportNavigateUp() {
        finish();
        return true;
    }

    private static class RecurringAdapter extends RecyclerView.Adapter<RecurringAdapter.ViewHolder> {
        private List<RecurringTransaction> transactions = new ArrayList<>();

        public void setTransactions(List<RecurringTransaction> transactions) {
            this.transactions = transactions;
            notifyDataSetChanged();
        }

        @NonNull
        @Override
        public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_recurring_transaction, parent,
                    false);
            return new ViewHolder(view);
        }

        @Override
        public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
            RecurringTransaction t = transactions.get(position);
            holder.tvNote.setText(t.note);
            holder.tvAmount.setText(String.valueOf(t.amount));
            holder.tvFrequency.setText(t.frequency);
        }

        @Override
        public int getItemCount() {
            return transactions.size();
        }

        static class ViewHolder extends RecyclerView.ViewHolder {
            TextView tvNote, tvAmount, tvFrequency;

            public ViewHolder(@NonNull View itemView) {
                super(itemView);
                tvNote = itemView.findViewById(R.id.tvNote);
                tvAmount = itemView.findViewById(R.id.tvAmount);
                tvFrequency = itemView.findViewById(R.id.tvFrequency);
            }
        }
    }
}

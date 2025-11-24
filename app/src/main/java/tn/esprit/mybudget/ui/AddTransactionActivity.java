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

    private TransactionViewModel viewModel;
    private TextInputEditText etAmount, etNote;
    private int selectedCategoryId = 1; // Default for MVP
    private int currentUserId = 1; // Hardcoded for MVP, should come from Session

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_transaction);

        viewModel = new ViewModelProvider(this).get(TransactionViewModel.class);

        etAmount = findViewById(R.id.etAmount);
        etNote = findViewById(R.id.etNote);
        Button btnSave = findViewById(R.id.btnSaveTransaction);

        btnSave.setOnClickListener(v -> {
            String amountStr = etAmount.getText().toString();
            String note = etNote.getText().toString();

            if (amountStr.isEmpty()) {
                Toast.makeText(this, "Enter amount", Toast.LENGTH_SHORT).show();
                return;
            }

            double amount = Double.parseDouble(amountStr);
            long date = System.currentTimeMillis();

            Transaction transaction = new Transaction(currentUserId, selectedCategoryId, amount, date, note);
            viewModel.addTransaction(transaction);

            Toast.makeText(this, "Transaction Added", Toast.LENGTH_SHORT).show();
            finish();
        });
    }
}

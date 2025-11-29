package tn.esprit.mybudget.ui.member;

import android.app.DatePickerDialog;
import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.RadioGroup;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
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
import tn.esprit.mybudget.data.entity.Member;
import tn.esprit.mybudget.ui.account.AccountViewModel;

public class MembersActivity extends AppCompatActivity {

    private static final String PREFS_NAME = "UserPrefs";
    private static final String KEY_USER_ID = "userId";

    private MemberViewModel viewModel;
    private AccountViewModel accountViewModel;
    private MemberAdapter adapter;
    private List<Account> accountList = new ArrayList<>();
    private int currentUserId = 1;

    private TextView tvTotalLent, tvTotalBorrowed;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_members);

        // Get user ID
        SharedPreferences prefs = getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        currentUserId = prefs.getInt(KEY_USER_ID, 1);

        androidx.appcompat.widget.Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setTitle("Lending & Borrowing");
        }

        tvTotalLent = findViewById(R.id.tvTotalLent);
        tvTotalBorrowed = findViewById(R.id.tvTotalBorrowed);
        RecyclerView recyclerView = findViewById(R.id.rvMembers);
        FloatingActionButton fabAdd = findViewById(R.id.fabAddMember);

        adapter = new MemberAdapter();
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        recyclerView.setAdapter(adapter);

        viewModel = new ViewModelProvider(this).get(MemberViewModel.class);
        accountViewModel = new ViewModelProvider(this).get(AccountViewModel.class);

        // Observe members
        viewModel.getAllMembers().observe(this, members -> {
            adapter.setMembers(members);
        });

        // Observe totals
        viewModel.getTotalLent().observe(this, total -> {
            tvTotalLent.setText(String.format(Locale.getDefault(), "Lent: %.2f", total != null ? total : 0.0));
        });

        viewModel.getTotalBorrowed().observe(this, total -> {
            tvTotalBorrowed.setText(String.format(Locale.getDefault(), "Borrowed: %.2f", total != null ? total : 0.0));
        });

        // Observe accounts for spinner
        accountViewModel.getAllAccounts().observe(this, accounts -> {
            accountList = accounts;
        });

        // Click to edit
        adapter.setOnMemberClickListener(this::showEditMemberDialog);

        // Delete
        adapter.setOnMemberDeleteListener(member -> {
            new AlertDialog.Builder(this)
                    .setTitle("Delete Record")
                    .setMessage("Delete this lending/borrowing record for " + member.name
                            + "?\n\nIf not settled, the amount will be restored to your account.")
                    .setPositiveButton("Delete", (dialog, which) -> viewModel.delete(member))
                    .setNegativeButton("Cancel", null)
                    .show();
        });

        // Settle debt
        adapter.setOnMemberSettleListener(member -> {
            String message = "Lent".equals(member.type)
                    ? "Mark " + member.name + " as having repaid the " + member.amount + "?"
                    : "Mark as you have repaid " + member.amount + " to " + member.name + "?";

            new AlertDialog.Builder(this)
                    .setTitle("Settle Debt")
                    .setMessage(message + "\n\nThis will create a transaction in your account.")
                    .setPositiveButton("Settle", (dialog, which) -> viewModel.settleDebt(member, currentUserId))
                    .setNegativeButton("Cancel", null)
                    .show();
        });

        fabAdd.setOnClickListener(v -> showAddMemberDialog());
    }

    private void showAddMemberDialog() {
        showMemberDialog(null);
    }

    private void showEditMemberDialog(Member member) {
        showMemberDialog(member);
    }

    private void showMemberDialog(Member existingMember) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle(existingMember == null ? "Add Lending/Borrowing" : "Edit Record");

        View view = LayoutInflater.from(this).inflate(R.layout.dialog_add_member, null);
        builder.setView(view);

        TextInputEditText etName = view.findViewById(R.id.etMemberName);
        TextInputEditText etAmount = view.findViewById(R.id.etMemberAmount);
        TextInputEditText etNote = view.findViewById(R.id.etMemberNote);
        RadioGroup rgType = view.findViewById(R.id.rgMemberType);
        Spinner spinnerAccount = view.findViewById(R.id.spinnerMemberAccount);
        Button btnSelectDate = view.findViewById(R.id.btnMemberDate);

        // Setup account spinner
        List<String> accountNames = new ArrayList<>();
        accountNames.add("No Account");
        for (Account acc : accountList) {
            accountNames.add(acc.name);
        }
        ArrayAdapter<String> accountAdapter = new ArrayAdapter<>(this,
                android.R.layout.simple_spinner_item, accountNames);
        accountAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerAccount.setAdapter(accountAdapter);

        // Setup date
        final long[] selectedDate = { System.currentTimeMillis() };
        SimpleDateFormat sdf = new SimpleDateFormat("MMM dd, yyyy", Locale.getDefault());
        btnSelectDate.setText(sdf.format(new Date(selectedDate[0])));

        btnSelectDate.setOnClickListener(v -> {
            Calendar cal = Calendar.getInstance();
            cal.setTimeInMillis(selectedDate[0]);

            new DatePickerDialog(this, (datePicker, year, month, day) -> {
                Calendar newCal = Calendar.getInstance();
                newCal.set(year, month, day);
                selectedDate[0] = newCal.getTimeInMillis();
                btnSelectDate.setText(sdf.format(new Date(selectedDate[0])));
            }, cal.get(Calendar.YEAR), cal.get(Calendar.MONTH), cal.get(Calendar.DAY_OF_MONTH)).show();
        });

        // Pre-fill for edit
        final Member oldMember;
        if (existingMember != null) {
            oldMember = new Member(existingMember.name, existingMember.amount, existingMember.type,
                    existingMember.date, existingMember.note, existingMember.accountId);
            oldMember.id = existingMember.id;
            oldMember.isSettled = existingMember.isSettled;

            etName.setText(existingMember.name);
            etAmount.setText(String.valueOf(existingMember.amount));
            etNote.setText(existingMember.note);
            selectedDate[0] = existingMember.date;
            btnSelectDate.setText(sdf.format(new Date(selectedDate[0])));

            if ("Borrowed".equals(existingMember.type)) {
                rgType.check(R.id.rbBorrowed);
            } else {
                rgType.check(R.id.rbLent);
            }

            // Set account spinner selection
            if (existingMember.accountId != null) {
                for (int i = 0; i < accountList.size(); i++) {
                    if (accountList.get(i).id == existingMember.accountId) {
                        spinnerAccount.setSelection(i + 1); // +1 for "No Account"
                        break;
                    }
                }
            }
        } else {
            oldMember = null;
        }

        builder.setPositiveButton("Save", (dialog, which) -> {
            String name = etName.getText().toString().trim();
            String amountStr = etAmount.getText().toString().trim();
            String note = etNote.getText().toString().trim();

            if (name.isEmpty()) {
                Toast.makeText(this, "Name is required", Toast.LENGTH_SHORT).show();
                return;
            }

            if (amountStr.isEmpty()) {
                Toast.makeText(this, "Amount is required", Toast.LENGTH_SHORT).show();
                return;
            }

            double amount;
            try {
                amount = Double.parseDouble(amountStr);
            } catch (NumberFormatException e) {
                Toast.makeText(this, "Invalid amount", Toast.LENGTH_SHORT).show();
                return;
            }

            String type = rgType.getCheckedRadioButtonId() == R.id.rbLent ? "Lent" : "Borrowed";

            Integer accountId = null;
            int accountPos = spinnerAccount.getSelectedItemPosition();
            if (accountPos > 0 && accountPos <= accountList.size()) {
                accountId = accountList.get(accountPos - 1).id;
            }

            if (existingMember == null) {
                Member member = new Member(name, amount, type, selectedDate[0], note, accountId);
                viewModel.insert(member, currentUserId);
                Toast.makeText(this, "Record added", Toast.LENGTH_SHORT).show();
            } else {
                existingMember.name = name;
                existingMember.amount = amount;
                existingMember.type = type;
                existingMember.date = selectedDate[0];
                existingMember.note = note;
                existingMember.accountId = accountId;
                viewModel.update(existingMember, oldMember, currentUserId);
                Toast.makeText(this, "Record updated", Toast.LENGTH_SHORT).show();
            }
        });

        builder.setNegativeButton("Cancel", null);
        builder.show();
    }

    @Override
    public boolean onSupportNavigateUp() {
        finish();
        return true;
    }
}

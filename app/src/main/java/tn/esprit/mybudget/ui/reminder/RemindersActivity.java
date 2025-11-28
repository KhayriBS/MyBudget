package tn.esprit.mybudget.ui.reminder;

import android.app.DatePickerDialog;
import android.app.TimePickerDialog;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.Spinner;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.ItemTouchHelper;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.floatingactionbutton.FloatingActionButton;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.Locale;

import tn.esprit.mybudget.R;
import tn.esprit.mybudget.data.entity.Account;
import tn.esprit.mybudget.data.entity.Category;
import tn.esprit.mybudget.data.entity.Reminder;
import tn.esprit.mybudget.util.ReminderScheduler;

/**
 * Activity for managing recurring transaction reminders.
 * Allows users to create, view, edit, and delete reminders with account and
 * category selection.
 */
public class RemindersActivity extends AppCompatActivity {

    private ReminderViewModel viewModel;
    private ReminderAdapter adapter;
    private List<Account> accountList = new ArrayList<>();
    private List<Category> categoryList = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_reminders);

        setupToolbar();
        setupViewModel();
        setupRecyclerView();
        setupFab();
        observeData();
    }

    private void setupToolbar() {
        androidx.appcompat.widget.Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setTitle("Recurring Transactions");
        }
    }

    private void setupViewModel() {
        viewModel = new ViewModelProvider(this).get(ReminderViewModel.class);
    }

    private void setupRecyclerView() {
        RecyclerView rvReminders = findViewById(R.id.rvReminders);
        rvReminders.setLayoutManager(new LinearLayoutManager(this));
        adapter = new ReminderAdapter(this::onReminderToggled, this::onReminderClicked);
        rvReminders.setAdapter(adapter);

        // Add swipe to delete
        ItemTouchHelper.SimpleCallback swipeCallback = new ItemTouchHelper.SimpleCallback(
                0, ItemTouchHelper.LEFT | ItemTouchHelper.RIGHT) {
            @Override
            public boolean onMove(@NonNull RecyclerView recyclerView,
                    @NonNull RecyclerView.ViewHolder viewHolder,
                    @NonNull RecyclerView.ViewHolder target) {
                return false;
            }

            @Override
            public void onSwiped(@NonNull RecyclerView.ViewHolder viewHolder, int direction) {
                int position = viewHolder.getAdapterPosition();
                Reminder reminder = adapter.getReminderAt(position);
                showDeleteConfirmation(reminder, position);
            }
        };
        new ItemTouchHelper(swipeCallback).attachToRecyclerView(rvReminders);
    }

    private void setupFab() {
        FloatingActionButton fabAdd = findViewById(R.id.fabAddReminder);
        fabAdd.setOnClickListener(v -> showAddReminderDialog());
    }

    private void observeData() {
        viewModel.getAllReminders().observe(this, reminders -> {
            adapter.setReminders(reminders);
        });

        viewModel.getAllAccounts().observe(this, accounts -> {
            accountList.clear();
            if (accounts != null) {
                accountList.addAll(accounts);
            }
        });

        // Load all categories initially
        viewModel.loadAllCategories();
        viewModel.getCategories().observe(this, categories -> {
            categoryList.clear();
            if (categories != null) {
                categoryList.addAll(categories);
            }
        });
    }

    private void onReminderToggled(Reminder reminder, boolean enabled) {
        viewModel.toggleEnabled(reminder.id, enabled);
        if (enabled) {
            ReminderScheduler.scheduleReminder(this, reminder);
            Toast.makeText(this, "Reminder enabled", Toast.LENGTH_SHORT).show();
        } else {
            ReminderScheduler.cancelReminder(this, reminder.id);
            Toast.makeText(this, "Reminder disabled", Toast.LENGTH_SHORT).show();
        }
    }

    private void onReminderClicked(Reminder reminder) {
        showEditReminderDialog(reminder);
    }

    private void showDeleteConfirmation(Reminder reminder, int position) {
        new AlertDialog.Builder(this)
                .setTitle("Delete Reminder")
                .setMessage("Are you sure you want to delete \"" + reminder.title + "\"?")
                .setPositiveButton("Delete", (dialog, which) -> {
                    ReminderScheduler.cancelReminder(this, reminder.id);
                    viewModel.delete(reminder);
                    Toast.makeText(this, "Reminder deleted", Toast.LENGTH_SHORT).show();
                })
                .setNegativeButton("Cancel", (dialog, which) -> {
                    adapter.notifyItemChanged(position);
                })
                .setOnCancelListener(dialog -> adapter.notifyItemChanged(position))
                .show();
    }

    private void showAddReminderDialog() {
        if (accountList.isEmpty()) {
            Toast.makeText(this, "Please create an account first", Toast.LENGTH_LONG).show();
            return;
        }

        showReminderDialog(null);
    }

    private void showEditReminderDialog(Reminder reminder) {
        showReminderDialog(reminder);
    }

    private void showReminderDialog(Reminder existingReminder) {
        boolean isEdit = existingReminder != null;

        View dialogView = LayoutInflater.from(this).inflate(R.layout.dialog_add_reminder, null);

        // Initialize views
        EditText etTitle = dialogView.findViewById(R.id.etReminderTitle);
        EditText etDescription = dialogView.findViewById(R.id.etReminderDescription);
        EditText etAmount = dialogView.findViewById(R.id.etReminderAmount);
        RadioGroup rgTransactionType = dialogView.findViewById(R.id.rgTransactionType);
        RadioButton rbExpense = dialogView.findViewById(R.id.rbExpense);
        RadioButton rbIncome = dialogView.findViewById(R.id.rbIncome);
        Spinner spinnerAccount = dialogView.findViewById(R.id.spinnerAccount);
        Spinner spinnerCategory = dialogView.findViewById(R.id.spinnerCategory);
        Spinner spinnerFrequency = dialogView.findViewById(R.id.spinnerFrequency);
        TextView tvSelectedDate = dialogView.findViewById(R.id.tvSelectedDate);
        TextView tvSelectedTime = dialogView.findViewById(R.id.tvSelectedTime);

        // Setup account spinner
        List<String> accountNames = new ArrayList<>();
        for (Account account : accountList) {
            accountNames.add(account.name + " (" + account.type + ")");
        }
        ArrayAdapter<String> accountAdapter = new ArrayAdapter<>(this,
                android.R.layout.simple_spinner_item, accountNames);
        accountAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerAccount.setAdapter(accountAdapter);

        // Setup category spinner based on transaction type
        setupCategorySpinner(spinnerCategory, "Expense");
        rgTransactionType.setOnCheckedChangeListener((group, checkedId) -> {
            String type = checkedId == R.id.rbIncome ? "Income" : "Expense";
            viewModel.loadCategoriesByType(type);
        });

        viewModel.getCategories().observe(this, categories -> {
            if (categories != null) {
                categoryList.clear();
                categoryList.addAll(categories);
                List<String> categoryNames = new ArrayList<>();
                for (Category category : categories) {
                    categoryNames.add(category.name);
                }
                ArrayAdapter<String> categoryAdapter = new ArrayAdapter<>(this,
                        android.R.layout.simple_spinner_item, categoryNames);
                categoryAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
                spinnerCategory.setAdapter(categoryAdapter);
            }
        });

        // Setup frequency spinner
        String[] frequencies = { "Once", "Daily", "Weekly", "Monthly", "Yearly" };
        ArrayAdapter<String> frequencyAdapter = new ArrayAdapter<>(this,
                android.R.layout.simple_spinner_item, frequencies);
        frequencyAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerFrequency.setAdapter(frequencyAdapter);

        // Setup date and time pickers
        final Calendar calendar = Calendar.getInstance();
        SimpleDateFormat dateFormat = new SimpleDateFormat("MMM dd, yyyy", Locale.getDefault());
        SimpleDateFormat timeFormat = new SimpleDateFormat("HH:mm", Locale.getDefault());

        if (isEdit) {
            calendar.setTimeInMillis(existingReminder.scheduledDate);
            etTitle.setText(existingReminder.title);
            etDescription.setText(existingReminder.description);
            etAmount.setText(String.valueOf(existingReminder.amount));

            if ("Income".equals(existingReminder.transactionType)) {
                rbIncome.setChecked(true);
            } else {
                rbExpense.setChecked(true);
            }

            // Set account selection
            for (int i = 0; i < accountList.size(); i++) {
                if (accountList.get(i).id == existingReminder.accountId) {
                    spinnerAccount.setSelection(i);
                    break;
                }
            }

            // Set frequency selection
            String[] freqValues = { "ONCE", "DAILY", "WEEKLY", "MONTHLY", "YEARLY" };
            for (int i = 0; i < freqValues.length; i++) {
                if (freqValues[i].equals(existingReminder.frequency)) {
                    spinnerFrequency.setSelection(i);
                    break;
                }
            }
        }

        tvSelectedDate.setText(dateFormat.format(calendar.getTime()));
        tvSelectedTime.setText(timeFormat.format(calendar.getTime()));

        tvSelectedDate.setOnClickListener(v -> {
            new DatePickerDialog(this, (view, year, month, dayOfMonth) -> {
                calendar.set(Calendar.YEAR, year);
                calendar.set(Calendar.MONTH, month);
                calendar.set(Calendar.DAY_OF_MONTH, dayOfMonth);
                tvSelectedDate.setText(dateFormat.format(calendar.getTime()));
            }, calendar.get(Calendar.YEAR), calendar.get(Calendar.MONTH),
                    calendar.get(Calendar.DAY_OF_MONTH)).show();
        });

        tvSelectedTime.setOnClickListener(v -> {
            new TimePickerDialog(this, (view, hourOfDay, minute) -> {
                calendar.set(Calendar.HOUR_OF_DAY, hourOfDay);
                calendar.set(Calendar.MINUTE, minute);
                calendar.set(Calendar.SECOND, 0);
                tvSelectedTime.setText(timeFormat.format(calendar.getTime()));
            }, calendar.get(Calendar.HOUR_OF_DAY), calendar.get(Calendar.MINUTE), true).show();
        });

        // Build dialog
        AlertDialog dialog = new AlertDialog.Builder(this)
                .setTitle(isEdit ? "Edit Reminder" : "Add Recurring Transaction")
                .setView(dialogView)
                .setPositiveButton(isEdit ? "Update" : "Save", null)
                .setNegativeButton("Cancel", null)
                .create();

        dialog.setOnShowListener(dialogInterface -> {
            dialog.getButton(AlertDialog.BUTTON_POSITIVE).setOnClickListener(v -> {
                // Validate inputs
                String title = etTitle.getText().toString().trim();
                String description = etDescription.getText().toString().trim();
                String amountStr = etAmount.getText().toString().trim();

                if (title.isEmpty()) {
                    etTitle.setError("Title is required");
                    return;
                }

                if (amountStr.isEmpty()) {
                    etAmount.setError("Amount is required");
                    return;
                }

                double amount;
                try {
                    amount = Double.parseDouble(amountStr);
                    if (amount <= 0) {
                        etAmount.setError("Amount must be positive");
                        return;
                    }
                } catch (NumberFormatException e) {
                    etAmount.setError("Invalid amount");
                    return;
                }

                if (spinnerAccount.getSelectedItemPosition() < 0 || accountList.isEmpty()) {
                    Toast.makeText(this, "Please select an account", Toast.LENGTH_SHORT).show();
                    return;
                }

                if (spinnerCategory.getSelectedItemPosition() < 0 || categoryList.isEmpty()) {
                    Toast.makeText(this, "Please select a category", Toast.LENGTH_SHORT).show();
                    return;
                }

                // Get selected values
                String transactionType = rgTransactionType.getCheckedRadioButtonId() == R.id.rbIncome
                        ? "Income"
                        : "Expense";
                Account selectedAccount = accountList.get(spinnerAccount.getSelectedItemPosition());
                Category selectedCategory = categoryList.get(spinnerCategory.getSelectedItemPosition());

                String[] freqValues = { "ONCE", "DAILY", "WEEKLY", "MONTHLY", "YEARLY" };
                String frequency = freqValues[spinnerFrequency.getSelectedItemPosition()];

                // Create or update reminder
                Reminder reminder;
                if (isEdit) {
                    reminder = existingReminder;
                    reminder.title = title;
                    reminder.description = description;
                    reminder.amount = amount;
                    reminder.transactionType = transactionType;
                    reminder.accountId = selectedAccount.id;
                    reminder.categoryId = selectedCategory.id;
                    reminder.scheduledDate = calendar.getTimeInMillis();
                    reminder.frequency = frequency;
                    reminder.currency = selectedAccount.currency;

                    viewModel.update(reminder);
                    ReminderScheduler.cancelReminder(this, reminder.id);
                    if (reminder.isEnabled) {
                        ReminderScheduler.scheduleReminder(this, reminder);
                    }
                    Toast.makeText(this, "Reminder updated", Toast.LENGTH_SHORT).show();
                } else {
                    reminder = new Reminder(
                            title, description, amount, transactionType,
                            selectedAccount.id, selectedCategory.id,
                            calendar.getTimeInMillis(), frequency, true, selectedAccount.currency);

                    viewModel.insert(reminder, reminderId -> {
                        reminder.id = reminderId;
                        runOnUiThread(() -> {
                            ReminderScheduler.scheduleReminder(this, reminder);
                            Toast.makeText(this, "Reminder created and scheduled", Toast.LENGTH_SHORT).show();
                        });
                    });
                }

                dialog.dismiss();
            });
        });

        dialog.show();
    }

    private void setupCategorySpinner(Spinner spinner, String type) {
        viewModel.loadCategoriesByType(type);
    }

    @Override
    public boolean onSupportNavigateUp() {
        finish();
        return true;
    }

    /**
     * Adapter for displaying reminders in RecyclerView
     */
    private static class ReminderAdapter extends RecyclerView.Adapter<ReminderAdapter.ViewHolder> {
        private List<Reminder> reminders = new ArrayList<>();
        private final OnReminderToggleListener toggleListener;
        private final OnReminderClickListener clickListener;

        interface OnReminderToggleListener {
            void onToggle(Reminder reminder, boolean enabled);
        }

        interface OnReminderClickListener {
            void onClick(Reminder reminder);
        }

        ReminderAdapter(OnReminderToggleListener toggleListener, OnReminderClickListener clickListener) {
            this.toggleListener = toggleListener;
            this.clickListener = clickListener;
        }

        public void setReminders(List<Reminder> reminders) {
            this.reminders = reminders != null ? reminders : new ArrayList<>();
            notifyDataSetChanged();
        }

        public Reminder getReminderAt(int position) {
            return reminders.get(position);
        }

        @NonNull
        @Override
        public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            View view = LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.item_reminder, parent, false);
            return new ViewHolder(view);
        }

        @Override
        public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
            Reminder reminder = reminders.get(position);
            holder.bind(reminder, toggleListener, clickListener);
        }

        @Override
        public int getItemCount() {
            return reminders.size();
        }

        static class ViewHolder extends RecyclerView.ViewHolder {
            private final TextView tvTitle;
            private final TextView tvTime;
            private final Switch switchEnabled;

            ViewHolder(@NonNull View itemView) {
                super(itemView);
                tvTitle = itemView.findViewById(R.id.tvTitle);
                tvTime = itemView.findViewById(R.id.tvTime);
                switchEnabled = itemView.findViewById(R.id.switchEnabled);
            }

            void bind(Reminder reminder, OnReminderToggleListener toggleListener,
                    OnReminderClickListener clickListener) {
                // Format title with amount and currency from account
                String currencyCode = reminder.currency != null ? reminder.currency : "TND";
                String amountStr = String.format(Locale.getDefault(), "%.2f %s", reminder.amount, currencyCode);
                String titleText = reminder.title + " - " + amountStr;
                tvTitle.setText(titleText);

                // Format date/time with frequency
                SimpleDateFormat dateTimeFormat = new SimpleDateFormat("MMM dd, HH:mm", Locale.getDefault());
                String frequencyLabel = getFrequencyLabel(reminder.frequency);
                String timeText = dateTimeFormat.format(reminder.scheduledDate) + " (" + frequencyLabel + ")";
                tvTime.setText(timeText);

                // Handle switch without triggering listener during bind
                switchEnabled.setOnCheckedChangeListener(null);
                switchEnabled.setChecked(reminder.isEnabled);
                switchEnabled.setOnCheckedChangeListener((buttonView, isChecked) -> {
                    if (toggleListener != null) {
                        toggleListener.onToggle(reminder, isChecked);
                    }
                });

                // Handle item click
                itemView.setOnClickListener(v -> {
                    if (clickListener != null) {
                        clickListener.onClick(reminder);
                    }
                });
            }

            private String getFrequencyLabel(String frequency) {
                if (frequency == null)
                    return "Once";
                switch (frequency) {
                    case "DAILY":
                        return "Daily";
                    case "WEEKLY":
                        return "Weekly";
                    case "MONTHLY":
                        return "Monthly";
                    case "YEARLY":
                        return "Yearly";
                    default:
                        return "Once";
                }
            }
        }
    }
}

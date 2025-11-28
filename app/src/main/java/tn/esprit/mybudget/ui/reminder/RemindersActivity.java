package tn.esprit.mybudget.ui.reminder;

import android.Manifest;
import android.app.DatePickerDialog;
import android.app.TimePickerDialog;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.ItemTouchHelper;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.button.MaterialButton;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.switchmaterial.SwitchMaterial;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;

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

    private final SimpleDateFormat dateFormat = new SimpleDateFormat("dd/MM/yyyy", Locale.getDefault());
    private final SimpleDateFormat timeFormat = new SimpleDateFormat("HH:mm", Locale.getDefault());

    // Notification permission launcher
    private final ActivityResultLauncher<String> notificationPermissionLauncher = registerForActivityResult(
            new ActivityResultContracts.RequestPermission(), isGranted -> {
                if (!isGranted) {
                    Toast.makeText(this, "Les notifications sont désactivées", Toast.LENGTH_LONG).show();
                }
            });

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_reminders);

        setupToolbar();
        setupViewModel();
        setupRecyclerView();
        setupFab();
        observeData();
        requestNotificationPermission();
    }

    private void requestNotificationPermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (ContextCompat.checkSelfPermission(this,
                    Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED) {
                notificationPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS);
            }
        }
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
            Toast.makeText(this, "Veuillez d'abord créer un compte", Toast.LENGTH_LONG).show();
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

        // Get views from new Material layout
        TextInputEditText editTitle = dialogView.findViewById(R.id.edit_title);
        TextInputEditText editAmount = dialogView.findViewById(R.id.edit_amount);
        RadioGroup radioType = dialogView.findViewById(R.id.radio_type);
        RadioButton radioExpense = dialogView.findViewById(R.id.radio_expense);
        RadioButton radioIncome = dialogView.findViewById(R.id.radio_income);
        AutoCompleteTextView spinnerAccount = dialogView.findViewById(R.id.spinner_account);
        AutoCompleteTextView spinnerCategory = dialogView.findViewById(R.id.spinner_category);
        AutoCompleteTextView spinnerFrequency = dialogView.findViewById(R.id.spinner_frequency);
        MaterialButton btnSelectDate = dialogView.findViewById(R.id.btn_select_date);
        MaterialButton btnSelectTime = dialogView.findViewById(R.id.btn_select_time);
        SwitchMaterial switchReminder = dialogView.findViewById(R.id.switch_reminder);
        AutoCompleteTextView spinnerReminderBefore = dialogView.findViewById(R.id.spinner_reminder_before);
        TextInputLayout layoutReminderBefore = dialogView.findViewById(R.id.layout_reminder_before);
        TextView labelReminderBefore = dialogView.findViewById(R.id.label_reminder_before);
        MaterialButton btnCancel = dialogView.findViewById(R.id.btn_cancel);
        MaterialButton btnSave = dialogView.findViewById(R.id.btn_save);

        // Calendar for date/time
        final Calendar calendar = Calendar.getInstance();
        if (isEdit) {
            calendar.setTimeInMillis(existingReminder.scheduledDate);
        }

        // Setup Account dropdown
        String[] accountNames = new String[accountList.size()];
        for (int i = 0; i < accountList.size(); i++) {
            accountNames[i] = accountList.get(i).name + " (" + accountList.get(i).currency + ")";
        }
        ArrayAdapter<String> accountAdapter = new ArrayAdapter<>(this,
                android.R.layout.simple_dropdown_item_1line, accountNames);
        spinnerAccount.setAdapter(accountAdapter);
        if (accountNames.length > 0) {
            spinnerAccount.setText(accountNames[0], false);
        }

        // Setup Frequency dropdown
        String[] frequencies = { "Une fois", "Quotidien", "Hebdomadaire", "Mensuel", "Annuel" };
        ArrayAdapter<String> frequencyAdapter = new ArrayAdapter<>(this,
                android.R.layout.simple_dropdown_item_1line, frequencies);
        spinnerFrequency.setAdapter(frequencyAdapter);
        spinnerFrequency.setText(frequencies[3], false); // Default: Monthly

        // Setup Reminder Before dropdown
        String[] reminderOptions = { "À l'heure prévue", "15 minutes avant", "30 minutes avant", "1 heure avant",
                "1 jour avant" };
        ArrayAdapter<String> reminderAdapter = new ArrayAdapter<>(this,
                android.R.layout.simple_dropdown_item_1line, reminderOptions);
        spinnerReminderBefore.setAdapter(reminderAdapter);
        spinnerReminderBefore.setText(reminderOptions[0], false);

        // Load categories
        viewModel.loadCategoriesByType("Expense");

        // Observe categories
        viewModel.getCategories().observe(this, categories -> {
            if (categories != null && !categories.isEmpty()) {
                categoryList.clear();
                categoryList.addAll(categories);
                String[] categoryNames = new String[categories.size()];
                for (int i = 0; i < categories.size(); i++) {
                    categoryNames[i] = categories.get(i).name;
                }
                ArrayAdapter<String> catAdapter = new ArrayAdapter<>(this,
                        android.R.layout.simple_dropdown_item_1line, categoryNames);
                spinnerCategory.setAdapter(catAdapter);
                if (categoryNames.length > 0) {
                    spinnerCategory.setText(categoryNames[0], false);
                }
            }
        });

        // Type change listener
        radioType.setOnCheckedChangeListener((group, checkedId) -> {
            String type = checkedId == R.id.radio_income ? "Income" : "Expense";
            viewModel.loadCategoriesByType(type);
        });

        // Toggle reminder visibility
        switchReminder.setOnCheckedChangeListener((buttonView, isChecked) -> {
            int visibility = isChecked ? View.VISIBLE : View.GONE;
            labelReminderBefore.setVisibility(visibility);
            layoutReminderBefore.setVisibility(visibility);
        });

        // Date picker
        btnSelectDate.setText(dateFormat.format(calendar.getTime()));
        btnSelectDate.setOnClickListener(v -> {
            DatePickerDialog dialog = new DatePickerDialog(this, (view, year, month, dayOfMonth) -> {
                calendar.set(Calendar.YEAR, year);
                calendar.set(Calendar.MONTH, month);
                calendar.set(Calendar.DAY_OF_MONTH, dayOfMonth);
                btnSelectDate.setText(dateFormat.format(calendar.getTime()));
            }, calendar.get(Calendar.YEAR), calendar.get(Calendar.MONTH), calendar.get(Calendar.DAY_OF_MONTH));
            dialog.show();
        });

        // Time picker
        btnSelectTime.setText(timeFormat.format(calendar.getTime()));
        btnSelectTime.setOnClickListener(v -> {
            TimePickerDialog dialog = new TimePickerDialog(this, (view, hourOfDay, minute) -> {
                calendar.set(Calendar.HOUR_OF_DAY, hourOfDay);
                calendar.set(Calendar.MINUTE, minute);
                calendar.set(Calendar.SECOND, 0);
                btnSelectTime.setText(timeFormat.format(calendar.getTime()));
            }, calendar.get(Calendar.HOUR_OF_DAY), calendar.get(Calendar.MINUTE), true);
            dialog.show();
        });

        // Populate fields if editing
        if (isEdit) {
            editTitle.setText(existingReminder.title);
            editAmount.setText(String.valueOf(existingReminder.amount));

            if ("Income".equals(existingReminder.transactionType)) {
                radioIncome.setChecked(true);
                viewModel.loadCategoriesByType("Income");
            } else {
                radioExpense.setChecked(true);
                viewModel.loadCategoriesByType("Expense");
            }

            // Set account
            for (int i = 0; i < accountList.size(); i++) {
                if (accountList.get(i).id == existingReminder.accountId) {
                    spinnerAccount.setText(accountNames[i], false);
                    break;
                }
            }

            // Set frequency
            spinnerFrequency.setText(existingReminder.getFrequencyDisplayName(), false);

            // Set notification settings
            switchReminder.setChecked(existingReminder.notificationEnabled);
            spinnerReminderBefore.setText(existingReminder.getReminderDisplayName(), false);

            int visibility = existingReminder.notificationEnabled ? View.VISIBLE : View.GONE;
            labelReminderBefore.setVisibility(visibility);
            layoutReminderBefore.setVisibility(visibility);
        }

        // Create dialog
        AlertDialog dialog = new AlertDialog.Builder(this)
                .setView(dialogView)
                .create();

        // Cancel button
        btnCancel.setOnClickListener(v -> dialog.dismiss());

        // Save button
        btnSave.setOnClickListener(v -> {
            String title = editTitle.getText() != null ? editTitle.getText().toString().trim() : "";
            String amountStr = editAmount.getText() != null ? editAmount.getText().toString().trim() : "";

            if (title.isEmpty()) {
                editTitle.setError("Le titre est requis");
                return;
            }

            if (amountStr.isEmpty()) {
                editAmount.setError("Le montant est requis");
                return;
            }

            double amount;
            try {
                amount = Double.parseDouble(amountStr);
                if (amount <= 0) {
                    editAmount.setError("Le montant doit être positif");
                    return;
                }
            } catch (NumberFormatException e) {
                editAmount.setError("Montant invalide");
                return;
            }

            // Get selected account
            int accountIndex = getDropdownIndex(spinnerAccount.getText().toString(), accountNames);
            if (accountIndex < 0 || accountList.isEmpty()) {
                Toast.makeText(this, "Veuillez sélectionner un compte", Toast.LENGTH_SHORT).show();
                return;
            }
            Account selectedAccount = accountList.get(accountIndex);

            // Get selected category
            int categoryIndex = -1;
            String selectedCategoryText = spinnerCategory.getText().toString();
            for (int i = 0; i < categoryList.size(); i++) {
                if (categoryList.get(i).name.equals(selectedCategoryText)) {
                    categoryIndex = i;
                    break;
                }
            }
            if (categoryIndex < 0 || categoryList.isEmpty()) {
                Toast.makeText(this, "Veuillez sélectionner une catégorie", Toast.LENGTH_SHORT).show();
                return;
            }
            Category selectedCategory = categoryList.get(categoryIndex);

            String transactionType = radioIncome.isChecked() ? "Income" : "Expense";
            String frequency = getFrequencyCode(spinnerFrequency.getText().toString());
            boolean notificationEnabled = switchReminder.isChecked();
            int reminderMinutes = getReminderMinutes(spinnerReminderBefore.getText().toString());

            // Create or update reminder
            Reminder reminder;
            if (isEdit) {
                reminder = existingReminder;
                reminder.title = title;
                reminder.amount = amount;
                reminder.transactionType = transactionType;
                reminder.accountId = selectedAccount.id;
                reminder.categoryId = selectedCategory.id;
                reminder.scheduledDate = calendar.getTimeInMillis();
                reminder.frequency = frequency;
                reminder.currency = selectedAccount.currency;
                reminder.notificationEnabled = notificationEnabled;
                reminder.reminderMinutesBefore = reminderMinutes;

                viewModel.update(reminder);
                ReminderScheduler.cancelReminder(this, reminder.id);
                if (reminder.isEnabled) {
                    ReminderScheduler.scheduleReminder(this, reminder);
                }
                Toast.makeText(this, "Rappel modifié", Toast.LENGTH_SHORT).show();
            } else {
                reminder = new Reminder(title, "", amount, transactionType,
                        selectedAccount.id, selectedCategory.id,
                        calendar.getTimeInMillis(), frequency, true,
                        selectedAccount.currency, notificationEnabled, reminderMinutes);

                viewModel.insert(reminder, reminderId -> {
                    reminder.id = reminderId;
                    runOnUiThread(() -> {
                        ReminderScheduler.scheduleReminder(this, reminder);
                        Toast.makeText(this, "Rappel créé", Toast.LENGTH_SHORT).show();
                    });
                });
            }

            dialog.dismiss();
        });

        dialog.show();
    }

    private int getDropdownIndex(String text, String[] items) {
        for (int i = 0; i < items.length; i++) {
            if (items[i].equals(text)) {
                return i;
            }
        }
        return -1;
    }

    private String getFrequencyCode(String displayName) {
        switch (displayName) {
            case "Quotidien":
                return "DAILY";
            case "Hebdomadaire":
                return "WEEKLY";
            case "Mensuel":
                return "MONTHLY";
            case "Annuel":
                return "YEARLY";
            default:
                return "ONCE";
        }
    }

    private int getReminderMinutes(String displayName) {
        switch (displayName) {
            case "15 minutes avant":
                return 15;
            case "30 minutes avant":
                return 30;
            case "1 heure avant":
                return 60;
            case "1 jour avant":
                return 1440;
            default:
                return 0;
        }
    }

    private void setupCategoryDropdown(String type) {
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

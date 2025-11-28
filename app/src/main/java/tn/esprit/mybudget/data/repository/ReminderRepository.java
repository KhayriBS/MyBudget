package tn.esprit.mybudget.data.repository;

import android.app.Application;

import androidx.lifecycle.LiveData;

import java.util.List;
import java.util.concurrent.ExecutorService;

import tn.esprit.mybudget.data.AppDatabase;
import tn.esprit.mybudget.data.dao.AccountDao;
import tn.esprit.mybudget.data.dao.CategoryDao;
import tn.esprit.mybudget.data.dao.ReminderDao;
import tn.esprit.mybudget.data.dao.TransactionDao;
import tn.esprit.mybudget.data.entity.Account;
import tn.esprit.mybudget.data.entity.Category;
import tn.esprit.mybudget.data.entity.Reminder;
import tn.esprit.mybudget.data.entity.Transaction;

/**
 * Repository for managing Reminder data operations.
 * Provides a clean API for data access to the rest of the application.
 */
public class ReminderRepository {

    private final ReminderDao reminderDao;
    private final AccountDao accountDao;
    private final CategoryDao categoryDao;
    private final TransactionDao transactionDao;
    private final LiveData<List<Reminder>> allReminders;
    private final LiveData<List<Account>> allAccounts;
    private final ExecutorService executor;

    public ReminderRepository(Application application) {
        AppDatabase db = AppDatabase.getDatabase(application);
        reminderDao = db.reminderDao();
        accountDao = db.accountDao();
        categoryDao = db.categoryDao();
        transactionDao = db.transactionDao();
        allReminders = reminderDao.getAllReminders();
        allAccounts = accountDao.getAllAccounts();
        executor = AppDatabase.databaseWriteExecutor;
    }

    // Reminder operations
    public LiveData<List<Reminder>> getAllReminders() {
        return allReminders;
    }

    public LiveData<List<Reminder>> getEnabledReminders() {
        return reminderDao.getEnabledReminders();
    }

    public void insert(Reminder reminder, OnReminderInsertedListener listener) {
        executor.execute(() -> {
            long id = reminderDao.insert(reminder);
            if (listener != null) {
                listener.onReminderInserted((int) id);
            }
        });
    }

    public void update(Reminder reminder) {
        executor.execute(() -> reminderDao.update(reminder));
    }

    public void delete(Reminder reminder) {
        executor.execute(() -> reminderDao.delete(reminder));
    }

    public void deleteById(int id) {
        executor.execute(() -> reminderDao.deleteById(id));
    }

    public void updateEnabledStatus(int id, boolean enabled) {
        executor.execute(() -> reminderDao.updateEnabledStatus(id, enabled));
    }

    // Account operations
    public LiveData<List<Account>> getAllAccounts() {
        return allAccounts;
    }

    // Category operations
    public List<Category> getCategoriesByType(String type) {
        return categoryDao.getCategoriesByType(type);
    }

    public List<Category> getAllCategories() {
        return categoryDao.getAllCategories();
    }

    /**
     * Execute a reminder by creating a transaction and updating the reminder.
     * This is called when a scheduled reminder triggers.
     */
    public void executeReminder(int reminderId, int userId) {
        executor.execute(() -> {
            Reminder reminder = reminderDao.getReminderById(reminderId);
            if (reminder == null || !reminder.isEnabled) {
                return;
            }

            // Create the transaction
            Transaction transaction = new Transaction(
                    userId,
                    reminder.categoryId,
                    reminder.accountId,
                    reminder.amount,
                    System.currentTimeMillis(),
                    reminder.title + (reminder.description != null ? " - " + reminder.description : ""));
            transactionDao.insert(transaction);

            // Update account balance
            Account account = accountDao.getAccountById(reminder.accountId);
            if (account != null) {
                if ("Expense".equals(reminder.transactionType)) {
                    account.balance -= reminder.amount;
                } else {
                    account.balance += reminder.amount;
                }
                accountDao.update(account);
            }

            // Update reminder for next execution
            if ("ONCE".equals(reminder.frequency)) {
                reminder.isEnabled = false;
                reminderDao.update(reminder);
            } else {
                long nextDate = reminder.calculateNextExecutionDate();
                reminderDao.updateAfterExecution(reminderId, nextDate, System.currentTimeMillis());
            }
        });
    }

    /**
     * Get due reminders synchronously (for background processing)
     */
    public List<Reminder> getDueReminders(long currentTime) {
        return reminderDao.getDueReminders(currentTime);
    }

    /**
     * Callback interface for reminder insertion
     */
    public interface OnReminderInsertedListener {
        void onReminderInserted(int reminderId);
    }
}

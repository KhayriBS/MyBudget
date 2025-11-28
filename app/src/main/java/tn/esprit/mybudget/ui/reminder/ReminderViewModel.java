package tn.esprit.mybudget.ui.reminder;

import android.app.Application;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import java.util.List;

import tn.esprit.mybudget.data.AppDatabase;
import tn.esprit.mybudget.data.entity.Account;
import tn.esprit.mybudget.data.entity.Category;
import tn.esprit.mybudget.data.entity.Reminder;
import tn.esprit.mybudget.data.repository.ReminderRepository;

/**
 * ViewModel for managing Reminder UI data.
 * Follows MVVM pattern and survives configuration changes.
 */
public class ReminderViewModel extends AndroidViewModel {

    private final ReminderRepository repository;
    private final LiveData<List<Reminder>> allReminders;
    private final LiveData<List<Account>> allAccounts;
    private final MutableLiveData<List<Category>> categories = new MutableLiveData<>();
    private final MutableLiveData<Boolean> isLoading = new MutableLiveData<>(false);
    private final MutableLiveData<String> errorMessage = new MutableLiveData<>();

    public ReminderViewModel(@NonNull Application application) {
        super(application);
        repository = new ReminderRepository(application);
        allReminders = repository.getAllReminders();
        allAccounts = repository.getAllAccounts();
    }

    // Getters for LiveData
    public LiveData<List<Reminder>> getAllReminders() {
        return allReminders;
    }

    public LiveData<List<Account>> getAllAccounts() {
        return allAccounts;
    }

    public LiveData<List<Category>> getCategories() {
        return categories;
    }

    public LiveData<Boolean> getIsLoading() {
        return isLoading;
    }

    public LiveData<String> getErrorMessage() {
        return errorMessage;
    }

    /**
     * Load categories by type (Income or Expense)
     */
    public void loadCategoriesByType(String type) {
        AppDatabase.databaseWriteExecutor.execute(() -> {
            List<Category> result = repository.getCategoriesByType(type);
            categories.postValue(result);
        });
    }

    /**
     * Load all categories
     */
    public void loadAllCategories() {
        AppDatabase.databaseWriteExecutor.execute(() -> {
            List<Category> result = repository.getAllCategories();
            categories.postValue(result);
        });
    }

    /**
     * Insert a new reminder with callback for scheduling
     */
    public void insert(Reminder reminder, ReminderRepository.OnReminderInsertedListener listener) {
        isLoading.setValue(true);
        repository.insert(reminder, reminderId -> {
            isLoading.postValue(false);
            if (listener != null) {
                listener.onReminderInserted(reminderId);
            }
        });
    }

    /**
     * Insert without callback
     */
    public void insert(Reminder reminder) {
        insert(reminder, null);
    }

    /**
     * Update an existing reminder
     */
    public void update(Reminder reminder) {
        repository.update(reminder);
    }

    /**
     * Delete a reminder
     */
    public void delete(Reminder reminder) {
        repository.delete(reminder);
    }

    /**
     * Toggle reminder enabled status
     */
    public void toggleEnabled(int reminderId, boolean enabled) {
        repository.updateEnabledStatus(reminderId, enabled);
    }

    /**
     * Execute a reminder (create transaction)
     */
    public void executeReminder(int reminderId, int userId) {
        repository.executeReminder(reminderId, userId);
    }

    /**
     * Clear error message after handling
     */
    public void clearError() {
        errorMessage.setValue(null);
    }
}

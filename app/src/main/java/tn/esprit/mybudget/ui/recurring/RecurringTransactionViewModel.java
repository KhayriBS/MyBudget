package tn.esprit.mybudget.ui.recurring;

import android.app.Application;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;

import java.util.List;

import tn.esprit.mybudget.data.AppDatabase;
import tn.esprit.mybudget.data.dao.RecurringTransactionDao;
import tn.esprit.mybudget.data.entity.RecurringTransaction;

public class RecurringTransactionViewModel extends AndroidViewModel {

    private RecurringTransactionDao recurringTransactionDao;
    private LiveData<List<RecurringTransaction>> allRecurringTransactions;

    public RecurringTransactionViewModel(@NonNull Application application) {
        super(application);
        AppDatabase db = AppDatabase.getDatabase(application);
        recurringTransactionDao = db.recurringTransactionDao();
        allRecurringTransactions = recurringTransactionDao.getAllRecurringTransactions(1); // Hardcoded UserID
    }

    public LiveData<List<RecurringTransaction>> getAllRecurringTransactions() {
        return allRecurringTransactions;
    }

    public void insert(RecurringTransaction transaction) {
        AppDatabase.databaseWriteExecutor.execute(() -> {
            recurringTransactionDao.insert(transaction);
        });
    }

    public void delete(RecurringTransaction transaction) {
        AppDatabase.databaseWriteExecutor.execute(() -> {
            recurringTransactionDao.delete(transaction);
        });
    }
}

package tn.esprit.mybudget.ui.viewmodel;

import android.app.Application;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import tn.esprit.mybudget.data.AppDatabase;
import tn.esprit.mybudget.data.dao.TransactionDao;
import tn.esprit.mybudget.data.entity.Transaction;

import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class TransactionViewModel extends AndroidViewModel {
    private final TransactionDao transactionDao;
    private final ExecutorService executorService;
    private final MutableLiveData<List<Transaction>> transactions = new MutableLiveData<>();

    public TransactionViewModel(Application application) {
        super(application);
        AppDatabase db = AppDatabase.getDatabase(application);
        transactionDao = db.transactionDao();
        executorService = Executors.newSingleThreadExecutor();
    }

    public LiveData<List<Transaction>> getTransactions() {
        return transactions;
    }

    public void loadTransactions(int userId) {
        executorService.execute(() -> {
            List<Transaction> list = transactionDao.getAllTransactions(userId);
            transactions.postValue(list);
        });
    }

    public void addTransaction(Transaction transaction) {
        executorService.execute(() -> {
            transactionDao.insert(transaction);
            // Reload after insert
            loadTransactions(transaction.userId);
        });
    }

    public void deleteTransaction(Transaction transaction) {
        executorService.execute(() -> {
            transactionDao.delete(transaction);
            loadTransactions(transaction.userId);
        });
    }
}

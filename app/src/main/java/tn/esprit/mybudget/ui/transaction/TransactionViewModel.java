package tn.esprit.mybudget.ui.transaction;

import android.app.Application;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import tn.esprit.mybudget.data.AppDatabase;
import tn.esprit.mybudget.data.dao.AccountDao;
import tn.esprit.mybudget.data.dao.TransactionDao;
import tn.esprit.mybudget.data.entity.Account;
import tn.esprit.mybudget.data.entity.Transaction;
import tn.esprit.mybudget.data.entity.TransactionWithAccount;

import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class TransactionViewModel extends AndroidViewModel {
    private final TransactionDao transactionDao;
    private final AccountDao accountDao;
    private final ExecutorService executorService;
    private final MutableLiveData<List<Transaction>> transactions = new MutableLiveData<>();
    private final MutableLiveData<List<TransactionWithAccount>> transactionsWithAccount = new MutableLiveData<>();

    public TransactionViewModel(Application application) {
        super(application);
        AppDatabase db = AppDatabase.getDatabase(application);
        transactionDao = db.transactionDao();
        accountDao = db.accountDao();
        executorService = Executors.newSingleThreadExecutor();
    }

    public LiveData<List<Transaction>> getTransactions() {
        return transactions;
    }

    public LiveData<List<TransactionWithAccount>> getTransactionsWithAccount() {
        return transactionsWithAccount;
    }

    public void loadTransactions(int userId) {
        executorService.execute(() -> {
            List<Transaction> list = transactionDao.getAllTransactions(userId);
            transactions.postValue(list);

            List<TransactionWithAccount> listWithAccount = transactionDao.getTransactionsWithAccount(userId);
            transactionsWithAccount.postValue(listWithAccount);
        });
    }

    public void addTransaction(Transaction transaction) {
        executorService.execute(() -> {
            transactionDao.insert(transaction);
            // Reload after insert
            loadTransactions(transaction.userId);
        });
    }

    public void updateTransaction(Transaction transaction, double oldAmount, int oldAccountId) {
        executorService.execute(() -> {
            // Check if account changed
            if (transaction.accountId != oldAccountId) {
                // Restore amount to old account
                Account oldAccount = accountDao.getAccountById(oldAccountId);
                if (oldAccount != null) {
                    oldAccount.balance += oldAmount;
                    accountDao.update(oldAccount);
                }

                // Deduct amount from new account
                Account newAccount = accountDao.getAccountById(transaction.accountId);
                if (newAccount != null) {
                    newAccount.balance -= transaction.amount;
                    accountDao.update(newAccount);
                }
            } else {
                // Same account - just adjust for amount difference
                Account account = accountDao.getAccountById(transaction.accountId);
                if (account != null) {
                    double difference = transaction.amount - oldAmount;
                    account.balance -= difference;
                    accountDao.update(account);
                }
            }

            transactionDao.update(transaction);
            loadTransactions(transaction.userId);
        });
    }

    public void deleteTransaction(Transaction transaction) {
        executorService.execute(() -> {
            // Restore the amount to the account before deleting
            Account account = accountDao.getAccountById(transaction.accountId);
            if (account != null) {
                account.balance += transaction.amount;
                accountDao.update(account);
            }

            transactionDao.delete(transaction);
            loadTransactions(transaction.userId);
        });
    }

    public LiveData<List<Transaction>> searchTransactions(int userId, String query) {
        MutableLiveData<List<Transaction>> searchResults = new MutableLiveData<>();
        executorService.execute(() -> {
            List<Transaction> results = transactionDao.searchTransactions(userId, query);
            searchResults.postValue(results);
        });
        return searchResults;
    }
}

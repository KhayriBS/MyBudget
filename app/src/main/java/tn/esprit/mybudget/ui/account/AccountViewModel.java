package tn.esprit.mybudget.ui.account;

import android.app.Application;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;

import java.util.List;

import tn.esprit.mybudget.data.AppDatabase;
import tn.esprit.mybudget.data.dao.AccountDao;
import tn.esprit.mybudget.data.entity.Account;

public class AccountViewModel extends AndroidViewModel {

    private AccountDao accountDao;
    private LiveData<List<Account>> allAccounts;

    public AccountViewModel(@NonNull Application application) {
        super(application);
        AppDatabase db = AppDatabase.getDatabase(application);
        accountDao = db.accountDao();
        allAccounts = accountDao.getAllAccounts();
    }

    public LiveData<List<Account>> getAllAccounts() {
        return allAccounts;
    }

    public void insert(Account account) {
        AppDatabase.databaseWriteExecutor.execute(() -> {
            accountDao.insert(account);
        });
    }

    public void update(Account account) {
        AppDatabase.databaseWriteExecutor.execute(() -> {
            accountDao.update(account);
        });
    }

    public void delete(Account account) {
        AppDatabase.databaseWriteExecutor.execute(() -> {
            accountDao.delete(account);
        });
    }
}

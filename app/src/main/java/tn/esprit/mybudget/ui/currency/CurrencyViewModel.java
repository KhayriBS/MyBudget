package tn.esprit.mybudget.ui.currency;

import android.app.Application;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;

import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import tn.esprit.mybudget.data.AppDatabase;
import tn.esprit.mybudget.data.dao.CurrencyDao;
import tn.esprit.mybudget.data.entity.Currency;

public class CurrencyViewModel extends AndroidViewModel {

    private CurrencyDao currencyDao;
    private LiveData<List<Currency>> allCurrencies;
    private final ExecutorService executorService = Executors.newSingleThreadExecutor();

    public CurrencyViewModel(@NonNull Application application) {
        super(application);
        AppDatabase db = AppDatabase.getDatabase(application);
        currencyDao = db.currencyDao();
        allCurrencies = currencyDao.getAllCurrencies();
    }

    public LiveData<List<Currency>> getAllCurrencies() {
        return allCurrencies;
    }

    public void insert(Currency currency) {
        executorService.execute(() -> currencyDao.insert(currency));
    }

    public void update(Currency currency) {
        executorService.execute(() -> currencyDao.insert(currency)); // Insert with REPLACE strategy acts as update
    }
}

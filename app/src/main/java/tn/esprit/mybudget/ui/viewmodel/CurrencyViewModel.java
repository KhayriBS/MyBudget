package tn.esprit.mybudget.ui.viewmodel;

import android.app.Application;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import tn.esprit.mybudget.data.AppDatabase;
import tn.esprit.mybudget.data.dao.CurrencyDao;
import tn.esprit.mybudget.data.entity.Currency;

import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class CurrencyViewModel extends AndroidViewModel {
    private final CurrencyDao currencyDao;
    private final ExecutorService executorService;
    private final MutableLiveData<List<Currency>> allCurrencies = new MutableLiveData<>();

    public CurrencyViewModel(Application application) {
        super(application);
        AppDatabase db = AppDatabase.getDatabase(application);
        currencyDao = db.currencyDao();
        executorService = Executors.newSingleThreadExecutor();
        loadCurrencies();
    }

    public LiveData<List<Currency>> getAllCurrencies() {
        return allCurrencies;
    }

    private void loadCurrencies() {
        executorService.execute(() -> {
            List<Currency> currencies = currencyDao.getAllCurrencies();
            if (currencies.isEmpty()) {
                // Pre-populate if empty
                populateCurrencies();
                currencies = currencyDao.getAllCurrencies();
            }
            allCurrencies.postValue(currencies);
        });
    }

    private void populateCurrencies() {
        Currency[] initialCurrencies = {
                new Currency("USD", "$", 1.0),
                new Currency("EUR", "€", 0.92),
                new Currency("GBP", "£", 0.79),
                new Currency("JPY", "¥", 150.0),
                new Currency("CAD", "C$", 1.35),
                new Currency("AUD", "A$", 1.52)
        };
        for (Currency c : initialCurrencies) {
            currencyDao.insert(c);
        }
    }
}

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

    private final androidx.lifecycle.MutableLiveData<String> statusMessage = new androidx.lifecycle.MutableLiveData<>();

    public LiveData<String> getStatusMessage() {
        return statusMessage;
    }

    public void fetchExchangeRates(String baseCurrency) {
        statusMessage.postValue("Fetching rates...");
        tn.esprit.mybudget.data.api.CurrencyApiService apiService = tn.esprit.mybudget.data.api.RetrofitClient
                .getClient().create(tn.esprit.mybudget.data.api.CurrencyApiService.class);

        apiService.getLatestRates(baseCurrency)
                .enqueue(new retrofit2.Callback<tn.esprit.mybudget.data.model.CurrencyResponse>() {
                    @Override
                    public void onResponse(retrofit2.Call<tn.esprit.mybudget.data.model.CurrencyResponse> call,
                            retrofit2.Response<tn.esprit.mybudget.data.model.CurrencyResponse> response) {
                        if (response.isSuccessful() && response.body() != null) {
                            executorService.execute(() -> {
                                java.util.Map<String, Double> rates = response.body().rates;
                                if (rates != null) {
                                    for (java.util.Map.Entry<String, Double> entry : rates.entrySet()) {
                                        String code = entry.getKey();
                                        Double rate = entry.getValue();

                                        tn.esprit.mybudget.data.entity.Currency existing = currencyDao
                                                .getCurrencyByCode(code);
                                        if (existing != null) {
                                            existing.exchangeRateToBase = rate;
                                            currencyDao.insert(existing); // Replace strategy
                                        } else {
                                            // Optional: Add new currencies automatically?
                                            // For now, let's only update existing ones to avoid cluttering with 100+
                                            // currencies
                                            // Or maybe add them? The user might want them.
                                            // Let's add them but maybe we need a way to filter.
                                            // For safety, let's ONLY update existing ones for now, or maybe add major
                                            // ones.
                                            // The user request was "taux/devise", implying updating rates.
                                            // If I add all, the list will be huge.
                                            // Let's stick to updating EXISTING currencies for now.
                                            // If user wants a new currency, they add it manually (code) and then update
                                            // rates.
                                            // Actually, if they add "EUR", they want the rate.
                                            // So updating existing is the safest and most expected behavior.
                                        }
                                    }
                                    statusMessage.postValue("Rates updated successfully");
                                }
                            });
                        } else {
                            statusMessage.postValue("Failed to fetch rates: " + response.message());
                        }
                    }

                    @Override
                    public void onFailure(retrofit2.Call<tn.esprit.mybudget.data.model.CurrencyResponse> call,
                            Throwable t) {
                        statusMessage.postValue("Network error: " + t.getMessage());
                    }
                });
    }

    public void insert(Currency currency) {
        executorService.execute(() -> currencyDao.insert(currency));
    }

    public void update(Currency currency) {
        executorService.execute(() -> currencyDao.insert(currency)); // Insert with REPLACE strategy acts as update
    }
}

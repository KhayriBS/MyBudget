package tn.esprit.mybudget.ui.viewmodel;

import android.app.Application;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import tn.esprit.mybudget.data.AppDatabase;
import tn.esprit.mybudget.data.dao.TransactionDao;
import tn.esprit.mybudget.data.entity.Transaction;
import com.github.mikephil.charting.data.PieEntry;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class ReportsViewModel extends AndroidViewModel {
    private final TransactionDao transactionDao;
    private final ExecutorService executorService;
    private final MutableLiveData<List<PieEntry>> categoryData = new MutableLiveData<>();

    public ReportsViewModel(Application application) {
        super(application);
        AppDatabase db = AppDatabase.getDatabase(application);
        transactionDao = db.transactionDao();
        executorService = Executors.newSingleThreadExecutor();
    }

    public LiveData<List<PieEntry>> getCategoryData() {
        return categoryData;
    }

    public void loadReportData(int userId) {
        executorService.execute(() -> {
            List<Transaction> transactions = transactionDao.getAllTransactions(userId);
            Map<Integer, Float> categoryTotals = new HashMap<>();

            for (Transaction t : transactions) {
                float current = categoryTotals.getOrDefault(t.categoryId, 0f);
                categoryTotals.put(t.categoryId, current + (float) t.amount);
            }

            List<PieEntry> entries = new ArrayList<>();
            for (Map.Entry<Integer, Float> entry : categoryTotals.entrySet()) {
                // In real app, fetch Category Name by ID
                entries.add(new PieEntry(entry.getValue(), "Cat " + entry.getKey()));
            }
            categoryData.postValue(entries);
        });
    }
}

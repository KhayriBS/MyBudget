package tn.esprit.mybudget.ui.budget;

import android.app.Application;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;

import java.util.List;

import tn.esprit.mybudget.data.AppDatabase;
import tn.esprit.mybudget.data.dao.BudgetDao;
import tn.esprit.mybudget.data.entity.Budget;

public class BudgetViewModel extends AndroidViewModel {

    private BudgetDao budgetDao;
    private LiveData<List<Budget>> allBudgets;

    public BudgetViewModel(@NonNull Application application) {
        super(application);
        AppDatabase db = AppDatabase.getDatabase(application);
        budgetDao = db.budgetDao();
        allBudgets = budgetDao.getAllBudgets();
    }

    public LiveData<List<Budget>> getAllBudgets() {
        return allBudgets;
    }

    public LiveData<List<tn.esprit.mybudget.data.model.BudgetWithCategory>> getBudgetsWithCategories() {
        return budgetDao.getBudgetsWithCategories();
    }

    public LiveData<Budget> getBudgetByCategory(int categoryId) {
        return budgetDao.getBudgetByCategory(categoryId);
    }

    public void insert(Budget budget) {
        AppDatabase.databaseWriteExecutor.execute(() -> {
            budgetDao.insert(budget);
        });
    }

    public void update(Budget budget) {
        AppDatabase.databaseWriteExecutor.execute(() -> {
            budgetDao.update(budget);
        });
    }

    public void delete(Budget budget) {
        AppDatabase.databaseWriteExecutor.execute(() -> {
            budgetDao.delete(budget);
        });
    }
}

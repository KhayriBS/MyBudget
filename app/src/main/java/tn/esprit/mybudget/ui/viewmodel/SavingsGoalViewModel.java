package tn.esprit.mybudget.ui.viewmodel;

import android.app.Application;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;

import java.util.List;

import tn.esprit.mybudget.data.AppDatabase;
import tn.esprit.mybudget.data.dao.SavingsGoalDao;
import tn.esprit.mybudget.data.entity.SavingsGoal;

public class SavingsGoalViewModel extends AndroidViewModel {

    private SavingsGoalDao savingsGoalDao;
    private LiveData<List<SavingsGoal>> allGoals;

    public SavingsGoalViewModel(@NonNull Application application) {
        super(application);
        AppDatabase db = AppDatabase.getDatabase(application);
        savingsGoalDao = db.savingsGoalDao();
        allGoals = savingsGoalDao.getAllGoals(1); // Hardcoded UserID
    }

    public LiveData<List<SavingsGoal>> getAllGoals() {
        return allGoals;
    }

    public void insert(SavingsGoal goal) {
        AppDatabase.databaseWriteExecutor.execute(() -> {
            savingsGoalDao.insert(goal);
        });
    }

    public void update(SavingsGoal goal) {
        AppDatabase.databaseWriteExecutor.execute(() -> {
            savingsGoalDao.update(goal);
        });
    }

    public void delete(SavingsGoal goal) {
        AppDatabase.databaseWriteExecutor.execute(() -> {
            savingsGoalDao.delete(goal);
        });
    }
}

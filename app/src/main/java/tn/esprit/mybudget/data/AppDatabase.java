package tn.esprit.mybudget.data;

import android.content.Context;

import androidx.room.Database;
import androidx.room.Room;
import androidx.room.RoomDatabase;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import tn.esprit.mybudget.data.dao.AccountDao;
import tn.esprit.mybudget.data.dao.BudgetDao;
import tn.esprit.mybudget.data.dao.CategoryDao;
import tn.esprit.mybudget.data.dao.CurrencyDao;
import tn.esprit.mybudget.data.dao.SavingsGoalDao;
import tn.esprit.mybudget.data.dao.TransactionDao;
import tn.esprit.mybudget.data.dao.UserDao;
import tn.esprit.mybudget.data.entity.Account;
import tn.esprit.mybudget.data.entity.Budget;
import tn.esprit.mybudget.data.entity.Category;
import tn.esprit.mybudget.data.entity.Currency;
import tn.esprit.mybudget.data.entity.SavingsGoal;
import tn.esprit.mybudget.data.entity.Transaction;
import tn.esprit.mybudget.data.entity.User;

@Database(entities = { User.class, Transaction.class, Category.class, Account.class, Currency.class,
        Budget.class, SavingsGoal.class }, version = 3, exportSchema = false)
public abstract class AppDatabase extends RoomDatabase {

    public abstract UserDao userDao();

    public abstract TransactionDao transactionDao();

    public abstract CategoryDao categoryDao();

    public abstract AccountDao accountDao();

    public abstract CurrencyDao currencyDao();

    public abstract BudgetDao budgetDao();

    public abstract SavingsGoalDao savingsGoalDao();

    private static volatile AppDatabase INSTANCE;
    private static final int NUMBER_OF_THREADS = 4;
    public static final ExecutorService databaseWriteExecutor = Executors.newFixedThreadPool(NUMBER_OF_THREADS);

    public static AppDatabase getDatabase(final Context context) {
        if (INSTANCE == null) {
            synchronized (AppDatabase.class) {
                if (INSTANCE == null) {
                    INSTANCE = Room.databaseBuilder(context.getApplicationContext(),
                            AppDatabase.class, "budget_app_database")
                            .fallbackToDestructiveMigration() // Handle version change
                            .build();
                }
            }
        }
        return INSTANCE;
    }
}

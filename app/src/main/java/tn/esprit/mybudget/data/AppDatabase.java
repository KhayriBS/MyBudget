package tn.esprit.mybudget.data;

import android.content.Context;

import androidx.room.Database;
import androidx.room.Room;
import androidx.room.RoomDatabase;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import tn.esprit.mybudget.data.dao.CategoryDao;
import tn.esprit.mybudget.data.dao.CurrencyDao;
import tn.esprit.mybudget.data.dao.TransactionDao;
import tn.esprit.mybudget.data.dao.UserDao;
import tn.esprit.mybudget.data.entity.Budget;
import tn.esprit.mybudget.data.entity.Category;
import tn.esprit.mybudget.data.entity.Currency;
import tn.esprit.mybudget.data.entity.Transaction;
import tn.esprit.mybudget.data.entity.User;

@Database(entities = { User.class, Category.class, Transaction.class, Budget.class,
        Currency.class }, version = 1, exportSchema = false)
public abstract class AppDatabase extends RoomDatabase {
    public abstract UserDao userDao();

    public abstract CategoryDao categoryDao();

    public abstract TransactionDao transactionDao();

    public abstract CurrencyDao currencyDao();

    private static volatile AppDatabase INSTANCE;
    private static final int NUMBER_OF_THREADS = 4;
    public static final ExecutorService databaseWriteExecutor = Executors.newFixedThreadPool(NUMBER_OF_THREADS);

    public static AppDatabase getDatabase(final Context context) {
        if (INSTANCE == null) {
            synchronized (AppDatabase.class) {
                if (INSTANCE == null) {
                    INSTANCE = Room.databaseBuilder(context.getApplicationContext(),
                                    AppDatabase.class, "budget_app_database")
                            .build();
                }
            }
        }
        return INSTANCE;
    }
}

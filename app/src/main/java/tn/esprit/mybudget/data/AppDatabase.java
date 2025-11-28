package tn.esprit.mybudget.data;

import android.content.Context;

import androidx.annotation.NonNull;
import androidx.room.Database;
import androidx.room.Room;
import androidx.room.RoomDatabase;
import androidx.sqlite.db.SupportSQLiteDatabase;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import tn.esprit.mybudget.data.dao.AccountDao;
import tn.esprit.mybudget.data.dao.BudgetDao;
import tn.esprit.mybudget.data.dao.CategoryDao;
import tn.esprit.mybudget.data.dao.CurrencyDao;
import tn.esprit.mybudget.data.dao.RecurringTransactionDao;
import tn.esprit.mybudget.data.dao.ReminderDao;
import tn.esprit.mybudget.data.dao.SavingsGoalDao;
import tn.esprit.mybudget.data.dao.TransactionDao;
import tn.esprit.mybudget.data.dao.UserDao;
import tn.esprit.mybudget.data.dao.MemberDao;
import tn.esprit.mybudget.data.entity.Account;
import tn.esprit.mybudget.data.entity.Budget;
import tn.esprit.mybudget.data.entity.Category;
import tn.esprit.mybudget.data.entity.Currency;
import tn.esprit.mybudget.data.entity.RecurringTransaction;
import tn.esprit.mybudget.data.entity.Reminder;
import tn.esprit.mybudget.data.entity.SavingsGoal;
import tn.esprit.mybudget.data.entity.Transaction;
import tn.esprit.mybudget.data.entity.User;
import tn.esprit.mybudget.data.entity.Member;

@Database(entities = { User.class, Transaction.class, Category.class, Account.class, Currency.class,
        Budget.class, SavingsGoal.class, RecurringTransaction.class,
        Reminder.class, Member.class }, version = 9, exportSchema = false)
public abstract class AppDatabase extends RoomDatabase {

    public abstract UserDao userDao();

    public abstract TransactionDao transactionDao();

    public abstract CategoryDao categoryDao();

    public abstract AccountDao accountDao();

    public abstract CurrencyDao currencyDao();

    public abstract BudgetDao budgetDao();

    public abstract SavingsGoalDao savingsGoalDao();

    public abstract RecurringTransactionDao recurringTransactionDao();

    public abstract ReminderDao reminderDao();

    public abstract MemberDao memberDao();

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
                            .addCallback(sRoomDatabaseCallback)
                            .build();
                }
            }
        }
        return INSTANCE;
    }

    private static RoomDatabase.Callback sRoomDatabaseCallback = new RoomDatabase.Callback() {
        @Override
        public void onOpen(@NonNull SupportSQLiteDatabase db) {
            super.onOpen(db);
            databaseWriteExecutor.execute(() -> {
                if (INSTANCE != null) {
                    CategoryDao dao = INSTANCE.categoryDao();

                    // Fetch all existing categories to check against
                    java.util.List<Category> existingCategories = dao.getAllCategories();
                    java.util.Set<String> existingNames = new java.util.HashSet<>();
                    for (Category c : existingCategories) {
                        existingNames.add(c.name);
                    }

                    // Helper to insert if not exists
                    java.util.function.Consumer<Category> insertIfNotExists = (category) -> {
                        if (!existingNames.contains(category.name)) {
                            dao.insert(category);
                        }
                    };

                    // Default Expense Categories
                    insertIfNotExists.accept(new Category("Alimentation", "Expense", "ic_food", "#FF5722"));
                    insertIfNotExists.accept(new Category("Quotidien", "Expense", "ic_daily", "#FF9800"));
                    insertIfNotExists.accept(new Category("Transport", "Expense", "ic_transport", "#03A9F4"));
                    insertIfNotExists.accept(new Category("Social", "Expense", "ic_social", "#E91E63"));
                    insertIfNotExists.accept(new Category("Résidentiel", "Expense", "ic_home", "#795548"));
                    insertIfNotExists.accept(new Category("Cadeaux", "Expense", "ic_gift", "#9C27B0"));
                    insertIfNotExists.accept(new Category("Communication", "Expense", "ic_phone", "#3F51B5"));
                    insertIfNotExists.accept(new Category("Vêtements", "Expense", "ic_clothing", "#00BCD4"));
                    insertIfNotExists.accept(new Category("Loisirs", "Expense", "ic_entertainment", "#673AB7"));
                    insertIfNotExists.accept(new Category("Beauté", "Expense", "ic_beauty", "#F06292"));
                    insertIfNotExists.accept(new Category("Médical", "Expense", "ic_health", "#F44336"));
                    insertIfNotExists.accept(new Category("Impôt", "Expense", "ic_tax", "#607D8B"));
                    insertIfNotExists.accept(new Category("Éducation", "Expense", "ic_education", "#FFC107"));
                    insertIfNotExists.accept(new Category("Bébé", "Expense", "ic_baby", "#FFEB3B"));
                    insertIfNotExists.accept(new Category("Animal de compagnie", "Expense", "ic_pet", "#8D6E63"));
                    insertIfNotExists.accept(new Category("Voyage", "Expense", "ic_travel", "#4CAF50"));

                    // Default Income Categories
                    insertIfNotExists.accept(new Category("Salaire", "Income", "ic_salary", "#4CAF50"));
                    insertIfNotExists.accept(new Category("Primes", "Income", "ic_bonus", "#8BC34A"));
                    insertIfNotExists.accept(new Category("Ventes", "Income", "ic_sales", "#CDDC39"));

                    // Default Currencies
                    CurrencyDao currencyDao = INSTANCE.currencyDao();
                    if (currencyDao.getAllCurrenciesSync().isEmpty()) {
                        currencyDao.insert(new Currency("USD", "$", 1.0));
                        currencyDao.insert(new Currency("EUR", "€", 0.92));
                        currencyDao.insert(new Currency("GBP", "£", 0.79));
                        currencyDao.insert(new Currency("JPY", "¥", 150.0));
                        currencyDao.insert(new Currency("CAD", "C$", 1.35));
                        currencyDao.insert(new Currency("AUD", "A$", 1.52));
                        currencyDao.insert(new Currency("TND", "DT", 3.1));
                    }
                }
            });
        }
    };
}

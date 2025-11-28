package tn.esprit.mybudget.data.entity;

import androidx.room.Entity;
import androidx.room.ForeignKey;
import androidx.room.Ignore;
import androidx.room.Index;
import androidx.room.PrimaryKey;

/**
 * Entity representing a recurring transaction reminder.
 * Links to an account and category for automatic transaction creation.
 */
@Entity(tableName = "reminders", foreignKeys = {
        @ForeignKey(entity = Account.class, parentColumns = "id", childColumns = "accountId", onDelete = ForeignKey.CASCADE),
        @ForeignKey(entity = Category.class, parentColumns = "id", childColumns = "categoryId", onDelete = ForeignKey.CASCADE)
}, indices = {
        @Index("accountId"),
        @Index("categoryId")
})
public class Reminder {
    @PrimaryKey(autoGenerate = true)
    public int id;

    public String title;
    public String description;
    public double amount;
    public String transactionType; // "Income" or "Expense"
    public int accountId;
    public int categoryId;

    // Scheduling fields
    public long scheduledDate; // Next scheduled execution timestamp
    public long createdAt;
    public String frequency; // "ONCE", "DAILY", "WEEKLY", "MONTHLY", "YEARLY"
    public boolean isEnabled;
    public String currency; // Currency code from the account (TND, USD, EUR, etc.)

    // For tracking
    public long lastExecutedAt;
    public int executionCount;

    /**
     * Default constructor for Room
     */
    public Reminder() {
    }

    /**
     * Full constructor for creating a new reminder
     */
    @Ignore
    public Reminder(String title, String description, double amount, String transactionType,
            int accountId, int categoryId, long scheduledDate, String frequency, boolean isEnabled, String currency) {
        this.title = title;
        this.description = description;
        this.amount = amount;
        this.transactionType = transactionType;
        this.accountId = accountId;
        this.categoryId = categoryId;
        this.scheduledDate = scheduledDate;
        this.frequency = frequency;
        this.isEnabled = isEnabled;
        this.currency = currency;
        this.createdAt = System.currentTimeMillis();
        this.lastExecutedAt = 0;
        this.executionCount = 0;
    }

    /**
     * Legacy constructor for backward compatibility
     */
    @Ignore
    public Reminder(String title, long time, boolean isEnabled) {
        this.title = title;
        this.scheduledDate = time;
        this.isEnabled = isEnabled;
        this.frequency = "ONCE";
        this.createdAt = System.currentTimeMillis();
        this.amount = 0;
        this.transactionType = "Expense";
        this.accountId = 0;
        this.categoryId = 0;
        this.currency = "TND";
        this.lastExecutedAt = 0;
        this.executionCount = 0;
    }

    /**
     * Calculate the next execution date based on frequency
     */
    public long calculateNextExecutionDate() {
        if (frequency == null || frequency.equals("ONCE")) {
            return scheduledDate;
        }

        java.util.Calendar calendar = java.util.Calendar.getInstance();
        calendar.setTimeInMillis(scheduledDate);

        switch (frequency) {
            case "DAILY":
                calendar.add(java.util.Calendar.DAY_OF_MONTH, 1);
                break;
            case "WEEKLY":
                calendar.add(java.util.Calendar.WEEK_OF_YEAR, 1);
                break;
            case "MONTHLY":
                calendar.add(java.util.Calendar.MONTH, 1);
                break;
            case "YEARLY":
                calendar.add(java.util.Calendar.YEAR, 1);
                break;
        }
        return calendar.getTimeInMillis();
    }
}

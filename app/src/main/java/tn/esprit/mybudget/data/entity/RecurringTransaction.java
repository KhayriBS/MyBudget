package tn.esprit.mybudget.data.entity;

import androidx.room.Entity;
import androidx.room.PrimaryKey;

@Entity(tableName = "recurring_transactions")
public class RecurringTransaction {
    @PrimaryKey(autoGenerate = true)
    public int id;

    public int userId;
    public String note;
    public double amount;
    public String type; // "Income" or "Expense"
    public int categoryId;
    public int accountId;
    public String frequency; // "Daily", "Weekly", "Monthly"
    public long nextExecutionDate; // Timestamp

    public RecurringTransaction(int userId, String note, double amount, String type, int categoryId, int accountId,
            String frequency, long nextExecutionDate) {
        this.userId = userId;
        this.note = note;
        this.amount = amount;
        this.type = type;
        this.categoryId = categoryId;
        this.accountId = accountId;
        this.frequency = frequency;
        this.nextExecutionDate = nextExecutionDate;
    }
}

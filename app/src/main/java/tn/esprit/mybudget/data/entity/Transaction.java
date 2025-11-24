package tn.esprit.mybudget.data.entity;

import androidx.room.Entity;
import androidx.room.ForeignKey;
import androidx.room.PrimaryKey;

@Entity(tableName = "transactions", foreignKeys = {
        @ForeignKey(entity = Category.class, parentColumns = "id", childColumns = "categoryId", onDelete = ForeignKey.CASCADE),
        @ForeignKey(entity = User.class, parentColumns = "uid", childColumns = "userId", onDelete = ForeignKey.CASCADE)
})
public class Transaction {
    @PrimaryKey(autoGenerate = true)
    public int id;

    public int userId;
    public int categoryId;
    public double amount;
    public long date;
    public String note;
    public boolean isRecurring;
    public String recurrenceInterval; // "DAILY", "WEEKLY", "MONTHLY"

    public String originalCurrencyCode;
    public double originalAmount;

    public Transaction(int userId, int categoryId, double amount, long date, String note) {
        this.userId = userId;
        this.categoryId = categoryId;
        this.amount = amount;
        this.date = date;
        this.note = note;
        this.isRecurring = false;
    }
}

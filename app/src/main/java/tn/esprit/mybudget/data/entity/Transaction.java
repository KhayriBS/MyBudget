package tn.esprit.mybudget.data.entity;

import androidx.room.Entity;
import androidx.room.ForeignKey;
import androidx.room.Index;
import androidx.room.PrimaryKey;

@Entity(tableName = "transactions", foreignKeys = {
        @ForeignKey(entity = User.class, parentColumns = "uid", childColumns = "userId", onDelete = ForeignKey.CASCADE),
        @ForeignKey(entity = Category.class, parentColumns = "id", childColumns = "categoryId", onDelete = ForeignKey.CASCADE),
        @ForeignKey(entity = tn.esprit.mybudget.data.entity.Account.class, parentColumns = "id", childColumns = "accountId", onDelete = ForeignKey.CASCADE)
}, indices = {
        @Index(value = "userId"),
        @Index(value = "categoryId"),
        @Index(value = "accountId")
})
public class Transaction {
    @PrimaryKey(autoGenerate = true)
    public int id;

    public int userId;
    public int categoryId;
    public int accountId;
    public double amount;
    public long date;
    public String note;

    public Transaction(int userId, int categoryId, int accountId, double amount, long date, String note) {
        this.userId = userId;
        this.categoryId = categoryId;
        this.accountId = accountId;
        this.amount = amount;
        this.date = date;
        this.note = note;
    }
}

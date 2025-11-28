package tn.esprit.mybudget.data.entity;

import androidx.room.Entity;
import androidx.room.ForeignKey;
import androidx.room.Index;
import androidx.room.PrimaryKey;

@Entity(tableName = "budgets", foreignKeys = {
        @ForeignKey(entity = Category.class, parentColumns = "id", childColumns = "categoryId", onDelete = ForeignKey.CASCADE),
        @ForeignKey(entity = User.class, parentColumns = "uid", childColumns = "userId", onDelete = ForeignKey.CASCADE)
}, indices = {
        @Index(value = "categoryId"),
        @Index(value = "userId")
})
public class Budget {
    @PrimaryKey(autoGenerate = true)
    public int id;

    public int userId;
    public int categoryId;
    public double limitAmount;
    public String period; // "MONTHLY", "WEEKLY"

    public Budget(int userId, int categoryId, double limitAmount, String period) {
        this.userId = userId;
        this.categoryId = categoryId;
        this.limitAmount = limitAmount;
        this.period = period;
    }
}

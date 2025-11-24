package tn.esprit.mybudget.data.entity;

import androidx.room.Entity;
import androidx.room.PrimaryKey;

@Entity(tableName = "savings_goals")
public class SavingsGoal {
    @PrimaryKey(autoGenerate = true)
    public int id;

    public int userId;
    public String name;
    public double targetAmount;
    public double currentAmount;
    public long deadline; // Timestamp

    public SavingsGoal(int userId, String name, double targetAmount, double currentAmount, long deadline) {
        this.userId = userId;
        this.name = name;
        this.targetAmount = targetAmount;
        this.currentAmount = currentAmount;
        this.deadline = deadline;
    }
}

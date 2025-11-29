package tn.esprit.mybudget.data.entity;

import androidx.room.Entity;
import androidx.room.ForeignKey;
import androidx.room.Index;
import androidx.room.PrimaryKey;

@Entity(tableName = "members", foreignKeys = {
        @ForeignKey(entity = Account.class, parentColumns = "id", childColumns = "accountId", onDelete = ForeignKey.SET_NULL)
}, indices = { @Index("accountId") })
public class Member {
    @PrimaryKey(autoGenerate = true)
    public int id;

    public String name;
    public double amount; // Positive = they owe you (lent), Negative = you owe them (borrowed)
    public String type; // "Lent" or "Borrowed"
    public long date; // Date of the transaction
    public String note; // Optional note/description
    public Integer accountId; // Account used for the transaction (nullable)
    public boolean isSettled; // Whether the debt is settled

    public Member(String name, double amount, String type, long date, String note, Integer accountId) {
        this.name = name;
        this.amount = amount;
        this.type = type;
        this.date = date;
        this.note = note;
        this.accountId = accountId;
        this.isSettled = false;
    }

    // Get the display amount (always positive for display)
    public double getDisplayAmount() {
        return Math.abs(amount);
    }

    // Check if this person owes you money
    public boolean owesYou() {
        return "Lent".equals(type);
    }
}

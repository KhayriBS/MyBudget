package tn.esprit.mybudget.data.entity;

import androidx.room.Entity;
import androidx.room.PrimaryKey;

@Entity(tableName = "accounts")
public class Account {
    @PrimaryKey(autoGenerate = true)
    public int id;

    public String name;
    public String type; // CASH, CARD, SAVINGS
    public double balance;
    public String currency; // TND, USD, EUR

    public Account(String name, String type, double balance, String currency) {
        this.name = name;
        this.type = type;
        this.balance = balance;
        this.currency = currency;
    }
}

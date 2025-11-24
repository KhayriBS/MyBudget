package tn.esprit.mybudget.data.entity;

import androidx.room.Entity;
import androidx.room.PrimaryKey;

@Entity(tableName = "currencies")

public class Currency {
    @PrimaryKey(autoGenerate = true)
    public int id;

    public String code; // USD, EUR, etc.
    public String symbol; // $, €, etc.
    public double exchangeRateToBase; // Rate relative to base currency (e.g. USD)

    public Currency(String code, String symbol, double exchangeRateToBase) {
        this.code = code;
        this.symbol = symbol;
        this.exchangeRateToBase = exchangeRateToBase;
    }
}

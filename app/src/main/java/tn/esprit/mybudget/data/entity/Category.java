package tn.esprit.mybudget.data.entity;

import androidx.room.Entity;
import androidx.room.PrimaryKey;

@Entity(tableName = "categories")

public class Category {
    @PrimaryKey(autoGenerate = true)
    public int id;

    public String name;
    public String type; // "INCOME" or "EXPENSE"
    public String iconName;
    public String colorHex;

    public Category(String name, String type, String iconName, String colorHex) {
        this.name = name;
        this.type = type;
        this.iconName = iconName;
        this.colorHex = colorHex;
    }
}

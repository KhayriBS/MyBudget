package tn.esprit.mybudget.data.entity;

import androidx.room.Entity;
import androidx.room.PrimaryKey;

@Entity(tableName = "users")

public class User {
    @PrimaryKey(autoGenerate = true)
    public int uid;

    public String username;
    public String passwordHash;
    public String email;
    public boolean hasBiometricEnabled;

    public User(String username, String passwordHash, String email) {
        this.username = username;
        this.passwordHash = passwordHash;
        this.email = email;
        this.hasBiometricEnabled = false;
    }
}

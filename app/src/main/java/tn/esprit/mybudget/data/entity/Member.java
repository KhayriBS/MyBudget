package tn.esprit.mybudget.data.entity;

import androidx.room.Entity;
import androidx.room.PrimaryKey;

@Entity(tableName = "members")
public class Member {
    @PrimaryKey(autoGenerate = true)
    public int id;

    public String name;
    public String email;
    public String role; // e.g., "Admin", "Member"

    public Member(String name, String email, String role) {
        this.name = name;
        this.email = email;
        this.role = role;
    }
}

package tn.esprit.mybudget.data.entity;

import androidx.room.Entity;
import androidx.room.PrimaryKey;

@Entity(tableName = "reminders")
public class Reminder {
    @PrimaryKey(autoGenerate = true)
    public int id;

    public String title;
    public long time; // Timestamp
    public boolean isEnabled;

    public Reminder(String title, long time, boolean isEnabled) {
        this.title = title;
        this.time = time;
        this.isEnabled = isEnabled;
    }
}

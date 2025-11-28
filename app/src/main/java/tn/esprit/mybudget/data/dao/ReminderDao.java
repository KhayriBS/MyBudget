package tn.esprit.mybudget.data.dao;

import androidx.lifecycle.LiveData;
import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.Query;
import androidx.room.Update;

import java.util.List;

import tn.esprit.mybudget.data.entity.Reminder;

@Dao
public interface ReminderDao {
    @Insert
    long insert(Reminder reminder);

    @Update
    void update(Reminder reminder);

    @Delete
    void delete(Reminder reminder);

    @Query("SELECT * FROM reminders ORDER BY scheduledDate ASC")
    LiveData<List<Reminder>> getAllReminders();

    @Query("SELECT * FROM reminders WHERE isEnabled = 1 ORDER BY scheduledDate ASC")
    LiveData<List<Reminder>> getEnabledReminders();

    @Query("SELECT * FROM reminders WHERE id = :id")
    Reminder getReminderById(int id);

    @Query("SELECT * FROM reminders WHERE scheduledDate <= :currentTime AND isEnabled = 1")
    List<Reminder> getDueReminders(long currentTime);

    @Query("SELECT * FROM reminders WHERE accountId = :accountId")
    LiveData<List<Reminder>> getRemindersByAccount(int accountId);

    @Query("UPDATE reminders SET isEnabled = :enabled WHERE id = :id")
    void updateEnabledStatus(int id, boolean enabled);

    @Query("UPDATE reminders SET scheduledDate = :nextDate, lastExecutedAt = :executedAt, executionCount = executionCount + 1 WHERE id = :id")
    void updateAfterExecution(int id, long nextDate, long executedAt);

    @Query("DELETE FROM reminders WHERE id = :id")
    void deleteById(int id);
}

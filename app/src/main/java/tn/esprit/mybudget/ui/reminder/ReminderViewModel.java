package tn.esprit.mybudget.ui.reminder;

import android.app.Application;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;

import java.util.List;

import tn.esprit.mybudget.data.AppDatabase;
import tn.esprit.mybudget.data.dao.ReminderDao;
import tn.esprit.mybudget.data.entity.Reminder;

public class ReminderViewModel extends AndroidViewModel {

    private ReminderDao reminderDao;
    private LiveData<List<Reminder>> allReminders;

    public ReminderViewModel(@NonNull Application application) {
        super(application);
        AppDatabase db = AppDatabase.getDatabase(application);
        reminderDao = db.reminderDao();
        allReminders = reminderDao.getAllReminders();
    }

    public LiveData<List<Reminder>> getAllReminders() {
        return allReminders;
    }

    public void insert(Reminder reminder) {
        AppDatabase.databaseWriteExecutor.execute(() -> {
            reminderDao.insert(reminder);
        });
    }

    public void delete(Reminder reminder) {
        AppDatabase.databaseWriteExecutor.execute(() -> {
            reminderDao.delete(reminder);
        });
    }
}

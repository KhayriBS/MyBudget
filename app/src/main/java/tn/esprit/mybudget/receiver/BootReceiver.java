package tn.esprit.mybudget.receiver;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

import java.util.List;

import tn.esprit.mybudget.data.AppDatabase;
import tn.esprit.mybudget.data.entity.Reminder;
import tn.esprit.mybudget.util.ReminderScheduler;

/**
 * BroadcastReceiver that reschedules all reminders after device boot.
 * This is necessary because AlarmManager alarms are cleared on device restart.
 */
public class BootReceiver extends BroadcastReceiver {

    private static final String TAG = "BootReceiver";

    @Override
    public void onReceive(Context context, Intent intent) {
        if (intent == null || intent.getAction() == null) {
            return;
        }

        String action = intent.getAction();
        if (Intent.ACTION_BOOT_COMPLETED.equals(action) ||
                Intent.ACTION_LOCKED_BOOT_COMPLETED.equals(action) ||
                "android.intent.action.QUICKBOOT_POWERON".equals(action)) {

            Log.d(TAG, "Device booted, rescheduling reminders");
            rescheduleAllReminders(context);
        }
    }

    private void rescheduleAllReminders(Context context) {
        AppDatabase.databaseWriteExecutor.execute(() -> {
            AppDatabase db = AppDatabase.getDatabase(context);

            // Get all enabled reminders that haven't passed
            long currentTime = System.currentTimeMillis();
            List<Reminder> dueReminders = db.reminderDao().getDueReminders(Long.MAX_VALUE);

            for (Reminder reminder : dueReminders) {
                if (reminder.isEnabled) {
                    // If the reminder is overdue, process it immediately or skip
                    if (reminder.scheduledDate < currentTime) {
                        // For recurring reminders, calculate the next valid date
                        if (!"ONCE".equals(reminder.frequency)) {
                            while (reminder.scheduledDate < currentTime) {
                                reminder.scheduledDate = reminder.calculateNextExecutionDate();
                            }
                            db.reminderDao().update(reminder);
                        }
                    }

                    // Schedule the reminder
                    if (reminder.scheduledDate >= currentTime) {
                        ReminderScheduler.scheduleReminder(context, reminder);
                        Log.d(TAG, "Rescheduled reminder: " + reminder.id);
                    }
                }
            }

            Log.d(TAG, "Finished rescheduling reminders");
        });
    }
}

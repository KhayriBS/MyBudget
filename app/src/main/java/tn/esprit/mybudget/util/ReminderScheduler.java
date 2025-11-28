package tn.esprit.mybudget.util;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.util.Log;

import tn.esprit.mybudget.data.entity.Reminder;
import tn.esprit.mybudget.receiver.ReminderBroadcastReceiver;

/**
 * Utility class for scheduling and canceling reminder alarms.
 * Handles the AlarmManager interactions for recurring transactions.
 */
public class ReminderScheduler {

    private static final String TAG = "ReminderScheduler";
    public static final String EXTRA_REMINDER_ID = "reminder_id";

    /**
     * Schedule a reminder alarm using AlarmManager.
     * 
     * @param context  The application context
     * @param reminder The reminder to schedule
     */
    public static void scheduleReminder(Context context, Reminder reminder) {
        if (!reminder.isEnabled) {
            Log.d(TAG, "Reminder " + reminder.id + " is disabled, not scheduling");
            return;
        }

        AlarmManager alarmManager = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
        if (alarmManager == null) {
            Log.e(TAG, "AlarmManager not available");
            return;
        }

        Intent intent = new Intent(context, ReminderBroadcastReceiver.class);
        intent.putExtra(EXTRA_REMINDER_ID, reminder.id);

        PendingIntent pendingIntent = PendingIntent.getBroadcast(
                context,
                reminder.id, // Use reminder ID as request code for uniqueness
                intent,
                PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE);

        // For exact timing on API 31+, we need special permission handling
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            if (alarmManager.canScheduleExactAlarms()) {
                alarmManager.setExactAndAllowWhileIdle(
                        AlarmManager.RTC_WAKEUP,
                        reminder.scheduledDate,
                        pendingIntent);
            } else {
                // Fall back to inexact alarm if exact alarm permission not granted
                alarmManager.setAndAllowWhileIdle(
                        AlarmManager.RTC_WAKEUP,
                        reminder.scheduledDate,
                        pendingIntent);
                Log.w(TAG, "Exact alarm permission not granted, using inexact alarm");
            }
        } else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            alarmManager.setExactAndAllowWhileIdle(
                    AlarmManager.RTC_WAKEUP,
                    reminder.scheduledDate,
                    pendingIntent);
        } else {
            alarmManager.setExact(
                    AlarmManager.RTC_WAKEUP,
                    reminder.scheduledDate,
                    pendingIntent);
        }

        Log.d(TAG, "Scheduled reminder " + reminder.id + " for " + reminder.scheduledDate);
    }

    /**
     * Cancel a scheduled reminder alarm.
     * 
     * @param context    The application context
     * @param reminderId The ID of the reminder to cancel
     */
    public static void cancelReminder(Context context, int reminderId) {
        AlarmManager alarmManager = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
        if (alarmManager == null) {
            return;
        }

        Intent intent = new Intent(context, ReminderBroadcastReceiver.class);
        PendingIntent pendingIntent = PendingIntent.getBroadcast(
                context,
                reminderId,
                intent,
                PendingIntent.FLAG_NO_CREATE | PendingIntent.FLAG_IMMUTABLE);

        if (pendingIntent != null) {
            alarmManager.cancel(pendingIntent);
            pendingIntent.cancel();
            Log.d(TAG, "Cancelled reminder " + reminderId);
        }
    }

    /**
     * Reschedule a reminder after it has been executed.
     * This is called for recurring reminders.
     * 
     * @param context  The application context
     * @param reminder The reminder with updated scheduledDate
     */
    public static void rescheduleReminder(Context context, Reminder reminder) {
        cancelReminder(context, reminder.id);

        // Update the scheduled date to the next occurrence
        reminder.scheduledDate = reminder.calculateNextExecutionDate();

        scheduleReminder(context, reminder);
    }

    /**
     * Check if reminders can be scheduled exactly (for API 31+)
     * 
     * @param context The application context
     * @return true if exact alarms can be scheduled
     */
    public static boolean canScheduleExactAlarms(Context context) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            AlarmManager alarmManager = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
            return alarmManager != null && alarmManager.canScheduleExactAlarms();
        }
        return true;
    }
}

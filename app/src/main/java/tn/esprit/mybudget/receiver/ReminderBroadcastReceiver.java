package tn.esprit.mybudget.receiver;

import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Build;
import android.util.Log;

import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;

import java.text.NumberFormat;
import java.util.Currency;
import java.util.Locale;

import tn.esprit.mybudget.R;
import tn.esprit.mybudget.data.AppDatabase;
import tn.esprit.mybudget.data.dao.AccountDao;
import tn.esprit.mybudget.data.dao.ReminderDao;
import tn.esprit.mybudget.data.dao.TransactionDao;
import tn.esprit.mybudget.data.entity.Account;
import tn.esprit.mybudget.data.entity.Reminder;
import tn.esprit.mybudget.data.entity.Transaction;
import tn.esprit.mybudget.ui.reminder.RemindersActivity;
import tn.esprit.mybudget.util.ReminderScheduler;

/**
 * BroadcastReceiver that handles reminder alarms.
 * Creates transactions and sends notifications when reminders are triggered.
 */
public class ReminderBroadcastReceiver extends BroadcastReceiver {

    private static final String TAG = "ReminderReceiver";
    private static final String CHANNEL_ID = "reminder_channel";
    private static final String CHANNEL_NAME = "Reminder Notifications";

    @Override
    public void onReceive(Context context, Intent intent) {
        int reminderId = intent.getIntExtra(ReminderScheduler.EXTRA_REMINDER_ID, -1);

        if (reminderId == -1) {
            Log.e(TAG, "Invalid reminder ID received");
            return;
        }

        Log.d(TAG, "Reminder triggered: " + reminderId);

        // Process the reminder in a background thread
        AppDatabase.databaseWriteExecutor.execute(() -> {
            processReminder(context, reminderId);
        });
    }

    private void processReminder(Context context, int reminderId) {
        AppDatabase db = AppDatabase.getDatabase(context);
        ReminderDao reminderDao = db.reminderDao();
        TransactionDao transactionDao = db.transactionDao();
        AccountDao accountDao = db.accountDao();

        Reminder reminder = reminderDao.getReminderById(reminderId);

        if (reminder == null) {
            Log.e(TAG, "Reminder not found: " + reminderId);
            return;
        }

        if (!reminder.isEnabled) {
            Log.d(TAG, "Reminder is disabled: " + reminderId);
            return;
        }

        // Get user ID from SharedPreferences
        SharedPreferences prefs = context.getSharedPreferences("UserPrefs", Context.MODE_PRIVATE);
        int userId = prefs.getInt("userId", 1); // Default to 1 if not set

        // Create the transaction
        Transaction transaction = new Transaction(
                userId,
                reminder.categoryId,
                reminder.accountId,
                reminder.amount,
                System.currentTimeMillis(),
                reminder.title + (reminder.description != null && !reminder.description.isEmpty()
                        ? " - " + reminder.description
                        : ""));
        transactionDao.insert(transaction);
        Log.d(TAG, "Transaction created for reminder: " + reminderId);

        // Update account balance
        Account account = accountDao.getAccountById(reminder.accountId);
        if (account != null) {
            if ("Expense".equals(reminder.transactionType)) {
                account.balance -= reminder.amount;
            } else {
                account.balance += reminder.amount;
            }
            accountDao.update(account);
            Log.d(TAG, "Account balance updated: " + account.name);
        }

        // Show notification
        showNotification(context, reminder, account);

        // Handle recurring reminders
        if ("ONCE".equals(reminder.frequency)) {
            // Disable one-time reminders after execution
            reminderDao.updateEnabledStatus(reminderId, false);
            Log.d(TAG, "One-time reminder disabled: " + reminderId);
        } else {
            // Schedule next occurrence for recurring reminders
            long nextDate = reminder.calculateNextExecutionDate();
            reminderDao.updateAfterExecution(reminderId, nextDate, System.currentTimeMillis());

            // Schedule the next alarm
            reminder.scheduledDate = nextDate;
            ReminderScheduler.scheduleReminder(context, reminder);
            Log.d(TAG, "Recurring reminder rescheduled: " + reminderId + " for " + nextDate);
        }
    }

    private void showNotification(Context context, Reminder reminder, Account account) {
        createNotificationChannel(context);

        Intent intent = new Intent(context, RemindersActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        PendingIntent pendingIntent = PendingIntent.getActivity(
                context,
                reminder.id,
                intent,
                PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE);

        String accountName = account != null ? account.name : "Unknown Account";
        NumberFormat currencyFormat = NumberFormat.getCurrencyInstance(Locale.getDefault());
        if (account != null && account.currency != null) {
            try {
                currencyFormat.setCurrency(Currency.getInstance(account.currency));
            } catch (IllegalArgumentException e) {
                // Use default currency if invalid
            }
        }
        String amountStr = currencyFormat.format(reminder.amount);

        String contentText = String.format("%s: %s from %s",
                reminder.transactionType, amountStr, accountName);

        NotificationCompat.Builder builder = new NotificationCompat.Builder(context, CHANNEL_ID)
                .setSmallIcon(R.drawable.ic_launcher_foreground)
                .setContentTitle("Transaction Recorded: " + reminder.title)
                .setContentText(contentText)
                .setPriority(NotificationCompat.PRIORITY_HIGH)
                .setContentIntent(pendingIntent)
                .setAutoCancel(true);

        try {
            NotificationManagerCompat notificationManager = NotificationManagerCompat.from(context);
            notificationManager.notify(reminder.id, builder.build());
        } catch (SecurityException e) {
            Log.e(TAG, "Notification permission not granted", e);
        }
    }

    private void createNotificationChannel(Context context) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel channel = new NotificationChannel(
                    CHANNEL_ID,
                    CHANNEL_NAME,
                    NotificationManager.IMPORTANCE_HIGH);
            channel.setDescription("Notifications for recurring transaction reminders");

            NotificationManager notificationManager = context.getSystemService(NotificationManager.class);
            if (notificationManager != null) {
                notificationManager.createNotificationChannel(channel);
            }
        }
    }
}

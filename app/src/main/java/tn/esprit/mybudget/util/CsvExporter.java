package tn.esprit.mybudget.util;

import android.content.Context;
import android.os.Environment;
import android.util.Log;
import android.widget.Toast;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;

import tn.esprit.mybudget.data.entity.Transaction;

public class CsvExporter {

    public static void exportTransactionsToCsv(Context context, List<Transaction> transactions) {
        if (transactions == null || transactions.isEmpty()) {
            Toast.makeText(context, "No transactions to export", Toast.LENGTH_SHORT).show();
            return;
        }

        File exportDir = new File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOCUMENTS),
                "MyBudget");
        if (!exportDir.exists()) {
            if (!exportDir.mkdirs()) {
                Log.e("CsvExporter", "Failed to create directory");
                Toast.makeText(context, "Failed to create export directory", Toast.LENGTH_SHORT).show();
                return;
            }
        }

        String fileName = "transactions_"
                + new SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(new Date()) + ".csv";
        File file = new File(exportDir, fileName);

        try (FileWriter writer = new FileWriter(file)) {
            // Write Header
            writer.append("ID,Date,Amount,Category,Account,Note\n");

            SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault());

            for (Transaction t : transactions) {
                writer.append(String.valueOf(t.id)).append(",");
                writer.append(dateFormat.format(new Date(t.date))).append(",");
                writer.append(String.valueOf(t.amount)).append(",");
                writer.append(String.valueOf(t.categoryId)).append(","); // TODO: Replace with Name
                writer.append(String.valueOf(t.accountId)).append(","); // TODO: Replace with Name
                writer.append(escapeSpecialCharacters(t.note)).append("\n");
            }

            writer.flush();
            Toast.makeText(context, "Exported to " + file.getAbsolutePath(), Toast.LENGTH_LONG).show();

        } catch (IOException e) {
            Log.e("CsvExporter", "Error writing CSV", e);
            Toast.makeText(context, "Export failed: " + e.getMessage(), Toast.LENGTH_SHORT).show();
        }
    }

    private static String escapeSpecialCharacters(String data) {
        if (data == null)
            return "";
        String escapedData = data.replaceAll("\\R", " ");
        if (data.contains(",") || data.contains("\"") || data.contains("'")) {
            data = data.replace("\"", "\"\"");
            escapedData = "\"" + data + "\"";
        }
        return escapedData;
    }
}

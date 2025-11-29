package tn.esprit.mybudget.ui.transaction;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import tn.esprit.mybudget.R;
import tn.esprit.mybudget.data.entity.TransactionWithAccount;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class TransactionAdapter extends RecyclerView.Adapter<TransactionAdapter.TransactionViewHolder> {

    private List<TransactionWithAccount> transactions = new ArrayList<>();
    private OnTransactionClickListener clickListener;
    private OnTransactionDeleteListener deleteListener;

    public interface OnTransactionClickListener {
        void onTransactionClick(TransactionWithAccount transaction);
    }

    public interface OnTransactionDeleteListener {
        void onTransactionDelete(TransactionWithAccount transaction);
    }

    public void setOnTransactionClickListener(OnTransactionClickListener listener) {
        this.clickListener = listener;
    }

    public void setOnTransactionDeleteListener(OnTransactionDeleteListener listener) {
        this.deleteListener = listener;
    }

    public void setTransactions(List<TransactionWithAccount> transactions) {
        this.transactions = transactions;
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public TransactionViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_transaction, parent, false);
        return new TransactionViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull TransactionViewHolder holder, int position) {
        TransactionWithAccount transactionWithAccount = transactions.get(position);
        holder.tvNote.setText(transactionWithAccount.transaction.note);

        // Get currency symbol from account
        String currencySymbol = getCurrencySymbol(transactionWithAccount.account != null
                ? transactionWithAccount.account.currency
                : "USD");
        holder.tvAmount.setText(String.format(Locale.getDefault(), "%s%.2f",
                currencySymbol, transactionWithAccount.transaction.amount));

        SimpleDateFormat sdf = new SimpleDateFormat("MMM dd, yyyy", Locale.getDefault());
        holder.tvDate.setText(sdf.format(new Date(transactionWithAccount.transaction.date)));

        // Click to edit
        holder.itemView.setOnClickListener(v -> {
            if (clickListener != null) {
                clickListener.onTransactionClick(transactionWithAccount);
            }
        });

        // Delete button
        if (holder.ivDelete != null) {
            holder.ivDelete.setOnClickListener(v -> {
                if (deleteListener != null) {
                    deleteListener.onTransactionDelete(transactionWithAccount);
                }
            });
        }
    }

    private String getCurrencySymbol(String currencyCode) {
        if (currencyCode == null)
            return "$";
        switch (currencyCode) {
            case "TND":
                return "DT ";
            case "EUR":
                return "€";
            case "GBP":
                return "£";
            case "JPY":
                return "¥";
            case "USD":
            default:
                return "$";
        }
    }

    @Override
    public int getItemCount() {
        return transactions.size();
    }

    static class TransactionViewHolder extends RecyclerView.ViewHolder {
        TextView tvNote;
        TextView tvAmount;
        TextView tvDate;
        ImageView ivDelete;

        public TransactionViewHolder(@NonNull View itemView) {
            super(itemView);
            tvNote = itemView.findViewById(R.id.tvNote);
            tvAmount = itemView.findViewById(R.id.tvAmount);
            tvDate = itemView.findViewById(R.id.tvDate);
            ivDelete = itemView.findViewById(R.id.ivDeleteTransaction);
        }
    }
}

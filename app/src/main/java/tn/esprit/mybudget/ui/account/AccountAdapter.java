package tn.esprit.mybudget.ui.account;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;
import java.util.List;

import tn.esprit.mybudget.R;
import tn.esprit.mybudget.data.entity.Account;

public class AccountAdapter extends RecyclerView.Adapter<AccountAdapter.AccountViewHolder> {

    private List<Account> accounts = new ArrayList<>();
    private OnAccountClickListener clickListener;
    private OnAccountDeleteListener deleteListener;

    public interface OnAccountClickListener {
        void onAccountClick(Account account);
    }

    public interface OnAccountDeleteListener {
        void onAccountDelete(Account account);
    }

    public void setOnAccountClickListener(OnAccountClickListener listener) {
        this.clickListener = listener;
    }

    public void setOnAccountDeleteListener(OnAccountDeleteListener listener) {
        this.deleteListener = listener;
    }

    public void setAccounts(List<Account> accounts) {
        this.accounts = accounts;
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public AccountViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_account, parent, false);
        return new AccountViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull AccountViewHolder holder, int position) {
        Account account = accounts.get(position);
        holder.tvName.setText(account.name);
        holder.tvType.setText(account.type);
        holder.tvBalance.setText(String.format("%.2f %s", account.balance, account.currency));

        holder.itemView.setOnClickListener(v -> {
            if (clickListener != null) {
                clickListener.onAccountClick(account);
            }
        });

        if (holder.ivDelete != null) {
            holder.ivDelete.setOnClickListener(v -> {
                if (deleteListener != null) {
                    deleteListener.onAccountDelete(account);
                }
            });
        }
    }

    @Override
    public int getItemCount() {
        return accounts.size();
    }

    static class AccountViewHolder extends RecyclerView.ViewHolder {
        TextView tvName, tvType, tvBalance;
        ImageView ivDelete;

        public AccountViewHolder(@NonNull View itemView) {
            super(itemView);
            tvName = itemView.findViewById(R.id.tvAccountName);
            tvType = itemView.findViewById(R.id.tvAccountType);
            tvBalance = itemView.findViewById(R.id.tvAccountBalance);
            ivDelete = itemView.findViewById(R.id.ivDeleteAccount);
        }
    }
}

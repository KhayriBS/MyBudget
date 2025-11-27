package tn.esprit.mybudget.ui.currency;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;
import java.util.List;

import tn.esprit.mybudget.R;
import tn.esprit.mybudget.data.entity.Currency;

public class CurrencyAdapter extends RecyclerView.Adapter<CurrencyAdapter.CurrencyViewHolder> {

    private List<Currency> currencies = new ArrayList<>();
    private OnItemClickListener listener;

    public interface OnItemClickListener {
        void onItemClick(Currency currency);
    }

    public void setOnItemClickListener(OnItemClickListener listener) {
        this.listener = listener;
    }

    public void setCurrencies(List<Currency> currencies) {
        this.currencies = currencies;
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public CurrencyViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_currency, parent, false);
        return new CurrencyViewHolder(view);
    }

    private double baseAmount = 0;
    private double baseRate = 0;
    private boolean isConverting = false;

    public void setConversion(double amount, double rate) {
        this.baseAmount = amount;
        this.baseRate = rate;
        this.isConverting = true;
        notifyDataSetChanged();
    }

    public void clearConversion() {
        this.isConverting = false;
        notifyDataSetChanged();
    }

    @Override
    public void onBindViewHolder(@NonNull CurrencyViewHolder holder, int position) {
        Currency currency = currencies.get(position);
        holder.tvCode.setText(currency.code);
        holder.tvSymbol.setText(currency.symbol);
        holder.tvRate.setText(String.format("%.4f", currency.exchangeRateToBase));

        if (isConverting && baseRate > 0) {
            // TargetAmount = Amount * (TargetRate / SourceRate)
            double convertedAmount = baseAmount * (currency.exchangeRateToBase / baseRate);
            holder.tvConvertedAmount.setVisibility(View.VISIBLE);
            holder.tvConvertedAmount.setText(String.format("%.2f %s", convertedAmount, currency.symbol));
        } else {
            holder.tvConvertedAmount.setVisibility(View.GONE);
        }

        holder.itemView.setOnClickListener(v -> {
            if (listener != null) {
                listener.onItemClick(currency);
            }
        });
    }

    @Override
    public int getItemCount() {
        return currencies.size();
    }

    static class CurrencyViewHolder extends RecyclerView.ViewHolder {
        TextView tvCode;
        TextView tvSymbol;
        TextView tvRate;
        TextView tvConvertedAmount;

        public CurrencyViewHolder(@NonNull View itemView) {
            super(itemView);
            tvCode = itemView.findViewById(R.id.tvCurrencyCode);
            tvSymbol = itemView.findViewById(R.id.tvCurrencySymbol);
            tvRate = itemView.findViewById(R.id.tvExchangeRate);
            tvConvertedAmount = itemView.findViewById(R.id.tvConvertedAmount);
        }
    }
}

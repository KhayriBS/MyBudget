package tn.esprit.mybudget.ui;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;
import java.util.List;

import tn.esprit.mybudget.R;
import tn.esprit.mybudget.data.entity.Budget;

public class BudgetAdapter extends RecyclerView.Adapter<BudgetAdapter.BudgetViewHolder> {

    private List<Budget> budgets = new ArrayList<>();

    public void setBudgets(List<Budget> budgets) {
        this.budgets = budgets;
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public BudgetViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_budget, parent, false);
        return new BudgetViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull BudgetViewHolder holder, int position) {
        Budget budget = budgets.get(position);
        // TODO: Fetch Category Name and Spent Amount
        holder.tvCategoryName.setText("Category " + budget.categoryId);
        holder.tvLimit.setText(String.format("Limit: %.2f", budget.limitAmount));

        // Placeholder for progress
        holder.progressBar.setProgress(0);
        holder.tvSpent.setText("Spent: 0.00");
    }

    @Override
    public int getItemCount() {
        return budgets.size();
    }

    static class BudgetViewHolder extends RecyclerView.ViewHolder {
        TextView tvCategoryName, tvSpent, tvLimit;
        ProgressBar progressBar;

        public BudgetViewHolder(@NonNull View itemView) {
            super(itemView);
            tvCategoryName = itemView.findViewById(R.id.tvCategoryName);
            tvSpent = itemView.findViewById(R.id.tvSpent);
            tvLimit = itemView.findViewById(R.id.tvLimit);
            progressBar = itemView.findViewById(R.id.progressBar);
        }
    }
}

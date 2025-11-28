package tn.esprit.mybudget.ui.budget;

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
import tn.esprit.mybudget.data.model.BudgetWithCategory;

public class BudgetAdapter extends RecyclerView.Adapter<BudgetAdapter.BudgetViewHolder> {

    private List<BudgetWithCategory> budgets = new ArrayList<>();

    public void setBudgets(List<BudgetWithCategory> budgets) {
        this.budgets = budgets;
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public BudgetViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_budget, parent, false);
        return new BudgetViewHolder(view);
    }

    private OnItemClickListener listener;

    public interface OnItemClickListener {
        void onEditClick(BudgetWithCategory budget);

        void onDeleteClick(BudgetWithCategory budget);
    }

    public void setOnItemClickListener(OnItemClickListener listener) {
        this.listener = listener;
    }

    @Override
    public void onBindViewHolder(@NonNull BudgetViewHolder holder, int position) {
        BudgetWithCategory budget = budgets.get(position);
        holder.tvCategoryName.setText(budget.categoryName);
        holder.tvLimit.setText(String.format("Limit: %.2f", budget.limitAmount));

        // Placeholder for progress
        holder.progressBar.setProgress(0);
        holder.tvSpent.setText("Spent: 0.00");

        holder.btnEdit.setOnClickListener(v -> {
            if (listener != null)
                listener.onEditClick(budget);
        });

        holder.btnDelete.setOnClickListener(v -> {
            if (listener != null)
                listener.onDeleteClick(budget);
        });
    }

    @Override
    public int getItemCount() {
        return budgets.size();
    }

    static class BudgetViewHolder extends RecyclerView.ViewHolder {
        TextView tvCategoryName, tvSpent, tvLimit;
        ProgressBar progressBar;
        android.widget.ImageButton btnEdit, btnDelete;

        public BudgetViewHolder(@NonNull View itemView) {
            super(itemView);
            tvCategoryName = itemView.findViewById(R.id.tvCategoryName);
            tvSpent = itemView.findViewById(R.id.tvSpent);
            tvLimit = itemView.findViewById(R.id.tvLimit);
            progressBar = itemView.findViewById(R.id.progressBar);
            btnEdit = itemView.findViewById(R.id.btnEdit);
            btnDelete = itemView.findViewById(R.id.btnDelete);
        }
    }
}

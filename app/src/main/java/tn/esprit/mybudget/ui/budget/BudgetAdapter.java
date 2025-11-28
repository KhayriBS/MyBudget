package tn.esprit.mybudget.ui.budget;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;
import java.util.List;

import tn.esprit.mybudget.R;
import tn.esprit.mybudget.data.model.BudgetWithCategory;
import tn.esprit.mybudget.data.entity.Category;

public class BudgetAdapter extends RecyclerView.Adapter<BudgetAdapter.BudgetViewHolder> {

    private List<BudgetWithCategory> budgets = new ArrayList<>();
    private List<Category> categories = new ArrayList<>(); // 🔥 Liste des catégories
    private OnDeleteClickListener deleteListener;

    public void setBudgets(List<BudgetWithCategory> budgets) {
        this.budgets = budgets;
        notifyDataSetChanged();
    }

    // 🔥 Permet de définir les catégories chargées depuis le ViewModel
    public void setCategories(List<Category> categories) {
        this.categories = categories;
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public BudgetViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_budget, parent, false);
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

        // 🔥 Récupération de la catégorie associée
        Category category = null;
        for (Category c : categories) {
            if (c.id == budget.categoryId) {
                category = c;
                break;
            }
        }

        if (category != null) {
            holder.tvCategoryName.setText(category.name);
            // Si tu veux afficher une icône spécifique
            // holder.ivIcon.setImageResource(category.iconResId);
        } else {
            holder.tvCategoryName.setText("Unknown");
        }

        // Montant limite
        holder.tvLimit.setText(String.format("Limit: %.2f", budget.limitAmount));

        // Progress
        holder.progressBar.setProgress(0); // tu peux calculer le vrai pourcentage ici

        // Delete icon
        holder.ivDelete.setImageResource(R.drawable.ic_delete_modern);
        holder.ivDelete.setOnClickListener(v -> {
            if (deleteListener != null) {
                deleteListener.onDeleteClick(budget);
            }
        });
    }

    @Override
    public int getItemCount() {
        return budgets.size();
    }

    static class BudgetViewHolder extends RecyclerView.ViewHolder {
        TextView tvCategoryName, tvSpent, tvLimit;
        ProgressBar progressBar;
        ImageView ivIcon, ivDelete;

        public BudgetViewHolder(@NonNull View itemView) {
            super(itemView);
            tvCategoryName = itemView.findViewById(R.id.tvCategoryName);
            tvSpent = itemView.findViewById(R.id.tvSpent);
            tvLimit = itemView.findViewById(R.id.tvLimit);
            progressBar = itemView.findViewById(R.id.progressBar);
            ivIcon = itemView.findViewById(R.id.ivIcon);
            ivDelete = itemView.findViewById(R.id.ivDelete);
        }
    }

    public interface OnDeleteClickListener {
        void onDeleteClick(BudgetWithCategory budget);
    }

    public void setOnDeleteClickListener(OnDeleteClickListener listener) {
        this.deleteListener = listener;
    }
}

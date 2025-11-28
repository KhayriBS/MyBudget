package tn.esprit.mybudget.ui.main;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;
import java.util.List;

import tn.esprit.mybudget.R;
import tn.esprit.mybudget.ui.auth.LoginActivity;
import tn.esprit.mybudget.ui.transaction.TransactionViewModel;
import tn.esprit.mybudget.ui.category.CategoryManagementActivity;
import tn.esprit.mybudget.ui.currency.CurrencyActivity;
import tn.esprit.mybudget.ui.budget.BudgetActivity;
import tn.esprit.mybudget.ui.search.SearchActivity;
import tn.esprit.mybudget.ui.saving.SavingsGoalsActivity;
import tn.esprit.mybudget.ui.reminder.RemindersActivity;
import tn.esprit.mybudget.ui.member.MembersActivity;
import tn.esprit.mybudget.util.CsvExporter;
import tn.esprit.mybudget.util.SessionManager;

import com.google.firebase.auth.FirebaseAuth;

public class MoreFragment extends Fragment {

    private static final String PREFS_NAME = "UserPrefs";
    private SessionManager sessionManager;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
            @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_more, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        sessionManager = new SessionManager(requireContext());

        RecyclerView rvMoreOptions = view.findViewById(R.id.rvMoreOptions);
        rvMoreOptions.setLayoutManager(new GridLayoutManager(getContext(), 3)); // 3 columns

        List<MoreOption> options = new ArrayList<>();
        // Add options based on the image
        options.add(new MoreOption("Savings Goals", android.R.drawable.ic_menu_my_calendar));
        options.add(new MoreOption("Reminders", android.R.drawable.ic_popup_reminder));
        options.add(new MoreOption("Currency/Rates", android.R.drawable.ic_menu_rotate));
        options.add(new MoreOption("Categories", android.R.drawable.ic_menu_sort_by_size));
        options.add(new MoreOption("Members", android.R.drawable.ic_menu_myplaces));
        options.add(new MoreOption("Budget", android.R.drawable.ic_menu_manage));
        options.add(new MoreOption("Search", android.R.drawable.ic_menu_search));
        options.add(new MoreOption("Export", android.R.drawable.ic_menu_share));
        options.add(new MoreOption("Profile", android.R.drawable.ic_menu_myplaces));
        options.add(new MoreOption("Logout", android.R.drawable.ic_lock_power_off));

        MoreAdapter adapter = new MoreAdapter(options);
        rvMoreOptions.setAdapter(adapter);
    }

    private void logout() {
        if (getContext() == null)
            return;

        new AlertDialog.Builder(requireContext())
                .setTitle("Logout")
                .setMessage("Are you sure you want to logout?")
                .setPositiveButton("Logout", (dialog, which) -> {
                    // Sign out from Firebase
                    FirebaseAuth.getInstance().signOut();

                    // Clear session using SessionManager
                    sessionManager.clearSession();

                    // Also clear legacy UserPrefs
                    SharedPreferences prefs = getContext().getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
                    prefs.edit()
                            .remove("userId")
                            .apply();

                    // Navigate to login
                    Intent intent = new Intent(getContext(), LoginActivity.class);
                    intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                    startActivity(intent);

                    if (getActivity() != null) {
                        getActivity().finish();
                    }
                })
                .setNegativeButton("Cancel", null)
                .show();
    }

    private class MoreAdapter extends RecyclerView.Adapter<MoreAdapter.ViewHolder> {
        private List<MoreOption> options;

        public MoreAdapter(List<MoreOption> options) {
            this.options = options;
        }

        @NonNull
        @Override
        public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_more_option, parent, false);
            return new ViewHolder(view);
        }

        @Override
        public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
            MoreOption option = options.get(position);
            holder.tvTitle.setText(option.getTitle());
            holder.ivIcon.setImageResource(option.getIconResId());
            holder.itemView.setOnClickListener(v -> {
                if ("Categories".equals(option.getTitle())) {
                    Intent intent = new Intent(getContext(), CategoryManagementActivity.class);
                    startActivity(intent);
                } else if ("Currency/Rates".equals(option.getTitle())) {
                    Intent intent = new Intent(getContext(), CurrencyActivity.class);
                    startActivity(intent);
                } else if ("Export".equals(option.getTitle())) {
                    Toast.makeText(getContext(), "Exporting...", Toast.LENGTH_SHORT).show();
                    TransactionViewModel viewModel = new ViewModelProvider(MoreFragment.this)
                            .get(TransactionViewModel.class);
                    viewModel.getTransactions().observe(getViewLifecycleOwner(), transactions -> {
                        if (transactions != null) {
                            CsvExporter.exportTransactionsToCsv(getContext(), transactions);
                        }
                    });
                    SharedPreferences exportPrefs = requireContext().getSharedPreferences(PREFS_NAME,
                            Context.MODE_PRIVATE);
                    int exportUserId = exportPrefs.getInt("userId", 1);
                    viewModel.loadTransactions(exportUserId);
                } else if ("Budget".equals(option.getTitle())) {
                    Intent intent = new Intent(getContext(), BudgetActivity.class);
                    startActivity(intent);
                } else if ("Search".equals(option.getTitle())) {
                    Intent intent = new Intent(getContext(), SearchActivity.class);
                    startActivity(intent);
                } else if ("Savings Goals".equals(option.getTitle())) {
                    Intent intent = new Intent(getContext(), SavingsGoalsActivity.class);
                    startActivity(intent);
                } else if ("Reminders".equals(option.getTitle())) {
                    Intent intent = new Intent(getContext(), RemindersActivity.class);
                    startActivity(intent);
                } else if ("Members".equals(option.getTitle())) {
                    Intent intent = new Intent(getContext(), MembersActivity.class);
                    startActivity(intent);
                } else if ("Profile".equals(option.getTitle())) {
                    Intent intent = new Intent(getContext(), tn.esprit.mybudget.ui.auth.ProfileActivity.class);
                    startActivity(intent);
                } else if ("Logout".equals(option.getTitle())) {
                    logout();
                } else {
                    Toast.makeText(getContext(), "Clicked: " + option.getTitle(), Toast.LENGTH_SHORT).show();
                }
            });
        }

        @Override
        public int getItemCount() {
            return options.size();
        }

        class ViewHolder extends RecyclerView.ViewHolder {
            TextView tvTitle;
            ImageView ivIcon;

            public ViewHolder(@NonNull View itemView) {
                super(itemView);
                tvTitle = itemView.findViewById(R.id.tvTitle);
                ivIcon = itemView.findViewById(R.id.ivIcon);
            }
        }
    }
}

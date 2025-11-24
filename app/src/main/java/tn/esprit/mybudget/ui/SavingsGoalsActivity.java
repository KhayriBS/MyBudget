package tn.esprit.mybudget.ui;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.floatingactionbutton.FloatingActionButton;

import java.util.ArrayList;
import java.util.List;

import tn.esprit.mybudget.R;
import tn.esprit.mybudget.data.entity.SavingsGoal;
import tn.esprit.mybudget.ui.viewmodel.SavingsGoalViewModel;

public class SavingsGoalsActivity extends AppCompatActivity {

    private SavingsGoalViewModel viewModel;
    private SavingsGoalAdapter adapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_savings_goals);

        androidx.appcompat.widget.Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setTitle("Savings Goals");
        }

        viewModel = new ViewModelProvider(this).get(SavingsGoalViewModel.class);

        RecyclerView rvSavingsGoals = findViewById(R.id.rvSavingsGoals);
        rvSavingsGoals.setLayoutManager(new LinearLayoutManager(this));
        adapter = new SavingsGoalAdapter();
        rvSavingsGoals.setAdapter(adapter);

        FloatingActionButton fabAddGoal = findViewById(R.id.fabAddGoal);
        fabAddGoal.setOnClickListener(v -> showAddGoalDialog());

        viewModel.getAllGoals().observe(this, goals -> {
            adapter.setGoals(goals);
        });
    }

    private void showAddGoalDialog() {
        android.widget.LinearLayout layout = new android.widget.LinearLayout(this);
        layout.setOrientation(android.widget.LinearLayout.VERTICAL);
        layout.setPadding(50, 40, 50, 10);

        final android.widget.EditText etName = new android.widget.EditText(this);
        etName.setHint("Goal Name");
        layout.addView(etName);

        final android.widget.EditText etTarget = new android.widget.EditText(this);
        etTarget.setHint("Target Amount");
        etTarget.setInputType(
                android.text.InputType.TYPE_CLASS_NUMBER | android.text.InputType.TYPE_NUMBER_FLAG_DECIMAL);
        layout.addView(etTarget);

        new AlertDialog.Builder(this)
                .setTitle("Add Savings Goal")
                .setView(layout)
                .setPositiveButton("Save", (dialog, which) -> {
                    String name = etName.getText().toString();
                    String targetStr = etTarget.getText().toString();

                    if (name.isEmpty() || targetStr.isEmpty()) {
                        Toast.makeText(this, "Please fill all fields", Toast.LENGTH_SHORT).show();
                        return;
                    }

                    double target = Double.parseDouble(targetStr);
                    SavingsGoal goal = new SavingsGoal(1, name, target, 0, System.currentTimeMillis()); // Hardcoded
                                                                                                        // UserID
                    viewModel.insert(goal);
                })
                .setNegativeButton("Cancel", null)
                .show();
    }

    @Override
    public boolean onSupportNavigateUp() {
        finish();
        return true;
    }

    private static class SavingsGoalAdapter extends RecyclerView.Adapter<SavingsGoalAdapter.ViewHolder> {
        private List<SavingsGoal> goals = new ArrayList<>();

        public void setGoals(List<SavingsGoal> goals) {
            this.goals = goals;
            notifyDataSetChanged();
        }

        @NonNull
        @Override
        public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_savings_goal, parent, false);
            return new ViewHolder(view);
        }

        @Override
        public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
            SavingsGoal goal = goals.get(position);
            holder.tvGoalName.setText(goal.name);
            holder.tvCurrentAmount.setText(String.format("Saved: %.2f", goal.currentAmount));
            holder.tvTargetAmount.setText(String.format("Target: %.2f", goal.targetAmount));

            int progress = (int) ((goal.currentAmount / goal.targetAmount) * 100);
            holder.progressBar.setProgress(progress);
        }

        @Override
        public int getItemCount() {
            return goals.size();
        }

        static class ViewHolder extends RecyclerView.ViewHolder {
            TextView tvGoalName, tvCurrentAmount, tvTargetAmount;
            ProgressBar progressBar;

            public ViewHolder(@NonNull View itemView) {
                super(itemView);
                tvGoalName = itemView.findViewById(R.id.tvGoalName);
                tvCurrentAmount = itemView.findViewById(R.id.tvCurrentAmount);
                tvTargetAmount = itemView.findViewById(R.id.tvTargetAmount);
                progressBar = itemView.findViewById(R.id.progressBar);
            }
        }
    }
}

package tn.esprit.mybudget.ui.member;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.textfield.TextInputEditText;

import tn.esprit.mybudget.R;
import tn.esprit.mybudget.data.entity.Member;

public class MembersActivity extends AppCompatActivity {

    private MemberViewModel viewModel;
    private MemberAdapter adapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_category_management); // Reusing layout with RecyclerView and FAB

        androidx.appcompat.widget.Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setTitle("Membres");
        }

        RecyclerView recyclerView = findViewById(R.id.rvCategories); // Reusing ID
        FloatingActionButton fabAdd = findViewById(R.id.fabAddCategory); // Reusing ID

        adapter = new MemberAdapter();
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        recyclerView.setAdapter(adapter);

        viewModel = new ViewModelProvider(this).get(MemberViewModel.class);
        viewModel.getAllMembers().observe(this, members -> {
            adapter.setMembers(members);
        });

        adapter.setOnMemberDeleteListener(member -> {
            new AlertDialog.Builder(this)
                    .setTitle("Delete Member")
                    .setMessage("Are you sure you want to delete " + member.name + "?")
                    .setPositiveButton("Yes", (dialog, which) -> viewModel.delete(member))
                    .setNegativeButton("No", null)
                    .show();
        });

        fabAdd.setOnClickListener(v -> showAddMemberDialog());
    }

    private void showAddMemberDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Add Member");

        View view = LayoutInflater.from(this).inflate(R.layout.dialog_add_member, null);
        builder.setView(view);

        TextInputEditText etName = view.findViewById(R.id.etMemberName);
        TextInputEditText etEmail = view.findViewById(R.id.etMemberEmail);
        TextInputEditText etRole = view.findViewById(R.id.etMemberRole);

        builder.setPositiveButton("Save", (dialog, which) -> {
            String name = etName.getText().toString();
            String email = etEmail.getText().toString();
            String role = etRole.getText().toString();

            if (name.isEmpty()) {
                Toast.makeText(this, "Name is required", Toast.LENGTH_SHORT).show();
                return;
            }

            Member member = new Member(name, email, role);
            viewModel.insert(member);
        });

        builder.setNegativeButton("Cancel", null);
        builder.show();
    }

    @Override
    public boolean onSupportNavigateUp() {
        finish();
        return true;
    }
}

package tn.esprit.mybudget.ui.search;

import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.SearchView;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import tn.esprit.mybudget.R;
import tn.esprit.mybudget.ui.transaction.TransactionViewModel;

public class SearchActivity extends AppCompatActivity {

    private TransactionViewModel viewModel;
    // We can reuse TransactionAdapter if it's public, or create a simple one.
    // Assuming TransactionAdapter is not easily reusable without context of
    // MainActivity (which had delete logic),
    // let's create a simple inner adapter or reuse if possible.
    // Checking previous files, TransactionAdapter was not created in this session,
    // it might be in MainActivity or separate.
    // I'll assume I need to create a simple adapter for search results.

    // Actually, let's check if we can reuse the one from WalletFragment if I
    // created it?
    // I created WalletFragment but didn't create TransactionAdapter in this
    // session.
    // I'll create a simple adapter here for display only.

    private SearchAdapter adapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_search);

        androidx.appcompat.widget.Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setTitle("Search");
        }

        viewModel = new ViewModelProvider(this).get(TransactionViewModel.class);

        RecyclerView rvSearchResults = findViewById(R.id.rvSearchResults);
        rvSearchResults.setLayoutManager(new LinearLayoutManager(this));
        adapter = new SearchAdapter();
        rvSearchResults.setAdapter(adapter);

        SearchView searchView = findViewById(R.id.searchView);
        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                performSearch(query);
                return true;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                performSearch(newText);
                return true;
            }
        });
    }

    private void performSearch(String query) {
        viewModel.searchTransactions(1, query).observe(this, transactions -> {
            adapter.setTransactions(transactions);
        });
    }

    @Override
    public boolean onSupportNavigateUp() {
        finish();
        return true;
    }

    // Simple Adapter for Search Results
    private static class SearchAdapter extends RecyclerView.Adapter<SearchAdapter.ViewHolder> {
        private java.util.List<tn.esprit.mybudget.data.entity.Transaction> transactions = new java.util.ArrayList<>();

        public void setTransactions(java.util.List<tn.esprit.mybudget.data.entity.Transaction> transactions) {
            this.transactions = transactions;
            notifyDataSetChanged();
        }

        @androidx.annotation.NonNull
        @Override
        public ViewHolder onCreateViewHolder(@androidx.annotation.NonNull android.view.ViewGroup parent, int viewType) {
            android.view.View view = android.view.LayoutInflater.from(parent.getContext())
                    .inflate(android.R.layout.simple_list_item_2, parent, false);
            return new ViewHolder(view);
        }

        @Override
        public void onBindViewHolder(@androidx.annotation.NonNull ViewHolder holder, int position) {
            tn.esprit.mybudget.data.entity.Transaction t = transactions.get(position);
            holder.text1.setText(t.note.isEmpty() ? "Transaction" : t.note);
            holder.text2.setText(String.valueOf(t.amount));
        }

        @Override
        public int getItemCount() {
            return transactions.size();
        }

        static class ViewHolder extends RecyclerView.ViewHolder {
            android.widget.TextView text1, text2;

            public ViewHolder(@androidx.annotation.NonNull android.view.View itemView) {
                super(itemView);
                text1 = itemView.findViewById(android.R.id.text1);
                text2 = itemView.findViewById(android.R.id.text2);
            }
        }
    }
}

package tn.esprit.mybudget.ui.currency;

import android.os.Bundle;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.floatingactionbutton.FloatingActionButton;

import tn.esprit.mybudget.R;
import tn.esprit.mybudget.data.entity.Currency;

public class CurrencyActivity extends AppCompatActivity {

    private CurrencyViewModel currencyViewModel;
    private CurrencyAdapter adapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_currency);

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setDisplayShowHomeEnabled(true);
        }

        RecyclerView recyclerView = findViewById(R.id.recyclerView);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        recyclerView.setHasFixedSize(true);

        adapter = new CurrencyAdapter();
        recyclerView.setAdapter(adapter);

        currencyViewModel = new ViewModelProvider(this).get(CurrencyViewModel.class);
        currencyViewModel.getAllCurrencies().observe(this, currencies -> {
            adapter.setCurrencies(currencies);
        });

        FloatingActionButton fab = findViewById(R.id.fabAddCurrency);
        fab.setOnClickListener(v -> showAddEditDialog(null));

        adapter.setOnItemClickListener(this::showAddEditDialog);

        currencyViewModel.getStatusMessage().observe(this, message -> {
            if (message != null) {
                Toast.makeText(this, message, Toast.LENGTH_SHORT).show();
            }
        });
    }

    @Override
    public boolean onCreateOptionsMenu(android.view.Menu menu) {
        getMenuInflater().inflate(R.menu.menu_currency, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(android.view.MenuItem item) {
        if (item.getItemId() == R.id.action_update_rates) {
            // Ask user for base currency or default to USD/EUR?
            // For simplicity, let's use "USD" as base for now, or maybe "TND" if supported?
            // Open Exchange Rates free tier usually only supports USD base.
            // Let's use USD.
            currencyViewModel.fetchExchangeRates("TND");
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    private void showAddEditDialog(Currency currencyToEdit) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle(currencyToEdit == null ? "Ajouter une devise" : "Modifier la devise");

        View view = LayoutInflater.from(this).inflate(R.layout.dialog_add_currency, null);
        builder.setView(view);

        final EditText etCode = view.findViewById(R.id.etCode);
        final EditText etSymbol = view.findViewById(R.id.etSymbol);
        final EditText etRate = view.findViewById(R.id.etRate);

        if (currencyToEdit != null) {
            etCode.setText(currencyToEdit.code);
            etSymbol.setText(currencyToEdit.symbol);
            etRate.setText(String.valueOf(currencyToEdit.exchangeRateToBase));
            etCode.setEnabled(false); // Can't change code of existing currency usually, or maybe allowed? Let's
                                      // disable for safety as it might be used as ID elsewhere.
        }

        builder.setPositiveButton("Enregistrer", (dialog, which) -> {
            String code = etCode.getText().toString().trim();
            String symbol = etSymbol.getText().toString().trim();
            String rateStr = etRate.getText().toString().trim();

            if (TextUtils.isEmpty(code) || TextUtils.isEmpty(symbol) || TextUtils.isEmpty(rateStr)) {
                Toast.makeText(this, "Veuillez remplir tous les champs", Toast.LENGTH_SHORT).show();
                return;
            }

            double rate;
            try {
                rate = Double.parseDouble(rateStr);
            } catch (NumberFormatException e) {
                Toast.makeText(this, "Taux invalide", Toast.LENGTH_SHORT).show();
                return;
            }

            if (currencyToEdit == null) {
                Currency newCurrency = new Currency(code, symbol, rate);
                currencyViewModel.insert(newCurrency);
            } else {
                currencyToEdit.symbol = symbol;
                currencyToEdit.exchangeRateToBase = rate;
                currencyViewModel.update(currencyToEdit);
            }
        });

        builder.setNegativeButton("Annuler", (dialog, which) -> dialog.dismiss());

        builder.create().show();
    }

    @Override
    public boolean onSupportNavigateUp() {
        onBackPressed();
        return true;
    }
}

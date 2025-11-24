package tn.esprit.mybudget.ui;

import android.os.Bundle;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.ViewModelProvider;
import tn.esprit.mybudget.R;
import tn.esprit.mybudget.data.entity.Currency;
import tn.esprit.mybudget.ui.viewmodel.CurrencyViewModel;
import java.util.ArrayList;
import java.util.List;

public class CurrencySelectionActivity extends AppCompatActivity {

    private CurrencyViewModel viewModel;
    private ListView lvCurrencies;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_currency_selection);

        viewModel = new ViewModelProvider(this).get(CurrencyViewModel.class);
        lvCurrencies = findViewById(R.id.lvCurrencies);

        viewModel.getAllCurrencies().observe(this, currencies -> {
            List<String> displayList = new ArrayList<>();
            for (Currency c : currencies) {
                displayList.add(c.code + " (" + c.symbol + ")");
            }
            ArrayAdapter<String> adapter = new ArrayAdapter<>(this,
                    android.R.layout.simple_list_item_1, displayList);
            lvCurrencies.setAdapter(adapter);

            lvCurrencies.setOnItemClickListener((parent, view, position, id) -> {
                Currency selected = currencies.get(position);
                // Return result to calling activity
                // For now just finish
                finish();
            });
        });
    }
}

package tn.esprit.mybudget.ui.main;

import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;

import com.google.android.material.bottomnavigation.BottomNavigationView;

import tn.esprit.mybudget.R;
import tn.esprit.mybudget.ui.account.BooksFragment;
import tn.esprit.mybudget.ui.transaction.WalletFragment;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        BottomNavigationView bottomNav = findViewById(R.id.bottom_navigation);
        bottomNav.setOnItemSelectedListener(item -> {
            Fragment selectedFragment = null;
            int itemId = item.getItemId();
            if (itemId == R.id.nav_books) {
                selectedFragment = new BooksFragment();
                setTitle("Books");
            } else if (itemId == R.id.nav_wallet) {
                selectedFragment = new WalletFragment();
                setTitle("Wallet");
            } else if (itemId == R.id.nav_more) {
                selectedFragment = new MoreFragment();
                setTitle("More");
            }

            if (selectedFragment != null) {
                getSupportFragmentManager().beginTransaction()
                        .replace(R.id.nav_host_fragment, selectedFragment)
                        .commit();
            }
            return true;
        });

        // Set default selection
        if (savedInstanceState == null) {
            bottomNav.setSelectedItemId(R.id.nav_wallet);
            setTitle("Wallet");
        }
    }
}
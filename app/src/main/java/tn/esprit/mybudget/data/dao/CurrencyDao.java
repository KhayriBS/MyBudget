package tn.esprit.mybudget.data.dao;

import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;

import java.util.List;

import tn.esprit.mybudget.data.entity.Currency;

@Dao
public interface CurrencyDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void insert(Currency currency);

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void insertAll(List<Currency> currencies);

    @Query("SELECT * FROM currencies")
    List<Currency> getAllCurrencies();

    @Query("SELECT * FROM currencies WHERE code = :code LIMIT 1")
    Currency getCurrencyByCode(String code);
}

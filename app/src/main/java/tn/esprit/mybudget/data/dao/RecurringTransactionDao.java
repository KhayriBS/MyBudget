package tn.esprit.mybudget.data.dao;

import androidx.lifecycle.LiveData;
import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.Query;
import androidx.room.Update;

import java.util.List;

import tn.esprit.mybudget.data.entity.RecurringTransaction;

@Dao
public interface RecurringTransactionDao {
    @Insert
    void insert(RecurringTransaction transaction);

    @Update
    void update(RecurringTransaction transaction);

    @Delete
    void delete(RecurringTransaction transaction);

    @Query("SELECT * FROM recurring_transactions WHERE userId = :userId")
    LiveData<List<RecurringTransaction>> getAllRecurringTransactions(int userId);

    @Query("SELECT * FROM recurring_transactions WHERE nextExecutionDate <= :currentTime")
    List<RecurringTransaction> getDueTransactions(long currentTime);
}

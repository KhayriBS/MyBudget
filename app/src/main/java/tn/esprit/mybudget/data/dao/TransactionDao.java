package tn.esprit.mybudget.data.dao;

import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.Query;
import androidx.room.Update;

import java.util.List;

import tn.esprit.mybudget.data.entity.Transaction;

@Dao
public interface TransactionDao {
    @Insert
    void insert(Transaction transaction);

    @Update
    void update(Transaction transaction);

    @Delete
    void delete(Transaction transaction);

    @Query("SELECT * FROM transactions WHERE userId = :userId ORDER BY date DESC")
    List<Transaction> getAllTransactions(int userId);

    @Query("SELECT * FROM transactions WHERE userId = :userId AND categoryId = :categoryId")
    List<Transaction> getTransactionsByCategory(int userId, int categoryId);

    @Query("SELECT SUM(amount) FROM transactions WHERE userId = :userId AND categoryId = :categoryId")
    double getTotalByCategory(int userId, int categoryId);

    @Query("SELECT * FROM transactions WHERE userId = :userId AND (note LIKE '%' || :query || '%' OR amount LIKE '%' || :query || '%') ORDER BY date DESC")
    List<Transaction> searchTransactions(int userId, String query);
}

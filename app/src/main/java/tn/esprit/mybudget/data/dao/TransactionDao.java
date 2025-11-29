package tn.esprit.mybudget.data.dao;

import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.Query;
import androidx.room.Update;
import androidx.room.Transaction;

import java.util.List;

import tn.esprit.mybudget.data.entity.TransactionWithAccount;

@Dao
public interface TransactionDao {
    @Insert
    void insert(tn.esprit.mybudget.data.entity.Transaction transaction);

    @Update
    void update(tn.esprit.mybudget.data.entity.Transaction transaction);

    @Delete
    void delete(tn.esprit.mybudget.data.entity.Transaction transaction);

    @Query("SELECT * FROM transactions WHERE userId = :userId ORDER BY date DESC")
    List<tn.esprit.mybudget.data.entity.Transaction> getAllTransactions(int userId);

    @Transaction
    @Query("SELECT * FROM transactions WHERE userId = :userId ORDER BY date DESC")
    List<TransactionWithAccount> getTransactionsWithAccount(int userId);

    @Query("SELECT * FROM transactions WHERE userId = :userId AND categoryId = :categoryId")
    List<tn.esprit.mybudget.data.entity.Transaction> getTransactionsByCategory(int userId, int categoryId);

    @Query("SELECT SUM(amount) FROM transactions WHERE userId = :userId AND categoryId = :categoryId")
    double getTotalByCategory(int userId, int categoryId);

    @Query("SELECT * FROM transactions WHERE userId = :userId AND (note LIKE '%' || :query || '%' OR amount LIKE '%' || :query || '%') ORDER BY date DESC")
    List<tn.esprit.mybudget.data.entity.Transaction> searchTransactions(int userId, String query);
}

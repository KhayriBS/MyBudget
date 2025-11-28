package tn.esprit.mybudget.data.dao;

import androidx.lifecycle.LiveData;
import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.Query;
import androidx.room.Update;

import java.util.List;

import tn.esprit.mybudget.data.entity.Budget;

@Dao
public interface BudgetDao {
    @Insert
    void insert(Budget budget);

    @Update
    void update(Budget budget);

    @Delete
    void delete(Budget budget);

    @Query("SELECT * FROM budgets WHERE categoryId = :categoryId")
    LiveData<Budget> getBudgetByCategory(int categoryId);

    @Query("SELECT * FROM budgets")
    LiveData<List<Budget>> getAllBudgets();

    @Query("SELECT b.id as budgetId, b.categoryId, c.name as categoryName, b.limitAmount, b.period " +
            "FROM budgets b INNER JOIN categories c ON b.categoryId = c.id")
    LiveData<List<tn.esprit.mybudget.data.model.BudgetWithCategory>> getBudgetsWithCategories();
}

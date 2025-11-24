package tn.esprit.mybudget.data.dao;

import androidx.lifecycle.LiveData;
import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.Query;
import androidx.room.Update;

import java.util.List;

import tn.esprit.mybudget.data.entity.SavingsGoal;

@Dao
public interface SavingsGoalDao {
    @Insert
    void insert(SavingsGoal goal);

    @Update
    void update(SavingsGoal goal);

    @Delete
    void delete(SavingsGoal goal);

    @Query("SELECT * FROM savings_goals WHERE userId = :userId")
    LiveData<List<SavingsGoal>> getAllGoals(int userId);
}

package tn.esprit.mybudget.data.dao;

import androidx.lifecycle.LiveData;
import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.Query;
import androidx.room.Update;

import java.util.List;

import tn.esprit.mybudget.data.entity.Member;

@Dao
public interface MemberDao {
    @Insert
    long insert(Member member);

    @Update
    void update(Member member);

    @Delete
    void delete(Member member);

    @Query("SELECT * FROM members ORDER BY date DESC")
    LiveData<List<Member>> getAllMembers();

    @Query("SELECT * FROM members WHERE isSettled = 0 ORDER BY date DESC")
    LiveData<List<Member>> getUnsettledMembers();

    @Query("SELECT * FROM members WHERE type = :type ORDER BY date DESC")
    LiveData<List<Member>> getMembersByType(String type);

    @Query("SELECT * FROM members WHERE id = :id")
    Member getMemberById(int id);

    @Query("SELECT SUM(amount) FROM members WHERE type = 'Lent' AND isSettled = 0")
    LiveData<Double> getTotalLent();

    @Query("SELECT SUM(amount) FROM members WHERE type = 'Borrowed' AND isSettled = 0")
    LiveData<Double> getTotalBorrowed();
}

package tn.esprit.mybudget.data.dao;

import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.Query;

import tn.esprit.mybudget.data.entity.User;

@Dao
public interface UserDao {
    @Insert
    void insert(User user);

    @Query("SELECT * FROM users WHERE username = :username LIMIT 1")
    User findByUsername(String username);

    @Query("SELECT * FROM users WHERE uid = :uid LIMIT 1")
    User findById(int uid);
}


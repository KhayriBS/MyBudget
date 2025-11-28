package tn.esprit.mybudget.data.dao;

import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.Query;

import tn.esprit.mybudget.data.entity.User;

@Dao
public interface UserDao {
    @Insert
    long insert(User user);

    @Query("SELECT * FROM users WHERE username = :username LIMIT 1")
    User findByUsername(String username);

    @Query("SELECT * FROM users WHERE uid = :uid LIMIT 1")
    User findById(int uid);

    @Query("SELECT * FROM users WHERE email = :email LIMIT 1")
    User findByEmail(String email);

    @androidx.room.Update
    void update(User user);

}

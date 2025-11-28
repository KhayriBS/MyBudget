package tn.esprit.mybudget.data.dao;

import androidx.lifecycle.LiveData;
import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.Query;

import java.util.List;

import tn.esprit.mybudget.data.entity.Category;

@Dao

public interface CategoryDao {
    @Insert
    void insert(Category category);

    @Insert
    void insertAll(List<Category> categories);

    @Delete
    void delete(Category category);

    @Query("SELECT * FROM categories")
    List<Category> getAllCategories();

    @Query("SELECT * FROM categories WHERE type = :type")
    List<Category> getCategoriesByType(String type);

    @Query("DELETE FROM categories")
    void deleteAll();

    @Query("SELECT * FROM categories WHERE id = :id")
    LiveData<Category> getCategoriesById(int id);

}

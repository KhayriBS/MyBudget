package tn.esprit.mybudget.ui.viewmodel;

import android.app.Application;

import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import tn.esprit.mybudget.data.AppDatabase;
import tn.esprit.mybudget.data.dao.CategoryDao;
import tn.esprit.mybudget.data.entity.Category;

public class CategoryViewModel extends AndroidViewModel {
    private final CategoryDao categoryDao;
    private final ExecutorService executorService;
    private final MutableLiveData<List<Category>> categories = new MutableLiveData<>();

    public CategoryViewModel(Application application) {
        super(application);
        AppDatabase db = AppDatabase.getDatabase(application);
        categoryDao = db.categoryDao();
        executorService = Executors.newSingleThreadExecutor();
    }

    public LiveData<List<Category>> getCategories() {
        return categories;
    }

    public void loadCategories() {
        executorService.execute(() -> {
            List<Category> list = categoryDao.getAllCategories();
            categories.postValue(list);
        });
    }

    public void addCategory(Category category) {
        executorService.execute(() -> {
            categoryDao.insert(category);
            loadCategories();
        });
    }

    public void deleteCategory(Category category) {
        executorService.execute(() -> {
            categoryDao.delete(category);
            loadCategories();
        });
    }
}

package tn.esprit.mybudget.ui.auth;

import android.app.Application;
import android.content.SharedPreferences;

import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import tn.esprit.mybudget.data.AppDatabase;
import tn.esprit.mybudget.data.dao.UserDao;
import tn.esprit.mybudget.data.entity.User;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class AuthViewModel extends AndroidViewModel {
    private static final String PREFS_NAME = "UserPrefs";
    private static final String KEY_USER_ID = "userId";

    private final UserDao userDao;
    private final ExecutorService executorService;
    private final Application application;

    private final MutableLiveData<User> currentUser = new MutableLiveData<>();
    private final MutableLiveData<String> error = new MutableLiveData<>();

    public AuthViewModel(Application application) {
        super(application);
        this.application = application;
        AppDatabase db = AppDatabase.getDatabase(application);
        userDao = db.userDao();
        executorService = Executors.newSingleThreadExecutor();
    }

    public LiveData<User> getCurrentUser() {
        return currentUser;
    }

    public LiveData<String> getError() {
        return error;
    }

    public void login(String username, String password) {
        executorService.execute(() -> {
            try {
                User user = userDao.findByUsername(username);
                if (user != null && user.passwordHash.equals(password)) { // In real app, verify hash
                    // Save user ID to SharedPreferences
                    saveUserId(user.uid);
                    currentUser.postValue(user);
                } else {
                    error.postValue("Invalid username or password");
                }
            } catch (Exception e) {
                error.postValue("Login failed: " + e.getMessage());
            }
        });
    }

    public void register(String username, String password, String email) {
        executorService.execute(() -> {
            try {
                User existing = userDao.findByUsername(username);
                if (existing != null) {
                    error.postValue("Username already exists");
                } else {
                    User newUser = new User(username, password, email); // In real app, hash password
                    long userId = userDao.insert(newUser);
                    // Fetch the user with the proper ID
                    User savedUser = userDao.findById((int) userId);
                    if (savedUser != null) {
                        // Save user ID to SharedPreferences
                        saveUserId(savedUser.uid);
                        currentUser.postValue(savedUser);
                    } else {
                        // Fallback: set the ID manually
                        newUser.uid = (int) userId;
                        saveUserId(newUser.uid);
                        currentUser.postValue(newUser);
                    }
                }
            } catch (Exception e) {
                error.postValue("Registration failed: " + e.getMessage());
            }
        });
    }

    private void saveUserId(int userId) {
        SharedPreferences prefs = application.getSharedPreferences(PREFS_NAME, Application.MODE_PRIVATE);
        prefs.edit().putInt(KEY_USER_ID, userId).apply();
    }
}

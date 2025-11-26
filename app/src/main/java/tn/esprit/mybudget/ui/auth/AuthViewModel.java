package tn.esprit.mybudget.ui.auth;

import android.app.Application;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import tn.esprit.mybudget.data.AppDatabase;
import tn.esprit.mybudget.data.dao.UserDao;
import tn.esprit.mybudget.data.entity.User;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class AuthViewModel extends AndroidViewModel {
    private final UserDao userDao;
    private final ExecutorService executorService;

    private final MutableLiveData<User> currentUser = new MutableLiveData<>();
    private final MutableLiveData<String> error = new MutableLiveData<>();

    public AuthViewModel(Application application) {
        super(application);
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
            User user = userDao.findByUsername(username);
            if (user != null && user.passwordHash.equals(password)) { // In real app, verify hash
                currentUser.postValue(user);
            } else {
                error.postValue("Invalid username or password");
            }
        });
    }

    public void register(String username, String password, String email) {
        executorService.execute(() -> {
            User existing = userDao.findByUsername(username);
            if (existing != null) {
                error.postValue("Username already exists");
            } else {
                User newUser = new User(username, password, email); // In real app, hash password
                userDao.insert(newUser);
                currentUser.postValue(newUser);
            }
        });
    }
}

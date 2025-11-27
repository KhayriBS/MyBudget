package tn.esprit.mybudget.ui.auth;

import android.app.Application;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

import tn.esprit.mybudget.data.AppDatabase;
import tn.esprit.mybudget.data.dao.UserDao;
import tn.esprit.mybudget.data.entity.User;
import tn.esprit.mybudget.util.SessionManager;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class AuthViewModel extends AndroidViewModel {
    private final UserDao userDao;
    private final ExecutorService executorService;
    private final SessionManager sessionManager;
    private final FirebaseAuth mAuth;

    private final MutableLiveData<User> currentUser = new MutableLiveData<>();
    private final MutableLiveData<String> error = new MutableLiveData<>();
    private final MutableLiveData<String> message = new MutableLiveData<>();

    public AuthViewModel(Application application) {
        super(application);
        AppDatabase db = AppDatabase.getDatabase(application);
        userDao = db.userDao();
        executorService = Executors.newSingleThreadExecutor();
        sessionManager = new SessionManager(application);
        mAuth = FirebaseAuth.getInstance();

        // Load user from session if logged in
        loadUserFromSession();
    }

    private void loadUserFromSession() {
        if (sessionManager.isLoggedIn()) {
            int userId = sessionManager.getUserId();
            if (userId != -1) {
                executorService.execute(() -> {
                    User user = userDao.findById(userId);
                    if (user != null) {
                        currentUser.postValue(user);
                    } else {
                        // User not found, clear session
                        sessionManager.clearSession();
                    }
                });
            }
        } else if (mAuth.getCurrentUser() != null) {
            // Firebase is logged in but local session might be expired or not set
            // Sync local user
            syncLocalUser(mAuth.getCurrentUser().getEmail());
        }
    }

    public LiveData<User> getCurrentUser() {
        return currentUser;
    }

    public LiveData<String> getError() {
        return error;
    }

    public LiveData<String> getMessage() {
        return message;
    }

    public void login(String email, String password) {
        mAuth.signInWithEmailAndPassword(email, password)
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        FirebaseUser firebaseUser = mAuth.getCurrentUser();
                        if (firebaseUser != null) {
                            if (firebaseUser.isEmailVerified()) {
                                syncLocalUser(email);
                            } else {
                                error.postValue("Please verify your email address.");
                                mAuth.signOut();
                            }
                        }
                    } else {
                        error.postValue("Authentication failed: " + task.getException().getMessage());
                    }
                });
    }

    public void register(String username, String password, String email) {
        mAuth.createUserWithEmailAndPassword(email, password)
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        FirebaseUser firebaseUser = mAuth.getCurrentUser();
                        if (firebaseUser != null) {
                            firebaseUser.sendEmailVerification()
                                    .addOnCompleteListener(emailTask -> {
                                        if (emailTask.isSuccessful()) {
                                            message.postValue("Verification email sent to " + email);
                                        }
                                    });

                            // Create local user
                            createLocalUser(username, email);
                        }
                    } else {
                        error.postValue("Registration failed: " + task.getException().getMessage());
                    }
                });
    }

    public void forgotPassword(String email) {
        mAuth.sendPasswordResetEmail(email)
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        message.postValue("Password reset email sent to " + email);
                    } else {
                        error.postValue("Failed to send reset email: " + task.getException().getMessage());
                    }
                });
    }

    private void syncLocalUser(String email) {
        executorService.execute(() -> {
            User user = userDao.findByEmail(email);
            if (user != null) {
                // Sync verification status
                FirebaseUser fUser = mAuth.getCurrentUser();
                if (fUser != null && email.equals(fUser.getEmail()) && fUser.isEmailVerified()
                        && !user.isEmailVerified) {
                    user.isEmailVerified = true;
                    userDao.update(user);
                }

                sessionManager.saveUserSession(user.uid);
                currentUser.postValue(user);
            } else {
                // User exists in Firebase but not locally (e.g. new device)
                // Create local user using email as username
                String username = email.split("@")[0];
                createLocalUser(username, email);
            }
        });
    }

    private void createLocalUser(String username, String email) {
        executorService.execute(() -> {
            User existing = userDao.findByEmail(email);
            if (existing == null) {
                User newUser = new User(username, "FIREBASE_AUTH", email);
                newUser.isEmailVerified = false;

                // Check if already verified
                FirebaseUser fUser = mAuth.getCurrentUser();
                if (fUser != null && email.equals(fUser.getEmail()) && fUser.isEmailVerified()) {
                    newUser.isEmailVerified = true;
                }

                userDao.insert(newUser);

                User insertedUser = userDao.findByEmail(email);
                if (insertedUser != null) {
                    sessionManager.saveUserSession(insertedUser.uid);
                    currentUser.postValue(insertedUser);
                }
            } else {
                // Already exists
                sessionManager.saveUserSession(existing.uid);
                currentUser.postValue(existing);
            }
        });
    }

    public void updateBiometricStatus(User user, boolean enabled) {
        executorService.execute(() -> {
            user.hasBiometricEnabled = enabled;
            userDao.update(user);
            currentUser.postValue(user);
        });
    }

    public void changePassword(String currentPassword, String newPassword) {
        FirebaseUser firebaseUser = mAuth.getCurrentUser();
        if (firebaseUser != null && firebaseUser.getEmail() != null) {
            // Re-authenticate first
            com.google.firebase.auth.AuthCredential credential = com.google.firebase.auth.EmailAuthProvider
                    .getCredential(firebaseUser.getEmail(), currentPassword);

            firebaseUser.reauthenticate(credential)
                    .addOnCompleteListener(task -> {
                        if (task.isSuccessful()) {
                            // Re-auth success, now update password
                            firebaseUser.updatePassword(newPassword)
                                    .addOnCompleteListener(updateTask -> {
                                        if (updateTask.isSuccessful()) {
                                            message.postValue("Password updated successfully");
                                        } else {
                                            error.postValue("Failed to update password: "
                                                    + updateTask.getException().getMessage());
                                        }
                                    });
                        } else {
                            error.postValue("Incorrect current password");
                        }
                    });
        }
    }

    public void updateProfilePicture(User user, String picturePath) {
        executorService.execute(() -> {
            user.profilePicturePath = picturePath;
            userDao.update(user);
            currentUser.postValue(user);
        });
    }

    public void checkEmailVerification() {
        FirebaseUser firebaseUser = mAuth.getCurrentUser();
        if (firebaseUser != null) {
            firebaseUser.reload().addOnCompleteListener(task -> {
                if (task.isSuccessful()) {
                    if (firebaseUser.isEmailVerified()) {
                        syncLocalUser(firebaseUser.getEmail());
                    } else {
                        error.postValue("Email not verified yet. Please check your inbox.");
                    }
                } else {
                    error.postValue("Failed to check verification status.");
                }
            });
        }
    }

    public void resendVerificationEmail() {
        FirebaseUser firebaseUser = mAuth.getCurrentUser();
        if (firebaseUser != null) {
            firebaseUser.sendEmailVerification()
                    .addOnCompleteListener(task -> {
                        if (task.isSuccessful()) {
                            message.postValue("Verification email sent.");
                        } else {
                            error.postValue("Failed to send verification email.");
                        }
                    });
        }
    }

    public void logout() {
        mAuth.signOut();
        sessionManager.clearSession();
        currentUser.setValue(null);
    }
}

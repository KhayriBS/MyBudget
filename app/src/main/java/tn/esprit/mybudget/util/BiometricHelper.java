package tn.esprit.mybudget.util;

import androidx.core.content.ContextCompat;
import androidx.fragment.app.FragmentActivity;
import android.content.Context;
import androidx.biometric.BiometricManager;
import androidx.biometric.BiometricPrompt;
import java.util.concurrent.Executor;

/**
 * Helper class for biometric authentication
 */
public class BiometricHelper {

    public interface BiometricCallback {
        void onSuccess();

        void onError(String error);

        void onCanceled();
    }

    /**
     * Authenticate using biometric
     * 
     * @param activity Fragment activity
     * @param callback Callback for authentication result
     */
    public static void authenticate(FragmentActivity activity, BiometricCallback callback) {
        // Check if biometric is available first
        if (!isBiometricAvailable(activity)) {
            callback.onError("Biometric authentication is not available on this device");
            return;
        }

        Executor executor = ContextCompat.getMainExecutor(activity);

        BiometricPrompt biometricPrompt = new BiometricPrompt(activity, executor,
                new BiometricPrompt.AuthenticationCallback() {
                    @Override
                    public void onAuthenticationError(int errorCode, CharSequence errString) {
                        super.onAuthenticationError(errorCode, errString);
                        if (errorCode == BiometricPrompt.ERROR_USER_CANCELED ||
                                errorCode == BiometricPrompt.ERROR_CANCELED ||
                                errorCode == BiometricPrompt.ERROR_NEGATIVE_BUTTON) {
                            callback.onCanceled();
                        } else {
                            callback.onError(errString.toString());
                        }
                    }

                    @Override
                    public void onAuthenticationSucceeded(BiometricPrompt.AuthenticationResult result) {
                        super.onAuthenticationSucceeded(result);
                        callback.onSuccess();
                    }

                    @Override
                    public void onAuthenticationFailed() {
                        super.onAuthenticationFailed();
                        // Failed attempt but can retry
                    }
                });

        BiometricPrompt.PromptInfo promptInfo = new BiometricPrompt.PromptInfo.Builder()
                .setTitle("Biometric Login")
                .setSubtitle("Log in using your biometric credential")
                .setDescription("Use your fingerprint or face to authenticate")
                .setNegativeButtonText("Use Password")
                .setConfirmationRequired(false)
                .build();

        biometricPrompt.authenticate(promptInfo);
    }

    /**
     * Check if biometric authentication is available
     * 
     * @param context Application context
     * @return true if available, false otherwise
     */
    public static boolean isBiometricAvailable(Context context) {
        BiometricManager biometricManager = BiometricManager.from(context);
        int canAuthenticate = biometricManager.canAuthenticate(
                BiometricManager.Authenticators.BIOMETRIC_STRONG);
        return canAuthenticate == BiometricManager.BIOMETRIC_SUCCESS;
    }

    /**
     * Get detailed biometric availability status
     * 
     * @param context Application context
     * @return Status message
     */
    public static String getBiometricStatus(Context context) {
        BiometricManager biometricManager = BiometricManager.from(context);
        int canAuthenticate = biometricManager.canAuthenticate(
                BiometricManager.Authenticators.BIOMETRIC_STRONG);

        switch (canAuthenticate) {
            case BiometricManager.BIOMETRIC_SUCCESS:
                return "Biometric authentication is available";
            case BiometricManager.BIOMETRIC_ERROR_NO_HARDWARE:
                return "No biometric hardware available";
            case BiometricManager.BIOMETRIC_ERROR_HW_UNAVAILABLE:
                return "Biometric hardware is currently unavailable";
            case BiometricManager.BIOMETRIC_ERROR_NONE_ENROLLED:
                return "No biometric credentials enrolled. Please set up fingerprint or face authentication in your device settings.";
            case BiometricManager.BIOMETRIC_ERROR_SECURITY_UPDATE_REQUIRED:
                return "Security update required for biometric authentication";
            case BiometricManager.BIOMETRIC_ERROR_UNSUPPORTED:
                return "Biometric authentication is not supported";
            case BiometricManager.BIOMETRIC_STATUS_UNKNOWN:
                return "Biometric status unknown";
            default:
                return "Unable to use biometric authentication";
        }
    }

    /**
     * Check if device has biometric hardware (even if not enrolled)
     * 
     * @param context Application context
     * @return true if hardware exists
     */
    public static boolean hasBiometricHardware(Context context) {
        BiometricManager biometricManager = BiometricManager.from(context);
        int canAuthenticate = biometricManager.canAuthenticate(
                BiometricManager.Authenticators.BIOMETRIC_STRONG);
        return canAuthenticate != BiometricManager.BIOMETRIC_ERROR_NO_HARDWARE
                && canAuthenticate != BiometricManager.BIOMETRIC_ERROR_UNSUPPORTED;
    }
}

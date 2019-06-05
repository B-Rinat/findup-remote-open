package production.app.rina.findme.activities.authorization;

import static android.support.constraint.Constraints.TAG;

import android.app.Activity;
import android.support.annotation.NonNull;
import android.util.Log;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.FirebaseException;
import com.google.firebase.FirebaseTooManyRequestsException;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseAuthInvalidCredentialsException;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.PhoneAuthCredential;
import com.google.firebase.auth.PhoneAuthProvider;
import java.util.Observable;
import java.util.concurrent.TimeUnit;
import production.app.rina.findme.testing.CustomDebugLogger;

// https://github.com/delaroy/EmailPasswordAuth/blob/PhoneAuthCodePicker/app/src/main/java/com/delaroystudios/emailpasswordauth/MainActivity.java

public class AuthFirebase extends Observable {

    private String phoneVerificationId;
    private String number;
    private PhoneAuthProvider.OnVerificationStateChangedCallbacks
            verificationCallbacks;
    private PhoneAuthProvider.ForceResendingToken resendToken;
    private FirebaseAuth fbAuth;
    private String authResult;
    private CustomDebugLogger log;

    public AuthFirebase(){
        fbAuth = FirebaseAuth.getInstance();
        log = new CustomDebugLogger();
    }
    public void sendCode(String num, Activity activity) {

        number = num;
        setUpVerificatonCallbacks(activity);
        PhoneAuthProvider.getInstance().verifyPhoneNumber(
                number,        // Phone number to verify
                120,                 // Timeout duration
                TimeUnit.SECONDS,   // Unit of timeout
                activity,               // Activity (for callback binding)
                verificationCallbacks);
    }

    private void setUpVerificatonCallbacks(final Activity activity) {

        verificationCallbacks =
                new PhoneAuthProvider.OnVerificationStateChangedCallbacks() {

                    @Override
                    public void onVerificationCompleted(
                            PhoneAuthCredential credential) {
                        signInWithPhoneAuthCredential(credential, activity);
                    }

                    @Override
                    public void onVerificationFailed(FirebaseException e) {

                        if (e instanceof FirebaseAuthInvalidCredentialsException) {
                            // Invalid request
                            Log.d(TAG, "Invalid credential: "
                                    + e.getLocalizedMessage());
                        } else if (e instanceof FirebaseTooManyRequestsException) {
                            // SMS quota exceeded
                            Log.d(TAG, "SMS Quota exceeded.");
                        }
                        setChanged();
                        notifyObservers();
                    }

                    @Override
                    public void onCodeSent(String verificationId,
                            PhoneAuthProvider.ForceResendingToken token) {

                        phoneVerificationId = verificationId;
                        resendToken = token;
                    }
                };
    }

    public void verifyCode(String userVerificationCode, Activity activity) {
        PhoneAuthCredential credential =
                PhoneAuthProvider.getCredential(phoneVerificationId, userVerificationCode);
        signInWithPhoneAuthCredential(credential, activity);
    }

    private void signInWithPhoneAuthCredential(PhoneAuthCredential credential, Activity activity) {
        fbAuth.signInWithCredential(credential)
                .addOnCompleteListener(activity, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (task.isSuccessful()) {
                            FirebaseUser user = task.getResult().getUser();
                            String phoneNumber = user.getPhoneNumber();
                            // Verification code was valid, proceed
                            authResult = "success@5dcAzx1";
                            setChanged();
                            notifyObservers();
                        } else {
                            if (task.getException() instanceof
                                    FirebaseAuthInvalidCredentialsException) {
                                // The verification code entered was invalid
                            }
                            setChanged();
                            notifyObservers();
                        }
                    }
                });
    }

    public void resendCode(String num, Activity activity) {
        number = num;
        setUpVerificatonCallbacks(activity);
        PhoneAuthProvider.getInstance().verifyPhoneNumber(
                number,
                120,
                TimeUnit.SECONDS,
                activity,
                verificationCallbacks,
                resendToken);
    }
    public String getResult(){
        if(authResult == null){
            return "x12aax158a16cas1a512";
        }
        return authResult;
    }
}

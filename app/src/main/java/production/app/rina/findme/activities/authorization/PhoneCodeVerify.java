package production.app.rina.findme.activities.authorization;

import android.app.Activity;
import android.content.DialogInterface;
import android.content.Intent;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;
import com.pixplicity.easyprefs.library.Prefs;
import java.util.Observable;
import java.util.Observer;
import production.app.rina.findme.R;
import production.app.rina.findme.services.authorization.AuthUtils;
import production.app.rina.findme.services.common.AppPreferences;
import production.app.rina.findme.testing.CustomDebugLogger;

public class PhoneCodeVerify extends AppCompatActivity implements Observer, View.OnClickListener {

    public static final String PHONE_TO_VERIFY = "verifyPhoneNumber";

    public static final String RESULT = "phoneVerificationResult";

    public static final String OK = "onSuccessfulResult";

    public static final String BACK_PRESSED = "onBackPressedResult";

    public static final String EXCEEDED_LIMIT = "numberOfAttemptsExceeded";

    public static final String WRONG_CODE = "enteredCodeIsWrong";

    AuthFirebase authFirebase;

    String authFirebasePass;

    CustomDebugLogger log;

    AuthUtils authUtils;

    Intent externalIntent;

    EditText codeEt;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_phone_code_verify);
        log = new CustomDebugLogger();
        authUtils = new AuthUtils();
        authFirebase = new AuthFirebase();
        authFirebase.addObserver(this);
        externalIntent = getIntent();
        codeScreenBackListener();
        String phone = externalIntent.getStringExtra(PhoneCodeVerify.PHONE_TO_VERIFY);
        authFirebase.sendCode("+" + phone, this);
        codeEt = findViewById(R.id.edit_user_code_sms);
        Prefs.putBoolean(AppPreferences.SET_CODE_VERIFICATION_SCREEN, true);
        TextView confirmBtn = findViewById(R.id.text_confirm_sms_code);
        confirmBtn.setOnClickListener(this);
    }

    /**
     * Back from activity_phone_code_verify layout to Auth.class
     */
    public void codeScreenBackListener() {
        ImageView back = findViewById(R.id.image_tool_bar_back_btn);
        if (back != null) {
            back.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    backPressedWarning();
                }
            });
        }
    }

    /**
     * When user supposed to enter verification code from server
     * and pressed back button, display warning that user should finish entering code first
     */
    public void backPressedWarning() {
        final AlertDialog.Builder builder;
        builder = new AlertDialog.Builder(PhoneCodeVerify.this);
        builder.setTitle(getString(R.string.attention) + "!!!")
                .setMessage(getString(R.string.are_you_sure_not_confirm_phone_by_sms_code))
                .setPositiveButton(getString(R.string.confirm_code), new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {

                    }
                })
                .setNegativeButton(getString(R.string.exit), new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        Prefs.putBoolean(AppPreferences.SET_CODE_VERIFICATION_SCREEN, false);
                        Prefs.putInt(AppPreferences.SMS_VERIFICATION_LIMIT, 10);

                        Intent intent = new Intent();
                        intent.putExtra(PhoneCodeVerify.RESULT, PhoneCodeVerify.BACK_PRESSED);
                        setResult(Activity.RESULT_CANCELED, intent);
                        finish();
                    }
                })
                .setIcon(android.R.drawable.ic_dialog_alert)
                .show();
    }

    @Override
    public void onClick(final View v) {
        log.e("TAG", "onClick");
        switch (v.getId()){
            case R.id.text_confirm_sms_code:
            {
                ProgressBar progressBar = this.findViewById(R.id.progress_bar);
                progressBar.setVisibility(View.VISIBLE);

                log.e("TAG", "onClick with id: " + R.id.text_confirm_sms_code);
                if (authUtils.isCodeVerificationLimit()) {
                    // Exceeded number of attempts
                    Prefs.putInt(AppPreferences.SMS_VERIFICATION_LIMIT, 10);
                    Prefs.putBoolean(AppPreferences.SET_CODE_VERIFICATION_SCREEN, false);
                    Intent result = new Intent();
                    result.putExtra(PhoneCodeVerify.RESULT, PhoneCodeVerify.EXCEEDED_LIMIT);
                    setResult(Activity.RESULT_CANCELED, result);
                    finish();
                    return;
                }
                String codeRawInput = codeEt.getText().toString();
                if(!codeRawInput.isEmpty()){
                    // put some spinner here
                    authFirebase.verifyCode(codeRawInput, this);
                } else {
                    progressBar.setVisibility(View.INVISIBLE);
                    Toast.makeText(this, getResources().getString(R.string.enter_received_code), Toast.LENGTH_SHORT).show();
                }
            }
        }
    }

    @Override
    public void onBackPressed() {
        backPressedWarning();
    }

    @Override
    public void update(final Observable o, final Object arg) {
        Prefs.putBoolean(AppPreferences.SET_CODE_VERIFICATION_SCREEN, false);

        if(authFirebase.getResult().substring(0, 7).equals("success")){
            authFirebasePass = "success";
            Intent result = new Intent();
            setResult(Activity.RESULT_OK, result);
            finish();
        } else {
            Intent result = new Intent();
            result.putExtra(PhoneCodeVerify.RESULT, PhoneCodeVerify.WRONG_CODE);
            setResult(Activity.RESULT_CANCELED, result);
            finish();
        }
    }
}

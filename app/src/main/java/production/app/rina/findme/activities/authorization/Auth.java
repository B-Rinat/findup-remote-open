package production.app.rina.findme.activities.authorization;

import static production.app.rina.findme.services.common.AppPreferences.getUserPhone;
import static production.app.rina.findme.services.common.AppPreferences.setIdOfLocation;
import static production.app.rina.findme.services.common.AppPreferences.setUniqueUserId;
import static production.app.rina.findme.services.common.AppPreferences.setUserLogin;
import static production.app.rina.findme.services.common.AppPreferences.setUserPhone;
import static production.app.rina.findme.services.common.AppPreferences.setUserToken;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.MainThread;
import android.support.annotation.NonNull;
import android.support.design.widget.Snackbar;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;
import com.pixplicity.easyprefs.library.Prefs;
import java.util.ArrayList;
import java.util.Timer;
import production.app.rina.findme.R;
import production.app.rina.findme.activities.contacts.ContactsHolderMain;
import production.app.rina.findme.services.authorization.AuthUtils;
import production.app.rina.findme.services.common.AppPreferences;
import production.app.rina.findme.services.network.ReportPerformer;
import production.app.rina.findme.testing.CustomDebugLogger;
import production.app.rina.findme.utils.AppUtils;
import production.app.rina.findme.utils.Countries;

public class Auth extends AppCompatActivity implements OnClickListener {

    private static final int PHONE_CODE_VERIFY = 1;

    AuthUtils authUtils;

    EditText countryCode, phoneNumber;

    Handler internetHandler = new Handler();

    Runnable internetRunnable;

    CustomDebugLogger log;

    final private int delayNetCheck = 1000; // seconds

    private int prevViewFocus = 0;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        log = new CustomDebugLogger();
        authUtils = new AuthUtils();
        final String[] permissions = {
                Manifest.permission.READ_PHONE_STATE
        };
        if (!authUtils.havePhoneStatePermission(Auth.this)) {
            ActivityCompat.requestPermissions(Auth.this, permissions, 1);
        }
        final String phone = getUserPhone();

        Runnable r = new Runnable() {
            @Override
            public void run() {
                if (Prefs.getBoolean(AppPreferences.SET_CODE_VERIFICATION_SCREEN, false)) {
                    Intent phoneCodeVerify = new Intent(Auth.this, PhoneCodeVerify.class);
                    phoneCodeVerify.putExtra(PhoneCodeVerify.PHONE_TO_VERIFY, phone);
                    startActivityForResult(phoneCodeVerify, PHONE_CODE_VERIFY);
                    return;
                }

                log.e("TAG", "inside | Runnable | r | re-reg " + authUtils.isReRegistered());
                if (authUtils.isReRegistered()) {
                    if (authUtils.isAuthByImei()) {
                        if (authUtils.havePhoneStatePermission(Auth.this)) {
                            String tok = authUtils.generateTokenImsi(phone, Auth.this);
                            if (!tok.isEmpty()) {
                                if (authUtils.compareTokens(tok)) {
                                    if (authUtils.authByToken(Auth.this)){
                                        Intent intent = new Intent(Auth.this, ContactsHolderMain.class);
                                        log.e("TAG", "AUTH by imei");
                                        AppUtils.sendUserLastActiveDateAndToken(getApplicationContext());
                                        startActivity(intent);
                                        finish();
                                    }
                                }
                            }
                        }
                    } else {
                        log.e("TAG", "auth not by imei");
                        if (authUtils.authByToken(Auth.this)) {
                            Intent intent = new Intent(Auth.this, ContactsHolderMain.class);
                            log.e("TAG", "AUTH by rand token");
                            AppUtils.sendUserLastActiveDateAndToken(getApplicationContext());
                            startActivity(intent);
                            finish();
                        }
                    }
                }
            }
        };
        new Thread(r).start();

        if (!AppUtils.isNetworkAvailable(this)) {
            // network is not available
            Toast.makeText(this, getResources().getString(R.string.internet_connection_failed), Toast.LENGTH_LONG)
                    .show();
        }

        displayLoginScreen();

        countryCode = findViewById(R.id.edit_country_code);
        phoneNumber = findViewById(R.id.edit_phone_number);

        setOnClickHelper(countryCode);
        setOnClickHelper(phoneNumber);
        countryCode.requestFocus();

        String phoneTemp, codeTemp = "";
        if (savedInstanceState != null) {
            phoneTemp = savedInstanceState.getString("phone_number", "");
            codeTemp = savedInstanceState.getString("country_code", "");
            if (countryCode != null) {
                countryCode.setText(codeTemp);
            }
            if (phoneNumber != null) {
                phoneNumber.setText(phoneTemp);
            }
        }
        if (codeTemp.equals("")) {
            Intent intent = getIntent();
            codeTemp = intent.getStringExtra("country_code");
            if (countryCode != null) {
                countryCode.setText(codeTemp);
            }
        }
        countryCodeListener();
    }

    public void countryCodeListener(){
        countryCode.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(final View v) {
                countryCodeChooser(v);
            }
        });
    }

    @Override
    protected void onPause() {
        internetHandler.removeCallbacks(internetRunnable);
        super.onPause();
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        if (countryCode != null) {
            outState.putString("country_code", countryCode.getText().toString());
        }
        if (phoneNumber != null) {
            outState.putString("phone_number", phoneNumber.getText().toString());
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions,
            @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        switch (requestCode) {
            case 1: {
                // If request is cancelled, the result arrays are empty.
                if (grantResults.length > 0
                        && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    Intent intent = new Intent(this, Auth.class);
                    startActivity(intent);
                    finish();
                }
            }
        }
    }

    @Override
    protected void onActivityResult(final int requestCode, final int resultCode, final Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        EditText phone_et = findViewById(R.id.edit_phone_number);
        if(requestCode == PHONE_CODE_VERIFY){
            if(resultCode == Activity.RESULT_OK){
                phone_et.setText("");
                onPhoneVerificationSuccess();
            } else {
                switch (data.getStringExtra(PhoneCodeVerify.RESULT)){
                    case PhoneCodeVerify.BACK_PRESSED:{}
                    case PhoneCodeVerify.EXCEEDED_LIMIT:{
                        if(getCurrentFocus() == null){return;}
                        Snackbar.make(getCurrentFocus(), getResources().getString(R.string.exceed_request_num), Snackbar.LENGTH_LONG).show();
                    }
                    case PhoneCodeVerify.WRONG_CODE:{
                        if(getCurrentFocus() == null){return;}
                        Snackbar.make(getCurrentFocus(), getResources().getString(R.string.smth_went_wrong_notice), Snackbar.LENGTH_LONG).show();
                    }
                    default: {
                        if(getCurrentFocus() == null){return;}
                        Snackbar.make(getCurrentFocus(), getResources().getString(R.string.some_error_occured_try_again), Snackbar.LENGTH_LONG).show();
                    }
                }
            }
        }
    }

    @Override
    public void onClick(final View v) {
        switch (v.getId()){
            case R.id.button_enter_into_app:{
                loginAuth(v);
            }
        }
    }

    @SuppressLint("StaticFieldLeak")
    public void onPhoneVerificationSuccess() {
        Thread thread = new Thread() {
            @Override
            public void run() {
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        log.e("TAG", "runOnUiThread");
                        AppUtils.sendUserLastActiveDateAndToken(getApplicationContext());
                    }
                });
                try {
                    log.e("TAG", "runInBackround");
                    Prefs.putBoolean(AppPreferences.SET_CODE_VERIFICATION_SCREEN, false);
                    Prefs.putInt(AppPreferences.SMS_VERIFICATION_LIMIT, 10);
                    Prefs.putBoolean(AppPreferences.IS_RE_REGISTERED, true);
                    AuthUtils util = new AuthUtils();
                    util.changePhoneStatus(Auth.this);

                    String token = "";
                    if (util.havePhoneStatePermission(Auth.this)) {
                        token = util
                                .generateTokenImsi(getUserPhone(), Auth.this);
                        if (token.length() > 10) {
                            log.e("TAG", "Token created by IMSI");
                            util.setAuthImei(true);
                            util.changeServerToken(token, Auth.this);
                            setUserToken(token);
                        } else {
                            log.e("TAG", "Token generated Randomly");
                            token = util.generateTokenRand(getUserPhone());
                            util.setAuthImei(false);
                            util.changeServerToken(token, Auth.this);
                            setUserToken(token);
                        }

                    } else {
                        log.e("TAG", "Token generated Randomly");
                        util.setAuthImei(false);
                        token = util.generateTokenRand(getUserPhone());
                        util.setAuthImei(false);
                        util.changeServerToken(token, Auth.this);
                        setUserToken(token);
                    }
                    util.setReRegistered(true);

                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            log.e("TAG", "runOnUiThread");
                            Intent intent = new Intent(getApplicationContext(), ContactsHolderMain.class);
                            startActivity(intent);
                            Toast.makeText(getApplicationContext(), getResources().getString(R.string.successful_login), Toast.LENGTH_LONG).show();
                            finish();
                        }
                    });
                } catch (Exception e) {
                    log.e("Exception", "exception in onPhoneVerificationSuccess");
                }
            }
        };
        thread.start();
    }

    public void displayLoginScreen() {
        setContentView(R.layout.activity_authorization);
        findViewById(R.id.button_enter_into_app).setOnClickListener(this);
    }

    /**
     * Reads and checks validity of user entered parameters from 'activity_auth' layout
     */
    public void loginAuth(final View v) {
        v.setBackgroundColor(getResources().getColor(R.color.buttonActive));
        v.setVisibility(View.INVISIBLE);
        if (countryCode == null) {
            // Enter country code
            v.setVisibility(View.VISIBLE);
            return;
        }
        if (phoneNumber == null) {
            // Enter phone number
            v.setVisibility(View.VISIBLE);
            return;
        }
        final String countryCodeTemp = countryCode.getText().toString().trim();
        final String ph = phoneNumber.getText().toString().trim();
        if (!authUtils.isValidCode(countryCodeTemp)) {
            // Country code not valid
            Toast.makeText(this, getResources().getString(R.string.country_code_invalid), Toast.LENGTH_LONG).show();
            postDelayViewColor(v, 1000);
            v.setVisibility(View.VISIBLE);
            return;
        }
        if (!authUtils.isProperPhoneNumber(ph)) {
            // Phone number is not valid
            Toast.makeText(this, getResources().getString(R.string.ensure_correct_phone_num), Toast.LENGTH_LONG)
                    .show();
            postDelayViewColor(v, 1000);
            v.setVisibility(View.VISIBLE);
            return;
        }
        if (!AppUtils.isNetworkAvailable(this)) {
            // network is not available
            Toast.makeText(this, getResources().getString(R.string.internet_connection_failed), Toast.LENGTH_LONG)
                    .show();
            // start looper that will check internet availability, if available -> auth? -> true -> login into app
            checkInternetConnectionLooper();
            countryCode.setText("");
            phoneNumber.setText("");
            postDelayViewColor(v, 1000);
            v.setVisibility(View.VISIBLE);
            return;
        }

        setUserToken("");
        Prefs.putBoolean(AppPreferences.IS_RE_REGISTERED, false);

        Thread thread1 = new Thread(new Runnable() {
            @Override
            public void run() {
                log.e("Thread", "Thread 1 started");
                if (!authUtils.isUserInIntegralDb(countryCodeTemp + ph, Auth.this)) {
                    log.e("TAG", "user not in DB");
                    boolean result = authUtils.createNewUser(countryCodeTemp + ph, Auth.this);
                    if (!result) {
                        // TODO Some error occurred with a server, display custom polite notice to try again
                        if (!AppUtils.isNetworkAvailable(Auth.this)) {
                            Toast.makeText(getApplicationContext(), getString(R.string.check_internet_con),
                                    Toast.LENGTH_SHORT).show();
                        } else {
                            Toast.makeText(getApplicationContext(), getString(R.string.some_error_occured_try_again),
                                    Toast.LENGTH_SHORT).show();
                        }
                        recreate();
                        v.setVisibility(View.VISIBLE);
                        return;
                    }
                    setUserLogin(countryCodeTemp + ph);
                } else {
                    try {
                        log.e("TAG", "user in DB");
                        ArrayList<String> keys = new ArrayList<>();
                        ArrayList<String> values = new ArrayList<>();
                        ArrayList<String> jsonKeys = new ArrayList<>();
                        keys.add("phone");
                        values.add(countryCodeTemp + ph);
                        jsonKeys.add("userId");
                        jsonKeys.add("locationId");

                        ReportPerformer report = new ReportPerformer(getString(R.string.METADATA_USER_LOCATION), keys, values,
                                jsonKeys);
                        ArrayList<String> result = report.execute();
                        setUserLogin(countryCodeTemp + ph);
                        setUniqueUserId(result.get(0));
                        if (!result.get(1).isEmpty()) {
                            setIdOfLocation(result.get(1));
                        }
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
                log.e("Thread", "Thread 1 ended");
            }
        });

        Thread thread2 = new Thread(new Runnable() {
            @Override
            public void run() {
                log.e("Thread", "Thread 2 started");
                setUserPhone(countryCodeTemp + ph);

                String tempToken = authUtils.generateTokenRand(countryCodeTemp + ph);
                Prefs.putString(AppPreferences.TEMP_USER_TOKEN, tempToken);
                authUtils.changeServerToken(tempToken, Auth.this);
                authUtils.changeSmsCodeServer(Auth.this);

                Intent phoneCodeVerify = new Intent(Auth.this, PhoneCodeVerify.class);
                phoneCodeVerify.putExtra(PhoneCodeVerify.PHONE_TO_VERIFY, countryCodeTemp + ph);
                startActivityForResult(phoneCodeVerify, PHONE_CODE_VERIFY);
                log.e("Thread", "Thread 2 ended");
            }
        });

        postDelayViewColor(v, 500);
        v.setVisibility(View.VISIBLE);

        thread1.start();
        thread2.start();
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        finish();
    }

    /**
     * Helper function; Apply custom view on EditText when it has focus
     */
    public void setOnClickHelper(final View tv) {
        log.e("TAG", "id: " + prevViewFocus);
        tv.setOnFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View v, boolean hasFocus) {
                if (hasFocus) {
                    log.e("TAG", "hasFocus: true, id: " + v.getId());
                    //handle your situation here
                    if (prevViewFocus != v.getId()) {
                        v.setBackgroundResource(R.drawable.input_text_style_active);
                        if (prevViewFocus != 0) {
                            View et = findViewById(prevViewFocus);
                            if (et != null) {
                                et.setBackgroundResource(R.drawable.input_text_style);
                            }
                        }
                    }
                    prevViewFocus = v.getId();
                }
            }
        });
    }

    /**
     * Run runnable every N seconds to check internet connection
     * if internet connection available -> check auth? -> true? -> login
     */
    protected void checkInternetConnectionLooper() {
        log.e("TAG", "starting check_internet_connection_looper");
        if (internetRunnable == null) {
            internetRunnable = new Runnable() {
                @Override
                public void run() {
                    if (AppUtils.isNetworkAvailable(getApplicationContext())) {
                        if (internetHandler != null) {
                            log.e("TAG", "network is available | removing callback | internet_runnable");
                            internetHandler.removeCallbacks(internetRunnable);
                            Intent intent = new Intent(getApplicationContext(), Auth.class);
                            startActivity(intent);
                            finish();
                        }
                    } else {
                        log.e("TAG", "net is unavailable, run again after " + delayNetCheck + " sec.");
                        internetHandler.postDelayed(internetRunnable, delayNetCheck);
                    }
                }
            };
        } else {
            internetHandler.removeCallbacks(internetRunnable);
        }
        internetHandler.postDelayed(internetRunnable, delayNetCheck);
    }

    void countryCodeChooser(View v) {
        setContentView(R.layout.countries);
        // final Countries c = new Countries();
        final LinearLayout ll = findViewById(R.id.list_countries);
        final LayoutInflater layoutInflater;
        layoutInflater = (LayoutInflater) getSystemService(LAYOUT_INFLATER_SERVICE);
        displayCountries(layoutInflater, ll);
        EditText searcher = findViewById(R.id.edit_country_searcher);
        searcher.addTextChangedListener(new TextWatcher() {
            @Override
            public void afterTextChanged(Editable s) {

            }

            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                final String searchedText = s.toString();
                ll.removeAllViews();
                if (searchedText.isEmpty()) {
                    displayCountries(layoutInflater, ll);
                    return;
                }
                log.e("TAG", "Searched Text onTextChanged : " + searchedText);
                displaySearchedCountries(searchedText, layoutInflater, ll);
            }
        });
    }

    void countryCodeChosen(String code) {
        log.e("TAG", "country_code_chosen: " + code);
        Intent intent = new Intent(this, Auth.class);
        intent.putExtra("country_code", code);
        startActivity(intent);
        finish();
    }

    void displayCountries(LayoutInflater layoutInflater, LinearLayout countriesFill) {
        Countries c = new Countries();

        log.e("TAG", "Countries.java _ display_countries _ start...");
        TextView countryTv, countryCodeTv;
        for (int i = 0; i < c.countriesEn.length; i++) {
            //log.e("TAG", "inside for loop ...");
            assert layoutInflater != null;
            //log.e("TAG", "layoutInflater is not null");
            final View countryAndCodeTemplate = layoutInflater.inflate(R.layout.country_layout, null, false);
            countryTv = countryAndCodeTemplate.findViewById(R.id.text_country);
            countryCodeTv = countryAndCodeTemplate.findViewById(R.id.text_country_code);
            countryTv.setText(c.countriesEn[i]);
            String s = "+" + c.countryCodes[i];
            countryCodeTv.setText(s);
            countriesFill.addView(countryAndCodeTemplate);
            setOnClickListenerHelper(countryAndCodeTemplate);
        }
    }

    void displaySearchedCountries(String search, LayoutInflater layoutInflater, LinearLayout countriesFillSearched) {
        Countries c = new Countries();
        if (search.isEmpty()) {
            log.e("TAG", "empty search for country");
            return;
        }
        // ArrayList<String> countries_en_searched = new ArrayList<String>();
        TextView countryTv, countryCodeTv;
        for (int i = 0; i < c.countriesEn.length; i++) {
            String temp = c.countriesEn[i].toLowerCase();
            search = search.toLowerCase();
            if (temp.contains(search)) {
                final View countryAndCodeTemplate = layoutInflater.inflate(R.layout.country_layout, null, false);
                countryTv = countryAndCodeTemplate.findViewById(R.id.text_country);
                countryCodeTv = countryAndCodeTemplate.findViewById(R.id.text_country_code);
                countryTv.setText(c.countriesEn[i]);
                String s = "+" + c.countryCodes[i];
                countryCodeTv.setText(s);
                countriesFillSearched.addView(countryAndCodeTemplate);
                setOnClickListenerHelper(countryAndCodeTemplate);
            }
        }
    }

    void setOnClickListenerHelper(View v) {
        v.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String chosenCountryCode = "";
                TextView code = v.findViewById(R.id.text_country_code);
                chosenCountryCode = code.getText().toString();
                chosenCountryCode = chosenCountryCode.substring(1);
                countryCodeChosen(chosenCountryCode);
            }
        });
    }

    /**
     * Change color of the view 'v' after specified period 'timeMill'
     */
    private void postDelayViewColor(final View v, int timeMill) {
        Runnable r = new Runnable() {
            @Override
            public void run() {
                v.setBackgroundColor(getResources().getColor(R.color.actionButton));
            }
        };
        Handler h = new Handler();
        h.postDelayed(r, timeMill);
    }
}

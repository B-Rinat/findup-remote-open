package production.app.rina.findme.services.authorization;

import static production.app.rina.findme.services.common.AppPreferences.getUniqueUserId;
import static production.app.rina.findme.services.common.AppPreferences.getUserPhone;
import static production.app.rina.findme.services.common.AppPreferences.getUserSmsCode;
import static production.app.rina.findme.services.common.AppPreferences.getUserToken;
import static production.app.rina.findme.services.common.AppPreferences.setIdOfLocation;
import static production.app.rina.findme.services.common.AppPreferences.setUniqueUserId;
import static production.app.rina.findme.services.common.AppPreferences.setUserPhone;
import static production.app.rina.findme.services.common.AppPreferences.setUserSmsCode;
import static production.app.rina.findme.services.common.AppPreferences.setUserXsrf;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.Context;
import android.content.pm.PackageManager;
import android.support.v4.app.ActivityCompat;
import android.telephony.TelephonyManager;
import com.pixplicity.easyprefs.library.Prefs;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.Random;
import production.app.rina.findme.R;
import production.app.rina.findme.services.common.AppPreferences;
import production.app.rina.findme.services.network.DatabaseObjectManager;
import production.app.rina.findme.services.network.ReportPerformer;
import production.app.rina.findme.testing.CustomDebugLogger;
import production.app.rina.findme.utils.AppUtils;
import production.app.rina.findme.utils.Countries;

@SuppressWarnings("WeakerAccess")
public class AuthUtils {

    private final int smsVerificationLimit = 10;

    /**
     * Check if local auth token equal to server auth token when user registered without imei, imsi, phone
     *
     * @return truth value
     */
    public boolean authByToken(Context context) {

        ArrayList<String> keys = new ArrayList<>();
        ArrayList<String> values = new ArrayList<>();
        ArrayList<String> jsonKeys = new ArrayList<>();
        keys.add("token");
        keys.add("phone");
        values.add(getUserToken());
        values.add(getUserPhone());
        jsonKeys.add("phone");
        ReportPerformer report = new ReportPerformer(context.getResources().getString(R.string.REPORT_AUTH), keys,
                values, jsonKeys);
        ArrayList<String> result = report.execute();
        if (!result.isEmpty()) {
            return result.get(0).equals(getUserPhone());
        }
        return false;
    }

    /**
     * Change user's phone status from reject ---> to approve
     *
     * @return always true
     */
    public boolean changePhoneStatus(Context context) {
        DatabaseObjectManager manager = new DatabaseObjectManager();
        ArrayList<String> keys = new ArrayList<>();
        ArrayList<String> values = new ArrayList<>();
        keys.add(context.getString(R.string.PHONE_STATUS));
        values.add(context.getString(R.string.PHONE_STATUS_APPROVE));
        manager.updateObject(getUniqueUserId(),
                getUserPhone(), keys, values);
        return true;
    }

    /**
     * Change token in integral db
     *
     * @param str Newly created token
     */
    public void changeServerToken(String str, Context context) {
        DatabaseObjectManager manager = new DatabaseObjectManager();
        ArrayList<String> keys = new ArrayList<>();
        ArrayList<String> values = new ArrayList<>();
        keys.add(context.getString(R.string.TOKEN));
        values.add(str);
        manager.updateObject(getUniqueUserId(),
                getUserPhone(), keys, values);
    }

    /**
     * Change sms code in integral server
     */
    public void changeSmsCodeServer(Context context) {
        DatabaseObjectManager manager = new DatabaseObjectManager();
        ArrayList<String> keys = new ArrayList<>();
        ArrayList<String> values = new ArrayList<>();
        keys.add(context.getString(R.string.SMS_CODE));
        values.add(Integer.toString(getSmsCode()));
        manager.updateObject(getUniqueUserId(),
                getUserPhone(), keys, values);
    }

    /**
     * Check user entered sms verification code and integral code
     *
     * @param server user entered sms verification code
     * @return true - codes equal
     */
    public boolean checkVerificationCodes(String server) {
        CustomDebugLogger log = new CustomDebugLogger();
        try {
            int r = Prefs.getInt(AppPreferences.SMS_VERIFICATION_LIMIT, smsVerificationLimit);
            r--;
            Prefs.putInt(AppPreferences.SMS_VERIFICATION_LIMIT, r);
            int s = Integer.valueOf(server);
            int local = getSmsCode();
            if (local != -1) {
                if (local == s) {
                    return true;
                }
            }
        } catch (Exception e) {
            log.e("TAG", "Exception || check_verification_codes || " + e);
        }
        return false;
    }

    /**
     * Compare two tokens, autogenerated now and local
     *
     * @param p autogenerated token
     * @return true - if equal
     */
    public boolean compareTokens(String p) {
        return getUserToken().equals(p);
    }

    /**
     * Create new user in Integral db
     *
     * @param str user's phone number with country code, ex: 996555440265
     */
    public boolean createNewUser(final String str, final Context context) {
        int smsCode = generateSmsCode();
        setSmsCode(smsCode);
        DatabaseObjectManager manager = new DatabaseObjectManager();
        ArrayList<String> keys = new ArrayList<>();
        ArrayList<String> values = new ArrayList<>();
        keys.add(context.getString(R.string.TELEPHONE));
        keys.add(context.getString(R.string.USER_ROLE));
        keys.add(context.getString(R.string.USER_XSRF));
        keys.add(context.getString(R.string.USER_ABN_URL));
        keys.add(context.getString(R.string.SMS_CODE));
        keys.add(context.getString(R.string.PHONE_STATUS));
        keys.add(context.getString(R.string.TOKEN));

        values.add(str);
        values.add(context.getString(R.string.USER_ROLE_CLIENT));
        String userXsrf = getSaltString(32);
        setUserXsrf(userXsrf);
        values.add(userXsrf);
        values.add(context.getString(R.string.FUNCTION_ABN_URL));
        values.add(String.valueOf(smsCode));
        values.add(context.getString(R.string.PHONE_STATUS_REJECT));
        String userToken = getSaltString(32);
        Prefs.putString(AppPreferences.TEMP_USER_TOKEN, userToken);
        values.add(userToken);

        manager.createObject(context.getString(R.string.USER), str, keys, values);
        String objectId = manager.getObjectIdCreated();
        if (objectId.isEmpty()) {
            return false;
        }
        setUniqueUserId(objectId);

        manager = new DatabaseObjectManager();
        manager.createObject(context.getString(R.string.LOCATION), "place", null, null);
        String locationId = manager.getObjectIdCreated();
        if (locationId.isEmpty()) {
            return false;
        }
        setIdOfLocation(locationId);

        manager = new DatabaseObjectManager();
        keys = new ArrayList<>();
        values = new ArrayList<>();
        keys.add(context.getString(R.string.USER_LOCATION));
        keys.add(context.getString(R.string.USER_ID));
        values.add(locationId);
        values.add(objectId);
        setUserPhone(str);

        manager.updateObject(objectId, str, keys, values);

        return true;
    }

    /**
     * Generate new token by imsi, imei, phone. First, need check permission prior to calling this method
     *
     * @param p Phone number with country code, ex: 996555440265
     * @return generated token or empty string if something went wrong
     */
    @SuppressLint("MissingPermission")
    @SuppressWarnings("HardwareIds")
    public String generateTokenImsi(String p, Context context) {
        CustomDebugLogger log = new CustomDebugLogger();
        TelephonyManager tm = (TelephonyManager) context
                .getSystemService(Context.TELEPHONY_SERVICE);
        if (tm != null) {
            String imsi = tm.getSubscriberId();
            String s = tm.getDeviceId();
            log.e("TAG", "imei: " + s + " imsi " + imsi);
            try {
                String combo = imsi + s + p + "";
                if (combo.length() > 8) {
                    return AppUtils.md(imsi + s + p);
                }
            } catch (NoSuchAlgorithmException e) {
                e.printStackTrace();
            }
        }
        return "";
    }

    /**
     * Generates random string
     *
     * @param p Phone number with country code, ex: 996555440265
     * @return random string of particular length, never empty
     */
    public String generateTokenRand(String p) {
        String q = AppUtils.getSaltString(32);
        try {
            return AppUtils.md(p + q);
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        }
        return AppUtils.getSaltString(32);
    }

    /**
     * Check if app has PHONE_STATE_PERMISSION to retrieve imei, imsi
     *
     * @return truth value, true - permission granted, false - otherwise
     */
    public boolean havePhoneStatePermission(Context context) {
        return ActivityCompat.checkSelfPermission(context, Manifest.permission.READ_PHONE_STATE)
                == PackageManager.PERMISSION_GRANTED;
    }

    /**
     * Check if user authorised by imei, imsi, and phone or not
     *
     * @return truth value
     */
    public boolean isAuthByImei() {
        return Prefs.getBoolean(AppPreferences.IS_AUTH_BY_IMEI, false);
    }

    /**
     * Check is user exceed limit of allowed attempts to enter verification code, default: SMS_VERIFY_LIMIT
     *
     * @return true - user exceeded allowed limit
     */
    public boolean isCodeVerificationLimit() {
        int limit = Prefs.getInt(AppPreferences.SMS_VERIFICATION_LIMIT, smsVerificationLimit);
        return limit <= 0;
    }

    /**
     * Parameter should be passed using trim() function without leading '0'
     *
     * @param p user's phone number without country code
     * @return return is user's phone number seems as valid
     */
    public boolean isProperPhoneNumber(String p) {
        try {
            if (p == null) {
                return false;
            }
            final int len = p.length();
            if (len < 7 || len > 20) {
                return false;
            }
            for (int i = 0; i < len; i++) {
                int ascii = (int) p.charAt(i);
                if (ascii < 48 || ascii > 57) {
                    return false;
                }
            }
            return true;
        } catch (Exception e) {
            return false;
        }
    }

    /**
     * @return Returns if user re registered in integral by new auth scheme
     */
    public boolean isReRegistered() {
        return Prefs.getBoolean(AppPreferences.IS_RE_REGISTERED, false);
    }

    /**
     * For new registration scheme: set if user re registered
     * in integral
     */
    public void setReRegistered(boolean b) {
        Prefs.putBoolean(AppPreferences.IS_RE_REGISTERED, b);
    }

    /**
     * Check if user already registered in Integral by report
     *
     * @param str user's phone number with country code ex: 996555440265
     * @return truth value; true - user in database, false - otherwise
     */
    public boolean isUserInIntegralDb(String str, Context context) {
        ArrayList<String> keys = new ArrayList<>();
        ArrayList<String> values = new ArrayList<>();
        ArrayList<String> jsonKeys = new ArrayList<>();
        keys.add("phone");
        values.add(str);
        jsonKeys.add("phone_status");
        ReportPerformer report = new ReportPerformer(context.getString(R.string.REPORT_PHONE_STATUS_CHECK), keys,
                values, jsonKeys);
        ArrayList<String> result = report.execute();
        if (result.size() > 0) {
            return result.get(0).equals("approve") || result.get(0).equals("reject");
        }
        return false;
    }

    /**
     * Parameter should be passed using trim() function
     *
     * @param s country code ex: 996
     * @return returns if user's code is valid or not
     */
    public boolean isValidCode(String s) {
        try {
            int code = Integer.valueOf(s);
            Countries c = new Countries();
            int len = c.countryCodes.length;
            for (int i = 0; i < len; i++) {
                if (c.countryCodes[i] == code) {
                    return true;
                }
            }
        } catch (Exception e) {
            return false;
        }
        return false;
    }

    /**
     * Set if user authorised by imei, imsi, and phone or not
     *
     * @param b store this value for future check
     */
    public void setAuthImei(boolean b) {
        Prefs.putBoolean(AppPreferences.IS_AUTH_BY_IMEI, b);
    }

    /**
     * Send sms verification code to user
     */
    public void triggerSms(Context context) {
        ArrayList<String> keys = new ArrayList<>();
        ArrayList<String> values = new ArrayList<>();
        ArrayList<String> jsonKeys = new ArrayList<>();
        keys.add("user");
        keys.add("secret");
        values.add(getUserPhone());
        values.add(Prefs.getString(AppPreferences.TEMP_USER_TOKEN, ""));
        ReportPerformer report = new ReportPerformer(context.getString(R.string.REPORT_SMS_TRIGGER), keys, values,
                jsonKeys);
        report.execute();
    }

    /**
     * Generates new random integer
     *
     * @return random integer
     */
    private int generateSmsCode() {
        Random rand = new Random();
        return rand.nextInt(9999999) + 1000000;
    }

    private String getSaltString(int length) {
        String saltChars = "ABCDEFGHIJKLMNOPQRSTUVWXYZ1234567890";
        StringBuilder salt = new StringBuilder();
        Random rnd = new Random();
        while (salt.length() < length) { // length of the random string.
            int index = (int) (rnd.nextFloat() * saltChars.length());
            salt.append(saltChars.charAt(index));
        }
        return salt.toString();
    }

    /**
     * Get generated code
     *
     * @return sms code
     */
    private int getSmsCode() {
        return getUserSmsCode();
    }

    /**
     * Set generated sms code
     *
     * @param c sms code
     */
    private void setSmsCode(int c) {
        setUserSmsCode(c);
    }


}

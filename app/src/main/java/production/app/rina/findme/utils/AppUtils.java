package production.app.rina.findme.utils;

import static production.app.rina.findme.services.common.AppPreferences.getUniqueUserId;
import static production.app.rina.findme.services.common.AppPreferences.getUserPhone;

import android.app.ActivityManager;
import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.support.annotation.NonNull;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.iid.FirebaseInstanceId;
import com.google.firebase.iid.InstanceIdResult;
import com.pixplicity.easyprefs.library.Prefs;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.Random;
import production.app.rina.findme.R;
import production.app.rina.findme.services.common.AppPreferences;
import production.app.rina.findme.services.common.DateTime;
import production.app.rina.findme.services.network.DatabaseObjectManager;

public class AppUtils {

    public static boolean isAppInForeground(Context context) {
        if (context == null) {
            return false;
        }
        try {
            String topPackage = ((ActivityManager) context.getSystemService(Context.ACTIVITY_SERVICE))
                    .getRunningTasks(1).get(0).topActivity.getPackageName();
            String currentPackage = context.getPackageName();
            return currentPackage.equals(topPackage);
        } catch (Exception localException) {

            // isAppInForeground  exception
        }
        return false;
    }

    public static boolean isNetworkAvailable(Context context) {
        try {
            ConnectivityManager connectivityManager
                    = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
            assert connectivityManager != null;
            NetworkInfo activeNetworkInfo = connectivityManager.getActiveNetworkInfo();
            return activeNetworkInfo != null && activeNetworkInfo.isConnected();
        } catch (Exception e) {
            return true;
        }
    }

    public static void sendUserLastActiveDateAndToken(final Context context) {
        FirebaseInstanceId.getInstance().getInstanceId()
                .addOnCompleteListener(new OnCompleteListener<InstanceIdResult>() {
                    @Override
                    public void onComplete(@NonNull Task<InstanceIdResult> task) {
                        if (!task.isSuccessful()) {
                            return;
                        }
                        String token = task.getResult().getToken();
                        DateTime time = new DateTime();
                        DatabaseObjectManager manager = new DatabaseObjectManager();
                        ArrayList<String> keys = new ArrayList<>();
                        ArrayList<String> values = new ArrayList<>();
                        keys.add(context.getResources().getString(R.string.LAST_USER_ACTIVITY_TIME));
                        keys.add(context.getResources().getString(R.string.FIREBASE_TOKEN_ID));
                        values.add(time.getToday());
                        values.add(token);
                        manager.updateObject(getUniqueUserId(),
                                getUserPhone(), keys, values);
                    }
                });
    }

    public static String md(String to) throws NoSuchAlgorithmException {
        MessageDigest md = MessageDigest.getInstance("SHA-512");
        byte[] digest = md.digest(to.getBytes());
        StringBuilder sb = new StringBuilder();
        for (final byte aDigest : digest) {
            sb.append(Integer.toString((aDigest & 0xff) + 0x110, 16).substring(1));
        }
        return sb.toString().substring(0, 64);
    }

    public static String getSaltString(int length) {
        String saltChars = "ABCDEFGHIJKLMNOPQRSTUVWXYZ1234567890";
        StringBuilder salt = new StringBuilder();
        Random rnd = new Random();
        while (salt.length() < length) { // length of the random string.
            int index = (int) (rnd.nextFloat() * saltChars.length());
            salt.append(saltChars.charAt(index));
        }
        return salt.toString();
    }

}

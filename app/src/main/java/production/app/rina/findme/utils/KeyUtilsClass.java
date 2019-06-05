package production.app.rina.findme.utils;

import static production.app.rina.findme.services.common.AppPreferences.getUniqueUserId;
import static production.app.rina.findme.services.common.AppPreferences.getUserPhone;

import android.content.Context;
import com.pixplicity.easyprefs.library.Prefs;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.Map;
import production.app.rina.findme.LoginHelperClass;
import production.app.rina.findme.R;
import production.app.rina.findme.services.common.AppPreferences;
import production.app.rina.findme.services.encryption.RSA;
import production.app.rina.findme.services.network.DatabaseObjectManager;
import production.app.rina.findme.testing.CustomDebugLogger;

public class KeyUtilsClass {

    /**
     * This is test button located in MainActivity.xml to retrieve public and private
     * keys for messaging from RSA static method
     */
    public static String pubKey;

    public static String priKey;

    CustomDebugLogger log;

    public void initChatKeysBackground(final Context context) {
        Runnable r = new Runnable() {
            @Override
            public void run() {
                initObj();
                try {
                    getPubPriKeys(context);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        };
        new Thread(r).start();
    }

    private void getPubPriKeys(Context context) throws Exception {
        Map<String, Object> map;
        boolean isKeysExist = Prefs.getBoolean(AppPreferences.IS_PRIVATE_PUBLIC_KEYS, false);
        isKeysExist = false; // TODO delete <---
        if (!isKeysExist) {
            map = RSA.initKey();
            pubKey = RSA.getPublicKey(map);
            priKey = RSA.getPrivateKey(map);
            Prefs.putString(AppPreferences.PRIVATE_KEY_MSG, priKey);
            Prefs.putString(AppPreferences.PUBLIC_KEY_MSG, pubKey);

            // Sent public key for chat to server
            String publicKey = URLEncoder.encode(pubKey, "UTF-8");
            log.e("TAG", "my public key encoded: " + publicKey);

            DatabaseObjectManager manager = new DatabaseObjectManager();
            ArrayList<String> keys = new ArrayList<>();
            ArrayList<String> values = new ArrayList<>();
            keys.add(context.getResources().getString(R.string.MESSAGE_PUBLIC_KEY));
            values.add(publicKey);
            manager.updateObject(getUniqueUserId(),
                    getUserPhone(), keys, values);

            Prefs.putBoolean(AppPreferences.IS_PRIVATE_PUBLIC_KEYS, true);
            log.e("TAG", "private key: " + priKey + " pub key: " + pubKey);
        } else {
            pubKey = Prefs.getString(AppPreferences.PUBLIC_KEY_MSG, "");
            priKey = Prefs.getString(AppPreferences.PRIVATE_KEY_MSG, "");
            log.e("TAG", "private key: " + priKey + " pub key: " + pubKey);
        }
    }

    private void initObj() {
        log = new CustomDebugLogger();
    }
}

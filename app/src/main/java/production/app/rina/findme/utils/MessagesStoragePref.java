package production.app.rina.findme.utils;

import static android.content.Context.MODE_PRIVATE;

import android.content.Context;
import android.content.SharedPreferences;
import production.app.rina.findme.testing.CustomDebugLogger;

public class MessagesStoragePref {

    public static final String MES_STORAGE = "production.app.rina.findme" + "DjxZml" + "MESSAGES"; // String

    public static final String GLOBAL_MESSAGE_COUNTER = "GLOBAL_MESSAGE_COUNTER"; // int

    private static SharedPreferences instance;

    public static void deleteAll() {
        instance.edit().clear().apply();
        CustomDebugLogger log = new CustomDebugLogger();
        log.e("TAG", "deleteAll");
    }

    public static int getSize() {
        return instance.getAll().size();
    }

    public static void init(Context context) {
        if (instance == null) {
            instance = context.getSharedPreferences(MES_STORAGE, MODE_PRIVATE);
        }
    }

    public static String read(String key, String defValue) {
        return instance.getString(key, defValue);
    }

    public static boolean read(String key, boolean defValue) {
        return instance.getBoolean(key, defValue);
    }

    public static Integer read(String key, int defValue) {
        return instance.getInt(key, defValue);
    }

    public static void remove(String key) {
        SharedPreferences.Editor prefsEditor = instance.edit();
        prefsEditor.remove(key);
        prefsEditor.apply();
    }

    public static void write(String key, String value) {
        SharedPreferences.Editor prefsEditor = instance.edit();
        prefsEditor.putString(key, value);
        prefsEditor.apply();
    }

    public static void write(String key, boolean value) {
        SharedPreferences.Editor prefsEditor = instance.edit();
        prefsEditor.putBoolean(key, value);
        prefsEditor.apply();
    }

    public static void write(String key, Integer value) {
        SharedPreferences.Editor prefsEditor = instance.edit();
        prefsEditor.putInt(key, value).apply();
    }

    public static boolean writeCommit(String key, boolean value) {
        SharedPreferences.Editor prefsEditor = instance.edit();
        prefsEditor.putBoolean(key, value);
        return prefsEditor.commit();
    }

    public static boolean writeCommit(String key, int value) {
        SharedPreferences.Editor prefsEditor = instance.edit();
        prefsEditor.putInt(key, value);
        return prefsEditor.commit();
    }

    private MessagesStoragePref() {
    }

}

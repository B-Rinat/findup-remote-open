package production.app.rina.findme.utils;

import android.graphics.Color;
import java.util.Map;
import org.json.JSONException;
import org.json.JSONObject;

public class JsonUtils {

    public static boolean getBoolean(JSONObject json, String key, boolean defaultValue) {
        //Boolean.parseBoolean is not appropriate here, because it returns false in any bad case
        String result = getString(json, key, Boolean.toString(defaultValue));
        if (Boolean.TRUE.toString().equalsIgnoreCase(result)) {
            return true;
        } else if (Boolean.FALSE.toString().equalsIgnoreCase(result)) {
            return false;
        }
        return defaultValue;
    }

    public static int getColor(JSONObject json, String key, int defaultValue) {
        try {
            return Color.parseColor(getString(json, key, Integer.toString(defaultValue)));
        } catch (Exception e) {
            return defaultValue;
        }
    }

    public static float getFloat(JSONObject json, String key, float defaultValue) {
        try {
            return Float.parseFloat(getString(json, key, Float.toString(defaultValue)));
        } catch (Exception e) {
            return defaultValue;
        }
    }

    public static int getInt(JSONObject json, String key, int defaultValue) {
        try {
            return Integer.parseInt(getString(json, key, Integer.toString(defaultValue)));
        } catch (Exception e) {
            return defaultValue;
        }
    }

    public static long getLong(JSONObject json, String key, long defaultValue) {
        try {
            return Long.parseLong(getString(json, key, Long.toString(defaultValue)));
        } catch (Exception e) {
            return defaultValue;
        }
    }

    public static String getString(JSONObject json, String key, String defaultValue) {
        if (!json.has(key)) {
            return defaultValue;
        }

        try {
            return json.getString(key);

        } catch (JSONException e) {
            return defaultValue;
        }
    }

    public static JSONObject parseMap(Map<String, String> map) {
        if (map == null) {
            return null;
        }

        try {
            JSONObject result = new JSONObject();
            for (String key : map.keySet()) {
                String valueString = map.get(key);
                Object value;
                try {
                    value = new JSONObject(valueString);
                } catch (Exception ex) {
                    value = valueString;
                }
                result.put(key, value);
            }

            return result;
        } catch (Exception ex) {
            ex.printStackTrace();
            return null;
        }
    }

    public static JSONObject parseString(String json) {
        if (json == null) {
            return null;
        }

        try {
            return new JSONObject(json);
        } catch (JSONException e) {
            e.printStackTrace();
            return null;
        }
    }
}
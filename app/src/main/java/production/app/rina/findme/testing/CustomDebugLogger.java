package production.app.rina.findme.testing;

import android.util.Log;

public class CustomDebugLogger {

    private final boolean SET_LOG_AVAILABLE = false;

    public void e(String tag, String msg) {
        if (!SET_LOG_AVAILABLE) {
            return;
        }
        boolean IS_ERROR = false;
        if (tag == null) {
            IS_ERROR = true;
            Log.e("SPECIAL", "CustomDebugLogger: TAG FIELD IS NULL");
        } else if (tag.length() == 0) {
            IS_ERROR = true;
            Log.e("SPECIAL", "CustomDebugLogger: TAG FIELD IS EMPTY");
        }
        if (msg == null) {
            IS_ERROR = true;
            Log.e("SPECIAL", "CustomDebugLogger: MSG FIELD IS NULL");
        } else if (msg.length() == 0) {
            IS_ERROR = true;
            Log.e("SPECIAL", "CustomDebugLogger: MSG FIELD IS EMPTY");
        }
        if (IS_ERROR) {
            return;
        }

        try {
            Log.e(tag, msg); // URLDecoder.decode(msg, "UTF-8")
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void n(String msg) {
        if (!SET_LOG_AVAILABLE) {
            return;
        }
        try {
            Log.e(new Object() {
            }.getClass().getEnclosingMethod().getName(), msg + "");
        } catch (Exception e) {
            Log.e("[CustomDebugLogger", ": <msg>" + e + "<msg/>]" + msg + "");
        }
    }

}

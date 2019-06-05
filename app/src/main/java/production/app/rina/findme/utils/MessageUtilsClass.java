package production.app.rina.findme.utils;

import android.support.v7.app.AppCompatActivity;
import com.pixplicity.easyprefs.library.Prefs;
import java.util.Vector;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import production.app.rina.findme.LoginHelperClass;
import production.app.rina.findme.services.common.AppPreferences;
import production.app.rina.findme.services.common.DateTime;
import production.app.rina.findme.testing.CustomDebugLogger;

public class MessageUtilsClass extends AppCompatActivity {

    private static final MessageUtilsClass ourInstance = new MessageUtilsClass();

    CustomDebugLogger log = new CustomDebugLogger();

    Vector<String> mesVec = new Vector<>();

    JSONArray messages;

    private final int maxNumberOfMessagesAllowed = 5;

    public static MessageUtilsClass getInstance() {
        return ourInstance;
    }

    public MessageUtilsClass() {
    }

    public void cleanVectorMessagesTemp() {
        mesVec.clear();
    }

    /**
     * Store messages into persistent storage
     */
    public void forceStoreMessages() {
        if (messages == null || messages.length() <= 0) {
            log.e("TAG", "Nothing to save, return");
            return;
        }
        log.e("TAG", "SAVING, EXCEEDED MAX LIMIT: " + maxNumberOfMessagesAllowed);
        int globalMessageCounter = MessagesStoragePref.read(MessagesStoragePref.GLOBAL_MESSAGE_COUNTER, 1);
        log.e("TAG", "global_message_counter: read: " + globalMessageCounter);
        JSONObject packageMessages = new JSONObject();
        try {
            packageMessages.put("1", messages);
        } catch (JSONException e) {
            e.printStackTrace();
        }
        messages = new JSONArray();
        log.e("TAG", "global_message_counter: " + globalMessageCounter);
        MessagesStoragePref.write(Integer.toString(globalMessageCounter), packageMessages.toString());
        globalMessageCounter++;
        MessagesStoragePref.write(MessagesStoragePref.GLOBAL_MESSAGE_COUNTER, globalMessageCounter);
    }

    public boolean readIsMsgReceived() {
        return Prefs.getBoolean(AppPreferences.IS_MESSAGE_RECEIVED, false);
    }

    /**
     * Read messages received from other user that are to be populated on user's screen
     *
     * @param fName file name for SHaredPreferences
     * @return messages that are being received for some period of time
     */
    public Vector<String> readMessages(String fName) {
        if (fName == null) {
            log.e("TAG", "<-file name: " + fName);
            return null;
        }
        if (mesVec != null) {
            return mesVec;
        }
        return null;
    }

    public void setIsMsgReceived(boolean b) {
        Prefs.putBoolean(AppPreferences.IS_MESSAGE_RECEIVED, b);
    }

    public void storeMessage(String m, String fName, String sender) {
        if (m == null || fName == null) {
            log.e("TAG", "<-msg: " + m + "<-file name: " + fName);
            return;
        }
        DateTime dateTime = new DateTime();
        if (messages == null) {
            messages = new JSONArray();
        }
        JSONObject msg = new JSONObject();
        try {
            msg.put("time", dateTime.getToday());
            msg.put("sender", sender);
            msg.put("message", m);
        } catch (JSONException e) {
            e.printStackTrace();
        }
        if (messages.length() > maxNumberOfMessagesAllowed) {
            forceStoreMessages();
        }
        messages.put(msg);
        if (!sender.equals("me")) {
            mesVec.add(m);
        }
    }

}

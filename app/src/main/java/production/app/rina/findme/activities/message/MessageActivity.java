package production.app.rina.findme.activities.message;


import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;
import com.github.library.bubbleview.BubbleTextView;
import com.pixplicity.easyprefs.library.Prefs;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.util.Timer;
import java.util.TimerTask;
import java.util.Vector;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import production.app.rina.findme.LoginHelperClass;
import production.app.rina.findme.R;
import production.app.rina.findme.services.common.AppPreferences;
import production.app.rina.findme.services.location.LocateUsersOnMapActivity;
import production.app.rina.findme.services.network.CustomFirebaseMessagingService;
import production.app.rina.findme.testing.CustomDebugLogger;
import production.app.rina.findme.utils.KeyUtilsClass;
import production.app.rina.findme.utils.MessageUtilsClass;
import production.app.rina.findme.utils.MessagesStoragePref;

public class MessageActivity extends AppCompatActivity {

    private class Message {

    }

    static String guestPublicKey;

    public static String guestPhoneNumberAndNameOfFolder = "";

    CustomFirebaseMessagingService fb;

    CustomDebugLogger log;

    LoginHelperClass login;

    MessageUtilsClass messageUtils;

    private final String PREFS_NAME = "rina.app.production.findme.user_credentials";

    private boolean isActive;

    private boolean isAdminMessage;

    private boolean isUserOnline = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.message);

        log = new CustomDebugLogger();
        messageUtils = MessageUtilsClass.getInstance();
        MessagesStoragePref.init(getApplicationContext());

        isAdminMessage = true;
        try {
            receivedMessage(URLDecoder.decode(getString(R.string.admin_message), "UTF-8"));
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }
        try {
            fb = new CustomFirebaseMessagingService(getApplicationContext());
        } catch (Exception e) {
            e.printStackTrace();
        }

        setOnClickListener();

        // TODO retrieve guest phone number from intent or from memory
        guestPhoneNumberAndNameOfFolder = Prefs.getString(AppPreferences.PHONE_OF_GUEST_TO_WHOM_MEETING_ONGOING, "");
        try {
            guestPublicKey = login
                    .performNewReport(getApplicationContext(), "4376", guestPhoneNumberAndNameOfFolder, "", "", "",
                            "get_pub_key");
            log.e("TAG", "guest_public_key: " + guestPublicKey);
        } catch (IOException | JSONException e) {
            e.printStackTrace();
        }
        CustomFirebaseMessagingService.tokenOfGuest = Prefs.getString(AppPreferences.TOKEN_GUEST, "");
        if (CustomFirebaseMessagingService.tokenOfGuest.isEmpty()) {
            try {
                CustomFirebaseMessagingService.tokenOfGuest = login.performNewReport(getApplicationContext(), "2618",
                        guestPhoneNumberAndNameOfFolder, "", "", "", "fb_token");
            } catch (IOException | JSONException e) {
                e.printStackTrace();
            }
        }
        checkIfUserOnline();

        // new task to periodically check for new messages
        checkNewMessages();

    }

    @Override
    protected void onStart() {
        super.onStart();
        if (KeyUtilsClass.priKey == null || KeyUtilsClass.pubKey == null) {
            // KeyUtilsClass k = new KeyUtilsClass();
            // k.init_chat_keys_background();
        }
        // TODO if empty screen and have some messages, display to the screen
        int size = MessagesStoragePref.getSize();
        log.e("TAG", "TOTAL MESSAGES: " + size);
        if (size > 0) {
            JSONObject json, msgJson;
            JSONArray ja;
            String msgObj;
            int counter = 1;
            for (int i = 0; i < size; i++) {
                log.e("TAG", "for counter: " + counter);
                msgObj = MessagesStoragePref.read(Integer.toString(counter), "");
                log.e("TAG", "msgObj: " + msgObj);
                if (!msgObj.isEmpty()) {
                    try {
                        json = new JSONObject(msgObj);
                        ja = json.getJSONArray("1");
                        for (int j = 0; j < ja.length(); j++) {
                            msgJson = ja.getJSONObject(j);
                            if (msgJson.getString("sender").equals("me")) {
                                myMessage(msgJson.getString("message"));
                            } else {
                                receivedMessage(msgJson.getString("message"));
                            }
                        }
                    } catch (JSONException e) {
                        log.e("TAG", "exception: | onStart | " + e);
                        e.printStackTrace();
                    }
                }
                counter++;
            }
        }
    }

    @Override
    public void onPause() {
        super.onPause();
        if (messageUtils != null) {
            messageUtils.forceStoreMessages();
        }
    }

    public void back_to_map(View v) {
        onBackPressed();
    }

    public void checkIfUserOnline() {
        isActive = true;
        //Declare the timer
        final Timer t = new Timer();
        //Set the schedule function and rate
        t.scheduleAtFixedRate(new TimerTask() {

                                  @Override
                                  public void run() {
                                      //Called each time when 1000 milliseconds (1 second) (the period parameter)
                                      try {
                                          log.e("TAG", "isActive: " + isActive + "");
                                          if (!isActive) {
                                              t.cancel();
                                              t.purge();
                                          }
                                          String result = login.performNewReport(getApplicationContext(), "2161",
                                                  guestPhoneNumberAndNameOfFolder, "", "", "", "IS_B_STARTED_MAP");
                                          log.e("TAG", "result: " + result);
                                          isUserOnline = result.charAt(0) == 'T';
                                      } catch (Exception e) {

                                      }
                                  }

                              },
                //Set how long before to start calling the TimerTask (in milliseconds)
                0,
                //Set the amount of time between each execution (in milliseconds)
                10000);
    }

    public void checkNewMessages() {
        final Timer t = new Timer();
        t.scheduleAtFixedRate(new TimerTask() {
            @Override
            public void run() {
                log.e("TAG", "isActive: " + isActive + "");
                if (!isActive) {
                    t.cancel();
                    t.purge();
                }
                onReceiveMessage();
            }
        }, 3000, 2000);
    }

    @Override
    public void onBackPressed() {
        isActive = false;
        Intent intent = new Intent(this, LocateUsersOnMapActivity.class);
        startActivity(intent);

//        overridePendingTransition(R.anim.slide_down, R.anim.no_animation);
//        finish();
    }

    public void setOnClickListener() {
        TextView b = findViewById(R.id.editText10);
        b.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                log.e("TAG", "is user online? " + isUserOnline);
                if (!isUserOnline) {
                    Toast.makeText(getApplicationContext(), getString(R.string.user_offline), Toast.LENGTH_LONG)
                            .show();
                    return;
                }
                TextView m = findViewById(R.id.edit_input_message);
                String message = m.getText().toString();
                message = message.trim();
                log.e("MESS", "->" + message + "<-");
                if (message.length() > 500) {
                    Toast.makeText(getApplicationContext(), getString(R.string.too_long_message), Toast.LENGTH_LONG)
                            .show();
                    return;
                }
                if (message.length() > 0) {
                    sentMessage(message);
                }
                m.setText("");
                m.requestFocus();
            }
        });
    }

    private void myMessage(String s) {
        LayoutInflater layoutInflater = (LayoutInflater) getSystemService(LAYOUT_INFLATER_SERVICE);
        assert layoutInflater != null;
        View sentMessage = layoutInflater.inflate(R.layout.right_message_bubble, null, false);
        LinearLayout ll = findViewById(R.id.list_messages);
        BubbleTextView message = sentMessage.findViewById(R.id.bubble_text_message_right_bubble);
        message.setText(s);
        message.requestFocus();
        ll.addView(sentMessage);
    }

    /**
     * Periodically has been called to check if messages came from other user
     */
    private void onReceiveMessage() {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                log.e("TAG", "is_message_received? " + messageUtils.readIsMsgReceived());
                if (messageUtils.readIsMsgReceived()) {
                    Vector<String> mess = messageUtils.readMessages(MessagesStoragePref.MES_STORAGE);
                    if (mess != null) {
                        for (String m : mess) {
                            if (m != null && !m.isEmpty()) {
                                receivedMessage(m);
                            }
                        }
                    }
                    messageUtils.setIsMsgReceived(false);
                    messageUtils.cleanVectorMessagesTemp();
                }
            }
        });
    }

    private void readAllMessagesAndPopulateOnScreen() {

    }

    private void receivedMessage(String s) {
        LayoutInflater layoutInflater = (LayoutInflater) getSystemService(LAYOUT_INFLATER_SERVICE);
        assert layoutInflater != null;
        final View sentMessage = layoutInflater.inflate(R.layout.left_message_bubble, null, false);
        final LinearLayout ll = findViewById(R.id.list_messages);
        BubbleTextView message = sentMessage.findViewById(R.id.bubble_text_message_left_bubble);
        message.setText(s);
        if (isAdminMessage) {
            sentMessage.setBackgroundColor(getResources().getColor(R.color.red));
            message.setTextColor(getResources().getColor(R.color.rectangleColorPassiveText));

            isAdminMessage = false;
        }
        message.requestFocus();
        ll.addView(sentMessage);
    }

    private void sentMessage(String s) {
        LayoutInflater layoutInflater = (LayoutInflater) getSystemService(LAYOUT_INFLATER_SERVICE);
        assert layoutInflater != null;
        View sentMessage = layoutInflater.inflate(R.layout.right_message_bubble, null, false);
        LinearLayout ll = findViewById(R.id.list_messages);
        BubbleTextView message = sentMessage.findViewById(R.id.bubble_text_message_right_bubble);
        message.setText(s);
        message.requestFocus();
        ll.addView(sentMessage);
        // log.e("TAG", "guest public key: " + guest_public_key);
        fb.SEND(s, guestPublicKey);
    }


}

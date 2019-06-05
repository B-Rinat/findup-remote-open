package production.app.rina.findme.services.network;

import static production.app.rina.findme.services.common.AppPreferences.getUserPhone;

import android.content.Context;
import android.os.AsyncTask;
import android.support.annotation.NonNull;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.iid.FirebaseInstanceId;
import com.google.firebase.iid.InstanceIdResult;
import com.google.firebase.messaging.FirebaseMessagingService;
import com.google.firebase.messaging.RemoteMessage;
import com.pixplicity.easyprefs.library.Prefs;
import java.net.URLDecoder;
import java.net.URLEncoder;
import java.nio.charset.Charset;
import java.util.Map;
import java.util.Objects;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;
import org.json.JSONObject;
import production.app.rina.findme.LoginHelperClass;
import production.app.rina.findme.activities.message.MessageActivity;
import production.app.rina.findme.services.common.AppPreferences;
import production.app.rina.findme.services.common.CustomNotification;
import production.app.rina.findme.testing.CustomDebugLogger;
import production.app.rina.findme.utils.MessageUtilsClass;
import production.app.rina.findme.utils.MessagesStoragePref;

public class CustomFirebaseMessagingService extends FirebaseMessagingService {

    public static String tokenOfGuest = "";

    public static String hostName = "";

    private static String putBody = "";

    private static CustomDebugLogger log;

    public static boolean isSendingInAppMessage = false;

    public static final MediaType JSON
            = MediaType.parse("application/json; charset=utf-8");

    public Context contextFb;

    public String tokenId = "";

    private MessageUtilsClass messageUtils;

    public static void sendNotification(final String regToken, final String[] extraData) {
        try {
            if (log == null) {
                log = new CustomDebugLogger();
            }
            new AsyncTask<Void, Void, Void>() {
                @Override
                protected Void doInBackground(Void... params) {
                    try {
                        OkHttpClient client = new OkHttpClient();
                        JSONObject json = new JSONObject();
                        JSONObject dataJson = new JSONObject();
                        dataJson.put("body", extraData[0]);
                        dataJson.put("title", extraData[1]);
                        dataJson.put("fromNumber", getUserPhone());
                        dataJson.put("time", extraData[2]);

                        json.put("data", dataJson);
                        json.put("to", regToken);

                        RequestBody body = RequestBody.create(JSON, json.toString());
                        String tokenFirebase
                                = "AAAAAWwfcaQ:APA91bEoSlNvtiVXkxqwXOfm8SdMK7lHI0iCC5bL_Wq_wOH9FcQYMaNlPmoQnVBY6ISBWqhi1SRvKrIaQ04JrKgToo4STRrSJHcfS8jgXAFwZg8DOnpeq16mvT3_1XgIu6enEQVEMUxd";
                        Request request = new Request.Builder()
                                .header("Authorization",
                                        "key=" + tokenFirebase) // SyncStateContract.Constants.LEGACY_SERVER_KEY
                                .url("https://fcm.googleapis.com/fcm/send")
                                .post(body)
                                .build();
                        Response response = client.newCall(request).execute();

                        assert response.body() != null;
                        String finalResponse = response.body().string();
                        log.e("TAG", "token send to server ---> " + regToken);
                        log.e("TAG", "finalResponse ---> " + finalResponse);
                        JSONObject s = new JSONObject(finalResponse);
                        String success = s.getString("success");
                        log.e("TAG", "here ---> " + success);

                        if (success.equals("1")) {
                            log.e("TAG", "push notification sent successfully");
                        } else {
                            log.e("TAG", "error in sending push notification");
                        }
                    } catch (Exception e) {
                        log.e("TAG", "error occurred inside sending a new notification" + "\n" + e);
                    }
                    return null;
                }
            }.execute();

        } catch (Exception e) {
            log.e("TAG", "Error in sendNotification in CustomFirebaseMessagingService");
        }

    }

    public CustomFirebaseMessagingService() {
        log = new CustomDebugLogger();
        messageUtils = MessageUtilsClass.getInstance();
    }

    public CustomFirebaseMessagingService(Context context) throws Exception {
        contextFb = context;
        boolean check = retrieveTokenKey();
        log = new CustomDebugLogger();
        messageUtils = MessageUtilsClass.getInstance();
    }

    public void SEND(String mes, String pubkey) {
        try {
            final String charSet = "UTF-8";
            if (!isCharsetSupported(charSet)) {
                log.e("PUSH", "CHAT NOT Supported due to charset incompatibility");
                return;
            }

//            byte[] bytesPlain = mes.getBytes(charSet);
//            byte[] encodeData = RSA.encryptByPublicKey(bytesPlain, pubKey);
////            log.e("TAG", "encodeData: " + encodeData.toString());
//
//            putBody = RSA.encryptBASE64(encodeData);
//            log.e("TAG", "putBody: " + putBody);
//
//            putBody = URLEncoder.encode(putBody, "UTF-8");
//            log.e("TAG", "Encoded message: " + putBody);

            isSendingInAppMessage = true;
            messageUtils.storeMessage(mes, MessagesStoragePref.MES_STORAGE, "me");
            putBody = URLEncoder.encode(mes, "UTF-8");
            log.e("TAG", "Send new message: " + putBody);
            // sendNotificationMessage("new_message");

        } catch (Exception e) {
            log.e("PUSH", "Exception Firebase inside SEND ..." + e);
        }
    }

    public void notifyUser(RemoteMessage remoteMessage) {

        log.e("msg", "onMessageReceived: " + remoteMessage.getData().toString());
        JSONObject json;
        try {
            Map<String, String> params = remoteMessage.getData();

            if (params == null || params.isEmpty()) {
                return;
            }

            json = new JSONObject(params);

            CustomNotification notification
                    = new CustomNotification(getBaseContext(), json);
            notification.build();

        } catch (Exception e) {
            e.printStackTrace();
            log.e("TAG", "Exception | notify_user");
        }
    }

    @Override
    public void onMessageReceived(RemoteMessage remoteMessage) {
        try {

//            log.e("PUSH", "onMessageReceived");
//            String base64_recieved = Objects.requireNonNull(remoteMessage.getNotification()).getBody();
//
//            log.e("PUSH", "base64_recieved -> " + base64_recieved);
//            String base64_recieved_decoded = URLDecoder.decode(base64_recieved, "UTF-8");
//
//            log.e("PUSH", "base64_recieved_decoded -> " + base64_recieved_decoded);
//            log.e("TAG", "priKey: " + KeyUtilsClass.priKey);
//            byte[] decrypt = RSA.decryptByPrivateKey(RSA.decryptBASE64(base64_recieved_decoded), KeyUtilsClass.priKey);
//            String decryptStr = new String(decrypt, "UTF-8");
//
//            log.e("PUSH", "Message from Firebase decrypted -> " + decryptStr);
//
//
//            messageUtils.store_message(decryptStr, MessageUtilsClass.FolderName.messages);
//            messageUtils.set_is_msg_received(true);

            super.onMessageReceived(remoteMessage);

            notifyUser(remoteMessage);

            String decStr = URLDecoder.decode(Objects.requireNonNull(remoteMessage.getData().get("body")), "UTF-8");
            log.e("TAG", "Receive new message: " + decStr);

            if (messageUtils != null) {
                log.e("TAG", "store new message");
                messageUtils.storeMessage(decStr, MessagesStoragePref.MES_STORAGE,
                        MessageActivity.guestPhoneNumberAndNameOfFolder);
                messageUtils.setIsMsgReceived(true);
            }

        } catch (Exception e) {
            log.e("TAG", "Exception inside onMessageReceived");
            log.e("TAG", "error -> " + e);
        }

    }

    /**
     * Called if InstanceID token is updated. This may occur if the security of
     * the previous token had been compromised. Note that this is called when the InstanceID token
     * is initially generated so this is where you would retrieve the token.
     */
    @Override
    public void onNewToken(String token) {

    }

    public boolean retrieveTokenKey() {
        try {
            FirebaseInstanceId.getInstance().getInstanceId()
                    .addOnCompleteListener(new OnCompleteListener<InstanceIdResult>() {
                        @Override
                        public void onComplete(@NonNull Task<InstanceIdResult> task) {
                            if (!task.isSuccessful()) {
                                log.e("TAG", "getInstanceId failed -> " + task.getException());
                                return;
                            }

                            // Get new Instance ID token
                            //      String token = task.getResult().getToken();
                            tokenId = task.getResult().getToken();
                            // Log and toast
                            //  String msg = getString(R.string.msg_token_fmt, token);
                            log.e("TAG", tokenId);
                            //   Toast.makeText(contextFb, tokenId, Toast.LENGTH_SHORT).show();
                        }
                    });
        } catch (Exception e) {
            log.e("TAG", "Error in retrieveTokenKey in CustomFirebaseMessagingService");
        }
        return true;
    }

    public void setHostName(String s) {
        hostName = s;
    }

    boolean isCharsetSupported(String name) {
        return Charset.availableCharsets().keySet().contains(name);
    }
}

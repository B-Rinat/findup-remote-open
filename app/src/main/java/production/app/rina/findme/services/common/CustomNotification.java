package production.app.rina.findme.services.common;

import static android.content.Context.NOTIFICATION_SERVICE;

import android.annotation.TargetApi;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.Build;
import android.support.v4.app.NotificationCompat;
import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import org.json.JSONObject;
import production.app.rina.findme.R;
import production.app.rina.findme.activities.main.MainActivity;
import production.app.rina.findme.services.contacts.ContactManager;
import production.app.rina.findme.testing.CustomDebugLogger;
import production.app.rina.findme.utils.AppUtils;
import production.app.rina.findme.utils.JsonUtils;

public class CustomNotification {

    static final String MAIN_NOTIFICATION_CHANNEL = "main_notification_channel";

    private static boolean channelCreated = false;

    private Context context;

    private JSONObject jsonObject;

    private String type;

    public static boolean createNotificationChannel(Context context) {
        if (channelCreated) {
            return true;
        }

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            return (channelCreated = createNotificationChannelImpl(context));
        } else {
            return true;
        }
    }

    public CustomNotification(final Context context, JSONObject jsonObject) {
        this.context = context;
        this.jsonObject = jsonObject;
    }

    public void build() {
        try {
            String body = URLDecoder.decode(jsonObject.getString("body"), "UTF-8");
            Uri uri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);
            String[] meta = body.split("\\*");
            if (!meta[0].equals("meta")) {
                return;
            }
            type = meta[1];
            String toBody = createCustomNotificationBody();

            //  Intent intent = new Intent(this, MainActivity.class);
            //  intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
            //  PendingIntent pendingIntent = PendingIntent.getActivity(this, 0, intent, PendingIntent.FLAG_ONE_SHOT);
            String channelId = "Default";
            NotificationCompat.Builder builder = new NotificationCompat.Builder(context, channelId)
                    .setSmallIcon(R.mipmap.ic_launcher)
                    .setSound(uri)
                    .setContentTitle(jsonObject.getString("title"))
                    .setContentText(toBody).setAutoCancel(true)
                    .setStyle(new NotificationCompat.BigTextStyle()
                            .bigText(toBody));

            //.setContentIntent(pendingIntent);

            NotificationManager manager = (NotificationManager) context.getSystemService(NOTIFICATION_SERVICE);
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                NotificationChannel channel = new NotificationChannel(channelId, "Default channel",
                        NotificationManager.IMPORTANCE_DEFAULT);
                if (manager != null) {
                    manager.createNotificationChannel(channel);
                }
            }
            if (manager != null) {
                manager.notify(0, builder.build());
            }
        } catch (Exception e) {
            CustomDebugLogger log = new CustomDebugLogger();
            log.e(new Object() {
            }.getClass().getEnclosingMethod().getName(), "exception: " + e);
        }
    }

    private String createCustomNotificationBody() throws Exception {
        final ContactManager contactManager = new ContactManager(context);
        final StringBuilder stringBuilder = new StringBuilder();
        if (type.equals("notificationOfNewInvitation")) {
            String date[] = jsonObject.getString("time").split("\\*");
            stringBuilder.append(date[0]);
            stringBuilder.append(" | ");
            stringBuilder.append(date[1]);
            stringBuilder.append("\n");
            stringBuilder.append(context.getString(R.string.new_meeting_invitation)).append(" ");
            final String number = jsonObject.getString("fromNumber");
//            ((Activity)context).runOnUiThread(new Runnable() {
//                @Override
//                public void run() {
//                    String temp = contactManager.getNameByNumber(number);
//                    if(temp.isEmpty()){
//                        stringBuilder.append(number);
//                    } else {
//                        stringBuilder.append(temp);
//                    }
//                }
//            });
            stringBuilder.append(number);
        } else if (type.equals("notificationOfInvitationAccepted")) {
            // put something in notification body
        } else if (type.equals("notificationOfInvitationRejected")) {
            // put something in notification body
        }

        return stringBuilder.toString();
    }

    private void createNotification(JSONObject jsonBody, Context context) {
        CustomDebugLogger log = new CustomDebugLogger();
        try {
            if (AppUtils.isAppInForeground(context)) {
                // do some other stuff if app in foreground and receive notification
                return;
            }

            if (JsonUtils.getString(jsonBody, "rejected", "").equals("true")) {
                if (context != null) {
                    context.sendBroadcast(new Intent("rejected_invitation"));
                }
            }

            int notificationId = (int) JsonUtils.getLong(jsonBody, "push_id", 0);
            Intent notificationIntent = new Intent(context.getApplicationContext(), MainActivity.class);
            notificationIntent.putExtra("notificationData", jsonBody.toString());

            PendingIntent contentIntent = PendingIntent
                    .getActivity(context, notificationId, notificationIntent, PendingIntent.FLAG_CANCEL_CURRENT);

            int notificationDefaults = android.app.Notification.FLAG_AUTO_CANCEL;

            if (JsonUtils.getBoolean(jsonBody, "vibrate", true)) {
                notificationDefaults |= android.app.Notification.DEFAULT_VIBRATE;
            }
            if (JsonUtils.getBoolean(jsonBody, "sound", true) && getSoundUri() != null) {
                notificationDefaults |= android.app.Notification.DEFAULT_SOUND;
            }

            String body = URLDecoder.decode(JsonUtils.getString(jsonBody, "body", ""), "UTF-8");

            NotificationCompat.Builder mBuilder = new NotificationCompat.Builder(context, MAIN_NOTIFICATION_CHANNEL)
                    .setContentIntent(contentIntent)
                    .setContentTitle(JsonUtils.getString(jsonBody, "title", ""))
                    .setContentText(body)
                    .setSmallIcon(R.mipmap.ic_launcher)
                    .setDefaults(notificationDefaults)
                    .setAutoCancel(true);

            //NotificationManager manager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
            NotificationManager notificationManager = (NotificationManager) context
                    .getSystemService(Context.NOTIFICATION_SERVICE);
            if (notificationManager != null) {
                notificationManager.notify(notificationId, mBuilder.build());
            }

        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
            log.e("TAG", "Exception | notify_user");
        }
    }

    private Uri getSoundUri() {
        Uri sound = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);
        if (sound == null) {
            sound = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_ALARM);
        }
        if (sound == null) {
            sound = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_RINGTONE);
        }

        return sound;
    }

    @TargetApi(Build.VERSION_CODES.O)
    private static boolean createNotificationChannelImpl(Context context) {
        int stringId = context.getResources().getIdentifier("notifications", "string", context.getPackageName());
        if (stringId == 0) {
            CustomDebugLogger log = new CustomDebugLogger();
            stringId = context.getResources().getIdentifier("notifications", "id", context.getPackageName());
            log.e("TAG", "id::: " + stringId);
        }

        NotificationChannel channel = new NotificationChannel(MAIN_NOTIFICATION_CHANNEL,
                context.getResources().getString(stringId), NotificationManager.IMPORTANCE_DEFAULT);
        channel.enableVibration(true);
        channel.setShowBadge(true);
        channel.setLockscreenVisibility(NotificationCompat.VISIBILITY_PUBLIC);

        NotificationManager nm = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
        if (nm != null) {
            nm.createNotificationChannel(channel);
            return true;
        }
        return false;
    }

}

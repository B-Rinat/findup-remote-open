package production.app.rina.findme.services.network;

import android.content.Context;
import production.app.rina.findme.R;
import production.app.rina.findme.services.common.DateTime;

public class Push {

    private Context context;

    private String participantToken;

    private DateTime time;

    public Push(Context context, final String participantToken) {
        time = new DateTime();
        this.context = context;
        this.participantToken = participantToken;
    }

    public void sendInvitation() {
        try {
            CustomFirebaseMessagingService.tokenOfGuest = participantToken;
            sendNotificationMessage("notificationOfNewInvitation");
        } catch (Exception e) {

        }
    }

    public void sendInvitationAccepted() {
        try {
            CustomFirebaseMessagingService.tokenOfGuest = participantToken;
            sendNotificationMessage("notificationOfInvitationAccepted");
        } catch (Exception e) {

        }
    }

    public void sendInvitationRejected() {
        try {
            CustomFirebaseMessagingService.tokenOfGuest = participantToken;
            sendNotificationMessage("notificationOfInvitationRejected");
        } catch (Exception e) {

        }
    }

    private void sendNotificationMessage(String parameter) {
        String extraData[] = new String[3];
        if (parameter.equals("notificationOfNewInvitation")) {
            extraData[0] = "meta" + "*" + "notificationOfNewInvitation"; // metadata, body
            extraData[1] = context.getResources().getString(R.string.new_invitation); // title
            extraData[2] = time.getDate() + "*" + time.getTime(); // time of sending invitation
        } else if (parameter.equals("notificationOfInvitationAccepted")) {
            extraData[0] = "meta" + "*" + "notificationOfInvitationAccepted"; // metadata, body
            extraData[1] = context.getResources().getString(R.string.invitation_accepted); // title
            extraData[2] = time.getDate() + "*" + time.getTime(); // time of sending invitation
        } else if (parameter.equals("notificationOfInvitationRejected")) {
            extraData[0] = "meta" + "*" + "notificationOfInvitationRejected"; // metadata, body
            extraData[1] = context.getResources().getString(R.string.invitation_rejected); // title
            extraData[2] = time.getDate() + "*" + time.getTime(); // time of sending invitation
        }
        CustomFirebaseMessagingService.sendNotification(participantToken, extraData);
    }

}

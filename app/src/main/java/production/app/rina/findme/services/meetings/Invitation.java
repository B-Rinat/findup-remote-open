package production.app.rina.findme.services.meetings;

import android.content.Context;
import java.util.ArrayList;
import production.app.rina.findme.R;
import production.app.rina.findme.services.common.DateTime;
import production.app.rina.findme.services.network.DatabaseObjectManager;
import production.app.rina.findme.services.network.Push;
import production.app.rina.findme.testing.CustomDebugLogger;

public class Invitation {

    public String meetingHash;

    public Participant participant;

    public String serverId;

    public String serverName;

    public DateTime time;

    private transient Context context;

    // DEBUG
    private transient CustomDebugLogger log;

    private String status;

    public Invitation(Context context) {
        log = new CustomDebugLogger();
        this.context = context;
        this.time = new DateTime();
        this.serverId = "";
        this.serverName = "";
    }

    /**
     * Need to attach at least one Participant with following
     * parameters to invitation to be valid invitation
     */
    public void attachParticipant(String name, String number, String date, String time, boolean isMeetingCreator) {
        attachParticipant(new Participant(context, name, number, isMeetingCreator));
        try {
            this.time.setInvitationTime(date, time);
        } catch (Exception e) {
            CustomDebugLogger log = new CustomDebugLogger();
            log.e(new Object() {
            }.getClass().getEnclosingMethod().getName(), "exception: " + e);
        }
    }

    /**
     * @param name should follow this format: initiator's [ name * number * date * time * meeting hash]
     */
    public void createInvitationOnServer(String name) {
        this.serverName = name;
        DatabaseObjectManager manager = new DatabaseObjectManager();
        manager.createObject("3626", name, null, null);
        this.serverId = manager.getObjectIdCreated();
        log.e("createInvitationOnSer..",
                "Created object with name {" + serverName + "}" + " and id " + "{" + serverId + "}");
    }

    public String getInvitationTime() {
        return time.getInvitationTime();
    }

    public void onAcceptInvitationNotif() {
        ArrayList<String> keys = new ArrayList<>();
        ArrayList<String> values = new ArrayList<>();
        keys.add(context.getString(R.string.INVITATION_STATUS));
        values.add(context.getString(R.string.STATUS_INV_ACCEPTED));
        boolean result = updateInvitationOnServer(keys, values);
        if (result) {
            participant.requestDataServer();
            String token = participant.getPushToken();
            Push push = new Push(context, token);
            push.sendInvitationAccepted();
        }
    }

    public void onRejectInvitationNotif() {
        ArrayList<String> keys = new ArrayList<>();
        ArrayList<String> values = new ArrayList<>();
        keys.add(context.getString(R.string.INVITATION_STATUS));
        keys.add(context.getString(R.string.INVITATION_PARTICIPANT));
        values.add(context.getString(R.string.STATUS_INV_REJECTED));
        values.add("");
        boolean result = updateInvitationOnServer(keys, values);
        if (result) {
            participant.requestDataServer();
            String token = participant.getPushToken();
            Push push = new Push(context, token);
            push.sendInvitationRejected();
        }
    }

    public String toString() {
        return "Participant info: \n"
                + participant.toString() + "\n"
                + "Meeting info: " + "\n"
                + "Time info: " + time.toString() + "\n";
    }

    public void updateInvitationNameOnServer(ArrayList<String> keys, ArrayList<String> values) {
        if (!serverName.isEmpty() && !serverId.isEmpty()) {
            DatabaseObjectManager manager = new DatabaseObjectManager();
            manager.updateObject(serverId, serverName, keys, values);
            serverName = values.get(0);
        }
    }

    public boolean updateInvitationOnServer(ArrayList<String> keys, ArrayList<String> values) {
        if (!serverName.isEmpty() && !serverId.isEmpty()) {
            DatabaseObjectManager manager = new DatabaseObjectManager();
            manager.updateObject(serverId, serverName, keys, values);
            CustomDebugLogger log = new CustomDebugLogger();
            log.e("updateInvitationOnSer..", "parameters: "
                    + "serverId: [" + serverId + "]"
                    + "serverName: [" + serverName + "]"
                    + " keys: [" + keys + "]"
                    + " values: " + "[" + values + "]");
            return true;
        } else {
            return false;
        }
    }

    private void attachParticipant(Participant participant) {
        this.participant = participant;
    }

}

package production.app.rina.findme.services.meetings;

import static production.app.rina.findme.services.common.AppPreferences.getUniqueUserId;
import static production.app.rina.findme.services.common.AppPreferences.getUserPhone;

import android.content.Context;
import com.pixplicity.easyprefs.library.Prefs;
import java.util.ArrayList;
import production.app.rina.findme.R;
import production.app.rina.findme.services.common.AppPreferences;
import production.app.rina.findme.services.common.DateTime;
import production.app.rina.findme.services.common.Randomizer;
import production.app.rina.findme.services.network.DatabaseObjectManager;
import production.app.rina.findme.services.network.Push;
import production.app.rina.findme.testing.CustomDebugLogger;

public class MeetingManager {

    public String guestName;

    public String guestNumber;

    public Meeting meeting;

    public String meetingHash;

    private transient Context context;

    private transient CustomDebugLogger log;

    public MeetingManager(Context context) {
        log = new CustomDebugLogger();
        this.meetingHash = "";
        this.context = context;
        this.meeting = new Meeting(context, new Invitation(context));
    }

    public MeetingManager(Context context, Meeting meeting) {
        log = new CustomDebugLogger();
        this.meetingHash = "";
        this.context = context;
        this.meeting = meeting;
    }

    /**
     * Before creating new meeting ensure that network
     * is available
     */
    public void createMeeting(String guestName, String guestNumber) {
        final MeetingCache cached = new MeetingCache();
        String userNumber = getUserPhone();
        DateTime time = new DateTime();
        Randomizer rand = new Randomizer();
        this.meetingHash = rand.getRandomString(30);
        log.e("TAG", "random meeting hash generated: " + this.meetingHash);
        this.guestName = guestName;
        this.guestNumber = guestNumber;
        // Call to get local copy of meeting metadata
        cached.fetchMeetingCache();

        // Update hash meetingHash
        updateMeetingHash(userNumber, this.meetingHash);

        // First check if we have one meeting metadata cached locally
        if (!cached.data.isEmpty) {
            // use cached metadata
            updateValuesServer(userNumber, guestName, guestNumber, time, cached);
            log.e("TAG", new Exception().getStackTrace()[0] + "");
            log.e("TAG", "use cached metadata");

        } else {
            log.e("TAG", new Exception().getStackTrace()[0] + "");
            // Check if meeting and invitation objects were created in Database
            // if yes, retrieve that data
            cached.fetchMeetingMetadataServer(userNumber);

            log.e("TAG", new Exception().getStackTrace()[0] + "");
            if (!cached.data.isEmpty) {
                log.e("TAG", new Exception().getStackTrace()[0] + "");
                // use cached metadata
                updateValuesServer(userNumber, guestName, guestNumber, time, cached);
                log.e("TAG", new Exception().getStackTrace()[0] + "");

            } else {

                log.e("TAG", new Exception().getStackTrace()[0] + "");
                // create new invitation and meeting on server first
                // then store metadata locally
                meeting.createInvitationOnServer(
                        "DEFAULT"
                                + "*"
                                + userNumber
                                + "*"
                                + time.getDate()
                                + "*"
                                + time.getTime() + "*" + this.meetingHash);
                meeting.createMeetingOnServer("meet" + getUniqueUserId());

                meeting.listInvitation.get(0)
                        .attachParticipant(guestName, guestNumber, time.getDate(), time.getTime(), true);
                meeting.listInvitation.get(0).participant.requestDataServer();

                // Update meeting by attaching invitation to it in database
                ArrayList<String> keysMeeting = new ArrayList<>();
                ArrayList<String> valuesMeeting = new ArrayList<>();
                keysMeeting.add(context.getResources().getString(R.string.MEETING_INVITATION));
                valuesMeeting.add(meeting.listInvitation.get(0).serverName);
                meeting.updateMeetingOnServer(keysMeeting, valuesMeeting);

                // Request meeting and invitation metadata from server and save locally
                cached.fetchMeetingMetadataServer(userNumber);

                if (!cached.data.isEmpty) {
                    // Update invitation data in database
                    ArrayList<String> keysInvitation = new ArrayList<>();
                    ArrayList<String> valuesInvitation = new ArrayList<>();
                    keysInvitation.add(context.getResources().getString(R.string.INVITATION_PARTICIPANT));
                    keysInvitation.add(context.getResources().getString(R.string.INVITATION_STATUS));
                    valuesInvitation.add(meeting.listInvitation.get(0).participant.getServerId());
                    valuesInvitation.add(context.getResources().getString(R.string.STATUS_INV_QUICK));

                    meeting.listInvitation.get(0).serverId = cached.data.invitationId;
                    meeting.listInvitation.get(0).serverName = cached.data.invitationName;
                    meeting.listInvitation.get(0).updateInvitationOnServer(keysInvitation, valuesInvitation);

                    new Thread(new Runnable() {
                        @Override
                        public void run() {
                            cached.cacheMetadata(meeting.id, meeting.name, meeting.listInvitation.get(0).serverId,
                                    meeting.listInvitation.get(0).serverName);
                        }
                    }).start();

                    Push push = new Push(context, meeting.listInvitation.get(0).participant.getPushToken());
                    push.sendInvitation();
                }
            }
        }
    }

    public void finishMeeting() {
        meeting.finishMeeting();
    }

    public void setContext(Context context) {
        this.context = context;
        if (this.meeting != null) {
            this.meeting.setContext(context);
        }
    }

    public void updateMeetingHash(String userNumber, String hash) {
        this.meetingHash = hash;
        DatabaseObjectManager manager = new DatabaseObjectManager();
        ArrayList<String> keys = new ArrayList<>();
        ArrayList<String> values = new ArrayList<>();
        keys.add(context.getString(R.string.MEETING_HASH));
        values.add(hash);
        manager.updateObject(getUniqueUserId(), userNumber, keys, values);
    }

    private void updateValuesServer(String userNumber, String guestName, String guestNumber, DateTime time,
            final MeetingCache cached) {
        log.e("TAG", "updateValuesServer");
        ArrayList<String> keys = new ArrayList<>();
        ArrayList<String> values = new ArrayList<>();

        meeting.listInvitation.get(0).attachParticipant(guestName, guestNumber, time.getDate(), time.getTime(), true);
        meeting.listInvitation.get(0).participant.requestDataServer();

        keys.add(context.getResources().getString(R.string.INVITATION_PARTICIPANT));
        keys.add(context.getResources().getString(R.string.INVITATION_STATUS));
        values.add(meeting.listInvitation.get(0).participant.getServerId());
        values.add(context.getResources().getString(R.string.STATUS_INV_QUICK));

        meeting.listInvitation.get(0).serverId = cached.data.invitationId;
        meeting.listInvitation.get(0).serverName = cached.data.invitationName;
        meeting.updateInvitationOnServer(0, keys, values);

        keys = new ArrayList<>();
        values = new ArrayList<>();

        keys.add(context.getResources().getString(R.string.MEETING_INVITATION));
        values.add("DEFAULT"
                + "*"
                + userNumber
                + "*"
                + time.getDate()
                + "*"
                + time.getTime()
                + "*"
                + this.meetingHash);
        meeting.listInvitation.get(0).updateInvitationNameOnServer(keys, values);

        new Thread(new Runnable() {
            @Override
            public void run() {
                cached.cacheMetadata("", "", "", meeting.listInvitation.get(0).serverName);
            }
        }).start();

        Push push = new Push(context, meeting.listInvitation.get(0).participant.getPushToken());
        push.sendInvitation();
    }

}

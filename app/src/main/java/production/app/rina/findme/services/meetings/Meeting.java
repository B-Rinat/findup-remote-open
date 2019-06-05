package production.app.rina.findme.services.meetings;

import android.content.Context;
import java.util.ArrayList;
import production.app.rina.findme.R;
import production.app.rina.findme.services.network.DatabaseObjectManager;
import production.app.rina.findme.testing.CustomDebugLogger;

public class Meeting {

    public String id;

    public ArrayList<Invitation> listInvitation;

    public String name;

    private transient Context context;

    private String hash;

    private transient CustomDebugLogger log;

    public Meeting(Context context, Invitation invitation) {
        log = new CustomDebugLogger();
        this.context = context;
        name = "";
        id = "";
        hash = "";
        listInvitation = new ArrayList<>();
        listInvitation.add(invitation);
    }

    /**
     * Only used once per Application to create unique database object
     * on server side
     *
     * @param name name that identify this object in database
     */
    public void createInvitationOnServer(String name) {
        log.e("TAG", "createInvitationOnServer");
        Invitation invitation = listInvitation.get(0);
        invitation.createInvitationOnServer(name);
        listInvitation.add(0, invitation);
    }

    /**
     * Only used once per Application to create unique database object
     * on server side
     *
     * @param name name that identify this object in database
     */
    public void createMeetingOnServer(String name) {
        this.name = name;
        DatabaseObjectManager manager = new DatabaseObjectManager();
        manager.createObject(context.getString(R.string.MEETING), name, null, null);
        this.id = manager.getObjectIdCreated();
    }

    public void finishMeeting() {
        ArrayList<String> keys = new ArrayList<>();
        ArrayList<String> values = new ArrayList<>();
        keys.add(context.getString(R.string.INVITATION_STATUS));
        keys.add(context.getString(R.string.INVITATION_PARTICIPANT));
        values.add(context.getString(R.string.STATUS_INV_REJECTED));
        values.add("");
        updateInvitationOnServer(0, keys, values);
    }

    public void setContext(Context context) {
        this.context = context;
    }

    /**
     * Updates invitation information
     *
     * @param invitationIndex index at which invitation is located
     * @param keys            database keys to which values are assigned
     * @param values          values which are assigned to each key accordingly and data to be changed
     *                        in database
     */
    public void updateInvitationOnServer(int invitationIndex, ArrayList<String> keys, ArrayList<String> values) {
        Invitation invitation = listInvitation.get(invitationIndex);
        invitation.updateInvitationOnServer(keys, values);
        listInvitation.set(invitationIndex, invitation);
    }

    /**
     * Updates meeting information
     *
     * @param keys   database keys to which values are assigned
     * @param values values which are assigned to each key accordingly and data to be changed
     *               in database
     */
    public void updateMeetingOnServer(ArrayList<String> keys, ArrayList<String> values) {
        DatabaseObjectManager manager = new DatabaseObjectManager();
        manager.updateObject(this.id, this.name, keys, values);
    }
}

package production.app.rina.findme.services.meetings;


import static production.app.rina.findme.services.common.AppPreferences.getUserPhone;

import android.content.Context;
import com.pixplicity.easyprefs.library.Prefs;
import java.util.ArrayList;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import production.app.rina.findme.R;
import production.app.rina.findme.services.common.AppPreferences;
import production.app.rina.findme.services.network.ReportPerformer;
import production.app.rina.findme.testing.CustomDebugLogger;

public class InvitationList {

    private Context context;

    private transient CustomDebugLogger log;

    private String userNumber;

    public InvitationList(final Context context) {
        this.context = context;
        log = new CustomDebugLogger();
    }

    public ArrayList<Invitation> getAllInvitations() {
        ArrayList<Invitation> invitations = new ArrayList<>();
        ArrayList<String> keys = new ArrayList<>();
        ArrayList<String> values = new ArrayList<>();
        keys.add("phone");
        keys.add("status");
        userNumber = getUserPhone();
        values.add(userNumber);
        values.add("Quick");
        ReportPerformer report = new ReportPerformer(context.getString(R.string.REPORT_INVITATIONS), keys, values,
                null);
        ArrayList<String> invitationsJson = report.execute();
        if (invitationsJson.size() > 0) {
            invitations = parseInvitationJson(invitationsJson.get(0));
        }
        return invitations;
    }

    private ArrayList<Invitation> parseInvitationJson(String json) {
        ArrayList<Invitation> parsedInvitations = new ArrayList<>();
        try {
            JSONObject inv = new JSONObject(json);
            JSONArray phone = inv.getJSONArray("phone");
            JSONArray status = inv.getJSONArray("status");
            JSONArray invitationId = inv.getJSONArray("invitationId");
            JSONArray invitationName = inv.getJSONArray("invitationName");
            JSONArray participant = inv.getJSONArray("participant");

            for (int i = 0; i < phone.length(); i++) {
                if (userNumber.equals(participant.getString(i))) {
                    if (status.getString(i).equals("Quick")) {
                        Invitation invitation = new Invitation(context);
                        invitation.serverId = invitationId.getString(i);
                        invitation.serverName = invitationName.getString(i);
                        String a[] = invitationName.getString(i).split("\\*");
                        log.e(new Object() {
                                }.getClass().getEnclosingMethod().getName(),
                                "parameters: " + a[0] + " @ " + a[1] + " @ " + a[2] + " @ " + a[3]);
                        // [0]->name of inviter, [1]->number of inviter, [2]->date of invitation creation, [3]->time of invitation creation
                        invitation.attachParticipant(a[0], a[1], a[2], a[3], false);
                        invitation.meetingHash = a[4];
                        parsedInvitations.add(invitation);
                    }
                }
            }

        } catch (JSONException e) {
            e.printStackTrace();
            log.e(new Object() {
            }.getClass().getEnclosingMethod().getName(), "exception: " + e);
        }
        return parsedInvitations;
    }

}

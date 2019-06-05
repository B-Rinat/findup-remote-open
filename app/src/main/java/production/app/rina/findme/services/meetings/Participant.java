package production.app.rina.findme.services.meetings;

import android.content.Context;
import java.util.ArrayList;
import production.app.rina.findme.R;
import production.app.rina.findme.services.contacts.ContactPhone;
import production.app.rina.findme.services.network.ReportPerformer;
import production.app.rina.findme.testing.CustomDebugLogger;

public class Participant {

    public Boolean hasMeeting;

    private transient Context context;

    private Boolean isMeetingCreator;

    private transient CustomDebugLogger log;

    private String name;

    private String number;

    private String pushToken;

    private String serverId;

    public Participant(final Context context, final String name, final String number,
            final Boolean isMeetingCreator) {
        log = new CustomDebugLogger();
        this.context = context;
        this.name = name;
        this.number = number;
        this.isMeetingCreator = isMeetingCreator;
    }

    public String getDigitsNumber(String number) {
        ContactPhone contactPhone = new ContactPhone("", "");
        return contactPhone.getDigitsFromNumber(number);
    }

    public String getName() {
        return name;
    }

    public String getNumber() {
        if (number == null) {
            return "";
        } else {
            return number;
        }
    }

    public String getPushToken() {
        return pushToken;
    }

    public String getServerId() {
        return serverId;
    }

    public void requestDataServer() {
        ArrayList<String> keys = new ArrayList<>();
        keys.add("phone");
        ArrayList<String> values = new ArrayList<>();
        values.add(getDigitsNumber(this.number));
        ArrayList<String> jsonKeys = new ArrayList<>();
        jsonKeys.add("user");
        jsonKeys.add("pushToken");
        // jsonKeys.add("isBusy");

        ReportPerformer report = new ReportPerformer(context.getString(R.string.REPORT_PARTICIPANT_INFO), keys,
                values, jsonKeys);
        ArrayList<String> results = report.execute();
        if (!results.isEmpty()) {
            this.serverId = results.get(0);
            this.pushToken = results.get(1);
        }
    }

    public String toString() {
        return "[" + serverId + "]" + " " + name + " " + "(" + number + ")" + "\n" + "<" + pushToken + ">";
    }
}

package production.app.rina.findme.services.common;

import android.annotation.SuppressLint;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import production.app.rina.findme.testing.CustomDebugLogger;

public class DateTime {

    CustomDebugLogger log;

    private String invitationTime;

    public DateTime() {
        this.log = new CustomDebugLogger();
    }

    public String getDate() {
        @SuppressLint("SimpleDateFormat") String date = new SimpleDateFormat("dd-MM-yyyy")
                .format(Calendar.getInstance().getTime());
        return date;
    }

    public String getInvitationTime() {
        return invitationTime;
    }

    public void setInvitationTime(String invitationTime) {
        this.invitationTime = invitationTime;
    }

    public String getTime() {
        @SuppressLint("SimpleDateFormat") String date = new SimpleDateFormat("HH:mm:ss")
                .format(Calendar.getInstance().getTime());
        return date;
    }

    public String getToday() {
        @SuppressLint("SimpleDateFormat") String date = new SimpleDateFormat("dd-MM-yyyy\nHH:mm:ss")
                .format(Calendar.getInstance().getTime());
        return date;
    }

    public void setInvitationTime(String date, String time) {
        log.e(new Object() {
        }.getClass().getEnclosingMethod().getName(), "parameters: " + date + " @ " + time);
        this.invitationTime = date + "\n" + time;
    }

    public String toString() {
        return "Time: " + invitationTime;
    }


}

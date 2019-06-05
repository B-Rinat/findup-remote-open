package production.app.rina.findme.activities.main;

import android.support.multidex.MultiDexApplication;
import production.app.rina.findme.services.common.AppPreferences;
import production.app.rina.findme.services.common.CustomNotification;
import production.app.rina.findme.testing.CustomDebugLogger;

public class FindMeApplication extends MultiDexApplication {

    public static CustomDebugLogger log;

    @Override
    public void onCreate() {
        super.onCreate();
        // Initialize the Prefs class
        AppPreferences.init(this);
        //create notification channel
        CustomNotification.createNotificationChannel(this);
        // Debugging tool
        log = new CustomDebugLogger();
    }
}
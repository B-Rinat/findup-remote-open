package production.app.rina.findme.services.location;

import android.Manifest;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.FragmentActivity;
import android.support.v4.content.ContextCompat;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.TextView;
import android.widget.Toast;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import java.util.LinkedHashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;
import production.app.rina.findme.R;
import production.app.rina.findme.activities.contacts.ContactsHolderMain;
import production.app.rina.findme.activities.message.MessageActivity;
import production.app.rina.findme.services.common.AppPreferences;
import production.app.rina.findme.services.common.OrderedStringSet;
import production.app.rina.findme.services.meetings.MeetingManager;
import production.app.rina.findme.services.network.CustomFirebaseMessagingService;
import production.app.rina.findme.testing.CustomDebugLogger;
import production.app.rina.findme.utils.AppUtils;
import production.app.rina.findme.utils.MessagesStoragePref;

public class LocateUsersOnMapActivity extends FragmentActivity implements OnMapReadyCallback {

    CustomFirebaseMessagingService fb;

    transient CustomDebugLogger log;

    final int myPermissionsRequestAccessFineLocation = 4;

    String otherUserOnline;

    private CustomLocation customLocation;

    private GoogleMap googleMap;

    private Boolean isRejectedInvitation;

    public BroadcastReceiver broadcastReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            String a = intent.getAction();
            log.e("TAG", "Intent received: " + a);
            if (a != null && a.equals("rejected_invitation")) {
                isRejectedInvitation = true;
            }
        }
    };

    private MeetingManager manager;

    private boolean playNotificationSoundOneAttempt = true;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        log = new CustomDebugLogger();
        fb = new CustomFirebaseMessagingService();
        initFinalStrings();
        isRejectedInvitation = false;
        setContentView(R.layout.activity_locate_users_on_map);

        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);

        log.e("TAG", "onCreate | LocateUsersOnMapActivity");

        String i = getIntent().getStringExtra("thread_meeting_created");
        log.e("TAG", "coming Intent Extra | " + i);
        if (i != null && !i.isEmpty()) {
            OrderedStringSet<MeetingManager> set = new OrderedStringSet<>();
            List<String> list = new LinkedList<>();
            list.add(i);
            Set<String> orderedSet = new LinkedHashSet<>(list);
            manager = set.fromOrderedStringSet(orderedSet, MeetingManager.class).get(0);
            manager.setContext(this);
            log.e("TAG", "meeting hash | onCreate | " + manager.meetingHash);
            customLocation = new CustomLocation(this, manager);
            customLocation.prepare();
            customLocation.onOffBottomPopup(5);
            if (this.googleMap != null) {
                customLocation.startLocationUpdates(this.googleMap);
            }
        } else {
            customLocation = new CustomLocation(this, null);
            customLocation.prepare();
            customLocation.onOffBottomPopup(5);
        }

        final TextView finish = findViewById(R.id.button_meeting_finished);
        finish.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(final View v) {
                finish.setClickable(false);
                finish.setBackgroundColor(getResources().getColor(R.color.buttonActive));
                meetingFinished();
            }
        });
    }

    @Override
    protected void onStart() {
        super.onStart();
        // List to store location points to draw a path between markers
        MessagesStoragePref.init(getApplicationContext());
    }

    @Override
    protected void onResume() {
        super.onResume();
        registerReceiver(broadcastReceiver, new IntentFilter("rejected_invitation"));
        if (googleMap != null) {
            customLocation.startLocationUpdates(googleMap);
        }
    }


    @Override
    protected void onPause() {
        try {
            unregisterReceiver(broadcastReceiver);
        } catch (Exception e) {
            e.printStackTrace();
            log.e("TAG", "onPause | exception: " + e);
        }

        customLocation.stopLocationUpdate();

        MessagesStoragePref.init(getApplicationContext());
        MessagesStoragePref.deleteAll();
        super.onPause();
        log.e("TAG", "onPause, done");
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        log.e("TAG", "onDestroy | LocateUsersOnMapActivity");
    }

    // Used to initialize every final string for UI interaction in app, need to always
    // init onCreate() to be accessible within whole activity lifecycle
    public void initFinalStrings() {
        otherUserOnline = getString(R.string.user_online);
    }

    public void meetingFinished() {
        this.onPause();
        /**
         * TODO first check if internet available,
         * if available then call MeetingManager.finishMeeting()
         * if not store current state and possibly quit map
         * after internet become available and before next meeting restore state and call
         * MeetingManager.finishMeeting()
         */
        AppPreferences.deleteMeetingCache();
        if (AppUtils.isNetworkAvailable(this)) {
            new Thread(new Runnable() {
                @Override
                public void run() {
                    if (manager != null) {
                        manager.finishMeeting();
                        finish();
                    }
                }
            }).start();
            Intent intent = new Intent(this, ContactsHolderMain.class);
            startActivity(intent);
        } else {
            Toast.makeText(this, getString(R.string.check_internet_con), Toast.LENGTH_SHORT).show();
        }
    }

    public void messaging(View v) {
        startActivity(new Intent(this, MessageActivity.class));
//        overridePendingTransition(R.anim.slide_down, R.anim.no_animation);
    }

    /* Block back button while have a meeting with guest*/
    @Override
    public void onBackPressed() {

    }

    /**
     * Manipulates the map once available.
     * This callback is triggered when the map is ready to be used.
     * This is where we can add markers or lines, add listeners or move the camera. In this case,
     * we just add a marker near Sydney, Australia.
     * If Google Play services is not installed on the device, the user will be prompted to install
     * it inside the SupportMapFragment. This method will only be triggered once the user has
     * installed Google Play services and returned to the app.
     */
    @Override
    public void onMapReady(final GoogleMap googleMap) {
        log.e("TAG", "onMapReady");
        this.googleMap = googleMap;
        customLocation.startLocationUpdates(googleMap);
    }

    public void refreshMap(View v) {
        Intent intent = new Intent(this, LocateUsersOnMapActivity.class);
        startActivity(intent);
    }

    public int requestSelfPermissionsLocation() {
        if (ContextCompat.checkSelfPermission(this,
                Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION},
                    myPermissionsRequestAccessFineLocation);
            return 404;
        }
        return 100;
    }


}

package production.app.rina.findme.services.location;

import static production.app.rina.findme.services.common.AppPreferences.getIdOfLocation;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.Context;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.location.Location;
import android.os.Build;
import android.os.Handler;
import android.os.Looper;
import android.support.annotation.NonNull;
import android.support.v4.app.FragmentActivity;
import android.support.v4.content.ContextCompat;
import android.view.View;
import android.widget.TextView;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.pixplicity.easyprefs.library.Prefs;
import java.util.ArrayList;
import production.app.rina.findme.R;
import production.app.rina.findme.services.common.AppPreferences;
import production.app.rina.findme.services.meetings.MeetingManager;
import production.app.rina.findme.services.network.DatabaseObjectManager;
import production.app.rina.findme.services.network.ReportPerformer;
import production.app.rina.findme.testing.CustomDebugLogger;

public class CustomLocation {

    MeetingManager meetingManager;

    private Context context; // Context should be FragmentActivity

    private FusedLocationProviderClient customFusedLocationClient;

    private LocationRequest customLocationRequest;

    private GoogleMap googleMap;

    private LatLng latLng;

    private Location location;

    private CustomDebugLogger log;

    private Marker marker, guestMarker;

    private boolean needZoomLevelFirstTime;

    private float zoomLevel;

    private LocationCallback customLocationCallback = new LocationCallback() {
        @Override
        public void onLocationResult(LocationResult locationResult) {
            log.e("TAG", "customLocationCallback | locationResult | " + locationResult);
            if (locationResult != null) {
                new Thread(new Runnable() {
                    @Override
                    public void run() {
                        // Get my location, send it to server, update my marker on map
                        getParticipantLocation();
                        sendParticipantLocationServer();
                        // Get guest location, update guest marker on map
                        final LatLng ll = getParticipantLocationServer();

                        // update markers on map
                        ((FragmentActivity) context).runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                updateInitiatorMarkerOnMap();
                                updateGuestMarkerOnMap(ll);
                            }
                        });
                    }
                }).start();
            }
        }
    };

    public CustomLocation(Context context, MeetingManager meetingManager) {
        log = new CustomDebugLogger();
        this.context = context;
        this.meetingManager = meetingManager;
    }

    public LatLng getParticipantLocationServer() {
        if (meetingManager == null) {
            return null;
        }
        String meetingHash = meetingManager.meetingHash;
        log.e("TAG", ":meetingHash: " + meetingHash);
        if (meetingHash != null && meetingHash.length() > 5) {
            ArrayList<String> keys = new ArrayList<>();
            ArrayList<String> values = new ArrayList<>();
            ArrayList<String> jsonKey = new ArrayList<>();
            keys.add("phone");
            keys.add("hash");
            values.add(meetingManager.guestNumber);
            values.add(meetingHash);
            jsonKey.add("latitude");
            jsonKey.add("longitude");

            ReportPerformer report = new ReportPerformer(context.getString(R.string.REPORT_USER_LOCATION), keys,
                    values, jsonKey);
            ArrayList<String> response = report.execute();
            if (response.isEmpty()) {
                return null;
            }
            return new LatLng(Double.parseDouble(response.get(0)), Double.parseDouble(response.get(1)));
        }
        return null;
    }

    public void onOffBottomPopup(int i) {
        TextView tv = ((FragmentActivity) context).findViewById(R.id.text_gps_notif);
        if (i == 0) {
            tv.setVisibility(View.INVISIBLE);
            tv.setText("");
        } else {
            tv.setVisibility(View.VISIBLE);
            tv.setText(context.getString(R.string.receiving_gps_data));
        }
    }

    /**
     * Call inside onCreate
     */
    public void prepare() {
        needZoomLevelFirstTime = true;
        zoomLevel = 15.0f;
        customLocationRequest = LocationRequest.create();
        customLocationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
        customLocationRequest.setInterval(8000);
        customLocationRequest.setFastestInterval(4000);
        customFusedLocationClient = LocationServices.getFusedLocationProviderClient(context);
    }

    public void sendParticipantLocationServer() {
        if (latLng != null) {
            DatabaseObjectManager manager = new DatabaseObjectManager();
            ArrayList<String> keys = new ArrayList<>();
            ArrayList<String> values = new ArrayList<>();
            keys.add(context.getString(R.string.LOCATION_LATITUDE));
            keys.add(context.getString(R.string.LOCATION_LONGITUDE));
            /**
             * TODO need to cipher LatLng before sending to server
             */
            values.add(Double.toString(latLng.latitude));
            values.add(Double.toString(latLng.longitude));
            manager.updateObject(getIdOfLocation(), "place", keys, values);
        }
    }

    public void showCustomToastNotification() {
        final TextView tv = ((FragmentActivity) context).findViewById(R.id.text_custom_notif_map);
        tv.setTextColor(Color.GREEN);
        tv.setText(context.getString(R.string.user_online));
        final Handler handler = new Handler();
        handler.postDelayed(new Runnable() {
            @Override
            public void run() {
                tv.setText("");
            }
        }, 5000);
    }

    /**
     * Call when map ready, onMapReady()
     */
    public void startLocationUpdates(GoogleMap googleMap) {
        log.e("TAG", "startLocationUpdates");
        this.googleMap = googleMap;
        if (android.os.Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (ContextCompat.checkSelfPermission(context,
                    Manifest.permission.ACCESS_FINE_LOCATION)
                    == PackageManager.PERMISSION_GRANTED) {
                //Location Permission already granted
                log.e("TAG", "Location Permission already granted");
                customFusedLocationClient
                        .requestLocationUpdates(customLocationRequest, customLocationCallback, Looper.myLooper());
            } else {
                log.e("TAG", "Request Location Permission");
                //TODO Request Location Permission
            }
        } else {
            customFusedLocationClient
                    .requestLocationUpdates(customLocationRequest, customLocationCallback, Looper.myLooper());
        }
    }

    /**
     * Usually onPause()
     */
    public void stopLocationUpdate() {
        customFusedLocationClient.removeLocationUpdates(customLocationCallback);
        // To make user offline for another user, send [0,0] latitude,longitude to server
        latLng = new LatLng(0, 0);
        sendParticipantLocationServer();
    }

    @SuppressLint("MissingPermission")
    private void getParticipantLocation() {
        try {
            customFusedLocationClient.getLastLocation()
                    .addOnCompleteListener(new OnCompleteListener<Location>() {
                        @Override
                        public void onComplete(@NonNull Task<Location> task) {
                            if (task.getResult() != null && task.isSuccessful()) {
                                location = task.getResult();
                                if (location != null) {
                                    latLng = new LatLng(location.getLatitude(), location.getLongitude());
                                    onOffBottomPopup(0);
                                }
                            }
                        }
                    });
        } catch (Exception e) {

        }
    }

    private void updateGuestMarkerOnMap(LatLng latLngLocal) {
        log.e("TAG", "updateGuestMarkerOnMap");
        TextView guestOff = ((FragmentActivity) context).findViewById(R.id.participant_on_off_notif);
        try {
            if (latLngLocal.latitude == 0 && latLngLocal.longitude == 0) {
                log.e("TAG", "updateGuestMarkerOnMap | latLngLocal is null");
                guestOff.setText(context.getString(R.string.offline));
                return;
            }
        } catch (Exception e) {
            log.e("TAG", "updateGuestMarkerOnMap | latLngLocal is null");
            guestOff.setText(context.getString(R.string.offline));
            return;
        }
        guestOff.setText(context.getString(R.string.online));
        if (guestMarker != null) {
            log.e("TAG", "updateGuestMarkerOnMap | guestMarker not null");
            guestMarker.remove();
        } else {
            showCustomToastNotification();
        }
        log.e("TAG", "lat: " + latLngLocal.latitude + " long: " + latLngLocal.longitude);
        guestMarker = googleMap.addMarker(new MarkerOptions().position(latLngLocal).title("Участник")
                .icon(BitmapDescriptorFactory.fromResource(R.drawable.guest_on_map)));
    }

    private void updateInitiatorMarkerOnMap() {
        log.e("TAG", "updateInitiatorMarkerOnMap");
        TextView guestOff = ((FragmentActivity) context).findViewById(R.id.text_user_on_off_notif);
        if (latLng == null) {
            guestOff.setText(context.getString(R.string.offline));
            return;
        }
        guestOff.setText(context.getString(R.string.online));
        log.e("TAG", "latLong: " + latLng.latitude + " | " + latLng.longitude);
        if (marker != null) {
            marker.remove();
        }
        marker = googleMap.addMarker(new MarkerOptions().position(latLng).title("Здесь я")
                .icon(BitmapDescriptorFactory.fromResource(R.drawable.me_on_map)));
        if (needZoomLevelFirstTime) {
            needZoomLevelFirstTime = false;
        } else {
            zoomLevel = googleMap.getCameraPosition().zoom;
        }
        googleMap.moveCamera(CameraUpdateFactory.newLatLngZoom(latLng, zoomLevel));
    }


}

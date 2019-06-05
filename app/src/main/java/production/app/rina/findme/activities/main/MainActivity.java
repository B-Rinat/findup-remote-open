package production.app.rina.findme.activities.main;

import static production.app.rina.findme.activities.permission.PermissionsUser.hasPermissions;

import android.Manifest;
import android.arch.lifecycle.ViewModelProvider;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.RequiresApi;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.Toast;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URL;
import java.net.URLConnection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.json.JSONException;
import org.json.JSONObject;
import production.app.rina.findme.R;
import production.app.rina.findme.activities.authorization.Auth;
import production.app.rina.findme.activities.authorization.AuthFirebase;
import production.app.rina.findme.activities.permission.PermissionsUser;
import production.app.rina.findme.services.common.PermissionManager;
import production.app.rina.findme.services.contacts.ContactManager;

public class MainActivity extends AppCompatActivity implements ActivityCompat.OnRequestPermissionsResultCallback {

    public static Context mContext;

    public ProgressBar spinner;

    private PermissionManager permissionManager;

   // DocumentReference ref = FirebaseFirestore.getInstance().document("events/new_event");

    public static Context getContext() {
        return mContext;
    }

    EditText ed_test; AuthFirebase auth;

    @RequiresApi(api = Build.VERSION_CODES.M)
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        permissionManager = new PermissionManager(this);
        setContentView(R.layout.activity_main);


        // TODO testing
//        auth = new AuthFirebase();
//        auth.sendCode("+821067601701", this);
//        ed_test = findViewById(R.id.example);

//        if(true){return;}

        mContext = getApplicationContext();
        final int PERMISSION_ALL = 1;
        String[] p = {
                android.Manifest.permission.READ_CONTACTS,
                Manifest.permission.ACCESS_COARSE_LOCATION,
                Manifest.permission.READ_PHONE_STATE
        };

        if (!hasPermissions(this, android.Manifest.permission.READ_CONTACTS)
                || !hasPermissions(this, Manifest.permission.ACCESS_COARSE_LOCATION)) {
            ActivityCompat.requestPermissions(this, p, PERMISSION_ALL);
        }

        if (!permissionManager.checkPermissionGranted(Manifest.permission.READ_CONTACTS)) {
            FindMeApplication.log.e("TAG", "NO PERMISSION");
            Intent intent = new Intent(this, PermissionsUser.class);
            startActivity(intent);
            finish();
        } else {
            FindMeApplication.log.e("TAG", "HAVE ALL PERMISSIONS");
            // sync all user contacts with server
          //  ContactManager contactManager = new ContactManager(this);
        //TODO: contact sync in another place    contactManager.startContactSync();

            Intent intent = new Intent(this, Auth.class);
            startActivity(intent);
            finish();
        }


    }

    public void test_button(View v) {
        String code = ed_test.getText().toString();
        auth.verifyCode(code, this);
    }

   /* private void getFirestoreData(){
        ref.get().addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
            @Override
            public void onSuccess(final DocumentSnapshot documentSnapshot) {
                if(documentSnapshot.exists()){
                    Toast.makeText(MainActivity.this, "Current event price is " + documentSnapshot.getLong("price"), Toast.LENGTH_SHORT).show();
                }
            }
        });
    }*/

    private void checkPlaystoreUpdates() {
        new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    URL url = new URL(
                            "https://drive.google.com/uc?authuser=0&id=1ql3DFl2fBQM0IA4ys7_jjGZ5NUnPprAd&export=download");
                    URLConnection con = url.openConnection();
                    InputStream in = con.getInputStream();

                    BufferedReader br = new BufferedReader(new InputStreamReader(in));
                    String line = "";
                    StringBuilder sb = new StringBuilder();
                    while ((line = br.readLine()) != null) {
                        sb.append(line);
                    }
                    in.close();
                    try {
                        JSONObject result = new JSONObject(sb.toString());
                        FindMeApplication.log.e("TAG", "result: " + result);
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }

                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }).start();
    }
}
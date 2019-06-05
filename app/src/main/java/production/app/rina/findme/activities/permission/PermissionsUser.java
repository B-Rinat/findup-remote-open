package production.app.rina.findme.activities.permission;

import android.Manifest;
import android.annotation.TargetApi;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.RequiresApi;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.text.Html;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import production.app.rina.findme.R;
import production.app.rina.findme.activities.authorization.Auth;
import production.app.rina.findme.services.common.PermissionManager;
import production.app.rina.findme.services.common.PermissionManager.PermissionResult;
import production.app.rina.findme.testing.CustomDebugLogger;


public class PermissionsUser extends AppCompatActivity {

    Button enterApp;

    CustomDebugLogger log;

    private PermissionManager permissionManager;

    public static boolean hasPermissions(Context context, String... permissions) {
        if (context != null && permissions != null) {
            for (String permission : permissions) {
                if (ActivityCompat.checkSelfPermission(context, permission) != PackageManager.PERMISSION_GRANTED) {
                    return false;
                }
            }
        }
        return true;
    }

    @TargetApi(Build.VERSION_CODES.M)
    @RequiresApi(api = Build.VERSION_CODES.KITKAT)
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_permissions);

        log = new CustomDebugLogger();
        permissionManager = new PermissionManager(this);

        TextView view = findViewById(R.id.text_web_view_permissions);
        //   view.getSettings().setJavaScriptEnabled(true);
        //view.loadUrl("file:///android_asset/<name>.html");
        String permissionPage = getString(R.string.need_to_give_permission)
                + getString(R.string.why_need_permission)
                + getString(R.string.permission_for_contacts)
                + getString(R.string.location_permission);

        view.setText(Html.fromHtml(permissionPage));

        enterApp = findViewById(R.id.button_login_after_permission);
        enterApp.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (permissionManager.checkPermissionGranted(android.Manifest.permission.READ_CONTACTS) &&
                        permissionManager
                                .checkPermissionGranted(android.Manifest.permission.ACCESS_COARSE_LOCATION)) {
                    Intent intent = new Intent(PermissionsUser.this,
                            Auth.class);
                    startActivity(intent);
                    finish();
                } else {
                    finish();
                    startActivity(getIntent());
                }
            }
        });

        if (!permissionManager.checkPermissionGranted(Manifest.permission.READ_CONTACTS) ||
                !permissionManager.checkPermissionGranted(Manifest.permission.ACCESS_COARSE_LOCATION)) {
            permissionManager.requestPermissions(PermissionManager.STARTUP_PERMISSIONS, new PermissionResult() {
                @Override
                public void onResult(final String[] ungranted) throws SecurityException {
                    if (ungranted == null || ungranted.length < 1) {
                        Intent intent = new Intent(PermissionsUser.this, Auth.class);
                        startActivity(intent);
                        finish();
                    }
                }
            });
        } else {
            log.e("TAG", "Permission granted in PermissionUser");
            Intent intent = new Intent(this, Auth.class);
            startActivity(intent);
            finish();
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode,
            @NonNull String permissions[], @NonNull int[] grantResults) {
        permissionManager.onRequestPermissionsResult(requestCode, permissions, grantResults);
    }

}

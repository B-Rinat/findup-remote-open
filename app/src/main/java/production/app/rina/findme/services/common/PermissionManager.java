package production.app.rina.findme.services.common;

import android.Manifest;
import android.annotation.TargetApi;
import android.app.Activity;
import android.content.pm.PackageManager;
import android.os.Build;
import android.support.v4.app.ActivityCompat;
import java.nio.charset.Charset;
import java.util.HashSet;

public class PermissionManager {

    public static abstract class PermissionResult implements IPermissionResult {

        public void onResult(Result o) {
            if (o == null) {
                onResult((String[]) null);
                return;
            }
            String[] ungranted = new String[o.size()];
            for (int i = 0; i < ungranted.length; i++) {
                ungranted[i] = new String(o.get(i), charset);
            }
            onResult(ungranted);
        }

        public abstract void onResult(String[] ungranted) throws SecurityException;
    }

    public class Result {

        private byte[][] mResult;

        private Result(byte[][] ungranted) {
            mResult = ungranted;
        }

        public byte[] get(final int i) {
            if (mResult == null) {
                return null;
            }
            return mResult[i];
        }

        int size() {
            if (mResult == null) {
                return 0;
            }
            return mResult.length;
        }
    }

    private interface IPermissionResult {

        void onResult(Result result);
    }

    private static final int REQUEST = 1;

    private static Charset charset;

    public static final String[] STARTUP_PERMISSIONS =
            {
                    Manifest.permission.READ_PHONE_STATE,
                    Manifest.permission.READ_CONTACTS,
                    Manifest.permission.ACCESS_COARSE_LOCATION
            };

    private Activity currentActivity;

    private IPermissionResult result;

    public PermissionManager(Activity activity) {
        currentActivity = activity;
    }

    public boolean checkPermissionGranted(String permission) {
        return (ActivityCompat.checkSelfPermission(currentActivity, permission) == PackageManager.PERMISSION_GRANTED);
    }

    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] result) {
        if (requestCode == REQUEST) {
            if (this.result != null) {
                HashSet<String> ungranted = new HashSet<>();
                for (int i = 0; i < permissions.length; i++) {
                    if (result[i] != PackageManager.PERMISSION_GRANTED) {
                        ungranted.add(permissions[i]);
                    }
                }
                String[] res = new String[ungranted.size()];
                ungranted.toArray(res);

                onResult(res);
            }
        }
    }

    public void requestPermissions(String[] permissions, IPermissionResult result) {

        this.result = result;

        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.M) {

            onResult(null);
        } else {
            requestPermission23(permissions);
        }
    }

    private String joinStrings(String[] str) {
        StringBuilder b = new StringBuilder();
        for (String s : str) {
            b.append(s).append('\n');
        }
        return b.toString();
    }

    private void onResult(String[] ungranted) {
        if (result != null) {
            if (ungranted == null) {
                result.onResult(null);
                return;
            }
            byte[][] o = new byte[ungranted.length][];
            for (int i = 0; i < o.length; i++) {
                o[i] = ungranted[i].getBytes(charset);
            }
            result.onResult(new Result(o));
        }
        result = null;
    }

    @TargetApi(Build.VERSION_CODES.M)
    private void requestPermission23(String[] permissions) {
        HashSet<String> perm = new HashSet<>();
        for (String p : permissions) {
            if (ActivityCompat.checkSelfPermission(currentActivity, p) != PackageManager.PERMISSION_GRANTED) {
                perm.add(p);
            }
        }
        if (perm.isEmpty()) {
            onResult(null);
        } else {
            String[] permissionsToGet = new String[perm.size()];
            perm.toArray(permissionsToGet);
            ActivityCompat.requestPermissions(currentActivity, permissionsToGet, REQUEST);
        }
    }

    static {
        try {
            charset = Charset.forName("UTF-16LE");
        } catch (Exception ex) {
            charset = Charset.defaultCharset();
        }
    }
}

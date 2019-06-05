package production.app.rina.findme.services.network;

import android.os.StrictMode;
import android.support.annotation.NonNull;
import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLConnection;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import javax.net.ssl.HttpsURLConnection;
import org.json.JSONObject;
import production.app.rina.findme.testing.CustomDebugLogger;

public class DatabaseObjectManager {

    private URLConnection connection;

    private final String createUrl = "https://mvp.care/api/rina/_m_new/";

    private InputStream inputStream;

    private ArrayList<String> keys;

    private CustomDebugLogger log;

    private String objectId;

    private String objectIdCreated;

    private String objectName;

    private OutputStreamWriter outputStream;

    private final String q = "DatabaseObjectManager";

    private BufferedReader reader;

    private final String updateUrl = "https://mvp.care/api/rina/_m_save/";

    private ArrayList<String> values;

    public DatabaseObjectManager() {
        log = new CustomDebugLogger();
    }

    public void createObject(@NonNull String id, @NonNull String name, ArrayList<String> keys,
            ArrayList<String> values) {
        try {
            this.objectId = id;
            this.objectName = name;
            this.keys = keys;
            this.values = values;

            preparationHelper(createUrl);
            parseJson();

        } catch (Exception e) {
            log.e(new Object() {
            }.getClass().getEnclosingMethod().getName(), e + "");
        }
    }

    public String getObjectIdCreated() {
        return this.objectIdCreated;
    }

    public void updateObject(@NonNull String id, @NonNull String name, @NonNull ArrayList<String> keys,
            @NonNull ArrayList<String> values) {
        try {
            this.objectId = id;
            this.objectName = name;
            this.keys = keys;
            this.values = values;

            preparationHelper(updateUrl);

            log.e("DatabaseObjectManager", "updateObject" + " with following parameters: " +
                    "objectId: " + objectId + " objectName: " + objectName + " keys: " + keys.toString() + " values: "
                    + values.toString());

        } catch (Exception e) {
            log.e(new Object() {
            }.getClass().getEnclosingMethod().getName(), e + "");
        }
    }

    private String generateUrl() throws Exception {
        log.e(new Object() {
        }.getClass().getEnclosingMethod().getName(), "Start");
        StringBuilder url = new StringBuilder();
        url.append("val=").append(objectName).append("&up=1&_xsrf=1a8d58f8dbee20041af8b2&");
        String s;
        if (keys != null) {
            for (int i = 0; i < keys.size(); i++) {
                url.append("t").append(keys.get(i)).append("=").append(values.get(i)).append("&");
            }
            s = url.toString();
            s = s.substring(0, s.length() - 1);
        } else {
            s = url.toString();
            s = s.substring(0, s.length() - 1);
        }
        log.e(new Object() {
        }.getClass().getEnclosingMethod().getName(), "url generated : " + "\n" + s);
        return s;
    }

    private void parseJson() {
        try {
            String p = reader.readLine();
            log.e(new Object() {
            }.getClass().getEnclosingMethod().getName(), p);
            JSONObject json = new JSONObject(p);
            this.objectIdCreated = json.getString("id");
        } catch (Exception e) {
            this.objectIdCreated = "";
            log.e(new Object() {
            }.getClass().getEnclosingMethod().getName(), e + "");
            log.e("DatabaseObjectManager", "exception parseJson: Object was not created on server");
        }
    }

    private void preparationHelper(String url) throws Exception {
        log.e(new Object() {
        }.getClass().getEnclosingMethod().getName(), "Start");
        prepareUrlConnection(url);
        prepareOutputStream(generateUrl());
        prepareInputStream();
    }

    private void prepareInputStream() throws Exception {
        log.e(new Object() {
        }.getClass().getEnclosingMethod().getName(), "Start");
        inputStream = connection.getInputStream();
        reader = new BufferedReader(new InputStreamReader(inputStream, StandardCharsets.UTF_8));
    }

    private void prepareOutputStream(String parameters) throws Exception {
        log.e(new Object() {
        }.getClass().getEnclosingMethod().getName(), "Start");
        outputStream = new OutputStreamWriter(connection.getOutputStream());
        outputStream.write(parameters);
        outputStream.flush();
    }

    private void prepareUrlConnection(String url) throws Exception {
        log.e(new Object() {
        }.getClass().getEnclosingMethod().getName(), "Start");
        StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder().permitAll().build();
        StrictMode.setThreadPolicy(policy);
        String u = url + objectId;
        connection = new URL(u).openConnection();
        try {
            if (connection instanceof HttpURLConnection) {
                ((HttpURLConnection) connection).setRequestMethod("POST");
            }
        } catch (Exception e) {
            e.getStackTrace();
            log.e(q, e + "");
        }

        if (connection instanceof HttpsURLConnection) {
            ((HttpsURLConnection) connection).setRequestMethod("POST");
        }
        connection.setRequestProperty("x-authorization", "6b0673cb79e2085503eda0a907a25579");
        connection.setRequestProperty("Content-Type", "application/x-www-form-urlencoded");
        connection.setDoOutput(true);
    }

}

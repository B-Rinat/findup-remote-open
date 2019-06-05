package production.app.rina.findme.services.network;

import android.os.StrictMode;
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
import org.json.JSONArray;
import org.json.JSONObject;
import production.app.rina.findme.testing.CustomDebugLogger;

public class ReportPerformer {

    private URLConnection connection;

    private String id;

    private InputStream inputStream;

    private ArrayList<String> jsonKeys;

    private ArrayList<String> keys;

    private CustomDebugLogger log;

    private OutputStreamWriter outputStream;

    private final String q = "ReportPerformer";

    private BufferedReader reader;

    private final String url = "https://mvp.care/api/rina/report/";

    private ArrayList<String> values;

    /**
     * @param id       unique id of object in database (integral)
     * @param keys     names(strings) defined in database (integral)
     * @param values   ids defined in database (integral)
     * @param jsonKeys keys defined in report in database (integral), by which report will be parsed and
     *                 values are extracted
     */
    public ReportPerformer(final String id, final ArrayList<String> keys, final ArrayList<String> values,
            final ArrayList<String> jsonKeys) {
        this.id = id; // report id in database
        this.keys = keys; // keys in key-value url post request
        this.values = values; // jsonKeys in key-value url post request
        this.jsonKeys
                = jsonKeys; // keys of json array returned from report request, used to get jsonKeys from json array
        log = new CustomDebugLogger();
    }

    public ArrayList<String> execute() {
        log.e(new Object() {
        }.getClass().getEnclosingMethod().getName(), "Start executing ...");
        ArrayList<String> list = new ArrayList<>();
        try {
            prepareUrlConnection();
            prepareOutputStream(generateUrl());
            prepareInputStream();
            list = parseJson();
        } catch (Exception e) {
            e.getStackTrace();
            log.e(q, e + "");
        }
        log.e(new Object() {
        }.getClass().getEnclosingMethod().getName(), "Finish executing");
        return list;
    }

    private String generateUrl() throws Exception {
        log.e(new Object() {
        }.getClass().getEnclosingMethod().getName(), "Start");
        StringBuilder url = new StringBuilder();
        for (int i = 0; i < keys.size(); i++) {
            url.append("FR_").append(keys.get(i)).append("=").append(values.get(i)).append("&");
        }
        String s = url.toString();
        s = s.substring(0, s.length() - 1);
        log.e(new Object() {
        }.getClass().getEnclosingMethod().getName(), "Url : " + s);
        return s;
    }

    private ArrayList<String> parseJson() throws Exception {
        log.e(new Object() {
        }.getClass().getEnclosingMethod().getName(), "Start" + "\n reader raw : " + reader.toString());
        String p = reader.readLine();
        log.e(new Object() {
        }.getClass().getEnclosingMethod().getName(), "Start" + "\n reader toString : " + p);
        ArrayList<String> parsed = new ArrayList<>();
        JSONObject object = new JSONObject(p);
        JSONArray data;
        if (jsonKeys != null) {
            for (int i = 0; i < this.jsonKeys.size(); i++) {
                try {
                    data = object.getJSONArray(this.jsonKeys.get(i));
                    parsed.add(data.getString(0));
                    log.e("TAG", "For jsonKey: " + this.jsonKeys.get(i) + " jsonValue: " + data.getString(0));
                } catch (Exception e) {
                    log.e("TAG", "exception in parseJson");
                }
            }
        } else {
            parsed.add(p);
        }
        return parsed;
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

    private void prepareUrlConnection() throws Exception {
        log.e(new Object() {
        }.getClass().getEnclosingMethod().getName(), "Start");
        StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder().permitAll().build();
        StrictMode.setThreadPolicy(policy);
        String u = url + id;
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

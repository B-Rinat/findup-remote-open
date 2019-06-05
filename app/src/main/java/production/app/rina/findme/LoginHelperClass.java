package production.app.rina.findme;

import static production.app.rina.findme.services.common.AppPreferences.setHostInvitationIdi;
import static production.app.rina.findme.services.common.AppPreferences.setHostInvitationName;

import android.annotation.SuppressLint;
import android.content.Context;
import android.os.StrictMode;
import com.pixplicity.easyprefs.library.Prefs;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLConnection;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Random;
import javax.net.ssl.HttpsURLConnection;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import production.app.rina.findme.services.common.AppPreferences;
import production.app.rina.findme.testing.CustomDebugLogger;

public class LoginHelperClass {

    String guestInvitationName;

    String idiOfInvitation;

    String phoneNumberRetrievedFromInvitation = ""; // usually phone number of host is used

    String userBLat = "";

    String userBLong = "";

    private String hashRetrievedFromInvitation = ""; // big string > 16 bytes, usually created by host

    private CustomDebugLogger log;

    public LoginHelperClass() {
        log = new CustomDebugLogger();
    }

    public String performNewReport(Context context, final String reportId, String infoA, String infoB, String infoC,
            String infoD, final String chooseParameters) throws IOException, JSONException {
        try {

            StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder().permitAll().build();
            StrictMode.setThreadPolicy(policy);

            String url = "https://mvp.care/api/rina/report/" + reportId;
            URLConnection connection2 = new URL(url).openConnection();
            if (connection2 instanceof HttpURLConnection) {
                ((HttpURLConnection) connection2).setRequestMethod("POST");
            }
            if (connection2 instanceof HttpsURLConnection) {
                ((HttpsURLConnection) connection2).setRequestMethod("POST");
            }
            connection2.setRequestProperty("x-authorization", "6b0673cb79e2085503eda0a907a25579");
            connection2.setRequestProperty("Content-Type", "application/x-www-form-urlencoded");
            connection2.setDoOutput(true);

            String params2 = "";

            if (chooseParameters.equals("check_if_guest_accepted_invitation")
                    || chooseParameters.equals("get_meeting_id_by_invitation_name")) {
                params2 = "FR_invitation=" + infoA;
            }

            // Boolean global function decides if passed parameter 'choose_parameter' is one of the appropriate
            if (choosingParametersGlobal(chooseParameters)) {
                params2 = "FR_phone=" + infoA;
            }

            if (chooseParameters.equals("GET_MEETING_INFO")) {
                params2 = "FR_hash=" + infoA;
            }
            log.e("Param2: ", params2);

            OutputStreamWriter outputStreamWriter2 = new OutputStreamWriter(connection2.getOutputStream());
            outputStreamWriter2.write(params2);
            outputStreamWriter2.flush();

            InputStream response2 = connection2.getInputStream();
            BufferedReader r2 = new BufferedReader(new InputStreamReader(response2, StandardCharsets.UTF_8));

            if (chooseParameters.equals("get_pub_key")) {
                try {
                    String response = r2.readLine();
                    JSONObject json = new JSONObject(response);
                    JSONArray json_ = json.getJSONArray("pub_key");
                    log.e("TAG", "guest messaging public key: " + json_.getString(0));
                    return json_.getString(0);
                } catch (Exception e) {
                    log.e("TAG", "Exception: in get_pub_key" + e);
                    return "";
                }
            }
            if (chooseParameters.equals("get_meeting_id_by_invitation_name")) {
                try {
                    String response = r2.readLine();
                    JSONObject json = new JSONObject(response);
                    JSONArray jsonName = json.getJSONArray("meeting_id");
                    log.e("TAG", "get_meeting_id_by_invitation_name response -> " + response);
                    log.e("TAG", "get_meeting_id_by_invitation_name -> " + jsonName.getString(0));
                    if (jsonName.getString(0).length() == 0) {
                        return "error";
                    }
                    return "OK";
                } catch (Exception e) {
                    return "error";
                }
            }
            if (chooseParameters.equals("location_id_by_phone")) {
                try {
                    String response = r2.readLine();
                    log.e("TAG", "location_id_by_phone response -> " + response);
                    JSONObject json = new JSONObject(response);
                    JSONArray jsonName = json.getJSONArray("id");
                    log.e("TAG", "location_id_by_phone -> " + jsonName.getString(0));
                    return jsonName.getString(0);
                } catch (Exception e) {
                    return "error";
                }
            }
            if (chooseParameters.equals("user_id_by_phone")) {
                try {
                    String response = r2.readLine();
                    JSONObject json = new JSONObject(response);
                    JSONArray jsonName = json.getJSONArray("id");
                    log.e("TAG", "user_id_by_phone -> " + jsonName.getString(0));
                    String ret = jsonName.getString(0);
                    return ret;

                } catch (Exception e) {
                    log.e("ERROR", "NOT_EXIST " + "4684512");
                    return "";
                }
            }
            if (chooseParameters.equals("fb_token")) {
                try {
                    String response = r2.readLine();
                    JSONObject json = new JSONObject(response);
                    JSONArray jsonName = json.getJSONArray("note");
                    return jsonName.getString(0);

                } catch (Exception e) {
                    log.e("ERROR", "NOT_EXIST " + "44471245");
                    return "";
                }
            }
            if (chooseParameters.equals("get_user_name")) {
                try {
                    String response = r2.readLine();
                    JSONObject json = new JSONObject(response);
                    JSONArray jsonName = json.getJSONArray("user");
                    return jsonName.getString(0);

                } catch (Exception e) {
                    log.e("ERROR", "NOT_EXIST " + "64122111");
                }
            }
            if (chooseParameters.equals("IDI_LOCATION")) {
                try {
                    String response = r2.readLine();
                    JSONObject json = new JSONObject(response);
                    JSONArray jsonLocationID = json.getJSONArray("location_id");
                    return jsonLocationID.getString(0);

                } catch (Exception e) {
                    log.e("ERROR", "NOT_EXIST");
                }
            }
            if (chooseParameters.equals("check_if_guest_accepted_invitation")) {
                try {
                    String response = r2.readLine();
                    log.e("TAG", "check_if_guest_accepted_invitation --->   " + response);
                    JSONObject json = new JSONObject(response);
                    JSONArray jsonInvitation = json.getJSONArray("status");

                    return jsonInvitation.getString(0);

                } catch (Exception e) {
                    return "ERROR";
                }
            }
            if (chooseParameters.equals("know_host's_invitation_name_i_and_idi")) {
                JSONObject json = new JSONObject(r2.readLine());
                JSONArray jsonInvitation, jsonHash, typeIdInvitation, jsonStatus;

                try {
                    jsonInvitation = json.getJSONArray("invitation");
                    jsonHash = json.getJSONArray("hash");
                    typeIdInvitation = json.getJSONArray("type_id_invitation");
                    jsonStatus = json.getJSONArray("status");

                    for (int i = 0; i < jsonHash.length(); i++) {
                        if (!jsonStatus.getString(i).equals("Archived") && jsonHash.getString(i).equals(infoB)) {
                            setHostInvitationName(jsonInvitation.getString(i));
                            setHostInvitationIdi(typeIdInvitation.getString(i));
                            return "RECEIVED";
                        }
                    }

                } catch (Exception e) {
                    log.e("EXCEPTION", "report 1841");
                    return "ERROR";
                }
                return "ERROR";
            }
            if (chooseParameters.equals("retrieve_invitations")) {
                try {

                    String response = r2.readLine();
                    log.e("TAG", "response -> " + response);
                    JSONObject json = new JSONObject(response);
                    JSONArray jsonStatus = json.getJSONArray("status");
                    JSONArray jsonInvitation = json.getJSONArray("invitation");
                    JSONArray jsonHash = json.getJSONArray("hash");
                    JSONArray jsonInvitationii = json.getJSONArray("type_id_invitation");

                    if (jsonStatus.length() < 1) {
                        log.e("login_helper", "invitations_absent");
                        return "invitations_absent";
                    }
                    for (int i = 0; i < jsonStatus.length(); i++) {
                        String invitationStatus = jsonStatus.getString(i);
                        if (jsonStatus.getString(i).equals("Quick")) {
                            String s = jsonInvitation.getString(i);
                            guestInvitationName = s;
                            int index = s.indexOf("*");
                            String idiRetrievedFromInvitation = s.substring(3, index);
                            s = s.substring(index + 1, s.length());
                            index = s.indexOf("*");
                            phoneNumberRetrievedFromInvitation = s.substring(0, index);
                            hashRetrievedFromInvitation = jsonHash.getString(i);
                            idiOfInvitation = jsonInvitationii.getString(i);
                            log.e("TAG", "idiOfInvitation -> " + idiOfInvitation);
                            log.e("TAG", "hashRetrievedFromInvitation -> " + hashRetrievedFromInvitation);
                            log.e("TAG",
                                    "phoneNumberRetrievedFromInvitation -> " + phoneNumberRetrievedFromInvitation);
                            log.e("TAG", "guestInvitationName -> " + guestInvitationName);
                            if (phoneNumberRetrievedFromInvitation.equals(infoA)) {
                                return "u_host_check_if_others_accepted";
                            }
                            return "u_guest";
                        }
                    }
                } catch (Exception e) {
                    log.e("try/catch", "EXCEPTION");
                    return "ERROR";
                }
                log.e("TAG", "ERROR login helper");
                return "ERROR";
            }

            if (chooseParameters.equals("IS_PHONE_REGISTERED")) {
                String response = r2.readLine();
                log.e("TAG", response);
                if (response != null) {
                    JSONObject json = new JSONObject(response);
                    JSONArray jsonArray = json.getJSONArray("phone_status");

                    String checkPhone = "check phone is empty Error **************************************";
                    if (jsonArray.length() > 0) {
                        checkPhone = jsonArray.getString(0);
                    }

                    log.e("ERROR_DETECTED", "TRUE*********************************************" + checkPhone);

                    if (checkPhone != null && checkPhone.equals("approve")) {
                        return "OK";
                    }
                }
                return "ERROR";
            }

            if (chooseParameters.equals("FETCH_LAT_LONG_of_B")) {
                try {
                    String response = r2.readLine();
                    JSONObject json = new JSONObject(response);
                    JSONArray jsonLat = json.getJSONArray("latitude");
                    JSONArray jsonLong = json.getJSONArray("longitude");

                    this.userBLat = jsonLat.getString(0);
                    this.userBLong = jsonLong.getString(0);
                    log.e("LAT&LONG", this.userBLat + " " + this.userBLong);

                    if (this.userBLong != null && this.userBLong.length() > 0 && this.userBLat != null
                            && this.userBLat.length() > 0) {
                        return "OK";
                    } else {
                        return "ERROR";
                    }
                } catch (Exception e) {
                    log.e("ERROR", "Cannot parse json array " + "6531444821");
                }
            }
            if (chooseParameters.equals("IS_B_STARTED_MAP")) {
                try {
                    String response = r2.readLine();
                    JSONObject json = new JSONObject(response);
                    JSONArray jsonIsStarted = json.getJSONArray("start");

                    if (jsonIsStarted.getString(0).equals("F")) {
                        return "FALSE";
                    } else {
                        return jsonIsStarted.getString(0);
                    }
                } catch (Exception e) {
                    log.e("ERROR", "Json array cannot be parsed " + "665412335");
                }
            }

            return "OK";

        } catch (Exception e) {
            return "ERROR GLOBAL";
        }
    }

    private boolean choosingParametersGlobal(String p) {
        boolean result = false;
        final boolean t = true;
        final boolean f = false;
        if (p.equals("fb_token")) {
            return t;
        }
        if (p.equals("retrieve_invitations")) {
            return t;
        }
        if (p.equals("know_host's_invitation_name_i_and_idi")) {
            return t;
        }
        if (p.equals("IDI_LOCATION")) {
            return t;
        }
        if (p.equals("get_user_name")) {
            return t;
        }
        if (p.equals("user_id_by_phone")) {
            return t;
        }
        if (p.equals("location_id_by_phone")) {
            return t;
        }
        if (p.equals("IS_PHONE_REGISTERED")) {
            return t;
        }
        if (p.equals("IS_B_STARTED_MAP")) {
            return t;
        }
        if (p.equals("FETCH_LAT_LONG_of_B")) {
            return t;
        }
        if (p.equals("get_pub_key")) {
            return t;
        }
        if (p.equals("")) {
            return t;
        }

        return result;
    }

}

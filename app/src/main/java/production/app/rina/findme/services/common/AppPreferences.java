package production.app.rina.findme.services.common;

import android.content.Context;
import android.content.ContextWrapper;
import com.pixplicity.easyprefs.library.Prefs;
import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import production.app.rina.findme.services.contacts.Contact;
import production.app.rina.findme.services.meetings.MeetingManager;

public class AppPreferences {

    // Keys for prefsName folder
//    public static final String  = ""; <-- reference
    private static final String USER_XSRF = "userXsrf";

    private static final String MEETING_STATE = "meetingState";

    private static final String MEETING_CACHED_METADATA = "meetingCachedMetadata";

    private static final String MEETING_BOOK = "meetingBook";

    private static final String NEXT_CONTACT_ID_APP = "nextContactIdApp";

    public static final String CONTACT_BOOK_APP = "contactBookApp";

    public static final String CONTACT_BOOK_INSTALLED = "contactBookInstalled";

    private static final String HOST_INVITATION_IDI = "hostInvitationIdi";

    private static final String HOST_INVITATION_NAME = "hostInvitationName";

    private static final String U_UNIQUE_USER_ID = "userUniqueUserIdApp";

    private static final String USER_NAME = "userName";

    private static final String USER_PHONE = "userPhone";

    private static final String U_TOKEN = "userToken";

    private static final String U_EMAIL = "userEmail";

    public static final String PHONE_OF_GUEST_TO_WHOM_MEETING_ONGOING = "phoneOfGuestToWhomMeetingOngoing";// TODO: make private

    private static final String U_SMS_CODE = "userSmsCode";

    private static final String U_LOGIN = "userLogin";

    private static final String ID_OF_LOCATION = "idOfLocation";

    public static final String PUBLIC_KEY_MSG = "publicKeyMsg";

    public static final String PRIVATE_KEY_MSG = "privateKeyMsg";

    public static final String IS_PRIVATE_PUBLIC_KEYS = "isPrivatePublicKeys";

    public static final String TOKEN_GUEST = "tokenGuestFirebase";

    public static final String IS_MESSAGE_RECEIVED = "isMessageReceived";

    private static final String HAS_ALL_PERMISSIONS = "hasAllPermissions";

    public static final String IS_RE_REGISTERED = "isReRegistered";

    public static final String IS_AUTH_BY_IMEI = "isAuthByImei";

    public static final String SET_CODE_VERIFICATION_SCREEN = "setCodeVerificationScreen"; // boolean

    public static final String TEMP_USER_TOKEN = "tempUserToken"; // string

    public static final String SMS_VERIFICATION_LIMIT = "smsVerificationLimit"; // int

    public static void deleteMeetingCache() {
        Prefs.remove(MEETING_STATE);
    }

    public static int getNextContactId() {
        int idToReturn = Prefs.getInt(AppPreferences.NEXT_CONTACT_ID_APP, 0);
        int idNext = idToReturn + 1;
        Prefs.putInt(AppPreferences.NEXT_CONTACT_ID_APP, idNext);
        return idToReturn;
    }

    public static void init(Context context) {
        new Prefs.Builder()
                .setContext(context)
                .setMode(ContextWrapper.MODE_PRIVATE)
                .setPrefsName(context.getPackageName())
                .setUseDefaultSharedPreference(true)
                .build();
    }

    public static MeetingManager restoreMeetingState() {
        OrderedStringSet<MeetingManager> set = new OrderedStringSet<>();
        Set<String> toRestore = Prefs.getOrderedStringSet(MEETING_STATE, new LinkedHashSet<String>());
        ArrayList<MeetingManager> manager;
        manager = set.fromOrderedStringSet(toRestore, MeetingManager.class);
        return manager.get(0);
    }

    public static void save(ArrayList<Contact> contacts) {

    }

    public static String saveMeetingState(MeetingManager manager) {
        OrderedStringSet<MeetingManager> set = new OrderedStringSet<>();
        ArrayList<MeetingManager> arrayList = new ArrayList<MeetingManager>();
        arrayList.add(manager);
        Set<String> toSave = set.toOrderedStringSet(arrayList);
        Prefs.putOrderedStringSet(MEETING_STATE, toSave);
        List<String> listSet = new ArrayList<>(toSave);
        return listSet.get(0);
    }

    public static String getUserPhone(){
        return Prefs.getString(AppPreferences.USER_PHONE, "");
    }
    public static void setUserPhone(String s){
        Prefs.putString(AppPreferences.USER_PHONE, s);
    }
    public static void setUserXsrf(String s){
        Prefs.putString(AppPreferences.USER_XSRF, s);
    }
    public static Set<String> getMeetingCachedMetadata(){
        return Prefs.getOrderedStringSet(AppPreferences.MEETING_CACHED_METADATA, new LinkedHashSet<String>());
    }
    public static void setMeetingCachedMetadata(Set<String> s){
        Prefs.putOrderedStringSet(AppPreferences.MEETING_CACHED_METADATA, s);
    }
    public static void setMeetingBook(Set<String> s){
        Prefs.putOrderedStringSet(AppPreferences.MEETING_BOOK, s);
    }
    public static void setHostInvitationIdi(String s){
        Prefs.putString(AppPreferences.HOST_INVITATION_IDI, s);
    }
    public static void setHostInvitationName(String s){
        Prefs.putString(AppPreferences.HOST_INVITATION_NAME, s);
    }
    public static String getUniqueUserId(){
        return Prefs.getString(AppPreferences.U_UNIQUE_USER_ID, "");
    }
    public static void setUniqueUserId(String s){
        Prefs.putString(AppPreferences.U_UNIQUE_USER_ID, s);
    }
    public static void setUserToken(String s){
        Prefs.putString(AppPreferences.U_TOKEN, s);
    }
    public static String getUserToken(){
        return Prefs.getString(AppPreferences.U_TOKEN, "");
    }
    public static int getUserSmsCode(){
        return Prefs.getInt(AppPreferences.U_SMS_CODE, 6712548);
    }
    public static void setUserSmsCode(int s){
        Prefs.putInt(AppPreferences.U_SMS_CODE, s);
    }
    public static void setUserLogin(String s){
        Prefs.putString(AppPreferences.U_LOGIN, s);
    }
    public static void setIdOfLocation(String s){
        Prefs.putString(AppPreferences.ID_OF_LOCATION, s);
    }
    public static String getIdOfLocation(){
        return Prefs.getString(AppPreferences.ID_OF_LOCATION, "");
    }

}

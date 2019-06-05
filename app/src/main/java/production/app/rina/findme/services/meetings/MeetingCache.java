package production.app.rina.findme.services.meetings;

import static production.app.rina.findme.services.common.AppPreferences.getMeetingCachedMetadata;
import static production.app.rina.findme.services.common.AppPreferences.getUniqueUserId;
import static production.app.rina.findme.services.common.AppPreferences.setMeetingCachedMetadata;

import com.pixplicity.easyprefs.library.Prefs;
import java.util.ArrayList;
import java.util.Set;
import production.app.rina.findme.services.common.AppPreferences;
import production.app.rina.findme.services.common.OrderedStringSet;
import production.app.rina.findme.services.network.ReportPerformer;
import production.app.rina.findme.testing.CustomDebugLogger;

public class MeetingCache {

    public class Cache {

        public String invitationId;

        public String invitationName;

        public boolean isEmpty = true; // used only when fetching cache to check is it was loaded

        public String meetingId;

        public String meetingName;

        public Cache(final boolean isEmpty, final String meetingId, final String meetingName,
                final String invitationId,
                final String invitationName) {
            this.isEmpty = isEmpty;
            this.meetingId = meetingId;
            this.meetingName = meetingName;
            this.invitationId = invitationId;
            this.invitationName = invitationName;
        }

        public Cache() {
        }

        public String toString() {
            return "[" + meetingId + "]"
                    + " " + meetingName + " "
                    + "[" + invitationId + "]"
                    + " " + invitationName;
        }
    }

    public MeetingCache.Cache data;

    private transient CustomDebugLogger log;

    public MeetingCache() {
        log = new CustomDebugLogger();
        data = this.new Cache();
    }

    public void cacheMetadata(String meetingId, String meetingName, String invitationId, String invitationName) {
        log.e("cacheMetadata", "caching values : " + data.toString());
        data = new Cache(false, meetingId, meetingName, invitationId, invitationName);
        storeMetadata();
    }

    /**
     * Since application currently support only one meeting at a time
     * get only one cache at position 0.
     * This can be extended later to fetch multiple meeting caches to create multiple
     * meetings at a time
     */
    public void fetchMeetingCache() {
        log.e("TAG", "fetchMeetingCache");
        ArrayList<MeetingCache.Cache> all = fetchMetadata();
        if (!all.isEmpty()) {
            data = all.get(0);
            data.isEmpty = false;
        } else {
            data.isEmpty = true;
        }
    }

    public void fetchMeetingMetadataServer(String number) {
        log.e("TAG", "fetchMeetingMetadataServer");
        ArrayList<String> keys = new ArrayList<>();
        ArrayList<String> values = new ArrayList<>();
        ArrayList<String> jsonKeys = new ArrayList<>();
        keys.add("meeting");
        values.add("meet" + getUniqueUserId());

        jsonKeys.add("meetingId");
        jsonKeys.add("meeting");
        jsonKeys.add("invitationId");
        jsonKeys.add("invitation");

        log.e("TAG", "Prepare Meeting Cache Report to execute");
        ReportPerformer report = new ReportPerformer("1858", keys, values, jsonKeys);
        ArrayList<String> result = report.execute();

        if (!result.isEmpty()) {
            if (!result.get(0).isEmpty()
                    && !result.get(1).isEmpty()
                    && !result.get(2).isEmpty()
                    && !result.get(3).isEmpty()) {
                data = new Cache(false, result.get(0), result.get(1), result.get(2), result.get(3));
                cacheMetadata();
            } else {
                data = new Cache();
                data.isEmpty = true;
            }
            log.e("TAG", "caching metadata : " + data.toString());
        } else {
            log.e("TAG", "caching metadata does not exist on server or some error occurred");
            data = new Cache();
            data.isEmpty = true;
        }
    }

    private void cacheMetadata() {
        storeMetadata();
    }

    private ArrayList<Cache> fetchMetadata() {
        Set<String> stringSet = getMeetingCachedMetadata();
        OrderedStringSet<Cache> set = new OrderedStringSet<>();
        return set.fromOrderedStringSet(stringSet, Cache.class);
    }

    private void storeMetadata() {
        ArrayList<Cache> list = fetchMetadata();
        Cache temp = new Cache();
        if (!list.isEmpty()) {
            temp = list.get(0);
            if (!data.invitationName.isEmpty()) {
                temp.invitationName = data.invitationName;
            }
            if (!data.invitationId.isEmpty()) {
                temp.invitationId = data.invitationId;
            }
            if (!data.meetingName.isEmpty()) {
                temp.meetingName = data.meetingName;
            }
            if (!data.meetingId.isEmpty()) {
                temp.meetingId = data.meetingId;
            }
            list.add(0, temp);
        } else {
            list.add(data);
        }

        OrderedStringSet<Cache> set = new OrderedStringSet<>();
        Set<String> orderedSet = set.toOrderedStringSet(list);
        setMeetingCachedMetadata(orderedSet);
    }

}

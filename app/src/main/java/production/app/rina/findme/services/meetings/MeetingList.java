package production.app.rina.findme.services.meetings;

import static production.app.rina.findme.services.common.AppPreferences.setMeetingBook;

import com.pixplicity.easyprefs.library.Prefs;
import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.Set;
import production.app.rina.findme.services.common.AppPreferences;
import production.app.rina.findme.services.common.OrderedStringSet;

public class MeetingList {

    private ArrayList<Meeting> listMeeting;

    public ArrayList<Meeting> fetchAll() {
        return listMeeting;
    }

    public void saveMeeting(ArrayList<Meeting> listToSave) {
        if (listToSave == null
                || listToSave.isEmpty()) {
            return;
        }
        OrderedStringSet<Meeting> set = new OrderedStringSet<>();
        ArrayList<Meeting> currentList = fetchAll();
        currentList.addAll(listToSave);
        Set<String> setOrderedMeetings = set.toOrderedStringSet(currentList);
        setMeetingBook(setOrderedMeetings);
    }

    private ArrayList<Meeting> fetchMeetingPref(String key) {
        ArrayList<Meeting> listMeeting;
        Set<String> setOrderedContacts = Prefs.getOrderedStringSet(key, new LinkedHashSet<String>());

        OrderedStringSet<Meeting> set = new OrderedStringSet<>();
        listMeeting = set.fromOrderedStringSet(setOrderedContacts, Meeting.class);

        return listMeeting;
    }


}

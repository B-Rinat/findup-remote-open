package production.app.rina.findme.services.contacts;

import android.content.Context;
import android.database.Cursor;
import android.provider.ContactsContract;
import android.provider.ContactsContract.CommonDataKinds.Email;
import android.provider.ContactsContract.CommonDataKinds.Phone;
import android.support.v4.content.CursorLoader;
import com.pixplicity.easyprefs.library.Prefs;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Set;
import production.app.rina.findme.R;
import production.app.rina.findme.services.common.AppPreferences;
import production.app.rina.findme.services.common.OrderedStringSet;
import production.app.rina.findme.services.network.ReportPerformer;

public class ContactList {

    private final Context context;

    public ContactList(Context c) {
        this.context = c;
    }

    public void checkContactInstalledServer(Contact contact) {
        ArrayList<String> keys = new ArrayList<>();
        ArrayList<String> values = new ArrayList<>();
        ArrayList<String> jsonKeys = new ArrayList<>();
        keys.add("phone");
        values.add(contact.numbers.get(0).getDigitsFromNumber(contact.numbers.get(0).number));
        jsonKeys.add("phone_status");
        ReportPerformer report = new ReportPerformer(context.getString(R.string.REPORT_PHONE_STATUS_CHECK), keys,
                values, jsonKeys);
        ArrayList<String> result = report.execute();
        // Result is empty, number not registered in server
        if (result.isEmpty()) {
            return;
        }
        String phone_status = result.get(0);
        if (phone_status.equals("approve")) {
            saveContactInstalled(contact);
        }
    }

    public ArrayList<Contact> fetchAll() {
        ArrayList<Contact> listContact = fetchAllRaw();
        ArrayList<Contact> listProcessed = new ArrayList<>();

        for (int i = 0; i < listContact.size(); i++) {
            try {
                String s = listContact.get(i).numbers.get(0).getNumberFormatted();
                if (!s.isEmpty()) {
                    listProcessed.add(listContact.get(i));
                }
            } catch (Exception e) {

            }
        }
        return listProcessed;
    }

    public ArrayList<Contact> fetchAllFromApp() {
        return fetchContactsPrefs(AppPreferences.CONTACT_BOOK_APP);
    }

    public ArrayList<Contact> fetchAllFromContactBook() {
        String[] projectionFields = new String[]{
                ContactsContract.Contacts._ID,
                ContactsContract.Contacts.DISPLAY_NAME,
        };
        ArrayList<Contact> listContacts = new ArrayList<>();
        CursorLoader cursorLoader = new CursorLoader(context,
                ContactsContract.Contacts.CONTENT_URI,
                projectionFields, // the columns to retrieve
                null, // the selection criteria (none)
                null, // the selection args (none)
                null // the sort order (default)
        );

        Cursor c = cursorLoader.loadInBackground();

        if (c == null) {
            return listContacts;
        }

        final Map<String, Contact> contactsMap = new HashMap<>(c.getCount());

        if (c.moveToFirst()) {

            int idIndex = c.getColumnIndex(ContactsContract.Contacts._ID);
            int nameIndex = c.getColumnIndex(ContactsContract.Contacts.DISPLAY_NAME);

            do {
                String contactId = c.getString(idIndex);
                String contactDisplayName = c.getString(nameIndex);
                Contact contact = new Contact(contactId, contactDisplayName);
                contactsMap.put(contactId, contact);
                listContacts.add(contact);
            } while (c.moveToNext());
        }

        c.close();

        matchContactNumbers(contactsMap);
        matchContactEmails(contactsMap);

        return listContacts;
    }

    public ArrayList<Contact> fetchAllInstalled() {
        return fetchContactsPrefs(AppPreferences.CONTACT_BOOK_INSTALLED);
    }

    public void saveAllInstalled(ArrayList<Contact> c) {
        Set<String> setOrderedContacts;
        OrderedStringSet<Contact> set = new OrderedStringSet<>();
        setOrderedContacts = set.toOrderedStringSet(c);

        Prefs.putOrderedStringSet(AppPreferences.CONTACT_BOOK_INSTALLED, setOrderedContacts);
    }

    public void saveAllToApp(ArrayList<Contact> listToSave) {
        if (listToSave == null
                || listToSave.isEmpty()) {
            return;
        }
        ArrayList<Contact> currentList = fetchAllFromApp();
        currentList.addAll(listToSave);
        Set<String> setOrderedContacts;
        OrderedStringSet<Contact> set = new OrderedStringSet<>();
        setOrderedContacts = set.toOrderedStringSet(currentList);

        Prefs.putOrderedStringSet(AppPreferences.CONTACT_BOOK_APP, setOrderedContacts);
    }

    public void saveContactInstalled(Contact contact) {
        ArrayList<Contact> list = fetchAllInstalled();
        list.add(contact);
        saveAllInstalled(list);
    }

    private ArrayList<Contact> fetchAllRaw() {
        ArrayList<Contact> listContactBook = fetchAllFromContactBook();
        ArrayList<Contact> listContactBookApp = fetchAllFromApp();
        listContactBook.addAll(listContactBookApp);
        return listContactBook;
    }

    private ArrayList<Contact> fetchContactsPrefs(String key) {
        ArrayList<Contact> listContacts;
        Set<String> setOrderedContacts = Prefs.getOrderedStringSet(key, new LinkedHashSet<String>());

        OrderedStringSet<Contact> set = new OrderedStringSet<>();
        listContacts = set.fromOrderedStringSet(setOrderedContacts, Contact.class);

        return listContacts;
    }

    private void matchContactEmails(Map<String, Contact> contactsMap) {
        // Get email
        final String[] emailProjection = new String[]{
                Email.DATA,
                Email.TYPE,
                Email.CONTACT_ID,
        };

        Cursor email = new CursorLoader(context,
                Email.CONTENT_URI,
                emailProjection,
                null,
                null,
                null).loadInBackground();

        if (email == null) {
            return;
        }

        if (email.moveToFirst()) {
            final int contactEmailColumnIndex = email.getColumnIndex(Email.DATA);
            final int contactTypeColumnIndex = email.getColumnIndex(Email.TYPE);
            final int contactIdColumnsIndex = email.getColumnIndex(Email.CONTACT_ID);

            while (!email.isAfterLast()) {
                final String address = email.getString(contactEmailColumnIndex);
                final String contactId = email.getString(contactIdColumnsIndex);
                final int type = email.getInt(contactTypeColumnIndex);
                String customLabel = "Custom";
                Contact contact = contactsMap.get(contactId);
                if (contact == null) {
                    continue;
                }
                CharSequence emailType = ContactsContract.CommonDataKinds.Email
                        .getTypeLabel(context.getResources(), type, customLabel);
                contact.addEmail(address, emailType.toString());
                email.moveToNext();
            }
        }

        email.close();
    }

    private void matchContactNumbers(Map<String, Contact> contactsMap) {
        // Get numbers
        final String[] numberProjection = new String[]{
                Phone.NUMBER,
                Phone.TYPE,
                Phone.CONTACT_ID,
        };

        Cursor phone = new CursorLoader(context,
                Phone.CONTENT_URI,
                numberProjection,
                null,
                null,
                null).loadInBackground();

        if (phone == null) {
            return;
        }

        if (phone.moveToFirst()) {
            final int contactNumberColumnIndex = phone.getColumnIndex(Phone.NUMBER);
            final int contactTypeColumnIndex = phone.getColumnIndex(Phone.TYPE);
            final int contactIdColumnIndex = phone.getColumnIndex(Phone.CONTACT_ID);

            while (!phone.isAfterLast()) {
                final String number = phone.getString(contactNumberColumnIndex);
                final String contactId = phone.getString(contactIdColumnIndex);
                Contact contact = contactsMap.get(contactId);
                if (contact == null) {
                    continue;
                }
                final int type = phone.getInt(contactTypeColumnIndex);
                String customLabel = "Custom";
                CharSequence phoneType = ContactsContract.CommonDataKinds.Phone
                        .getTypeLabel(context.getResources(), type, customLabel);
                contact.addNumber(number, phoneType.toString());
                phone.moveToNext();
            }
        }

        phone.close();
    }
}
package production.app.rina.findme.services.contacts;

import android.content.Context;
import android.content.Intent;
import android.support.annotation.NonNull;
import com.google.i18n.phonenumbers.PhoneNumberUtil;
import com.google.i18n.phonenumbers.PhoneNumberUtil.PhoneNumberFormat;
import com.google.i18n.phonenumbers.Phonenumber;
import java.util.ArrayList;
import production.app.rina.findme.services.common.AppPreferences;

public class ContactManager {

    private ContactList contactList;

    private ArrayList<Contact> contacts;

    private final Context context;

    public ContactManager(Context c) {
        this.context = c;
        contactList = new ContactList(c);
    }

    public ArrayList<Contact> getContacts() {
        if (contacts == null) {
            contacts = contactList.fetchAll();
        }
        return contacts;
    }

    public String getNameByNumber(String number) {
        getContacts();
        String num;
        for (Contact c : contacts) {
            number = c.numbers.get(0).getDigitsFromNumber(number);
            num = c.numbers.get(0).getDigitsFromNumber("");
            if (num.equals(number)) {
                return c.name;
            }
        }
        return "";
    }

    public void putContact(ArrayList<Contact> listContactToSave) {
        contactList.saveAllToApp(listContactToSave);
    }

    public boolean putContact(@NonNull final String name, @NonNull final String number, final String email) {
        String norm = normalizeNumber(number);
        if (norm.length() > 5) {
            ArrayList<Contact> list = new ArrayList<>();
            Contact contact = new Contact(getContactId(), name);
            String s = formatNumber(norm);
            if (s.isEmpty()) {
                contact.addNumber(norm, "custom");
            } else {
                contact.addNumber(s, "custom");
            }
            if (email != null && !email.isEmpty()) {
                contact.addEmail(email, "custom");
            }
            list.add(contact);
            contactList.saveAllToApp(list);
            // Check if number registered on server, if installed saved to app
            contactList.checkContactInstalledServer(contact);
            return true;
        } else {
            return false;
        }
    }

    public void startContactSync() {
        context.startService(new Intent(context, UpdateContactListService.class));
    }

    private String formatNumber(String number) {
        try {
            PhoneNumberUtil phoneNumberUtil = PhoneNumberUtil.getInstance();
            Phonenumber.PhoneNumber phoneNumberObj = phoneNumberUtil.parse(number, "");
            number = phoneNumberUtil.format(phoneNumberObj, PhoneNumberFormat.INTERNATIONAL);
            return number;
        } catch (Exception e) {
            return "";
        }
    }

    private String getContactId() {
        int id = AppPreferences.getNextContactId();
        return "app" + "[" + id + "]";
    }

    private String normalizeNumber(String number) {
        String numNorm;
        boolean isPlus = false;
        StringBuilder s = new StringBuilder();
        if (number.charAt(0) == '+') {
            isPlus = true;
        }
        for (Character c : number.toCharArray()) {
            if (c != '+') { // put constrains
                s.append(c);
            }
        }
        numNorm = s.toString();
        if (numNorm.substring(0, 2).equals("00")) {
            numNorm = numNorm.substring(2, numNorm.length());
            isPlus = true;
        }

        if (isPlus) {
            return "+" + numNorm;
        } else {
            return numNorm;
        }
    }


}

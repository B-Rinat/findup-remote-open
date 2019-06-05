package production.app.rina.findme.services.contacts;

import android.support.annotation.NonNull;
import android.util.Log;
import com.google.i18n.phonenumbers.PhoneNumberUtil;
import com.google.i18n.phonenumbers.PhoneNumberUtil.PhoneNumberFormat;
import com.google.i18n.phonenumbers.Phonenumber;
import java.util.ArrayList;
import production.app.rina.findme.testing.CustomDebugLogger;

public class ContactPhone {

    public String number;

    public String numberInternational;

    public transient String type;

    private CustomDebugLogger log;

    private ArrayList<String> parsedNumber;

    private String phoneFormattedRaw;

    public ContactPhone(String number, String type) {
        log = new CustomDebugLogger();
        this.number = number;
        this.type = type;
    }

    public String getDigitsFromNumber(@NonNull String number) {
        if (number.isEmpty()) {
            number = this.number;
        }
        return number.replaceAll("\\D+", "");
    }

    public String getNumberFormatted() {
        try {
            if (this.number.length() < 6) {
                return "";
            }
            PhoneNumberUtil phoneNumberUtil = PhoneNumberUtil.getInstance();
            Phonenumber.PhoneNumber phoneNumberObj = phoneNumberUtil.parse(this.number, "");
            numberInternational = phoneNumberUtil.format(phoneNumberObj, PhoneNumberFormat.INTERNATIONAL);
            Log.e("PARSE", numberInternational);
            return numberInternational;
        } catch (Exception e) {
            Log.e("PARSE", "exception : " + e);
            numberInternational = this.number;
            return numberInternational;
        }
    }

    /**
     * @return parsed number, i.e. country code, local number
     * or throws Exception
     */
    public ArrayList<String> getParsedNumber() throws Exception {
        if (!isNumberParsable()) {
            throw new Exception("in 'ContactPhone', number is not parsable");
        } else {
            return parsedNumber;
        }
    }

    public String getParsedNumberToString() {
        if (!isNumberParsable()) {
            return "";
        } else {
            return phoneFormattedRaw;
        }
    }

    private boolean isNumberParsable() {
        try {
            parsedNumber = new ArrayList<String>();
            // Google's Library (library libphonenumber)
            PhoneNumberUtil phoneNumberUtil = PhoneNumberUtil.getInstance();
            Phonenumber.PhoneNumber phoneNumberObj = phoneNumberUtil.parse(this.number, "");
            final int countryCode = phoneNumberObj.getCountryCode();
            final long phoneNumberLocal = phoneNumberObj.getNationalNumber();
            String code = Integer.toString(countryCode);
            String phone = Long.toString(phoneNumberLocal);
            parsedNumber.add(code);
            parsedNumber.add(phone);
            phoneFormattedRaw = code + phone;
        } catch (Exception e) {
            return false;
        }
        return true;
    }

}
package production.app.rina.findme.services.common;

import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;

public class Randomizer {

    private final String ALLOWED_CHARACTERS = "01S2T3U4V5W6X7Y8Z9qwertAyBuiCopDasEdfFgGhHjIkJlKzLxMcNvObPnQmR";

    public String getRandomString(final int sizeOfRandomString) {
        SecureRandom random;
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
            try {
                random = SecureRandom.getInstanceStrong();
            } catch (NoSuchAlgorithmException e) {
                random = new SecureRandom();
                e.printStackTrace();
            }
        } else {
            random = new SecureRandom();
        }
        final StringBuilder sb = new StringBuilder(sizeOfRandomString);
        for (int i = 0; i < sizeOfRandomString; i++) {
            sb.append(ALLOWED_CHARACTERS.charAt(random.nextInt(ALLOWED_CHARACTERS.length())));
        }
        return sb.toString();
    }

}

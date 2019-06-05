package production.app.rina.findme.services.common;

import com.google.gson.Gson;
import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.Set;
import production.app.rina.findme.testing.CustomDebugLogger;

public class OrderedStringSet<T> {

    CustomDebugLogger log;

    public OrderedStringSet() {
        log = new CustomDebugLogger();
    }

    public ArrayList<T> fromOrderedStringSet(Set<String> list, Class<T> c) {
        ArrayList<T> arrayList = new ArrayList<>();
        Gson gson = new Gson();
        T object;
        for (String s : list) {
            object = gson.fromJson(s, c);
            if (object != null) {
                log.e("TAG", "fromOrderedStringSet: " + object.toString());
            }
            arrayList.add(object);
        }
        return arrayList;
    }

    public Set<String> toOrderedStringSet(ArrayList<T> list) {
        Set<String> setOrderedList = new LinkedHashSet<>();
        Gson gson = new Gson();
        String s;
        for (T cursor : list) {
            s = gson.toJson(cursor);
            log.e("TAG", "toOrderedStringSet: " + s);
            setOrderedList.add(s);
        }
        return setOrderedList;
    }

}

package production.app.rina.findme.activities.contacts;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Filter;
import android.widget.Filterable;
import android.widget.TextView;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import production.app.rina.findme.R;
import production.app.rina.findme.services.contacts.Contact;
import production.app.rina.findme.services.contacts.ContactPhone;

public class ContactAdapter extends ArrayAdapter<Contact> implements Filterable {

    private ArrayList<Contact> contactArrayList;

    private Comparator<Contact> contactComparator = new Comparator<Contact>() {
        @Override
        public int compare(final Contact o1, final Contact o2) {
            String str1 = ((Contact) o1).name;
            String str2 = ((Contact) o2).name;
            int r = String.CASE_INSENSITIVE_ORDER.compare(str1, str2);
            return (r != 0) ? r : str1.compareTo(str2);
        }
    };

    private ArrayList<Contact> contactDisplayArrayList;

    private Context context;

    private int isVisible;

    private final Object lock = new Object();

    private final String q = "ContactAdapter";

    public ContactAdapter(@NonNull final Context context, final int resource,
            @NonNull final ArrayList<Contact> objects) {
        super(context, resource, objects);
        Collections.sort(objects, contactComparator);
        this.context = context;
        contactArrayList = objects;
        contactDisplayArrayList = objects;
        isVisible = View.INVISIBLE;
    }

    public ContactAdapter(@NonNull final Context context, final int resource,
            @NonNull final ArrayList<Contact> objects, boolean visibility) {
        super(context, resource, objects);
        Collections.sort(objects, contactComparator);
        this.context = context;
        contactArrayList = objects;
        contactDisplayArrayList = objects;
        isVisible = visibility ? View.VISIBLE : View.INVISIBLE;
    }

    @Override
    public int getCount() {
        return contactDisplayArrayList.size();
    }

    @NonNull
    @Override
    public Filter getFilter() {
        Filter filter = new Filter() {

            @Override
            protected FilterResults performFiltering(CharSequence constraint) {
                synchronized (lock) {
                    FilterResults results = new FilterResults();
                    ArrayList<Contact> filteredContactsArrayList = new ArrayList<Contact>();

                    if (constraint == null || constraint.length() == 0) {
                        results.count = contactArrayList.size();
                        results.values = contactArrayList;
                    } else {
                        constraint = constraint.toString().toLowerCase();

                        for (int i = 0; i < contactArrayList.size(); i++) {
                            String rawData = contactArrayList.get(i).name.toLowerCase();
                            if (rawData.contains(constraint.toString())) {
                                Log.e(q, "rawData | " + rawData);
                                Contact contact = new Contact(contactArrayList.get(i).id,
                                        contactArrayList.get(i).name);
                                if (contactArrayList.get(i).numbers == null
                                        || contactArrayList.get(i).numbers.size() == 0) {
                                    ContactPhone contactPhone = new ContactPhone("custom", "custom");
                                    contact.numbers.add(contactPhone);
                                }
                                contact.numbers.add(contactArrayList.get(i).numbers.get(0));
                                if (contactArrayList.get(i).emails != null
                                        && contactArrayList.get(i).emails.size() > 0) {
                                    contact.emails.add(contactArrayList.get(i).emails.get(0));
                                }
                                filteredContactsArrayList.add(contact);
                            }
                        }
                        if (filteredContactsArrayList.size() == 0) {
                            Contact contact = new Contact("", context.getString(R.string.contact_not_found));
                            contact.addNumber("", "");
                            filteredContactsArrayList.add(contact);
                        }
                        results.count = filteredContactsArrayList.size();
                        results.values = (ArrayList<Contact>) filteredContactsArrayList;
                    }
                    return results;
                }
            }

            @SuppressWarnings("unchecked")
            @Override
            protected void publishResults(final CharSequence constraint, final FilterResults results) {
                synchronized (lock) {
                    contactDisplayArrayList = (ArrayList<Contact>) results.values;
                    notifyDataSetChanged();
                }
            }
        };
        return filter;
    }

    @Nullable
    @Override
    public Contact getItem(final int position) {
        return contactDisplayArrayList.get(position);
    }

    @Override
    public long getItemId(final int position) {
        return position;
    }

    @NonNull
    @Override
    public View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {
        View listItem = convertView;
        if (listItem == null) {
            listItem = LayoutInflater.from(context).inflate(R.layout.display_contact_template, parent, false);
        }
        Contact currentContact = contactDisplayArrayList.get(position);
        TextView name = (TextView) listItem.findViewById(R.id.text_contact_name);
        name.setText(currentContact.name);
        TextView locator = (TextView) listItem.findViewById(R.id.text_location_img);
        locator.setVisibility(isVisible);
        TextView circle = listItem.findViewById(R.id.text_solid_circle);
        circle.setVisibility(isVisible);

        if (currentContact.numbers != null && currentContact.numbers.size() > 0) {
            if (isVisible == View.VISIBLE) {
                TextView phone = (TextView) listItem.findViewById(R.id.text_phone_number);
                phone.setText(currentContact.numbers.get(0).number);
            } else {
                TextView phone = (TextView) listItem.findViewById(R.id.text_phone_number);
                phone.setText(currentContact.numbers.get(0).numberInternational);
            }
            if (currentContact.name.equals(context.getString(R.string.contact_not_found)) && currentContact.numbers
                    .get(0).number.equals("")) {
                locator.setVisibility(View.INVISIBLE);
                circle.setVisibility(View.INVISIBLE);
            }
        }
        return listItem;
    }


}

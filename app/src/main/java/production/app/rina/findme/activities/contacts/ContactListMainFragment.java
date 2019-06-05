package production.app.rina.findme.activities.contacts;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.Toast;
import java.util.ArrayList;
import production.app.rina.findme.R;
import production.app.rina.findme.services.common.AppPreferences;
import production.app.rina.findme.services.contacts.Contact;
import production.app.rina.findme.services.contacts.ContactList;
import production.app.rina.findme.services.location.LocateUsersOnMapActivity;
import production.app.rina.findme.services.meetings.MeetingManager;
import production.app.rina.findme.utils.AppUtils;

public class ContactListMainFragment extends Fragment {

    public Activity activity;

    EditText searcher;

    TextWatcher textWatcher;

    private ListView listView;

    public static ContactListMainFragment newInstance(String text) {

        ContactListMainFragment f = new ContactListMainFragment();
        Bundle b = new Bundle();
        b.putString("msg", text);

        f.setArguments(b);

        return f;
    }

    @Override
    public void onCreate(@Nullable final Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    // Start new Meeting
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.contact_list_main_fragment, container, false);

        ContactList contactList = new ContactList(getActivity());
        listView = v.findViewById(R.id.list_contact_main_fragment);
        ArrayList<Contact> contacts = contactList.fetchAllInstalled();
        final ContactAdapter contactAdapter = new ContactAdapter(getActivity(), 0, contacts, true);
        listView.setAdapter(contactAdapter);

        setUserVisibleHint(true);

        searcher = getActivity().findViewById(R.id.edit_search_contact);
        searcher.addTextChangedListener(textWatcher = new TextWatcher() {
            @Override
            public void afterTextChanged(final Editable s) {

            }

            @Override
            public void beforeTextChanged(final CharSequence s, final int start, final int count,
                    final int after) {
            }

            @Override
            public void onTextChanged(final CharSequence s, final int start, final int before, final int count) {
                contactAdapter.getFilter().filter(s.toString());
            }
        });

        listView.setOnItemClickListener(new OnItemClickListener() {
            @Override
            public void onItemClick(final AdapterView<?> parent, final View view, final int position, final long id) {
                Log.e("TAG", "position: " + position);
                final Contact contact = contactAdapter.getItem(position);
                Log.e("TAG", contact.toString());
                /**
                 * Carry to background
                 * Check network availability
                 */
                if (!AppUtils.isNetworkAvailable(activity)) {
                    Toast.makeText(getContext(), getString(R.string.check_internet_con), Toast.LENGTH_SHORT).show();
                    return;
                }
                if (contact.name.equals(getContext().getString(R.string.contact_not_found)) && contact.numbers
                        .get(0).number.equals("")) {
                    return;
                }
                new Thread(new Runnable() {
                    @Override
                    public void run() {
                        final MeetingManager manager = new MeetingManager(getActivity());
                        manager.createMeeting("defaultGuestName",
                                contact.numbers.get(0).getDigitsFromNumber(contact.numbers.get(0).number));
                        String meeting_state = AppPreferences.saveMeetingState(manager);
                        Intent intent = new Intent(activity.getBaseContext(), LocateUsersOnMapActivity.class);
                        intent.putExtra("thread_meeting_created", meeting_state);
                        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                        activity.getBaseContext().startActivity(intent);
                    }
                }).start();
                Intent intent = new Intent(getActivity(), LocateUsersOnMapActivity.class);
                startActivity(intent);
            }
        });

        return v;
    }

    @Override
    public void setUserVisibleHint(boolean isVisibleToUser) {
        super.setUserVisibleHint(isVisibleToUser);
        activity = getActivity();
        if (!isVisibleToUser) {
            if (searcher != null) {
                // searcher.removeTextChangedListener(textWatcher);
            }
        }
    }
}

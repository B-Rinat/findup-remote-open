package production.app.rina.findme.activities.contacts;

import android.Manifest.permission;
import android.app.Activity;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.telephony.PhoneNumberFormattingTextWatcher;
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
import production.app.rina.findme.services.common.PermissionManager;
import production.app.rina.findme.services.common.PermissionManager.PermissionResult;
import production.app.rina.findme.services.common.ViewFocus;
import production.app.rina.findme.services.contacts.Contact;
import production.app.rina.findme.services.contacts.ContactManager;

public class ContactListFragment extends Fragment implements View.OnClickListener{

    ContactManager contactManager;

    EditText searcher;
    TextWatcher textWatcher;


    public ViewFocus addNewContact;

    public View[] listContactParam;

    public static ContactListFragment newInstance(String text) {

        ContactListFragment f = new ContactListFragment();
        Bundle b = new Bundle();
        b.putString("msg", text);

        f.setArguments(b);

        return f;
    }

    @Override
    public void onCreate(@Nullable final Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    /**
     * My contacts list
     * @param inflater
     * @param container
     * @param savedInstanceState
     * @return
     */
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.contact_list_fragment, container, false);

        contactManager = new ContactManager(getActivity());

        ListView listView = v.findViewById(R.id.list_contact_fragment);
        PermissionManager permissionManager = new PermissionManager(getActivity());
        ArrayList<Contact> contacts = null;
        if (permissionManager.checkPermissionGranted(permission.READ_CONTACTS)) {
            contacts = contactManager.getContacts();
        } else {
            permissionManager.requestPermissions(new String[]{permission.READ_CONTACTS}, new PermissionResult() {
                @Override
                public void onResult(final String[] ungranted) throws SecurityException {
                    if (ungranted == null || ungranted.length < 1) {
                    }
                }
            });
            return v;
        }
        final ContactAdapter contactAdapter = new ContactAdapter(getActivity(), 0, contacts);
        listView.setAdapter(contactAdapter);
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
                Contact c = contactAdapter.getItem(position);

                Log.e("TAG", "position: " + position + " c: " + c.toString());
            }
        });

        return v;
    }

    @Override
    public void onViewCreated(@NonNull final View view, @Nullable final Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        View addContact = getView().findViewById(R.id.image_add_new_contact);
        addContact.setOnClickListener(this);
    }

    @Override
    public void onClick(final View v) {
        switch (v.getId()){
            case R.id.image_add_new_contact: {
                manageViewOnFocusChanged();
                break;
            }
            case R.id.button_save_new_contact: {
                saveNewContact();
                break;
            }
            case R.id.image_tool_bar_back_btn: {
                startActivity(getActivity().getIntent());
                getActivity().finish();
                break;
            }
        }
    }

    private void manageViewOnFocusChanged() {
        getActivity().setContentView(R.layout.add_new_contact);
        addNewContact = new ViewFocus(getActivity());
        listContactParam = new View[3];
        listContactParam[0] = getActivity().findViewById(R.id.edit_new_contact_name);
        listContactParam[1] = getActivity().findViewById(R.id.edit_country_code_new_contact);
        ((EditText) listContactParam[1]).addTextChangedListener(new PhoneNumberFormattingTextWatcher());
        listContactParam[2] = getActivity().findViewById(R.id.edit_new_contact_email);

        View back = getActivity().findViewById(R.id.image_tool_bar_back_btn);
        back.setOnClickListener(this);
        back.setVisibility(View.VISIBLE);

        View save = getActivity().findViewById(R.id.button_save_new_contact);
        save.setOnClickListener(this);

        addNewContact.setOnClickHelper(listContactParam);
    }

    private void saveNewContact() {
        if (!addNewContact.isViewNullOrEmpty(listContactParam[0], getString(R.string.field_name_empty))) {
            if (!addNewContact.isViewNullOrEmpty(listContactParam[1], getString(R.string.ensure_correct_phone_num))) {
                final String name = ((EditText) listContactParam[0]).getText().toString();
                final String number = ((EditText) listContactParam[1]).getText().toString();
                final String email = ((EditText) listContactParam[2]).getText().toString();
                getActivity().setContentView(R.layout.contacts_holder_main);
                new Thread(new Runnable() {
                    @Override
                    public void run() {
                        final boolean result = contactManager.putContact(name, number, email);
                        getActivity().runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                if (result) {
                                    Toast.makeText(getContext(),
                                            getContext().getString(R.string.added_new_contact),
                                            Toast.LENGTH_SHORT).show();
                                } else {
                                    Toast.makeText(getContext(),
                                            getContext().getString(R.string.contact_not_saved),
                                            Toast.LENGTH_SHORT).show();
                                }
                            }
                        });
                    }
                }).start();
                startActivity(getActivity().getIntent());
                getActivity().finish();
            }
        }
    }

    @Override
    public void setUserVisibleHint(boolean isVisibleToUser) {
        super.setUserVisibleHint(isVisibleToUser);
        if (!isVisibleToUser) {
            if (searcher != null) {
                // searcher.removeTextChangedListener(textWatcher);
            }
        }
    }

}

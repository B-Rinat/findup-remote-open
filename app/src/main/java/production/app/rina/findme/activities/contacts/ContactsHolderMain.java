package production.app.rina.findme.activities.contacts;

import static production.app.rina.findme.activities.permission.PermissionsUser.hasPermissions;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.design.widget.TabLayout;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.view.ViewPager;
import android.telephony.PhoneNumberFormattingTextWatcher;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;
import production.app.rina.findme.R;
import production.app.rina.findme.activities.meetings.InvitationListFragment;
import production.app.rina.findme.services.common.ViewFocus;
import production.app.rina.findme.services.contacts.ContactManager;

public class ContactsHolderMain extends FragmentActivity implements View.OnClickListener {

    private class PagerAdapterCustom extends FragmentPagerAdapter {

        PagerAdapterCustom(FragmentManager fm) {
            super(fm);
        }

        @Override
        public int getCount() {
            return 3;
        }

        @Override
        public Fragment getItem(int pos) {
            switch (pos) {
                case 0:
                    return InvitationListFragment.newInstance("FirstFragment, Instance 1");
                case 1:
                    return ContactListMainFragment.newInstance("SecondFragment, Instance 1");
                case 2:
                    return ContactListFragment.newInstance("s");
                default:
                    return ContactListMainFragment.newInstance("ThirdFragment, Default");
            }
        }

        @Nullable
        @Override
        public CharSequence getPageTitle(final int position) {
            switch (position) {
                case 0:
                    return getString(R.string.invitations);
                case 1:
                    return getString(R.string.start_meeting);
                case 2:
                    return getString(R.string.my_contacts);
                default:
                    return "";
            }
        }
    }

    public ViewFocus addNewContact;

    public View[] listContactParam;

    final String q = "ContactsHolderMain";

    private ContactManager contactManager;

    private TabLayout tabLayout;

    @SuppressLint("NewApi")
    @Override
    public void onCreate(Bundle savedInstanceState) {
        Log.e(q, "onCreate");
        super.onCreate(savedInstanceState);
        setContentView(R.layout.contacts_holder_main);

        contactManager = new ContactManager(this);
        ViewPager pager = (ViewPager) findViewById(R.id.viewPager);
        pager.setAdapter(new PagerAdapterCustom(getSupportFragmentManager()));

        tabLayout = findViewById(R.id.tab_mode_layout);
        tabLayout.setupWithViewPager(pager);

        if (hasPermissions(this, android.Manifest.permission.READ_CONTACTS)){
            // sync all user contacts with server
            ContactManager contactManager = new ContactManager(this);
            contactManager.startContactSync();
        }
    }

    @Override
    protected void onStart() {
        super.onStart();
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        Intent homeIntent = new Intent(Intent.ACTION_MAIN);
        homeIntent.addCategory(Intent.CATEGORY_HOME);
        homeIntent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        startActivity(homeIntent);
    }

    @Override
    public void onClick(final View v) {
        switch (v.getId()) {
            default: {
            }
        }
    }
}

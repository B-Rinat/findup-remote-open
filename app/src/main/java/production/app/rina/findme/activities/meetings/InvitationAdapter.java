package production.app.rina.findme.activities.meetings;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import production.app.rina.findme.R;
import production.app.rina.findme.services.contacts.ContactManager;
import production.app.rina.findme.services.meetings.Invitation;

public class InvitationAdapter extends ArrayAdapter<Invitation> {

    private ContactManager contactManager;

    private Context context;

    private Comparator<Invitation> dateComparator = new Comparator<Invitation>() {
        @Override
        public int compare(final Invitation o1, final Invitation o2) {
            String str1 = ((Invitation) o1).getInvitationTime();
            String str2 = ((Invitation) o2).getInvitationTime();
            int r = String.CASE_INSENSITIVE_ORDER.compare(str1, str2);
            return (r != 0) ? r : str1.compareTo(str2);
        }
    };

    private ArrayList<Invitation> invitationArrayList;

    private ArrayList<Invitation> invitationDisplayArrayList;

    public InvitationAdapter(@NonNull final Context context, final int resource,
            @NonNull final ArrayList<Invitation> objects) {
        super(context, resource, objects);
        Collections.sort(objects, dateComparator);
        this.context = context;
        invitationArrayList = objects;
        invitationDisplayArrayList = objects;
        contactManager = new ContactManager(context);
    }

    @Override
    public int getCount() {
        return invitationDisplayArrayList.size();
    }


    @Nullable
    @Override
    public Invitation getItem(final int position) {
        return invitationDisplayArrayList.get(position);
    }

    @Override
    public long getItemId(final int position) {
        return position;
    }

    @NonNull
    @Override
    public View getView(final int position, @Nullable View convertView, @NonNull final ViewGroup parent) {
        View listItem = convertView;
        if (listItem == null) {
            listItem = LayoutInflater.from(context).inflate(R.layout.display_invitation_template, parent, false);
        }

        Invitation currentInvitation = invitationDisplayArrayList.get(position);

        TextView name = listItem.findViewById(R.id.text_contact_name);
        TextView number = listItem.findViewById(R.id.text_phone_number);
        TextView date = listItem.findViewById(R.id.text_date_time);

        name.setText(contactManager.getNameByNumber(currentInvitation.participant.getNumber()));
        number.setText(currentInvitation.participant.getNumber());
        date.setText(currentInvitation.getInvitationTime());

        final Button reject = (Button) listItem.findViewById(R.id.button_reject);
        final Button accept = (Button) listItem.findViewById(R.id.button_accept);

        reject.setTag(position);
        reject.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(final View v) {
                Log.e("TAG", "reject");
                ((ListView) parent).performItemClick(v, position, 0);
            }
        });
        accept.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(final View v) {
                Log.e("TAG", "accept");
                ((ListView) parent).performItemClick(v, position, 0);
            }
        });

        return listItem;
    }

    public boolean refreshInvitations(ArrayList<Invitation> invitations) {
        if (!invitations.isEmpty()) {
            this.invitationArrayList = invitations;
            this.invitationDisplayArrayList = invitations;
            notifyDataSetChanged();
        }
        return true;
    }

    public void remove(int position) {
        invitationDisplayArrayList.remove(position);
        notifyDataSetChanged();
    }
}

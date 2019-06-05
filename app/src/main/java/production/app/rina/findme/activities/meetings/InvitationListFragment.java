package production.app.rina.findme.activities.meetings;

import static production.app.rina.findme.services.common.AppPreferences.getUserPhone;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.NonNull;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.Button;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.TextView;
import com.pixplicity.easyprefs.library.Prefs;
import java.util.ArrayList;
import java.util.Objects;
import production.app.rina.findme.R;
import production.app.rina.findme.services.common.AppPreferences;
import production.app.rina.findme.services.location.LocateUsersOnMapActivity;
import production.app.rina.findme.services.meetings.Invitation;
import production.app.rina.findme.services.meetings.InvitationList;
import production.app.rina.findme.services.meetings.Meeting;
import production.app.rina.findme.services.meetings.MeetingManager;
import production.app.rina.findme.utils.AppUtils;

public class InvitationListFragment extends Fragment {

    InvitationAdapter invitationAdapter;

    InvitationList invitationList;

    private ListView listView;

    public static InvitationListFragment newInstance(String text) {

        InvitationListFragment f = new InvitationListFragment();
        Bundle b = new Bundle();
        b.putString("msg", text);

        f.setArguments(b);

        return f;
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.invitation_list_fragment, container, false);

        invitationList = new InvitationList(getActivity());
        listView = v.findViewById(R.id.list_invitations);
        ArrayList<Invitation> invitations = new ArrayList<>();

        invitationAdapter = new InvitationAdapter(getActivity(), 0, invitations);
        listView.setAdapter(invitationAdapter);

        ProgressBar bar = getActivity().findViewById(R.id.progress_bar);
        bar.setVisibility(View.VISIBLE);

        updateInvitationList();

        setUserVisibleHint(true);

        return v;
    }

    @Override
    public void setUserVisibleHint(boolean isVisibleToUser) {
        super.setUserVisibleHint(isVisibleToUser);
    }

    private void setOnItemClickListener() {
        listView.setOnItemClickListener(new OnItemClickListener() {
            @Override
            public void onItemClick(final AdapterView<?> parent, final View view, final int position, final long id) {

                final Invitation invitation = invitationAdapter.getItem(position);

                Log.e("TAG", "position: " + position + " c: " + invitation.toString());

                long clickedViewId = view.getId();
                if (clickedViewId == R.id.button_accept) {
                    Log.e("TAG", "accepted");
                    invitation.onAcceptInvitationNotif();
                    Meeting meeting = new Meeting(getActivity(), invitation);
                    MeetingManager manager = new MeetingManager(getActivity(), meeting);
                    manager.updateMeetingHash(getUserPhone(), invitation.meetingHash);
                    manager.guestNumber = invitation.participant.getNumber();
                    AppPreferences.saveMeetingState(manager);
                    Intent intent = new Intent(getActivity(), LocateUsersOnMapActivity.class);
                    // start map activity
                    startActivity(intent);
                }
                if (clickedViewId == R.id.button_reject) {
                    invitationAdapter.remove((int) view.getTag());
                    Log.e("TAG", "rejected" + " count: " + invitationAdapter.getCount());
                    new Thread(new Runnable() {
                        @Override
                        public void run() {
                            Log.e("TAG", "background Thread");
                            invitation.onRejectInvitationNotif();
                        }
                    }).start();
                }
            }
        });
    }

    private void updateInvitationList() {
        new Thread(new Runnable() {
            @Override
            public void run() {
                final ArrayList<Invitation> updatedInvitations = invitationList.getAllInvitations();
                Objects.requireNonNull(getActivity()).runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        listView.setVisibility(View.VISIBLE);
                        final Button refreshInvitations = Objects.requireNonNull(getActivity())
                                .findViewById(R.id.button_refresh_invitations);
                        if (refreshInvitations != null) {
                            refreshInvitations.setBackgroundColor(
                                    getResources().getColor(R.color.actionButton));
                            refreshInvitations.setOnClickListener(new OnClickListener() {
                                @Override
                                public void onClick(final View v) {
                                    refreshInvitations
                                            .setBackgroundColor(getResources().getColor(R.color.buttonActive));
                                    updateInvitationList();
                                }
                            });
                        }
                        if (updatedInvitations.isEmpty()) {
                            listView.setVisibility(View.INVISIBLE);
                            if (!AppUtils.isNetworkAvailable(getContext())) {
                                TextView internetFailure = getActivity()
                                        .findViewById(R.id.text_internet_failure_notif);
                                internetFailure.setText(R.string.check_internet_con);
                                internetFailure.setVisibility(View.VISIBLE);
                                if (refreshInvitations != null) {
                                    refreshInvitations.setVisibility(View.VISIBLE);
                                }
                            } else {
                                TextView noInvitations = getActivity()
                                        .findViewById(R.id.text_no_invitations);
                                noInvitations.setText(R.string.no_new_invitations);
                                noInvitations.setVisibility(View.VISIBLE);
                            }
                        } else {
                            TextView noInvitations = getActivity()
                                    .findViewById(R.id.text_no_invitations);
                            noInvitations.setVisibility(View.GONE);
                        }
                        new Handler().postDelayed(new Runnable() {
                            @Override
                            public void run() {
                                Activity current = getActivity();
                                TextView internetFailure = null;
                                if (current != null) {
                                    internetFailure = current.findViewById(R.id.text_internet_failure_notif);
                                }
                                if (internetFailure != null) {
                                    internetFailure.setVisibility(View.INVISIBLE);
                                }
                            }
                        }, 7000);

                        invitationAdapter.refreshInvitations(updatedInvitations);
                        ProgressBar bar = getActivity().findViewById(R.id.progress_bar);
                        if (bar != null) {
                            bar.setVisibility(View.INVISIBLE);
                        }

                        setOnItemClickListener();
                    }
                });
            }
        }).start();
    }
}

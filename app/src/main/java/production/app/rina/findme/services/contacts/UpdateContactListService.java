package production.app.rina.findme.services.contacts;

import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.os.Handler;
import android.os.IBinder;
import android.os.Looper;
import android.support.annotation.Nullable;
import java.util.ArrayList;
import production.app.rina.findme.R;
import production.app.rina.findme.services.network.ReportPerformer;
import production.app.rina.findme.testing.CustomDebugLogger;
import production.app.rina.findme.utils.AppUtils;

public class UpdateContactListService extends Service {

    ArrayList<Contact> contacts = null;

    private Context appContext;

    private boolean isFinishedReadingContacts = false;

    private CustomDebugLogger log = new CustomDebugLogger();

    private ReportPerformer report;

    @Override
    public void onCreate() {
        appContext = getApplicationContext();
        super.onCreate();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        log.e("TAG", "Service stopped | UpdateContactListService");
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        log.e("TAG", "onStartCommand | START SERVICE: | UpdateContactListService");
        startCheckingContactList();
        return START_STICKY;
    }

    private void startCheckingContactList() {
        final ArrayList<String> keys = new ArrayList<>();
        final ArrayList<String> jsonKeys = new ArrayList<>();

        keys.add("phone");
        jsonKeys.add("phone_status");

        Handler t = new Handler();
        Runnable r;
        r = new Runnable() {
            @Override
            public void run() {
                final ContactList listContacts = new ContactList(appContext);
                new Handler(Looper.getMainLooper()).post(new Runnable() {
                    @Override
                    public void run() {
                        log.e("TAG", "fetching contacts ...");
                        contacts = listContacts.fetchAll();
                        isFinishedReadingContacts = true;
                    }
                });
                Thread t = new Thread((new Runnable() {
                    @Override
                    public void run() {
                        while (!isFinishedReadingContacts) {
                            try {
                                Thread.sleep(3000);
                            } catch (InterruptedException e) {
                                e.printStackTrace();
                            }
                        }
                        log.e("TAG", "starting contact sync | network");
                        ArrayList<Contact> installed = new ArrayList<Contact>();
                        if (contacts == null || contacts.size() == 0) {
                            return;
                        }
                        for (int i = 0; i < contacts.size(); i++) {
                            try {
                                if (!AppUtils.isNetworkAvailable(appContext)) {
                                    return;
                                }
                                String number = contacts.get(i).numbers.get(0).getParsedNumberToString();
                                if (number == null || number.isEmpty()) {
                                    continue;
                                }
                                ArrayList<String> values = new ArrayList<>();
                                values.add(number);
                                report = new ReportPerformer(
                                        getApplicationContext().getString(R.string.REPORT_PHONE_STATUS_CHECK), keys,
                                        values, jsonKeys);
                                ArrayList<String> result = report.execute();

                                if (result.get(0).equals("approve")) {
                                    log.e("TAG", "number: " + number + " status: " + result.get(0));
                                    installed.add(contacts.get(i));
                                } else {
                                    log.e("TAG", "number: " + number + " status: " + result.get(0));
                                }

                            } catch (Exception e) {
                                e.printStackTrace();
                            }
                        }
                        listContacts.saveAllInstalled(installed);

                    }
                }));
                t.start();
            }
        };
        t.post(r);
    }
}

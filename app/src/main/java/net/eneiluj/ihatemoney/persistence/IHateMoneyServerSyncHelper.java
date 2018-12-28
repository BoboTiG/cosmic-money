package net.eneiluj.ihatemoney.persistence;

import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.content.SharedPreferences;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.AsyncTask;
import android.os.IBinder;
//import android.preference.PreferenceManager;
import android.support.v7.preference.PreferenceManager;
import android.util.ArrayMap;
import android.util.Log;

import org.json.JSONException;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import at.bitfire.cert4android.CustomCertManager;
import at.bitfire.cert4android.CustomCertService;
import net.eneiluj.ihatemoney.R;
import net.eneiluj.ihatemoney.android.activity.BillsListViewActivity;

import net.eneiluj.ihatemoney.model.DBBill;
import net.eneiluj.ihatemoney.model.DBBillOwer;
import net.eneiluj.ihatemoney.model.DBMember;
import net.eneiluj.ihatemoney.model.DBProject;
import net.eneiluj.ihatemoney.util.ICallback;
import net.eneiluj.ihatemoney.util.IHateMoneyClient;
import net.eneiluj.ihatemoney.util.PhoneTrackClientUtil.LoginStatus;
import net.eneiluj.ihatemoney.util.ServerResponse;
import net.eneiluj.ihatemoney.util.SupportUtil;

/**
 * Helps to synchronize the Database to the Server.
 */
public class IHateMoneyServerSyncHelper {

    public static final String BROADCAST_SESSIONS_SYNC_FAILED = "net.eneiluj.ihatemoney.broadcast.sessions_sync_failed";
    public static final String BROADCAST_SESSIONS_SYNCED = "net.eneiluj.ihatemoney.broadcast.sessions_synced";

    private static IHateMoneyServerSyncHelper instance;

    /**
     * Get (or create) instance from IHateMoneyServerSyncHelper.
     * This has to be a singleton in order to realize correct registering and unregistering of
     * the BroadcastReceiver, which listens on changes of network connectivity.
     *
     * @param dbHelper IHateMoneySQLiteOpenHelper
     * @return IHateMoneyServerSyncHelper
     */
    public static synchronized IHateMoneyServerSyncHelper getInstance(IHateMoneySQLiteOpenHelper dbHelper) {
        if (instance == null) {
            instance = new IHateMoneyServerSyncHelper(dbHelper);
        }
        return instance;
    }

    private final IHateMoneySQLiteOpenHelper dbHelper;
    private final Context appContext;

    private CustomCertManager customCertManager;

    // Track network connection changes using a BroadcastReceiver
    private boolean networkConnected = false;
    private final BroadcastReceiver networkReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            updateNetworkStatus();
            if (isSyncPossible()) {
                String lastId = PreferenceManager.getDefaultSharedPreferences(context).getString("last_selected_project", "");
                if (!lastId.equals("")) {
                    scheduleSync(false, Long.valueOf(lastId));
                }
            }
        }
    };

    private boolean cert4androidReady = false;
    private final ServiceConnection certService = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName componentName, IBinder iBinder) {
            cert4androidReady = true;
            if (isSyncPossible()) {
                String lastId = PreferenceManager.getDefaultSharedPreferences(dbHelper.getContext()).getString("last_selected_project", "");
                if (!lastId.equals("")) {
                    scheduleSync(false, Long.valueOf(lastId));
                }
            }
        }

        @Override
        public void onServiceDisconnected(ComponentName componentName) {
            cert4androidReady = false;
        }
    };

    // current state of the synchronization
    private boolean syncActive = false;
    private boolean syncScheduled = false;

    // list of callbacks for both parts of synchronziation
    private List<ICallback> callbacksPush = new ArrayList<>();
    private List<ICallback> callbacksPull = new ArrayList<>();


    private IHateMoneyServerSyncHelper(IHateMoneySQLiteOpenHelper db) {
        this.dbHelper = db;
        this.appContext = db.getContext().getApplicationContext();
        new Thread() {
            @Override
            public void run() {
                customCertManager = SupportUtil.getCertManager(appContext);
            }
        }.start();

        // Registers BroadcastReceiver to track network connection changes.
        appContext.registerReceiver(networkReceiver, new IntentFilter(ConnectivityManager.CONNECTIVITY_ACTION));
        updateNetworkStatus();
        // bind to certifciate service to block sync attempts if service is not ready
        appContext.bindService(new Intent(appContext, CustomCertService.class), certService, Context.BIND_AUTO_CREATE);
    }

    @Override
    protected void finalize() throws Throwable {
        appContext.unregisterReceiver(networkReceiver);
        appContext.unbindService(certService);
        if (customCertManager != null) {
            customCertManager.close();
        }
        super.finalize();
    }

    /*public static boolean isConfigured(Context context) {
        return !PreferenceManager.getDefaultSharedPreferences(context).getString(SettingsActivity.SETTINGS_URL, SettingsActivity.DEFAULT_SETTINGS).isEmpty();
    }*/

    /**
     * Synchronization is only possible, if there is an active network connection and
     * Cert4Android service is available.
     * IHateMoneyServerSyncHelper observes changes in the network connection.
     * The current state can be retrieved with this method.
     *
     * @return true if sync is possible, otherwise false.
     */
    public boolean isSyncPossible() {
        return networkConnected && cert4androidReady;
    }

    public CustomCertManager getCustomCertManager() {
        return customCertManager;
    }

    /**
     * Adds a callback method to the IHateMoneyServerSyncHelper for the synchronization part push local changes to the server.
     * All callbacks will be executed once the synchronization operations are done.
     * After execution the callback will be deleted, so it has to be added again if it shall be
     * executed the next time all synchronize operations are finished.
     *
     * @param callback Implementation of ICallback, contains one method that shall be executed.
     */
    public void addCallbackPush(ICallback callback) {
        callbacksPush.add(callback);
    }

    /**
     * Adds a callback method to the IHateMoneyServerSyncHelper for the synchronization part pull remote changes from the server.
     * All callbacks will be executed once the synchronization operations are done.
     * After execution the callback will be deleted, so it has to be added again if it shall be
     * executed the next time all synchronize operations are finished.
     *
     * @param callback Implementation of ICallback, contains one method that shall be executed.
     */
    public void addCallbackPull(ICallback callback) {
        callbacksPull.add(callback);
    }


    /**
     * Schedules a synchronization and start it directly, if the network is connected and no
     * synchronization is currently running.
     *
     * @param onlyLocalChanges Whether to only push local changes to the server or to also load the whole list of sessions from the server.
     */
    public void scheduleSync(boolean onlyLocalChanges, long projId) {
        Log.d(getClass().getSimpleName(), "Sync requested (" + (onlyLocalChanges ? "onlyLocalChanges" : "full") + "; " + (syncActive ? "sync active" : "sync NOT active") + ") ...");
        Log.d(getClass().getSimpleName(), "(network:" + networkConnected + "; cert4android:" + cert4androidReady + ")");
        if (isSyncPossible() && (!syncActive || onlyLocalChanges)) {
            Log.d(getClass().getSimpleName(), "... starting now");
            SyncTask syncTask = new SyncTask(onlyLocalChanges, projId);
            syncTask.addCallbacks(callbacksPush);
            callbacksPush = new ArrayList<>();
            if (!onlyLocalChanges) {
                syncTask.addCallbacks(callbacksPull);
                callbacksPull = new ArrayList<>();
            }
            syncTask.execute();
        } else if (!onlyLocalChanges) {
            Log.d(getClass().getSimpleName(), "... scheduled");
            syncScheduled = true;
            for (ICallback callback : callbacksPush) {
                callback.onScheduled();
            }
        } else {
            Log.d(getClass().getSimpleName(), "... do nothing");
            for (ICallback callback : callbacksPush) {
                callback.onScheduled();
            }
        }
    }

    private void updateNetworkStatus() {
        ConnectivityManager connMgr = (ConnectivityManager) appContext.getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo activeInfo = connMgr.getActiveNetworkInfo();
        if (activeInfo != null && activeInfo.isConnected()) {
            Log.d(IHateMoneyServerSyncHelper.class.getSimpleName(), "Network connection established.");
            networkConnected = true;
        } else {
            networkConnected = false;
            Log.d(IHateMoneyServerSyncHelper.class.getSimpleName(), "No network connection.");
        }
    }

    /**
     * SyncTask is an AsyncTask which performs the synchronization in a background thread.
     * Synchronization consists of two parts: pushLocalChanges and pullRemoteChanges.
     */
    private class SyncTask extends AsyncTask<Void, Void, LoginStatus> {
        private final boolean onlyLocalChanges;
        private DBProject project;
        private final List<ICallback> callbacks = new ArrayList<>();
        private IHateMoneyClient client;
        private List<Throwable> exceptions = new ArrayList<>();

        public SyncTask(boolean onlyLocalChanges, long projId) {
            this.onlyLocalChanges = onlyLocalChanges;
            Log.i(getClass().getSimpleName(), "SYNC TASK pid : "+projId);
            this.project = dbHelper.getProject(projId);
        }

        public void addCallbacks(List<ICallback> callbacks) {
            this.callbacks.addAll(callbacks);
        }

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            if (!onlyLocalChanges && syncScheduled) {
                syncScheduled = false;
            }
            syncActive = true;
        }

        @Override
        protected LoginStatus doInBackground(Void... voids) {
            client = createIHateMoneyClient(); // recreate PhoneTrackClients on every sync in case the connection settings was changed
            Log.i(getClass().getSimpleName(), "STARTING SYNCHRONIZATION");
            //dbHelper.debugPrintFullDB();
            LoginStatus status = LoginStatus.OK;
            // TODO
            status = pushLocalChanges();
            if (!onlyLocalChanges) {
                status = pullRemoteChanges();
            }
            //dbHelper.debugPrintFullDB();
            Log.i(getClass().getSimpleName(), "SYNCHRONIZATION FINISHED");
            return status;
        }

        private LoginStatus pushLocalChanges() {
            LoginStatus status;

            Log.d(getClass().getSimpleName(), "PUSH LOCAL CHANGES");

            try {
                // TODO push member changes BEFORE

                // get members
                List<DBMember> members = dbHelper.getMembersOfProject(project.getId());
                // get member id map
                Map<Long, Long> memberIdToRemoteId = new ArrayMap<>();
                for (DBMember m : members) {
                    memberIdToRemoteId.put(m.getId(), m.getRemoteId());
                }

                // delete what's been deleted
                List<DBBill> toDelete = dbHelper.getBillsOfProjectWithState(project.getId(), DBBill.STATE_DELETED);
                for (DBBill bToDel : toDelete) {
                    ServerResponse.DeleteRemoteBillResponse deleteRemoteBillResponse = client.deleteRemoteBill(customCertManager, project, bToDel.getRemoteId());
                    if (deleteRemoteBillResponse.getStringContent().equals("OK")) {
                        dbHelper.deleteBill(bToDel.getId());
                    }
                }
                // edit what's been edited
                List<DBBill> toEdit = dbHelper.getBillsOfProjectWithState(project.getId(), DBBill.STATE_EDITED);
                for (DBBill bToEdit : toEdit) {
                    ServerResponse.EditRemoteBillResponse editRemoteBillResponse = client.editRemoteBill(customCertManager, project, bToEdit, memberIdToRemoteId);
                    if (editRemoteBillResponse.getStringContent().equals(String.valueOf(bToEdit.getRemoteId()))) {
                        dbHelper.setBillState(bToEdit.getId(), DBBill.STATE_OK);
                        Log.d(getClass().getSimpleName(), "SUCESS to edit bill ("+editRemoteBillResponse.getStringContent()+")");
                    }
                    else {
                        Log.d(getClass().getSimpleName(), "FAILED to edit bill ("+editRemoteBillResponse.getStringContent()+")");
                    }
                }
                // add what's been added
                List<DBBill> toAdd = dbHelper.getBillsOfProjectWithState(project.getId(), DBBill.STATE_ADDED);
                for (DBBill bToAdd : toAdd) {
                    ServerResponse.CreateRemoteBillResponse createRemoteBillResponse = client.createRemoteBill(customCertManager, project, bToAdd, memberIdToRemoteId);
                    long newRemoteId = Long.valueOf(createRemoteBillResponse.getStringContent());
                    if (newRemoteId > 0) {
                        dbHelper.updateBill(
                                bToAdd.getId(), newRemoteId, null,
                                null, null, null, DBBill.STATE_OK
                        );
                    }
                }
                status = LoginStatus.OK;
            } catch (ServerResponse.NotModifiedException e) {
                Log.d(getClass().getSimpleName(), "No changes, nothing to do.");
                status = LoginStatus.OK;
            } catch (IOException e) {
                Log.e(getClass().getSimpleName(), "Exception", e);
                exceptions.add(e);
                status = LoginStatus.CONNECTION_FAILED;
            }
            Log.d(getClass().getSimpleName(), "END PUSH LOCAL CHANGES");
            return status;
        }

        /**
         * Pull remote Changes: update or create each remote session and remove remotely deleted sessions.
         */
        private LoginStatus pullRemoteChanges() {
            // TODO add/remove sessions
            Log.d(getClass().getSimpleName(), "pullRemoteChanges("+project+")");
            SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(appContext);
            String lastETag = null;
            long lastModified = 0;
            LoginStatus status;
            try {
                ServerResponse.ProjectResponse projResponse = client.getProject(customCertManager, project, lastModified, lastETag);
                String name = projResponse.getName();
                String email = projResponse.getEmail();
                Log.d(getClass().getSimpleName(), "EMAIL : "+email);
                dbHelper.updateProject(project.getId(), name, email, null);

                // get members
                List<DBMember> members = projResponse.getMembers(project.getId());

                // add/update members
                for (DBMember m : members) {
                    DBMember localMember = dbHelper.getMember(m.getRemoteId(), project.getId());
                    // member does not exist locally, add it
                    if (localMember == null) {
                        Log.d(getClass().getSimpleName(), "Add member : "+m);
                        dbHelper.addMember(m);
                    }
                    // member exists, check if needs update
                    else {
                        if (m.getName().equals(localMember.getName()) &&
                                m.getWeight() == localMember.getWeight() &&
                                m.isActivated() == localMember.isActivated()
                                ) {
                            // alright
                            Log.d(getClass().getSimpleName(), "Nothing to do for member : "+localMember);
                        }
                        else {
                            Log.d(getClass().getSimpleName(), "Update member : "+m);
                            dbHelper.updateMember(m.getRemoteId(), project.getId(), m.getName(), m.getWeight(), m.isActivated());
                        }
                    }
                }

                // get up-to-date DB members
                List<DBMember> dbMembers = dbHelper.getMembersOfProject(project.getId());
                // get member id map
                Map<Long, Long> memberRemoteIdToId = new ArrayMap<>();
                for (DBMember m : dbMembers) {
                    memberRemoteIdToId.put(m.getRemoteId(), m.getId());
                }

                // get bills
                ServerResponse.BillsResponse billsResponse = client.getBills(customCertManager, project);
                List<DBBill> remoteBills = billsResponse.getBills(project.getId(), memberRemoteIdToId);
                Map<Long,DBBill> remoteBillsByRemoteId = new ArrayMap<>();
                for (DBBill remoteBill : remoteBills) {
                    remoteBillsByRemoteId.put(remoteBill.getRemoteId(), remoteBill);
                }
                List<DBBill> localBills = dbHelper.getBillsOfProject(project.getId());
                Map<Long,DBBill> localBillsByRemoteId = new ArrayMap<>();
                for (DBBill localBill : localBills) {
                    localBillsByRemoteId.put(localBill.getRemoteId(), localBill);
                }

                // add, update or delete DB bills
                for (DBBill remoteBill : remoteBills) {
                    // add if local does not exist
                    if (!localBillsByRemoteId.containsKey(remoteBill.getRemoteId())) {
                        long billId = dbHelper.addBill(remoteBill);
                        Log.d(getClass().getSimpleName(), "Add bill : " + remoteBill);
                        /*//////// billowers
                        for (DBBillOwer rbo : remoteBill.getBillOwers()) {
                            dbHelper.addBillower(billId, rbo);
                        }*/
                    }
                    // update bill if necessary
                    // and billOwers if necessary
                    else {
                        DBBill localBill = localBillsByRemoteId.get(remoteBill.getRemoteId());
                        if (localBill.getPayerId() == remoteBill.getPayerId() &&
                                localBill.getAmount() == remoteBill.getAmount() &&
                                localBill.getDate().equals(remoteBill.getDate()) &&
                                localBill.getWhat().equals(remoteBill.getWhat())
                                ) {
                            // fine
                            Log.d(getClass().getSimpleName(), "Nothing to do for bill : "+localBill);
                        }
                        else {
                            dbHelper.updateBill(
                                    localBill.getId(), null, remoteBill.getPayerId(),
                                    remoteBill.getAmount(), remoteBill.getDate(),
                                    remoteBill.getWhat(), DBBill.STATE_OK
                            );
                            Log.d(getClass().getSimpleName(), "Update bill : "+remoteBill);
                        }
                        //////// billowers
                        Map<Long, DBBillOwer> localBillOwersByIds = new ArrayMap<>();
                        for (DBBillOwer bo : localBill.getBillOwers()) {
                            localBillOwersByIds.put(bo.getMemberId(), bo);
                        }
                        Map<Long, DBBillOwer> remoteBillOwersByIds = new ArrayMap<>();
                        for (DBBillOwer bo : remoteBill.getBillOwers()) {
                            remoteBillOwersByIds.put(bo.getMemberId(), bo);
                        }
                        // add remote which are not here
                        for (DBBillOwer rbo : remoteBill.getBillOwers()) {
                            if (!localBillOwersByIds.containsKey(rbo.getMemberId())) {
                                dbHelper.addBillower(localBill.getId(), rbo.getMemberId());
                                Log.d(getClass().getSimpleName(), "Add billOwer : " + rbo);
                            }
                        }
                        // delete local which are not there remotely
                        for (DBBillOwer lbo : localBill.getBillOwers()) {
                            if (!remoteBillOwersByIds.containsKey(lbo.getMemberId())) {
                                dbHelper.deleteBillOwer(lbo.getId());
                                Log.d(getClass().getSimpleName(), "Delete billOwer : " + lbo);
                            }
                        }
                    }
                }
                // delete local bill
                for (DBBill localBill : localBills) {
                    // if local bill does not exist remotely
                    if (!remoteBillsByRemoteId.containsKey(localBill.getRemoteId())) {
                        dbHelper.deleteBill(localBill.getId());
                        Log.d(getClass().getSimpleName(), "Delete bill : " + localBill);
                    }
                }
                status = LoginStatus.OK;
            } catch (ServerResponse.NotModifiedException e) {
                Log.d(getClass().getSimpleName(), "No changes, nothing to do.");
                status = LoginStatus.OK;
            } catch (IOException e) {
                Log.e(getClass().getSimpleName(), "Exception", e);
                exceptions.add(e);
                status = LoginStatus.CONNECTION_FAILED;
            } catch (JSONException e) {
                Log.e(getClass().getSimpleName(), "Exception", e);
                exceptions.add(e);
                status = LoginStatus.JSON_FAILED;
            }
            return status;
        }

        @Override
        protected void onPostExecute(LoginStatus status) {
            super.onPostExecute(status);
            if (status != LoginStatus.OK) {
                String errorString = appContext.getString(
                        R.string.error_sync,
                        appContext.getString(status.str)
                );
                errorString += "\n\n";
                for (Throwable e : exceptions) {
                    errorString += e.getClass().getName() + ": " + e.getMessage();
                }
                // broadcast the error
                // if the log job list is not visible, no toast
                Intent intent = new Intent(BROADCAST_SESSIONS_SYNC_FAILED);
                intent.putExtra(BillsListViewActivity.BROADCAST_ERROR_MESSAGE, errorString);
                appContext.sendBroadcast(intent);
            }
            else {
                Intent intent = new Intent(BROADCAST_SESSIONS_SYNCED);
                appContext.sendBroadcast(intent);
            }
            syncActive = false;
            // notify callbacks
            for (ICallback callback : callbacks) {
                callback.onFinish();
            }
            dbHelper.notifySessionsChanged();
            // start next sync if scheduled meanwhile
            if (syncScheduled) {
                scheduleSync(false, project.getId());
            }
        }
    }

    private IHateMoneyClient createIHateMoneyClient() {
        return new IHateMoneyClient();
    }

    public boolean editRemoteProject(long projId, String newName, String newEmail, String newPassword, ICallback callback) {
        if (isSyncPossible()) {
            EditRemoteProjectTask editRemoteProjectTask = new EditRemoteProjectTask(projId, newName, newEmail, newPassword, callback);
            editRemoteProjectTask.execute();
            return true;
        }
        return false;
    }

    /**
     * task to ask server to create public share with name restriction on device
     * or just get the share token if it already exists
     *
     */
    private class EditRemoteProjectTask extends AsyncTask<Void, Void, LoginStatus> {
        private IHateMoneyClient client;
        private String newName;
        private String newEmail;
        private String newPassword;
        private DBProject project;
        private ICallback callback;
        private List<Throwable> exceptions = new ArrayList<>();

        public EditRemoteProjectTask(long projId, String newName, String newEmail, String newPassword, ICallback callback) {
            this.project = dbHelper.getProject(projId);
            this.newName = newName;
            this.newEmail = newEmail;
            this.newPassword = newPassword;
            this.callback = callback;
        }

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
        }

        @Override
        protected LoginStatus doInBackground(Void... voids) {
            client = createIHateMoneyClient();
            SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(appContext);
            if (BillsListViewActivity.DEBUG) { Log.i(getClass().getSimpleName(), "STARTING edit remote project"); }
            LoginStatus status = LoginStatus.OK;
            try {
                ServerResponse.EditRemoteProjectResponse response = client.editRemoteProject(customCertManager, project, newName, newEmail, newPassword);
                if (BillsListViewActivity.DEBUG) { Log.i(getClass().getSimpleName(), "RESPONSE edit remote project : "+response.getStringContent()); }
            } catch (IOException e) {
                if (BillsListViewActivity.DEBUG) {
                    Log.e(getClass().getSimpleName(), "Exception", e);
                }
                exceptions.add(e);
                status = LoginStatus.CONNECTION_FAILED;
            }
            if (BillsListViewActivity.DEBUG) {
                Log.i(getClass().getSimpleName(), "FINISHED edit remote project");
            }
            return status;
        }

        @Override
        protected void onPostExecute(LoginStatus status) {
            super.onPostExecute(status);
            String errorString = "";
            if (status != LoginStatus.OK) {
                errorString = appContext.getString(
                        R.string.error_sync,
                        appContext.getString(status.str)
                );
                errorString += "\n\n";
                for (Throwable e : exceptions) {
                    errorString += e.getClass().getName() + ": " + e.getMessage();
                }
            }
            else {
                dbHelper.updateProject(project.getId(), newName, newEmail, newPassword);
            }
            callback.onFinish(newName, errorString);
        }
    }

    public boolean deleteRemoteProject(long projId, ICallback callback) {
        if (isSyncPossible()) {
            DeleteRemoteProjectTask deleteRemoteProjectTask = new DeleteRemoteProjectTask(projId, callback);
            deleteRemoteProjectTask.execute();
            return true;
        }
        return false;
    }

    /**
     * task to ask server to create public share with name restriction on device
     * or just get the share token if it already exists
     *
     */
    private class DeleteRemoteProjectTask extends AsyncTask<Void, Void, LoginStatus> {
        private IHateMoneyClient client;
        private DBProject project;
        private ICallback callback;
        private List<Throwable> exceptions = new ArrayList<>();

        public DeleteRemoteProjectTask(long projId, ICallback callback) {
            this.project = dbHelper.getProject(projId);
            this.callback = callback;
        }

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
        }

        @Override
        protected LoginStatus doInBackground(Void... voids) {
            client = createIHateMoneyClient();
            SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(appContext);
            if (BillsListViewActivity.DEBUG) { Log.i(getClass().getSimpleName(), "STARTING delete remote project"); }
            LoginStatus status = LoginStatus.OK;
            try {
                ServerResponse.DeleteRemoteProjectResponse response = client.deleteRemoteProject(customCertManager, project);
                if (BillsListViewActivity.DEBUG) { Log.i(getClass().getSimpleName(), "RESPONSE delete remote project : "+response.getStringContent()); }
            } catch (IOException e) {
                if (BillsListViewActivity.DEBUG) {
                    Log.e(getClass().getSimpleName(), "Exception", e);
                }
                exceptions.add(e);
                status = LoginStatus.CONNECTION_FAILED;
            }
            if (BillsListViewActivity.DEBUG) {
                Log.i(getClass().getSimpleName(), "FINISHED delete device");
            }
            return status;
        }

        @Override
        protected void onPostExecute(LoginStatus status) {
            super.onPostExecute(status);
            String errorString = "";
            if (status != LoginStatus.OK) {
                errorString = appContext.getString(
                        R.string.error_sync,
                        appContext.getString(status.str)
                );
                errorString += "\n\n";
                for (Throwable e : exceptions) {
                    errorString += e.getClass().getName() + ": " + e.getMessage();
                }
            }
            else {
                dbHelper.deleteProject(project.getId());
            }
            callback.onFinish(String.valueOf(project.getId()), errorString);
        }
    }

    public boolean createRemoteProject(String remoteId, String name, String email, String password, String ihmUrl, ICallback callback) {
        if (isSyncPossible()) {
            DBProject proj = new DBProject(0, remoteId, password, name, ihmUrl, email);
            CreateRemoteProjectTask createRemoteProjectTask = new CreateRemoteProjectTask(proj, callback);
            createRemoteProjectTask.execute();
            return true;
        }
        return false;
    }

    /**
     * task to ask server to create public share with name restriction on device
     * or just get the share token if it already exists
     *
     */
    private class CreateRemoteProjectTask extends AsyncTask<Void, Void, LoginStatus> {
        private IHateMoneyClient client;
        private DBProject project;
        private ICallback callback;
        private List<Throwable> exceptions = new ArrayList<>();

        public CreateRemoteProjectTask(DBProject project, ICallback callback) {
            this.project = project;
            this.callback = callback;
        }

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
        }

        @Override
        protected LoginStatus doInBackground(Void... voids) {
            client = createIHateMoneyClient();
            SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(appContext);
            if (BillsListViewActivity.DEBUG) { Log.i(getClass().getSimpleName(), "STARTING create remote project"); }
            LoginStatus status = LoginStatus.OK;
            try {
                ServerResponse.CreateRemoteProjectResponse response = client.createRemoteProject(customCertManager, project);
                if (BillsListViewActivity.DEBUG) { Log.i(getClass().getSimpleName(), "RESPONSE create remote project : "+response.getStringContent()); }
            } catch (IOException e) {
                if (BillsListViewActivity.DEBUG) {
                    Log.e(getClass().getSimpleName(), "Exception", e);
                }
                exceptions.add(e);
                status = LoginStatus.CONNECTION_FAILED;
            }
            if (BillsListViewActivity.DEBUG) {
                Log.i(getClass().getSimpleName(), "FINISHED create remote project");
            }
            return status;
        }

        @Override
        protected void onPostExecute(LoginStatus status) {
            super.onPostExecute(status);
            String errorString = "";
            if (status != LoginStatus.OK) {
                errorString = appContext.getString(
                        R.string.error_sync,
                        appContext.getString(status.str)
                );
                errorString += "\n\n";
                for (Throwable e : exceptions) {
                    errorString += e.getClass().getName() + ": " + e.getMessage();
                }
            }
            else {
                //dbHelper.deleteProject(project.getId());
            }
            callback.onFinish(project.getRemoteId(), errorString);
        }
    }

}

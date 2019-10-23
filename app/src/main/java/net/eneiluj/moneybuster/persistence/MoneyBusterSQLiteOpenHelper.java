package net.eneiluj.moneybuster.persistence;

import android.content.ContentValues;
import android.content.Context;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.text.TextUtils;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.WorkerThread;
import androidx.preference.PreferenceManager;

import net.eneiluj.moneybuster.R;
import net.eneiluj.moneybuster.android.activity.BillsListViewActivity;
import net.eneiluj.moneybuster.model.DBAccountProject;
import net.eneiluj.moneybuster.model.DBBill;
import net.eneiluj.moneybuster.model.DBBillOwer;
import net.eneiluj.moneybuster.model.DBMember;
import net.eneiluj.moneybuster.model.DBProject;
import net.eneiluj.moneybuster.model.ProjectType;
import net.eneiluj.moneybuster.util.SupportUtil;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

//import android.util.ArrayMap;

/**
 * Helps to add, get, update and delete bills, members, projects with the option to trigger a sync with the server.
 */
public class MoneyBusterSQLiteOpenHelper extends SQLiteOpenHelper {

    private static final String TAG = MoneyBusterSQLiteOpenHelper.class.getSimpleName();

    private static final int database_version = 7;
    private static final String database_name = "IHATEMONEY";

    private static final String table_members = "MEMBERS";
    public static final String key_id = "ID";
    public static final String key_remoteId = "REMOTEID";
    private static final String key_projectid = "PROJECTID";
    public static final String key_name = "NAME";
    private static final String key_activated = "ACTIVATED";
    private static final String key_weight = "WEIGHT";
    private static final String key_state = "STATE";
    private static final String key_r = "R";
    private static final String key_g = "G";
    private static final String key_b = "B";

    private static final String table_projects = "PROJECTS";
    //private static final String key_id = "ID";
    //private static final String key_remoteId = "REMOTEID";
    //private static final String key_name = "NAME";
    private static final String key_email = "EMAIL";
    private static final String key_password = "PASSWORD";
    private static final String key_ihmUrl = "IHMURL";
    private static final String key_lastPayerId = "LASTPAYERID";
    private static final String key_type = "TYPE";

    private static final String table_bills = "BILLS";
    //private static final String key_id = "ID";
    //private static final String key_remoteId = "REMOTEID";
    //private static final String key_projectId = "PROJECTID";
    private static final String key_payer_id = "PAYERID";
    private static final String key_amount = "AMOUNT";
    private static final String key_date = "DATE";
    private static final String key_what = "WHAT";
    //private static final String key_state = "STATE";
    private static final String key_repeat = "REPEAT";
    private static final String key_payment_mode = "PAYMENTMODE";
    private static final String key_category_id = "CATEGORYID";

    private static final String table_billowers = "BILLOWERS";
    //private static final String key_id = "ID";
    private static final String key_billId = "BILLID";
    private static final String key_member_id = "MEMBERID";

    // if Nextcloud account set
    private static final String table_account_projects = "ACCOUNTPROJECTS";
    //private static final String key_id = "ID";
    //private static final String key_remoteId = "REMOTEID";
    //private static final String key_name = "NAME";
    //private static final String key_password = "PASSWORD";
    private static final String key_ncUrl = "NCURL";

    private static final String[] columnsMembers = {
            key_id, key_remoteId, key_projectid, key_name, key_activated, key_weight, key_state,
            key_r, key_g, key_b
    };

    private static final String[] columnsProjects = {
            // long id, String remoteId, String password, String name, String ihmUrl, String email
            key_id, key_remoteId, key_password,  key_name, key_ihmUrl, key_email, key_lastPayerId, key_type
    };

    private static final String[] columnsBills = {
            key_id, key_remoteId, key_projectid, key_payer_id, key_amount,
            key_date, key_what, key_state, key_repeat, key_payment_mode, key_category_id
    };

    private static final String[] columnsBillowers = {
            key_id, key_billId, key_member_id
    };

    private static final String[] columnsAccountProjects = {
            key_id, key_remoteId, key_password,  key_name, key_ncUrl
    };

    private static final String default_order = key_id + " DESC";

    private static MoneyBusterSQLiteOpenHelper instance;

    private MoneyBusterServerSyncHelper serverSyncHelper;
    private Context context;

    private MoneyBusterSQLiteOpenHelper(Context context) {
        super(context, database_name, null, database_version);
        this.context = context.getApplicationContext();
        serverSyncHelper = MoneyBusterServerSyncHelper.getInstance(this);
        //recreateDatabase(getWritableDatabase());
    }

    public static MoneyBusterSQLiteOpenHelper getInstance(Context context) {
        if (instance == null)
            return instance = new MoneyBusterSQLiteOpenHelper(context.getApplicationContext());
        else
            return instance;
    }

    public MoneyBusterServerSyncHelper getMoneyBusterServerSyncHelper() {
        return serverSyncHelper;
    }

    /**
     * Creates initial the Database
     *
     * @param db Database
     */
    @Override
    public void onCreate(SQLiteDatabase db) {
        createTableMembers(db, table_members);
        createTableBills(db, table_bills);
        createTableBillowers(db, table_billowers);
        createTableProjects(db, table_projects);
        createTableAccountProjects(db, table_account_projects);
        createIndexes(db);
    }

    private void createTableMembers(SQLiteDatabase db, String tableName) {
        // key_id, key_remoteId, key_projectid, key_name, key_activated, key_weight
        db.execSQL("CREATE TABLE " + tableName + " ( " +
                key_id + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
                key_remoteId + " INTEGER, " +
                key_projectid + " INTEGER, " +
                key_name + " TEXT, " +
                key_activated + " INTEGER, " +
                key_weight + " FLOAT, " +
                key_r + " INTEGER, " +
                key_g + " INTEGER, " +
                key_b + " INTEGER, " +
                key_state + " INTEGER)");
    }

    private void createTableProjects(SQLiteDatabase db, String tableName) {
        db.execSQL("CREATE TABLE " + tableName + " ( " +
                key_id + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
                key_remoteId + " TEXT, " +
                key_name + " TEXT, " +
                key_ihmUrl + " TEXT, " +
                key_password + " TEXT, " +
                key_lastPayerId + " INTEGER, " +
                key_email + " TEXT, " +
                key_type + " TEXT)");
    }

    //key_id, key_remoteId, key_projectid, key_payerid, key_amount, key_date, key_what
    private void createTableBills(SQLiteDatabase db, String tableName) {
        db.execSQL("CREATE TABLE " + tableName + " ( " +
                key_id + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
                key_remoteId + " INTEGER, " +
                key_projectid + " INTEGER, " +
                key_payer_id + " INTEGER, " +
                key_amount + " FLOAT, " +
                key_what + " TEXT, " +
                key_state + " INTEGER, " +
                key_date + " TEXT, " +
                key_payment_mode + " TEXT DEFAULT \"n\", " +
                key_category_id + " INTEGER DEFAULT 0, " +
                key_repeat + " TEXT)");
    }

    //key_id, key_billId, key_member_remoteId
    private void createTableBillowers(SQLiteDatabase db, String tableName) {
        db.execSQL("CREATE TABLE " + tableName + " ( " +
                key_id + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
                key_billId + " INTEGER, " +
                key_member_id + " INTEGER)");
    }

    private void createTableAccountProjects(SQLiteDatabase db, String tableName) {
        db.execSQL("CREATE TABLE " + tableName + " ( " +
                key_id + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
                key_remoteId + " TEXT, " +
                key_name + " TEXT, " +
                key_ncUrl + " TEXT, " +
                key_password + " TEXT)");
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        if (oldVersion < 2) {
            db.execSQL("ALTER TABLE " + table_projects + " ADD COLUMN " + key_lastPayerId + " INTEGER DEFAULT 0");
        }
        if (oldVersion < 3) {
            db.execSQL("ALTER TABLE " + table_bills + " ADD COLUMN " + key_repeat + " TEXT");
        }

        if (oldVersion < 4) {
            db.execSQL("ALTER TABLE " + table_projects + " ADD COLUMN " + key_type + " TEXT");
            List<DBProject> projects = getProjectsCustom("", new String[]{}, default_order, db);

            String url;
            for (DBProject project : projects) {
                url = project.getIhmUrl();
                if (url == null) {
                    project.setType(ProjectType.LOCAL);
                } else if (url.contains("index.php/apps/cospend")) {
                    project.setType(ProjectType.COSPEND);
                } else {
                    project.setType(ProjectType.IHATEMONEY);
                }

                updateProject(project.getId(), project.getName(), project.getEmail(), project.getPassword(), project.getLastPayerId(), project.getType(), db);
            }
        }

        if (oldVersion < 5) {
            createTableAccountProjects(db, table_account_projects);
            createIndex(db, table_account_projects, key_id);
        }

        if (oldVersion < 6) {
            db.execSQL("ALTER TABLE " + table_bills + " ADD COLUMN " + key_payment_mode + " TEXT DEFAULT \"n\"");
            db.execSQL("ALTER TABLE " + table_bills + " ADD COLUMN " + key_category_id + " INTEGER DEFAULT 0");
        }

        if (oldVersion < 7) {
            db.execSQL("ALTER TABLE " + table_members + " ADD COLUMN " + key_r + " INTEGER DEFAULT NULL");
            db.execSQL("ALTER TABLE " + table_members + " ADD COLUMN " + key_g + " INTEGER DEFAULT NULL");
            db.execSQL("ALTER TABLE " + table_members + " ADD COLUMN " + key_b + " INTEGER DEFAULT NULL");
        }
    }

    @Override
    public void onDowngrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        recreateDatabase(db);
    }

    private void clearDatabase(SQLiteDatabase db) {
        db.delete(table_members, null, null);
        db.delete(table_projects, null, null);
        db.delete(table_bills, null, null);
        db.delete(table_billowers, null, null);
        db.delete(table_account_projects, null, null);
    }

    private void recreateDatabase(SQLiteDatabase db) {
        dropIndexes(db);
        db.execSQL("DROP TABLE " + table_members);
        db.execSQL("DROP TABLE " + table_projects);
        db.execSQL("DROP TABLE " + table_bills);
        db.execSQL("DROP TABLE " + table_billowers);
        db.execSQL("DROP TABLE " + table_account_projects);
        onCreate(db);
    }

    private void dropIndexes(SQLiteDatabase db) {
        Cursor c = db.query("sqlite_master", new String[]{"name"}, "type=?", new String[]{"index"}, null, null, null);
        while (c.moveToNext()) {
            db.execSQL("DROP INDEX " + c.getString(0));
        }
        c.close();
    }

    private void createIndexes(SQLiteDatabase db) {
        createIndex(db, table_members, key_id);
        createIndex(db, table_projects, key_id);
        createIndex(db, table_bills, key_id);
        createIndex(db, table_billowers, key_id);
        createIndex(db, table_account_projects, key_id);
    }

    private void createIndex(SQLiteDatabase db, String table, String column) {
        String indexName = table + "_" + column + "_idx";
        db.execSQL("CREATE INDEX IF NOT EXISTS " + indexName + " ON " + table + "(" + column + ")");
    }

    public Context getContext() {
        return context;
    }

    public long addAccountProject(DBAccountProject accountProject) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(key_remoteId, accountProject.getRemoteId());
        values.put(key_password, accountProject.getPassword());
        values.put(key_ncUrl, accountProject.getncUrl());
        values.put(key_name, accountProject.getName());
        return db.insert(table_account_projects, null, values);
    }

    public DBAccountProject getAccountProject(long id) {
        List<DBAccountProject> accountProjects = getAccountProjectsCustom(key_id + " = ?", new String[]{String.valueOf(id)}, null);
        return accountProjects.isEmpty() ? null : accountProjects.get(0);
    }

    @NonNull
    public List<DBAccountProject> getAccountProjects() {
        return getAccountProjectsCustom("", new String[]{}, default_order);
    }

    @NonNull
    @WorkerThread
    private List<DBAccountProject> getAccountProjectsCustom(@NonNull String selection, @NonNull String[] selectionArgs, @Nullable String orderBy) {
        return getAccountProjectsCustom(selection, selectionArgs, orderBy, getReadableDatabase());
    }

    @NonNull
    @WorkerThread
    private List<DBAccountProject> getAccountProjectsCustom(@NonNull String selection, @NonNull String[] selectionArgs, @Nullable String orderBy, SQLiteDatabase db) {
        if (selectionArgs.length > 2) {
            Log.v("AccountProject", selection + "   ----   " + selectionArgs[0] + " " + selectionArgs[1] + " " + selectionArgs[2]);
        }
        Cursor cursor = db.query(table_account_projects, columnsAccountProjects, selection, selectionArgs, null, null, orderBy);
        List<DBAccountProject> accountProjects = new ArrayList<>();
        while (cursor.moveToNext()) {
            accountProjects.add(getAccountProjectFromCursor(cursor));
        }
        cursor.close();
        return accountProjects;
    }

    /**
     * Creates a DBProject object from the current row of a Cursor.
     *
     * @param cursor database cursor
     * @return DBProject
     */
    @NonNull
    private DBAccountProject getAccountProjectFromCursor(@NonNull Cursor cursor) {
        // key_id, key_remoteId, key_password,  key_name, key_ncUrl
        return new DBAccountProject(cursor.getLong(0),
                cursor.getString(1),
                cursor.getString(2),
                cursor.getString(3),
                cursor.getString(4)
        );
    }

    public void clearAccountProjects() {
        SQLiteDatabase db = this.getWritableDatabase();
        db.delete(table_account_projects, null, null);
    }

    public long addProject(DBProject project) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(key_remoteId, project.getRemoteId());
        values.put(key_password, project.getPassword());
        values.put(key_email, project.getEmail());
        values.put(key_ihmUrl, project.getIhmUrl());
        values.put(key_type, project.getType().getId());
        return db.insert(table_projects, null, values);
    }

    /**
     * Get a project by ID
     *
     * @param id int - ID of the requested project
     * @return requested project
     */
    public DBProject getProject(long id) {
        List<DBProject> projects = getProjectsCustom(key_id + " = ?", new String[]{String.valueOf(id)}, null);
        return projects.isEmpty() ? null : projects.get(0);
    }

    /**
     * Returns a list of all projects in the Database
     *
     * @return List&lt;DBProject&gt;
     */
    @NonNull
    //@WorkerThread
    public List<DBProject> getProjects() {
        return getProjectsCustom("", new String[]{}, default_order);
    }

    /**
     * Query the database with a custom raw query.
     *
     * @param selection     A filter declaring which rows to return, formatted as an SQL WHERE clause (excluding the WHERE itself).
     * @param selectionArgs You may include ?s in selection, which will be replaced by the values from selectionArgs, in order that they appear in the selection. The values will be bound as Strings.
     * @param orderBy       How to order the rows, formatted as an SQL ORDER BY clause (excluding the ORDER BY itself). Passing null will use the default sort order, which may be unordered.
     * @return list of projects
     */
    @NonNull
    @WorkerThread
    private List<DBProject> getProjectsCustom(@NonNull String selection, @NonNull String[] selectionArgs, @Nullable String orderBy) {
        return getProjectsCustom(selection, selectionArgs,orderBy,getReadableDatabase());
    }

    /**
     * Query the database with a custom raw query.
     *
     * @param selection     A filter declaring which rows to return, formatted as an SQL WHERE clause (excluding the WHERE itself).
     * @param selectionArgs You may include ?s in selection, which will be replaced by the values from selectionArgs, in order that they appear in the selection. The values will be bound as Strings.
     * @param orderBy       How to order the rows, formatted as an SQL ORDER BY clause (excluding the ORDER BY itself). Passing null will use the default sort order, which may be unordered.
     * @return list of projects
     */
    @NonNull
    @WorkerThread
    private List<DBProject> getProjectsCustom(@NonNull String selection, @NonNull String[] selectionArgs, @Nullable String orderBy, SQLiteDatabase db) {
        if (selectionArgs.length > 2) {
            Log.v("Project", selection + "   ----   " + selectionArgs[0] + " " + selectionArgs[1] + " " + selectionArgs[2]);
        }
        Cursor cursor = db.query(table_projects, columnsProjects, selection, selectionArgs, null, null, orderBy);
        List<DBProject> projects = new ArrayList<>();
        while (cursor.moveToNext()) {
            projects.add(getProjectFromCursor(cursor));
        }
        cursor.close();
        return projects;
    }

    /**
     * Creates a DBProject object from the current row of a Cursor.
     *
     * @param cursor database cursor
     * @return DBProject
     */
    @NonNull
    private DBProject getProjectFromCursor(@NonNull Cursor cursor) {
        return new DBProject(cursor.getLong(0),
                cursor.getString(1),
                cursor.getString(2),
                cursor.getString(3),
                cursor.getString(4),
                cursor.getString(5),
                cursor.getLong(6),
                ProjectType.getTypeById(cursor.getString(7))
        );
    }

    public void deleteProject(long id) {
        SQLiteDatabase db = this.getWritableDatabase();
        // delete bills and billowers
        for (DBBill b : getBillsOfProject(id)) {
            deleteBill(b.getId());
        }
        // delete members
        db.delete(table_members,
                key_projectid + " = ?",
                new String[]{String.valueOf(id)});
        // delete project
        db.delete(table_projects,
                key_id + " = ?",
                new String[]{String.valueOf(id)});
    }

    public void updateProject(long projId, @Nullable String newName, @Nullable String newEmail, @Nullable String newPassword, @Nullable Long newLastPayerId) {
        //debugPrintFullDB();
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        if (newName != null) {
            values.put(key_name, newName);
        }
        if (newEmail != null) {
            values.put(key_email, newEmail);
        }
        if (newPassword != null) {
            values.put(key_password, newPassword);
        }
        if (newLastPayerId != null) {
            values.put(key_lastPayerId, newLastPayerId);
        }
        if (values.size() > 0) {
            int rows = db.update(table_projects, values, key_id + " = ?", new String[]{String.valueOf(projId)});
        }
    }

    public void updateProject(long projId, @Nullable String newName, @Nullable String newEmail, @Nullable String newPassword, @Nullable Long newLastPayerId, @NonNull ProjectType projectType) {
        //debugPrintFullDB();
        SQLiteDatabase db = this.getWritableDatabase();
        updateProject(projId, newName, newEmail, newPassword, newLastPayerId, projectType, db);
    }

    private void updateProject(long projId, @Nullable String newName, @Nullable String newEmail, @Nullable String newPassword, @Nullable Long newLastPayerId, @NonNull ProjectType projectType, SQLiteDatabase db) {
        //debugPrintFullDB();
        ContentValues values = new ContentValues();
        if (newName != null) {
            values.put(key_name, newName);
        }
        if (newEmail != null) {
            values.put(key_email, newEmail);
        }
        if (newPassword != null) {
            values.put(key_password, newPassword);
        }
        if (newLastPayerId != null) {
            values.put(key_lastPayerId, newLastPayerId);
        }
        values.put(key_type, projectType.getId());
        if (values.size() > 0) {
            int rows = db.update(table_projects, values, key_id + " = ?", new String[]{String.valueOf(projId)});
        }
    }

    public void debugPrintFullDB() {
        List<DBProject> projects = getProjectsCustom("", new String[]{}, default_order);
        Log.v(getClass().getSimpleName(), "Full Database (" + projects.size() + " projects):");
        for (DBProject project : projects) {
            Log.v(getClass().getSimpleName(), "     " + project);
        }

        List<DBMember> members = getMembersCustom("", new String[]{}, default_order);
        Log.v(getClass().getSimpleName(), "Full Database (" + members.size() + " members):");
        for (DBMember member : members) {
            Log.v(getClass().getSimpleName(), "     " + member);
        }
    }

    /**
     *
     */
    public void addMember(DBMember m) {
        // key_id, key_remoteId, key_projectid, key_name, key_activated, key_weight
        if (BillsListViewActivity.DEBUG) { Log.d(TAG, "[add member]"); }
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(key_remoteId, m.getRemoteId());
        values.put(key_projectid, m.getProjectId());
        values.put(key_name, m.getName());
        values.put(key_activated, m.isActivated() ? "1" : "0");
        values.put(key_weight, m.getWeight());
        values.put(key_state, m.getState());
        values.put(key_r, m.getR());
        values.put(key_g, m.getG());
        values.put(key_b, m.getB());

        db.insert(table_members, null, values);
    }

    public void addMemberAndSync(DBMember m) {
        addMember(m);
        DBProject proj = getProject(m.getProjectId());
        if (!proj.isLocal()) {
            SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(getContext());
            boolean offlineMode = preferences.getBoolean(getContext().getString(R.string.pref_key_offline_mode), false);
            if (!offlineMode) {
                serverSyncHelper.scheduleSync(true, m.getProjectId());
            }
        }
    }

    public void updateMember(long memberId, @Nullable String newName, @Nullable Double newWeight,
                             @Nullable Boolean newActivated, @Nullable Integer newState, @Nullable Long newRemoteId,
                             @Nullable Integer newR, @Nullable Integer newG, @Nullable Integer newB) {
        //debugPrintFullDB();
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        if (newName != null) {
            values.put(key_name, newName);
        }
        if (newWeight != null) {
            //Log.d(getClass().getSimpleName(), "MID : "+ memberId + " NEW WEIGHT "+newWeight);
            values.put(key_weight, newWeight);
        }
        if (newRemoteId != null) {
            values.put(key_remoteId, newRemoteId);
        }
        if (newActivated != null) {
            values.put(key_activated, newActivated ? 1 : 0);
        }
        if (newState != null) {
            values.put(key_state, newState);
        }
        if (newR != null) {
            values.put(key_r, newR);
        }
        if (newG != null) {
            values.put(key_g, newG);
        }
        if (newB != null) {
            values.put(key_b, newB);
        }
        if (values.size() > 0) {
            int rows = db.update(table_members, values, key_id + " = ?",
                    new String[]{String.valueOf(memberId)});
        }
    }

    public void updateMemberAndSync(DBMember m, @Nullable String newName,  @Nullable Double newWeight,
                                    @Nullable Boolean newActivated,
                                    @Nullable Integer newR, @Nullable Integer newG, @Nullable Integer newB) {
        int newState = DBBill.STATE_EDITED;
        if (m.getState() == DBBill.STATE_ADDED) {
            newState = DBBill.STATE_ADDED;
        }

        updateMember(m.getId(), newName, newWeight, newActivated, newState, null, newR, newG, newB);

        Log.v(TAG, "UPDATE MEMBER AND SYNC");
        DBProject proj = getProject(m.getProjectId());
        if (!proj.isLocal()) {
            SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(getContext());
            boolean offlineMode = preferences.getBoolean(getContext().getString(R.string.pref_key_offline_mode), false);
            if (!offlineMode) {
                serverSyncHelper.scheduleSync(true, m.getProjectId());
            }
        }
    }

    /**
     *
     */
    public List<DBMember> getMembersOfProject(long projId, String orderBy) {
        if (orderBy == null) {
            orderBy = "LOWER("+key_name+")";
        }
        return getMembersCustom(key_projectid + " = ?", new String[]{String.valueOf(projId)}, orderBy + " ASC");
    }

    public List<DBMember> getMembersOfProjectWithState(long projId, int state) {
        List<DBMember> members = getMembersCustom(
                key_projectid + " = ? AND " + key_state + " = ?",
                new String[]{String.valueOf(projId), String.valueOf(state)},
                key_name + " ASC");
        return members;
    }

    public List<DBMember> getActivatedMembersOfProject(long projId) {
        List<DBMember> members = getMembersCustom(
                key_projectid + " = ? AND " + key_activated + " = 1",
                new String[]{String.valueOf(projId)},
                key_name + " ASC");
        return members;
    }

    public DBMember getMember(long remoteId, long projId) {
        List<DBMember> members = getMembersCustom(
                key_remoteId + " = ? AND " + key_projectid + " = ?",
                new String[]{String.valueOf(remoteId), String.valueOf(projId)},
                null
        );
        return members.isEmpty() ? null : members.get(0);
    }

    public DBMember getMember(long id) {
        List<DBMember> members = getMembersCustom(
                key_id + " = ?",
                new String[]{String.valueOf(id)},
                null
        );
        return members.isEmpty() ? null : members.get(0);
    }

    /**
     *
     */
    @NonNull
    @WorkerThread
    private List<DBMember> getMembersCustom(@NonNull String selection, @NonNull String[] selectionArgs, @Nullable String orderBy) {
        SQLiteDatabase db = getReadableDatabase();
        if (selectionArgs.length > 2) {
            Log.v("Member", selection + "   ----   " + selectionArgs[0] + " " + selectionArgs[1] + " " + selectionArgs[2]);
        }
        Cursor cursor = db.query(table_members, columnsMembers, selection, selectionArgs, null, null, orderBy);
        List<DBMember> members = new ArrayList<>();
        while (cursor.moveToNext()) {
            members.add(getMemberFromCursor(cursor));
        }
        cursor.close();
        return members;
    }

    /**
     *
     */
    @NonNull
    private DBMember getMemberFromCursor(@NonNull Cursor cursor) {
        // key_id, key_remoteId, key_projectid, key_name, key_activated, key_weight, key_state
        //Log.v("Member","get dbmember "+ cursor.getString(3) + " : "+cursor.getDouble(5));
        return new DBMember(
                cursor.getLong(0),
                cursor.getLong(1),
                cursor.getLong(2),
                cursor.getString(3),
                cursor.getInt(4) == 1,
                cursor.getDouble(5),
                cursor.getInt(6),
                cursor.isNull(7) ? null : cursor.getInt(7),
                cursor.isNull(8) ? null : cursor.getInt(8),
                cursor.isNull(9) ? null : cursor.getInt(9)
        );
    }

    public void deleteMember(long id) {
        SQLiteDatabase db = this.getWritableDatabase();
        db.delete(table_members,
                key_id + " = ?",
                new String[]{String.valueOf(id)});
    }

    public long addBill(DBBill b) {
        // key_id, key_remoteId, key_projectid, key_payer_id, key_amount, key_date, key_what, key_state
        if (BillsListViewActivity.DEBUG) { Log.d(TAG, "[add bill]"); }
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(key_remoteId, b.getRemoteId());
        values.put(key_projectid, b.getProjectId());
        values.put(key_payer_id, b.getPayerId());
        values.put(key_amount, b.getAmount());
        values.put(key_date, b.getDate());
        values.put(key_what, b.getWhat());
        values.put(key_state, b.getState());
        values.put(key_repeat, b.getRepeat());
        values.put(key_payment_mode, b.getPaymentMode());
        values.put(key_category_id, b.getCategoryId());

        long billId = db.insert(table_bills, null, values);

        for (DBBillOwer bo : b.getBillOwers()) {
            addBillower(billId, bo.getMemberId());
        }
        return billId;
    }

    public void updateBill(long remoteId, long projId, long newPayerId, double newAmount,
                           @Nullable String newDate, @Nullable String newWhat, int newState,
                           @Nullable String newRepeat, @Nullable String newPaymentMode, int newCategoryId) {
        //debugPrintFullDB();
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        if (newDate != null) {
            values.put(key_date, newDate);
        }
        if (newWhat != null) {
            values.put(key_what, newWhat);
        }
        values.put(key_payer_id, newPayerId);
        values.put(key_amount, newAmount);
        values.put(key_state, newState);
        values.put(key_repeat, newRepeat);
        values.put(key_payment_mode, newPaymentMode);
        values.put(key_category_id, newCategoryId);
        if (values.size() > 0) {
            int rows = db.update(table_bills, values, key_remoteId + " = ? AND "+key_projectid+" = ?",
                    new String[]{String.valueOf(remoteId), String.valueOf(projId)});
        }
    }

    public void setBillState(long billId, int state) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(key_state, state);
        int rows = db.update(table_bills, values, key_id + " = ?",
                    new String[]{String.valueOf(billId)});
    }

    public void updateBill(long billId, @Nullable Long newRemoteId, @Nullable Long newPayerId,
                           @Nullable Double newAmount, @Nullable String newDate,
                           @Nullable String newWhat, @Nullable Integer newState,
                           @Nullable String newRepeat, @Nullable String newPaymentMode,
                           @Nullable Integer newCategoryId) {
        //debugPrintFullDB();
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        if (newDate != null) {
            values.put(key_date, newDate);
        }
        if (newWhat != null) {
            values.put(key_what, newWhat);
        }
        if (newRemoteId != null) {
            values.put(key_remoteId, newRemoteId);
        }
        if (newPayerId != null) {
            values.put(key_payer_id, newPayerId);
        }
        if (newAmount != null) {
            values.put(key_amount, newAmount);
        }
        if (newState != null) {
            values.put(key_state, newState);
        }
        if (newRepeat != null) {
            values.put(key_repeat, newRepeat);
        }
        if (newPaymentMode != null) {
            values.put(key_payment_mode, newPaymentMode);
        }
        if (newCategoryId != null) {
            values.put(key_category_id, newCategoryId);
        }
        if (values.size() > 0) {
            int rows = db.update(table_bills, values, key_id + " = ?",
                    new String[]{String.valueOf(billId)});
        }
    }

    public void updateBillAndSync(DBBill bill, long newPayerId, double newAmount,
                                  @Nullable String newDate, @Nullable String newWhat,
                                  @Nullable List<Long> newOwersIds, @Nullable String newRepeat,
                                  @Nullable String newPaymentMode, @Nullable Integer newCategoryId) {
        // bill values
        // state
        int newState = DBBill.STATE_EDITED;
        if (bill.getState() == DBBill.STATE_ADDED) {
            newState = DBBill.STATE_ADDED;
        }
        updateBill(bill.getId(), null, newPayerId, newAmount, newDate, newWhat, newState,
                   newRepeat, newPaymentMode, newCategoryId);

        // bill owers
        List<DBBillOwer> dbBillOwers = getBillowersOfBill(bill.getId());
        Map<Long, DBBillOwer> dbBillOwersByMemberId = new HashMap<>();
        for (DBBillOwer bo : dbBillOwers) {
            dbBillOwersByMemberId.put(bo.getMemberId(), bo);
        }
        // add the owers who are not in the DB
        for (long newOwerId : newOwersIds) {
            if (!dbBillOwersByMemberId.containsKey(newOwerId)) {
                addBillower(bill.getId(), newOwerId);
            }
        }
        // delete the db owers that are not in new owers
        for (DBBillOwer dbBo : dbBillOwers) {
            if (!newOwersIds.contains(dbBo.getMemberId())) {
                deleteBillOwer(dbBo.getId());
            }
        }
        Log.v(TAG, "UPDATE BILL AND SYNC");
        DBProject proj = getProject(bill.getProjectId());
        if (!proj.isLocal()) {
            SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(getContext());
            boolean offlineMode = preferences.getBoolean(getContext().getString(R.string.pref_key_offline_mode), false);
            if (!offlineMode) {
                serverSyncHelper.scheduleSync(true, bill.getProjectId());
            }
        }
    }

    /**
     *
     */
    public List<DBBill> getBillsOfProject(long projId) {
        List<DBBill> bills = getBillsCustom(key_projectid + " = ?", new String[]{String.valueOf(projId)}, key_date + " ASC");
        return bills;
    }

    public List<DBBill> getBillsOfProjectWithState(long projId, int state) {
        List<DBBill> bills = getBillsCustom(
                key_projectid + " = ? AND " + key_state + " = ?",
                new String[]{String.valueOf(projId), String.valueOf(state)},
                key_date + " ASC");
        return bills;
    }

    public List<DBBill> getBillsOfMember(long memberId) {
        List<DBBill> bills = getBillsCustom(key_payer_id + " = ?", new String[]{String.valueOf(memberId)}, key_date + " ASC");
        return bills;
    }

    public DBBill getBill(long remoteId, long projId) {
        List<DBBill> bills = getBillsCustom(
                key_remoteId + " = ? AND " + key_projectid + " = ?",
                new String[]{String.valueOf(remoteId), String.valueOf(projId)},
                null
        );
        return bills.isEmpty() ? null : bills.get(0);
    }

    public DBBill getBill(long billId) {
        List<DBBill> bills = getBillsCustom(
                key_id + " = ?",
                new String[]{String.valueOf(billId)},
                null
        );
        return bills.isEmpty() ? null : bills.get(0);
    }

    @NonNull
    @WorkerThread
    public List<DBBill> searchBills(@Nullable CharSequence query, long projectId) {
        List<String> andWhere = new ArrayList<>();
        List<String> args = new ArrayList<>();

        andWhere.add("(" + key_projectid + " = " + projectId + ")");
        andWhere.add("(" + key_state + " != " + DBBill.STATE_DELETED + ")");
        if (query != null) {
            args.add("%" + query + "%");
            args.add("%" + query + "%");
            String whereStr = "(" + key_what + " LIKE ? OR " + key_date + " LIKE ?";
            if (SupportUtil.isDouble(query.toString())) {
                whereStr += " OR (" + key_amount + " <= (? + 10) AND " + key_amount + " >= (? - 10))";
                args.add(query.toString());
                args.add(query.toString());
            }

            // get member names
            List<DBMember> members = getMembersOfProject(projectId, null);
            List<String> memberNames = new ArrayList<>();
            List<Long> memberIds = new ArrayList<>();
            for (DBMember m : members) {
                memberNames.add(m.getName().toLowerCase());
                memberIds.add(m.getId());
            }

            int memberIndex = memberNames.indexOf(query.toString().toLowerCase());
            if (memberIndex != -1) {
                Log.v(TAG, "found a member with same name as query: "+query);
                long searchMemberId = memberIds.get(memberIndex);
                // search BY PAYER NAME
                whereStr += " OR ("+key_payer_id+"=?)";
                args.add(String.valueOf(searchMemberId));
                // search BY OWER NAME
                // build a sub query with inner join
                String joinOwer = "select "+table_bills+"."+key_id+" from "+table_bills+" inner join "+table_billowers+
                        " where "+key_member_id+"=? and "+
                        table_bills+"."+key_id+"="+table_billowers+"."+key_billId;
                whereStr += " OR ("+key_id+" IN ("+joinOwer+"))";
                args.add(String.valueOf(searchMemberId));
            }

            // close the big OR
            whereStr += ")";
            andWhere.add(whereStr);
        }

        String order = key_date + " DESC";
        return getBillsCustom(TextUtils.join(" AND ", andWhere), args.toArray(new String[]{}), order);
    }

    /**
     *
     */
    @NonNull
    @WorkerThread
    private List<DBBill> getBillsCustom(@NonNull String selection, @NonNull String[] selectionArgs, @Nullable String orderBy) {
        SQLiteDatabase db = getReadableDatabase();
        if (selectionArgs.length > 2) {
            Log.v("Bill", selection + "   ----   " + selectionArgs[0] + " " + selectionArgs[1] + " " + selectionArgs[2]);
        }
        Cursor cursor = db.query(table_bills, columnsBills, selection, selectionArgs, null, null, orderBy);
        List<DBBill> bills = new ArrayList<>();
        while (cursor.moveToNext()) {
            DBBill bill = getBillFromCursor(cursor);
            bill.setBillOwers(getBillowersOfBill(bill.getId()));
            bills.add(bill);
        }
        cursor.close();
        return bills;
    }

    /**
     *
     */
    @NonNull
    private DBBill getBillFromCursor(@NonNull Cursor cursor) {
        // key_id, key_remoteId, key_projectid, key_payer_id, key_amount, key_date, key_what, key_state, key_repeat
        // key_payment_mode, key_category_id
        return new DBBill(
                cursor.getLong(0),
                cursor.getLong(1),
                cursor.getLong(2),
                cursor.getLong(3),
                cursor.getDouble(4),
                cursor.getString(5),
                cursor.getString(6),
                cursor.getInt(7),
                cursor.getString(8),
                cursor.getString(9),
                cursor.getInt(10)
        );
    }

    public void deleteBill(long id) {
        SQLiteDatabase db = this.getWritableDatabase();
        db.delete(table_billowers,
                key_billId + " = ?",
                new String[]{String.valueOf(id)});
        db.delete(table_bills,
                key_id + " = ?",
                new String[]{String.valueOf(id)});
    }

    public void addBillower(long billId, long memberId) {
        if (BillsListViewActivity.DEBUG) { Log.d(TAG, "[add billower]"); }
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(key_billId, billId);
        values.put(key_member_id, memberId);

        db.insert(table_billowers, null, values);
    }

    /**
     *
     */
    public List<DBBillOwer> getBillowersOfBill(long billId) {
        List<DBBillOwer> billOwers = getBillOwersCustom(key_billId + " = ?", new String[]{String.valueOf(billId)}, null);
        return billOwers;
    }

    public List<DBBillOwer> getBillowersOfMember(long memberId) {
        List<DBBillOwer> billOwers = getBillOwersCustom(key_member_id + " = ?", new String[]{String.valueOf(memberId)}, null);
        return billOwers;
    }

    /**
     *
     */
    @NonNull
    @WorkerThread
    private List<DBBillOwer> getBillOwersCustom(@NonNull String selection, @NonNull String[] selectionArgs, @Nullable String orderBy) {
        SQLiteDatabase db = getReadableDatabase();
        if (selectionArgs.length > 2) {
            Log.v("BillOwers", selection + "   ----   " + selectionArgs[0] + " " + selectionArgs[1] + " " + selectionArgs[2]);
        }
        Cursor cursor = db.query(table_billowers, columnsBillowers, selection, selectionArgs, null, null, orderBy);
        List<DBBillOwer> billOwers = new ArrayList<>();
        while (cursor.moveToNext()) {
            DBBillOwer billOwer = getBillOwerFromCursor(cursor);
            billOwers.add(billOwer);
        }
        cursor.close();
        return billOwers;
    }

    /**
     *
     */
    @NonNull
    private DBBillOwer getBillOwerFromCursor(@NonNull Cursor cursor) {
        // key_id, key_billId, key_member_id, key_member_remoteId
        return new DBBillOwer(
                cursor.getLong(0),
                cursor.getLong(1),
                cursor.getLong(2)
        );
    }

    public void deleteBillOwer(long id) {
        SQLiteDatabase db = this.getWritableDatabase();
        db.delete(table_billowers,
                key_id + " = ?",
                new String[]{String.valueOf(id)});
    }
}

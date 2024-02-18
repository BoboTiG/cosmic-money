package net.eneiluj.moneybuster.android.activity;

import android.Manifest;
import android.animation.AnimatorInflater;
import android.annotation.SuppressLint;
import android.app.SearchManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.content.res.ColorStateList;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.PorterDuff;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.GradientDrawable;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.text.InputType;
import android.text.TextUtils;
import android.util.Base64;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewTreeObserver;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.result.ActivityResult;
import androidx.activity.result.ActivityResultCallback;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.view.ActionMode;
import androidx.appcompat.view.ContextThemeWrapper;
import androidx.appcompat.widget.AppCompatImageButton;
import androidx.appcompat.widget.AppCompatImageView;
import androidx.appcompat.widget.SearchView;
import androidx.appcompat.widget.Toolbar;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.core.view.GravityCompat;
import androidx.core.view.ViewCompat;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.preference.PreferenceManager;
import androidx.recyclerview.widget.DividerItemDecoration;
import androidx.recyclerview.widget.ItemTouchHelper;
import androidx.recyclerview.widget.ItemTouchHelper.SimpleCallback;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import com.google.android.material.appbar.AppBarLayout;
import com.google.android.material.card.MaterialCardView;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.snackbar.Snackbar;
import com.google.android.material.textview.MaterialTextView;
import com.larswerkman.lobsterpicker.LobsterPicker;
import com.larswerkman.lobsterpicker.sliders.LobsterShadeSlider;
import com.nextcloud.android.sso.exceptions.NextcloudFilesAppAccountNotFoundException;
import com.nextcloud.android.sso.exceptions.NoCurrentAccountSelectedException;
import com.nextcloud.android.sso.helper.SingleAccountHelper;
import com.nextcloud.android.sso.model.SingleSignOnAccount;

import net.eneiluj.moneybuster.R;
import net.eneiluj.moneybuster.android.dialogs.ProjectSettlementDialogBuilder;
import net.eneiluj.moneybuster.android.dialogs.ProjectShareDialogBuilder;
import net.eneiluj.moneybuster.android.dialogs.ProjectStatisticsDialogBuilder;
import net.eneiluj.moneybuster.android.fragment.NewProjectFragment;
import net.eneiluj.moneybuster.android.ui.ProjectDrawerAdapter;
import net.eneiluj.moneybuster.android.ui.TextDrawable;
import net.eneiluj.moneybuster.model.Category;
import net.eneiluj.moneybuster.model.DBBill;
import net.eneiluj.moneybuster.model.DBMember;
import net.eneiluj.moneybuster.model.DBProject;
import net.eneiluj.moneybuster.model.Item;
import net.eneiluj.moneybuster.model.ItemAdapter;
import net.eneiluj.moneybuster.model.NavigationAdapter;
import net.eneiluj.moneybuster.model.ProjectType;
import net.eneiluj.moneybuster.persistence.LoadBillsListTask;
import net.eneiluj.moneybuster.persistence.MoneyBusterSQLiteOpenHelper;
import net.eneiluj.moneybuster.persistence.MoneyBusterServerSyncHelper;
import net.eneiluj.moneybuster.service.SyncService;
import net.eneiluj.moneybuster.util.CospendClientUtil;
import net.eneiluj.moneybuster.util.ExportUtil;
import net.eneiluj.moneybuster.util.ICallback;
import net.eneiluj.moneybuster.util.MoneyBuster;
import net.eneiluj.moneybuster.util.IRefreshBillsListCallback;
import net.eneiluj.moneybuster.util.SupportUtil;
import net.eneiluj.moneybuster.util.ThemeUtils;

import java.io.IOException;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static android.view.View.GONE;
import static android.view.View.VISIBLE;
import static net.eneiluj.moneybuster.android.activity.EditProjectActivity.PARAM_PROJECT_ID;
import static net.eneiluj.moneybuster.util.SupportUtil.getVersionName;

public class BillsListViewActivity
        extends AppCompatActivity
        implements ItemAdapter.BillClickListener, IRefreshBillsListCallback, ProjectDrawerAdapter.IOnProjectMenuClick {

    private final static int PERMISSION_FOREGROUND = 1;
    private final static int PERMISSION_POST_NOTIFICATIONS = 2;
    public static boolean DEBUG = true;
    public static final String BROADCAST_EXTRA_PARAM = "net.eneiluj.moneybuster.broadcast_extra_param";
    public static final String BROADCAST_ERROR_MESSAGE = "net.eneiluj.moneybuster.broadcast_error_message";
    public static final String BROADCAST_PROJECT_ID = "net.eneiluj.moneybuster.broadcast_project_id";
    public static final String BROADCAST_ACCOUNT_PROJECTS_SYNC_FAILED = "net.eneiluj.moneybuster.broadcast_acc_proj_failed";
    public static final String BROADCAST_SSO_TOKEN_MISMATCH = "net.eneiluj.moneybuster.broadcast.token_mismatch";
    public static final String BROADCAST_ACCOUNT_PROJECTS_SYNCED = "net.eneiluj.moneybuster.broadcast.broadcast_acc_proj_synced";

    public final static String PARAM_DIALOG_CONTENT = "net.eneiluj.moneybuster.PARAM_DIALOG_CONTENT";
    public final static String PARAM_PROJECT_TO_SELECT = "net.eneiluj.moneybuster.PARAM_PROJECT_TO_SELECT";

    private static final String TAG = BillsListViewActivity.class.getSimpleName();

    public final static String SAVED_BILL_ID = "net.eneiluj.moneybuster.saved_bill_id";
    public final static String CREATED_PROJECT = "net.eneiluj.moneybuster.created_project";
    public final static String ADDED_PROJECT = "net.eneiluj.moneybuster.added_project";
    public final static String EDITED_PROJECT = "net.eneiluj.moneybuster.edited_project";
    public final static String DELETED_PROJECT = "net.eneiluj.moneybuster.deleted_project";
    public final static String DELETED_BILL = "net.eneiluj.moneybuster.deleted_bill";
    public final static String BILL_TO_DUPLICATE = "net.eneiluj.moneybuster.bill_to_duplicate";
    public static final String ADAPTER_KEY_ALL = "all";

    public final static String CREDENTIALS_CHANGED = "net.eneiluj.moneybuster.CREDENTIALS_CHANGED";

    private static final String SAVED_STATE_NAVIGATION_SELECTION = "navigationSelection";
    private static final String SAVED_STATE_NAVIGATION_ADAPTER_SLECTION = "navigationAdapterSelection";
    private static final String SAVED_STATE_NAVIGATION_OPEN = "navigationOpen";

    private static String contentToExport = "";

    private static boolean activityVisible = false;

    Toolbar toolbar;
    DrawerLayout drawerLayout;
    TextView configuredAccount;
    SwipeRefreshLayout swipeRefreshLayout;
    FloatingActionButton fabAddBill;
    RecyclerView listBillItems;
    RecyclerView listDrawerProjects;
    RecyclerView listDrawerMembers;
    ImageView avatarView;
    LinearLayout lastSyncLayout;
    TextView lastSyncText;
    AppCompatImageButton menuButton;
    AppCompatImageView accountButton;
    MaterialCardView homeToolbar;
    AppBarLayout appBar;
    View noMembersView;

    private final ProjectDrawerAdapter projectAdapter = new ProjectDrawerAdapter(this);

    private ItemAdapter adapter = null;
    private NavigationAdapter adapterMembers;
    private NavigationAdapter.NavigationItem itemAll;
    private Category navigationSelection = new Category(null, null);
    private String navigationOpen = "";

    private ActionMode mActionMode;
    private MoneyBusterSQLiteOpenHelper db = null;
    private SearchView searchView = null;
    private MaterialTextView searchText = null;
    private ICallback syncCallBack = new ICallback() {
        @Override
        public void onFinish() {
            adapter.clearSelection();
            if (mActionMode != null) {
                mActionMode.finish();
            }
            refreshLists();
            swipeRefreshLayout.setRefreshing(false);
        }

        @Override
        public void onFinish(String result, String message) {
        }

        @Override
        public void onScheduled() {
            swipeRefreshLayout.setRefreshing(false);
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        activityVisible = true;
        String categoryAdapterSelectedItem = ADAPTER_KEY_ALL;
        if (savedInstanceState != null) {
            navigationSelection = (Category) savedInstanceState.getSerializable(SAVED_STATE_NAVIGATION_SELECTION);
            navigationOpen = savedInstanceState.getString(SAVED_STATE_NAVIGATION_OPEN);
            categoryAdapterSelectedItem = savedInstanceState.getString(SAVED_STATE_NAVIGATION_ADAPTER_SLECTION);
        }

        setContentView(R.layout.drawer_layout);
        toolbar = findViewById(R.id.billsListActivityActionBar);
        drawerLayout = findViewById(R.id.drawerLayout);
        configuredAccount = findViewById(R.id.configuredAccount);
        swipeRefreshLayout = findViewById(R.id.swipe_refresh_layout);
        fabAddBill = findViewById(R.id.fab_add_bill);
        listBillItems = findViewById(R.id.list_bill_items);
        listDrawerProjects = findViewById(R.id.list_drawer_projects);
        listDrawerMembers = findViewById(R.id.list_drawer_project_members);
        avatarView = findViewById(R.id.drawer_nc_logo);
        lastSyncLayout = findViewById(R.id.drawer_last_sync_layout);
        lastSyncText = findViewById(R.id.last_sync_text);
        menuButton = findViewById(R.id.menu_button);
        accountButton = findViewById(R.id.launchAccountSwitcher);
        searchView = findViewById(R.id.search_view);
        searchText = findViewById(R.id.search_text);
        homeToolbar = findViewById(R.id.home_toolbar);
        appBar = findViewById(R.id.appBar);
        noMembersView = findViewById(R.id.layout_no_project_members);

        lastSyncLayout.setVisibility(GONE);
        lastSyncLayout.setBackgroundColor(ThemeUtils.primaryDarkColor(this));

        db = MoneyBusterSQLiteOpenHelper.getInstance(this);

        setupToolBar();
        setupBillsList();
        setupBillsListCornerCases();
        setupDrawerButtons();
        setupDrawerProjects();
        setupDrawerProjectMembers(categoryAdapterSelectedItem);

        updateUsernameInDrawer();

        // ask user what to do if no project an no account configured
        if (db.getProjects().isEmpty() && !MoneyBusterServerSyncHelper.isNextcloudAccountConfigured(this)) {
            AlertDialog.Builder selectBuilder = new AlertDialog.Builder(new ContextThemeWrapper(this, R.style.AppThemeDialog));
            selectBuilder.setTitle(getString(R.string.empty_action_dialog_title));

            List<String> options = new ArrayList<>();
            options.add(getString(R.string.configure_account_choice));
            options.add(getString(R.string.add_project_choice));
            CharSequence[] optcs = options.toArray(new CharSequence[options.size()]);

            selectBuilder.setSingleChoiceItems(optcs, -1, new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {

                    if (which == 0) {
                        Intent newProjectIntent = new Intent(getApplicationContext(), AccountActivity.class);
                        serverSettingsLauncher.launch(newProjectIntent);
                        dialog.dismiss();
                    } else if (which == 1) {
                        Intent newProjectIntent = new Intent(getApplicationContext(), NewProjectActivity.class);
                        newProjectIntent.putExtra(NewProjectFragment.PARAM_DEFAULT_IHM_URL, "https://ihatemoney.org");
                        newProjectIntent.putExtra(NewProjectFragment.PARAM_DEFAULT_NC_URL, "https://mynextcloud.org");
                        addProjectLauncher.launch(newProjectIntent);
                        dialog.dismiss();
                    }
                }
            });
            selectBuilder.setNegativeButton(getString(R.string.simple_cancel), null);
            selectBuilder.setPositiveButton(getString(R.string.simple_ok), new DialogInterface.OnClickListener() {
                public void onClick(DialogInterface dialog, int which) {
                }
            });

            AlertDialog selectDialog = selectBuilder.create();
            selectDialog.show();
        }

        // select a project if there are some and none is selected
        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
        long selectedProjectId = preferences.getLong("selected_project", 0);
        List<DBProject> dbProjects = db.getProjects();
        if (selectedProjectId == 0 && dbProjects.size() > 0) {
            setSelectedProject(dbProjects.get(0).getId());
            Log.v(TAG, "set selection 0");
        }

        displayWelcomeDialog();

        checkAndRequestPermissions();

        if (!SyncService.isRunning() && preferences.getBoolean(getString(R.string.pref_key_periodical_sync), false)) {
            Intent intent = new Intent(this, SyncService.class);
            if (Build.VERSION.SDK_INT < Build.VERSION_CODES.O) {
                startService(intent);
            } else {
                startForegroundService(intent);
            }
        }

        long projectToSelect = getIntent().getLongExtra(PARAM_PROJECT_TO_SELECT, 0);
        if (projectToSelect != 0) {
            setSelectedProject(projectToSelect);
            DBProject project = db.getProject(projectToSelect);

            String dialogContent = getIntent().getStringExtra(PARAM_DIALOG_CONTENT);
            if (dialogContent != null) {
                android.app.AlertDialog.Builder builder;
                builder = new android.app.AlertDialog.Builder(new ContextThemeWrapper(this, R.style.AppThemeDialog));
                builder.setTitle(this.getString(R.string.activity_dialog_title, project.getName()))
                        .setMessage(dialogContent)
                        .setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int which) {

                            }
                        })
                        .setIcon(R.drawable.ic_sync_grey_24dp)
                        .show();
            }
        }

    }

    private void checkAndRequestPermissions(){
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.FOREGROUND_SERVICE)
                    != PackageManager.PERMISSION_GRANTED) {

                Log.d(TAG, "[requesting permission FOREGROUND_SERVICE]");
                ActivityCompat.requestPermissions(
                        this,
                        new String[]{Manifest.permission.FOREGROUND_SERVICE},
                        PERMISSION_FOREGROUND
                );
            }
        }

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.POST_NOTIFICATIONS)
                    != PackageManager.PERMISSION_GRANTED) {

                Log.d(TAG, "[requesting permission POST_NOTIFICATIONS]");
                ActivityCompat.requestPermissions(
                        this,
                        new String[]{Manifest.permission.POST_NOTIFICATIONS},
                        PERMISSION_POST_NOTIFICATIONS
                );
            }
        }
    }

    private void displayWelcomeDialog() {
        // WELCOME/NEWS dialog
        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(this);
        //preferences.edit().putLong("last_welcome_dialog_displayed_at_version", -1).apply();
        long lastV = preferences.getLong("last_welcome_dialog_displayed_at_version", -1);
        String dialogContent = null;
        if (lastV == -1) {
            dialogContent = getString(R.string.first_welcome_dialog_content);
            // save last version for which welcome dialog was shown
            preferences.edit().putLong("last_welcome_dialog_displayed_at_version", 0).apply();
        }

        if (dialogContent != null) {
            // show the dialog
            String dialogTitle = getString(R.string.welcome_dialog_title, getVersionName(this));

            androidx.appcompat.app.AlertDialog.Builder builder = new androidx.appcompat.app.AlertDialog.Builder(
                    new ContextThemeWrapper(
                            this,
                            R.style.AppThemeDialog
                    )
            );
            builder.setTitle(dialogTitle);
            builder.setMessage(dialogContent);
            // Set up the buttons
            builder.setPositiveButton(getString(R.string.simple_ok), new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    displayWelcomeDialog();
                }
            });
            builder.setNeutralButton(getString(R.string.changelog), new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    Intent i = new Intent(Intent.ACTION_VIEW);
                    i.setData(Uri.parse(getString(R.string.changelog_url)));
                    startActivity(i);
                }
            });

            builder.show();
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        // refresh and sync every time the activity gets visible
        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
        long selectedProjectId = preferences.getLong("selected_project", 0);
        if (selectedProjectId != 0) {
            refreshLists();
        }
        swipeRefreshLayout.setRefreshing(false);

        if (!db.getMoneyBusterServerSyncHelper().isSyncPossible()) {
            swipeRefreshLayout.setEnabled(false);
        } else {
            swipeRefreshLayout.setEnabled(true);
            db.getMoneyBusterServerSyncHelper().addCallbackPull(syncCallBack);
            if (DEBUG) {
                Log.d(TAG, "[onResume]");
            }
            boolean offlineMode = preferences.getBoolean(getString(R.string.pref_key_offline_mode), false);
            if (!offlineMode) {
                synchronize();
            }
        }

        registerBroadcastReceiver();

        displayWelcomeDialog();
        activityVisible = true;
    }

    @Override
    protected void onPause() {
        if (DEBUG) { Log.d(TAG, "[onPause]"); }
        super.onPause();
        try {
            unregisterReceiver(mBroadcastReceiver);
        }
        catch (RuntimeException e) {
            if (DEBUG) { Log.d(TAG, "RECEIVER PROBLEM, let's ignore it..."); }
        }
        activityVisible = false;
    }

    public static boolean isActivityVisible() {
        return activityVisible;
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putSerializable(SAVED_STATE_NAVIGATION_SELECTION, navigationSelection);
        outState.putString(SAVED_STATE_NAVIGATION_ADAPTER_SLECTION, adapterMembers.getSelectedItem());
        outState.putString(SAVED_STATE_NAVIGATION_OPEN, navigationOpen);
    }

    private void setupToolBar() {
        setSupportActionBar(toolbar);
        int colors[] = {ThemeUtils.primaryColor(this), ThemeUtils.primaryLightColor(this)};
        GradientDrawable gradientDrawable = new GradientDrawable(
                GradientDrawable.Orientation.LEFT_RIGHT, colors);
        drawerLayout.findViewById(R.id.drawer_top_layout).setBackground(gradientDrawable);

        // hide nextcloud related stuff if needed
        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
        boolean showNextcloudSettings = preferences.getBoolean(getString(R.string.pref_key_show_nextcloud_settings), true);
        if (!showNextcloudSettings) {
            drawerLayout.findViewById(R.id.configuredAccountLayout).setVisibility(GONE);
            drawerLayout.findViewById(R.id.launchAccountSwitcher).setVisibility(GONE);
        }

        ImageView logoView = drawerLayout.findViewById(R.id.drawer_logo);
        logoView.setColorFilter(ThemeUtils.primaryColor(this), PorterDuff.Mode.OVERLAY);

        int colorsLastSync[] = {ThemeUtils.primaryDarkColor(this), ThemeUtils.primaryColor(this)};
        GradientDrawable gradientDrawable2 = new GradientDrawable(
                GradientDrawable.Orientation.LEFT_RIGHT, colorsLastSync);
        lastSyncLayout.setBackground(gradientDrawable2);

        menuButton.setOnClickListener((v) -> drawerLayout.openDrawer(GravityCompat.START));

        final BillsListViewActivity that = this;
        accountButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent settingsIntent = new Intent(that, AccountActivity.class);
                serverSettingsLauncher.launch(settingsIntent);
            }
        });

        ///////// SEARCH
        homeToolbar.setOnClickListener((v) -> {
            if (toolbar.getVisibility() == GONE) {
                updateToolbars(false);
            }
        });

        final LinearLayout searchEditFrame = searchView.findViewById(R.id.search_edit_frame);

        searchEditFrame.getViewTreeObserver().addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
            int oldVisibility = -1;

            @Override
            public void onGlobalLayout() {
                int currentVisibility = searchEditFrame.getVisibility();

                if (currentVisibility != oldVisibility) {
                    if (currentVisibility == VISIBLE) {
                        fabAddBill.setVisibility(View.INVISIBLE);
                    } else {
                        new Handler().postDelayed(() -> fabAddBill.setVisibility(VISIBLE), 150);
                    }

                    oldVisibility = currentVisibility;
                }
            }

        });
        searchView.setOnCloseListener(() -> {
            if (toolbar.getVisibility() == VISIBLE && TextUtils.isEmpty(searchView.getQuery())) {
                updateToolbars(true);
                return true;
            }
            return false;
        });
        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                return false;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                refreshLists();
                return true;
            }
        });
    }

    @SuppressLint("PrivateResource")
    private void updateToolbars(boolean disableSearch) {
        if (!disableSearch) {
            displaySearchHelp();
        }
        homeToolbar.setVisibility(disableSearch ? VISIBLE : GONE);
        toolbar.setVisibility(disableSearch ? GONE : VISIBLE);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            appBar.setStateListAnimator(AnimatorInflater.loadStateListAnimator(appBar.getContext(),
                    disableSearch ? R.animator.appbar_elevation_off : R.animator.appbar_elevation_on));
        } else {
            ViewCompat.setElevation(appBar, disableSearch ? 0 : getResources().getDimension(R.dimen.design_appbar_elevation));
        }
        if (disableSearch) {
            searchView.setQuery(null, true);
        }
        searchView.setIconified(disableSearch);
    }

    @Override
    public boolean onSupportNavigateUp() {
        if (toolbar.getVisibility() == VISIBLE) {
            updateToolbars(true);
            return true;
        } else {
            return super.onSupportNavigateUp();
        }
    }

    private void setupBillsList() {
        initList();

        // Pull to Refresh
        swipeRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                if (DEBUG) {
                    Log.d(TAG, "[onRefresh]");
                }
                if (db.getMoneyBusterServerSyncHelper().isSyncPossible()) {
                    synchronize();
                } else {
                    swipeRefreshLayout.setRefreshing(false);

                    Toast.makeText(getApplicationContext(), getString(R.string.error_sync, getString(CospendClientUtil.LoginStatus.NO_NETWORK.str)), Toast.LENGTH_LONG).show();
                }
            }
        });

        if (!db.getMoneyBusterServerSyncHelper().isSyncPossible()) {
            Log.d(TAG, "[no sync, disabled pull-to-refresh]");
            swipeRefreshLayout.setEnabled(false);
        }

        fabAddBill.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent createIntent = new Intent(getApplicationContext(), EditBillActivity.class);
                SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
                long selectedProjectId = preferences.getLong("selected_project", 0);
                if (selectedProjectId != 0) {
                    if (db.getActivatedMembersOfProject(selectedProjectId).size() < 1) {
                        showToast(getString(R.string.add_bill_impossible_no_member));
                    } else {
                        createIntent.putExtra(EditBillActivity.PARAM_PROJECT_ID, selectedProjectId);
                        createIntent.putExtra(EditBillActivity.PARAM_PROJECT_TYPE, db.getProject(selectedProjectId).getType().getId());
                        createBillLauncher.launch(createIntent);
                    }
                }
            }
        });

        // color
        boolean darkTheme = MoneyBuster.isDarkTheme(this);
        // if dark theme and main color is black, make fab button lighter/gray
        if (darkTheme && ThemeUtils.primaryColor(this) == Color.BLACK) {
            fabAddBill.setBackgroundTintList(ColorStateList.valueOf(Color.DKGRAY));
            //fabBillListAddProject.setBackgroundTintList(ColorStateList.valueOf(Color.DKGRAY));
        } else {
            fabAddBill.setBackgroundTintList(ColorStateList.valueOf(ThemeUtils.primaryColor(this)));
            //fabBillListAddProject.setBackgroundTintList(ColorStateList.valueOf(ThemeUtils.primaryColor(this)));
        }
        fabAddBill.setRippleColor(ThemeUtils.primaryDarkColor(this));
        //fabBillListAddProject.setRippleColor(ThemeUtils.primaryDarkColor(this));
    }

    private void setupBillsListCornerCases() {
        findViewById(R.id.button_no_members_add_member).setOnClickListener((view) -> {
            final SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
            final long selectedProjectId = preferences.getLong("selected_project", 0);
            onAddMemberClick(selectedProjectId);
        });
    }

    private void setupDrawerButtons() {
        configuredAccount.setOnClickListener((View view) -> {
            Intent intent = new Intent(getApplicationContext(), AccountActivity.class);
            serverSettingsLauncher.launch(intent);
        });

        findViewById(R.id.button_add_project).setOnClickListener((View view) -> {
            addProject();
            drawerLayout.closeDrawers();
        });

        findViewById(R.id.button_app_settings).setOnClickListener((View view) -> {
            Intent settingsIntent = new Intent(getApplicationContext(), PreferencesActivity.class);
            serverSettingsLauncher.launch(settingsIntent);
        });
    }

    private void setupDrawerProjects() {
        final SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
        final long selectedProjectId = preferences.getLong("selected_project", 0);
        final List<DBProject> dbProjects = db.getProjects();

        projectAdapter.setItems(dbProjects, selectedProjectId);
        listDrawerProjects.setAdapter(projectAdapter);

        // restore last selected project
        Log.v(TAG, "RESTORE PROJECT SELECTION " + selectedProjectId);
        setSelectedProject(selectedProjectId);
    }

    /* --- Project options --- */

    @Override
    public void onProjectClick(long projectId) {
        // FIXME: setting the state correctly is currently all over the place. Maybe unify somewhere?
        // order matters...
        setSelectedProject(projectId);
        adapterMembers.setSelectedItem(ADAPTER_KEY_ALL);
        navigationSelection = new Category(null, null);
        refreshLists(true);

        final SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
        boolean offlineMode = preferences.getBoolean(getString(R.string.pref_key_offline_mode), false);
        if (!offlineMode) {
            synchronize();
        }

        drawerLayout.closeDrawers();
    }

    @Override
    public void onManageProjectClick(long projectId) {
        CharSequence[] choices = new CharSequence[]{
                getString(R.string.action_edit_project),
                getString(R.string.fab_rm_project)
        };

        AlertDialog.Builder selectBuilder = new AlertDialog.Builder(new ContextThemeWrapper(BillsListViewActivity.this, R.style.AppThemeDialog));
        selectBuilder.setTitle(getString(R.string.choose_project_management_action));
        selectBuilder.setSingleChoiceItems(choices, -1, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                if (which == 0) {
                    onEditProjectClick(projectId);
                } else {
                    onRemoveProjectClick(projectId);
                }
                dialog.dismiss();
            }
        });
        selectBuilder.setNegativeButton(getString(R.string.simple_cancel), null);
        selectBuilder.show();
    }

    private void onEditProjectClick(long projectId) {
        if (projectId == 0) return;

        DBProject proj = db.getProject(projectId);
        if (!proj.isLocal()) {
            Intent editProjectIntent = new Intent(getApplicationContext(), EditProjectActivity.class);
            editProjectIntent.putExtra(PARAM_PROJECT_ID, projectId);
            editProjectLauncher.launch(editProjectIntent);

            drawerLayout.closeDrawers();
        } else {
            showToast(getString(R.string.edit_project_local_impossible));
        }
    }

    private void onRemoveProjectClick(long projectId) {
        if (projectId == 0) return;
        DBProject proj = db.getProject(projectId);

        AlertDialog.Builder builder = new AlertDialog.Builder(
                new ContextThemeWrapper(
                        this,
                        R.style.AppThemeDialog
                )
        );
        builder.setTitle(getString(R.string.confirm_remove_project_dialog_title));
        if (!proj.isLocal()) {
            builder.setMessage(getString(R.string.confirm_remove_project_dialog_message));
        }

        // Set up the buttons
        builder.setPositiveButton(getString(R.string.simple_yes), new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                db.deleteProject(projectId);
                List<DBProject> dbProjects = db.getProjects();
                if (dbProjects.size() > 0) {
                    setSelectedProject(dbProjects.get(0).getId());
                    Log.v(TAG, "set selection 0");
                } else {
                    setSelectedProject(0);
                }

                //drawerLayout.closeDrawers();
                setupDrawerProjects();
                refreshLists();
                synchronize();
                String projName = proj.getName();
                String projectNameString = (projName == null || "".equals(projName)) ? proj.getRemoteId() : projName;
                showToast(getString(R.string.remove_project_confirmation, projectNameString));
            }
        });
        builder.setNegativeButton(getString(R.string.simple_no), new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.cancel();
            }
        });

        builder.show();
    }


    @Override
    public void onManageMembersClick(long projectId) {
        CharSequence[] choices = new CharSequence[]{
                getString(R.string.fab_add_member),
                getString(R.string.fab_edit_member)
        };

        AlertDialog.Builder selectBuilder = new AlertDialog.Builder(new ContextThemeWrapper(BillsListViewActivity.this, R.style.AppThemeDialog));
        selectBuilder.setTitle(getString(R.string.choose_member_management_action));
        selectBuilder.setSingleChoiceItems(choices, -1, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                if (which == 0) {
                    onAddMemberClick(projectId);
                } else {
                    onEditMemberClick(projectId);
                }
                dialog.dismiss();
            }
        });
        selectBuilder.setNegativeButton(getString(R.string.simple_cancel), null);
        selectBuilder.show();
    }

    private void onEditMemberClick(long projectId) {
        if (projectId == 0) return;

        // check if we are allowed
        DBProject project = db.getProject(projectId);
        int myAccessLevel = project.getMyAccessLevel();
        if (myAccessLevel != DBProject.ACCESS_LEVEL_UNKNOWN && myAccessLevel < DBProject.ACCESS_LEVEL_MAINTAINER) {
            showToast(getString(R.string.insufficient_access_level));
            return;
        }

        final List<DBMember> members = db.getMembersOfProject(projectId, null);
        List<String> memberNames = new ArrayList<>();
        for (DBMember m : members) {
            memberNames.add(m.getName());
        }
        CharSequence[] namescs = memberNames.toArray(new CharSequence[memberNames.size()]);

        AlertDialog.Builder selectBuilder = new AlertDialog.Builder(new ContextThemeWrapper(BillsListViewActivity.this, R.style.AppThemeDialog));
        selectBuilder.setTitle(getString(R.string.choose_member_to_edit));
        selectBuilder.setSingleChoiceItems(namescs, -1, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                // user checked an item
                editMember(members.get(which).getId());
                dialog.dismiss();
            }
        });
        selectBuilder.setNegativeButton(getString(R.string.simple_cancel), null);
        selectBuilder.show();
    }

    private void onAddMemberClick(long projectId) {
        if (projectId == 0) return;

        // check if we are allowed
        DBProject project = db.getProject(projectId);
        int myAccessLevel = project.getMyAccessLevel();
        if (myAccessLevel != DBProject.ACCESS_LEVEL_UNKNOWN && myAccessLevel < DBProject.ACCESS_LEVEL_MAINTAINER) {
            showToast(getString(R.string.insufficient_access_level));
            return;
        }

        AlertDialog.Builder builder = new AlertDialog.Builder(
                new ContextThemeWrapper(BillsListViewActivity.this, R.style.AppThemeDialog)
        );
        builder.setTitle(getString(R.string.add_member_dialog_title));

        // Set up the input
        final EditText input = new EditText(new ContextThemeWrapper(
                BillsListViewActivity.this,
                R.style.AppThemeDialog
        ));
        input.setInputType(InputType.TYPE_CLASS_TEXT);
        input.setTextColor(ContextCompat.getColor(BillsListViewActivity.this, R.color.fg_default));
        builder.setView(input);

        // Set up the buttons
        builder.setPositiveButton(getString(R.string.simple_ok), new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                String memberName = input.getText().toString();

                if (!memberName.equals("")) {
                    List<DBMember> members = db.getMembersOfProject(projectId, null);
                    List<String> memberNames = new ArrayList<>();
                    for (DBMember m : members) {
                        memberNames.add(m.getName());
                    }
                    if (!memberNames.contains(memberName)) {
                        db.addMemberAndSync(
                                new DBMember(0, 0, projectId, memberName,
                                        true, 1, DBBill.STATE_ADDED,
                                        null, null, null, null, null)
                        );
                        refreshLists();
                        drawerLayout.closeDrawers();

                        View parent = findViewById(R.id.root_view);
                        String titleText = getString(R.string.snackbar_member_added, memberName);
                        String actionText = getString(R.string.snackbar_member_added_add_another);
                        Snackbar.make(parent, titleText, 6000)
                                .setAction(actionText, (view) -> {
                                    onAddMemberClick(projectId);
                                })
                                .show();
                    } else {
                        showToast(getString(R.string.member_already_exists));
                    }
                } else {
                    showToast(getString(R.string.member_edit_empty_name));
                }

                //new LoadCategoryListTask().executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
                InputMethodManager inputMethodManager = (InputMethodManager) input.getContext().getSystemService(Context.INPUT_METHOD_SERVICE);
                inputMethodManager.toggleSoftInput(InputMethodManager.HIDE_IMPLICIT_ONLY, 0);
            }
        });
        builder.setNegativeButton(getString(R.string.simple_cancel), new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.cancel();
                InputMethodManager inputMethodManager = (InputMethodManager) input.getContext().getSystemService(Context.INPUT_METHOD_SERVICE);
                inputMethodManager.toggleSoftInput(InputMethodManager.HIDE_IMPLICIT_ONLY, 0);
            }
        });

        builder.show();
        input.setSelectAllOnFocus(true);
        input.requestFocus();
        // show keyboard
        InputMethodManager inputMethodManager = (InputMethodManager) BillsListViewActivity.this.getSystemService(Context.INPUT_METHOD_SERVICE);
        inputMethodManager.toggleSoftInput(InputMethodManager.SHOW_FORCED, 0);
    }

    @Override
    public void onManageCurrenciesClick(long projectId) {
        final DBProject proj = db.getProject(projectId);
        if (proj != null && proj.getType().equals(ProjectType.COSPEND)) {
            Intent createIntent = new Intent(getApplicationContext(), ManageCurrenciesActivity.class);
            createIntent.putExtra(ManageCurrenciesActivity.EXTRA_PROJECT_ID, projectId);
            startActivity(createIntent);
        } else {
            showToast(getString(R.string.currency_management_unavailable));
        }
    }

    @Override
    public void onProjectStatisticsClick(long projectId) {
        final DBProject proj = db.getProject(projectId);
        AlertDialog dialog = new ProjectStatisticsDialogBuilder(this, db, proj).build();
        dialog.show();
    }

    @Override
    public void onSettleProjectClick(long projectId) {
        final DBProject proj = db.getProject(projectId);
        AlertDialog dialog = new ProjectSettlementDialogBuilder(this, db, proj, this).build();
        dialog.show();
    }

    @Override
    public void onShareProjectClick(long projectId) {
        final DBProject proj = db.getProject(projectId);
        if (projectId != 0 && proj.isShareable()) {
            AlertDialog dialog = new ProjectShareDialogBuilder(this, proj).build();
            dialog.show();
        } else {
            showToast(getString(R.string.share_impossible), Toast.LENGTH_LONG);
        }
    }

    @Override
    public void onExportProjectClick(long projectId) {
        if (projectId == 0) return;

        contentToExport = ExportUtil.createExportContent(db, projectId);
        String fileName = ExportUtil.createExportFileName(db, projectId);

        Intent intent = new Intent(Intent.ACTION_CREATE_DOCUMENT);
        intent.addCategory(Intent.CATEGORY_OPENABLE);
        intent.setType("text/csv");
        intent.putExtra(Intent.EXTRA_TITLE, fileName);

        // Open the launcher for a user to pick a location.
        // This will return an ActivityResult which will call saveToFileUri() to write the content to the selected file.
        saveFileLauncher.launch(intent);
    }

    private void saveToFileUri(String content, Uri fileUri) {
        try {
            OutputStream fOut = getContentResolver().openOutputStream(fileUri);
            OutputStreamWriter myOutWriter = new OutputStreamWriter(fOut);
            myOutWriter.append(content);
            myOutWriter.close();
            fOut.flush();
            fOut.close();
            showToast(getString(R.string.file_saved_success, fileUri.getLastPathSegment().replace(
                    Environment.getExternalStorageDirectory().toString(),
                    ""))
            );
        }
        catch (IOException e) {
            Log.e("Exception", "File write failed: " + e.toString());
            showToast(e.toString());
        }
    }

    /* --- End project options --- */

    private void addProject() {
        String defaultNcUrl = "https://mynextcloud.org";
        if (MoneyBusterServerSyncHelper.isNextcloudAccountConfigured(this)) {
            defaultNcUrl = MoneyBusterServerSyncHelper.getNextcloudAccountServerUrl(this);
        }
        String defaultIhmUrl = "https://ihatemoney.org";
        Intent newProjectIntent = new Intent(getApplicationContext(), NewProjectActivity.class);
        List<DBProject> projects = db.getProjects();

        String url;
        // look for a default NC url in existing projects
        for (DBProject project : projects) {
            url = project.getServerUrl();
            if (url != null && !url.equals("")) {
                if (url.contains("/index.php/apps/cospend")) {
                    defaultNcUrl = url.replace("/index.php/apps/cospend", "");
                    break;
                }
            }
        }
        // look for a default IHM url in existing projects
        for (DBProject project : projects) {
            url = project.getServerUrl();
            if (url != null && !url.equals("")) {
                if (!url.contains("/index.php/apps/cospend")) {
                    defaultIhmUrl = url;
                    break;
                }
            }
        }

        newProjectIntent.putExtra(NewProjectFragment.PARAM_DEFAULT_NC_URL, defaultNcUrl);
        newProjectIntent.putExtra(NewProjectFragment.PARAM_DEFAULT_IHM_URL, defaultIhmUrl);
        addProjectLauncher.launch(newProjectIntent);
    }

    private void setSelectedProject(long projectId) {
        // save to prefs
        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
        preferences.edit().putLong("selected_project", projectId).apply();

        DBProject proj = db.getProject(projectId);
        if (proj == null) {
            List<DBProject> dbProjects = db.getProjects();
            if (dbProjects.size() > 0) {
                proj = dbProjects.get(0);
                preferences.edit().putLong("selected_project", proj.getId()).apply();
            } else {
                searchText.setText(getString(R.string.action_search));
                return;
            }
        }

        // search text
        if (proj.getName() == null || "".equals(proj.getName())) {
            searchText.setText(getString(R.string.action_search));
        } else {
            searchText.setText(getString(R.string.action_search_in_project, proj.getName()));
        }

        // drawer
        updateLastSyncText();
        projectAdapter.setSelected(projectId);
        // XXX: The members list contents and the bills list handled in refreshLists()
    }

    // this is just called in setSelectedProject which is called often enough
    private void updateLastSyncText() {
        Log.v(TAG, "updateLastSyncText called");
        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
        long selectedId = preferences.getLong("selected_project", 0);
        if (selectedId == 0) {
            lastSyncLayout.setVisibility(GONE);
        } else {
            DBProject proj = db.getProject(selectedId);
            if (proj.isLocal()) {
                lastSyncLayout.setVisibility(GONE);
            } else {
                long lastSyncTimestamp = proj.getLastSyncedTimestamp();
                Calendar cal = Calendar.getInstance();
                cal.setTimeInMillis(lastSyncTimestamp * 1000);
                int hours = cal.get(Calendar.HOUR_OF_DAY);
                int minutes = cal.get(Calendar.MINUTE);
                lastSyncText.setText(getString(R.string.drawer_last_sync_text, hours, minutes));
                lastSyncLayout.setVisibility(VISIBLE);
            }
        }
    }

    private void editMember(long memberId) {
        final DBMember memberToEdit = db.getMember(memberId);
        Integer r = memberToEdit.getR();
        Integer g = memberToEdit.getG();
        Integer b = memberToEdit.getB();

        int color;
        if (r != null && g != null && b != null) {
            color = Color.rgb(memberToEdit.getR(), memberToEdit.getG(), memberToEdit.getB());
        } else {
            color = TextDrawable.getColorFromName(memberToEdit.getName());
        }

        Log.v(TAG, "MEMBER ID " + memberId);

        AlertDialog.Builder builder = new AlertDialog.Builder(
                new ContextThemeWrapper(
                        BillsListViewActivity.this,
                        R.style.AppThemeDialog
                )
        );
        builder.setTitle(getString(R.string.edit_member_dialog_title));

        // Set up the inputs
        final View iView = LayoutInflater.from(getApplicationContext()).inflate(R.layout.items_editmember_dialog, null);
        EditText nv = iView.findViewById(R.id.editMemberName);
        nv.setText(memberToEdit.getName());
        nv.setInputType(InputType.TYPE_CLASS_TEXT);
        nv.setTextColor(ContextCompat.getColor(BillsListViewActivity.this, R.color.fg_default));
        EditText we = iView.findViewById(R.id.editMemberWeight);
        we.setText(String.valueOf(memberToEdit.getWeight()));
        we.setTextColor(ContextCompat.getColor(BillsListViewActivity.this, R.color.fg_default));

        TextView tv = iView.findViewById(R.id.editMemberNameLabel);
        tv.setTextColor(ContextCompat.getColor(BillsListViewActivity.this, R.color.fg_default));
        TextView wv = iView.findViewById(R.id.editMemberWeightLabel);
        wv.setTextColor(ContextCompat.getColor(BillsListViewActivity.this, R.color.fg_default));
        CheckBox ch = iView.findViewById(R.id.editMemberActivated);
        ch.setTextColor(ContextCompat.getColor(BillsListViewActivity.this, R.color.fg_default));
        ch.setChecked(memberToEdit.isActivated());

        TextView tvCol = iView.findViewById(R.id.editMemberColorLabel);
        tvCol.setTextColor(ContextCompat.getColor(BillsListViewActivity.this, R.color.fg_default));
        Button bu = iView.findViewById(R.id.editMemberColor);
        bu.setBackgroundColor(color);
        bu.setText("");

        bu.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View buview) {
                LayoutInflater inflater = getLayoutInflater();
                View colorView = inflater.inflate(R.layout.dialog_color, new LinearLayout(BillsListViewActivity.this));

                final LobsterPicker lobsterPicker = colorView.findViewById(R.id.lobsterPicker);
                LobsterShadeSlider shadeSlider = colorView.findViewById(R.id.shadeSlider);

                lobsterPicker.addDecorator(shadeSlider);
                lobsterPicker.setColorHistoryEnabled(true);
                lobsterPicker.setHistory(color);
                lobsterPicker.setColor(color);

                new AlertDialog.Builder(new ContextThemeWrapper(
                        BillsListViewActivity.this,
                        R.style.AppThemeDialog
                ))
                        .setView(colorView)
                        .setTitle(getString(R.string.settings_colorpicker_title))
                        .setPositiveButton(getString(R.string.simple_ok), new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialogInterface, int i) {
                                int newColor = lobsterPicker.getColor();
                                bu.setBackgroundColor(newColor);
                            }
                        })
                        .setNegativeButton(getString(R.string.simple_cancel), null)
                        .show();

            }
        });

                    /*final EditText input = new EditText(getApplicationContext());
                    input.setInputType(InputType.TYPE_CLASS_TEXT);
                    input.setTextColor(ContextCompat.getColor(view.getContext(), R.color.fg_default));
                    input.setText(memberToEdit.getName());*/
        builder.setView(iView);

        // Set up the buttons
        builder.setPositiveButton(getString(R.string.simple_ok), new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                EditText nvi = iView.findViewById(R.id.editMemberName);
                String newMemberName = nvi.getText().toString();

                EditText wvi = iView.findViewById(R.id.editMemberWeight);
                double newMemberWeight = 1.0;
                try {
                    newMemberWeight = Double.valueOf(wvi.getText().toString().replace(',', '.'));
                }
                catch (Exception e) {
                    showToast(getString(R.string.member_edit_weight_error));
                    return;
                }

                CheckBox cvi = iView.findViewById(R.id.editMemberActivated);
                boolean newActivated = cvi.isChecked();

                Button bu = iView.findViewById(R.id.editMemberColor);
                int newColor = ((ColorDrawable) bu.getBackground()).getColor();
                int red = Color.red(newColor);
                int green = Color.green(newColor);
                int blue = Color.blue(newColor);

                SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
                long selectedProjectId = preferences.getLong("selected_project", 0);

                if (selectedProjectId != 0) {
                    if (!newMemberName.isEmpty() && !newMemberName.equals("")) {
                        db.updateMemberAndSync(
                            memberToEdit, newMemberName, newMemberWeight, newActivated,
                            red, green, blue, "", ""
                        );
                        refreshLists();
                        // this was used to programmatically select member
                        //navigationSelection = new Category(newMemberName, memberToEdit.getId());
                    } else {
                        showToast(getString(R.string.member_edit_empty_name));
                    }
                }
                // restore keyboard auto hide behaviour
                InputMethodManager inputMethodManager = (InputMethodManager) iView.getContext().getSystemService(Context.INPUT_METHOD_SERVICE);
                inputMethodManager.toggleSoftInput(InputMethodManager.HIDE_IMPLICIT_ONLY, 0);
            }
        });
        builder.setNegativeButton(getString(R.string.simple_cancel), new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.cancel();
                // restore keyboard auto hide behaviour
                InputMethodManager inputMethodManager = (InputMethodManager) iView.getContext().getSystemService(Context.INPUT_METHOD_SERVICE);
                inputMethodManager.toggleSoftInput(InputMethodManager.HIDE_IMPLICIT_ONLY, 0);
            }
        });

        builder.show();
        nv.setSelectAllOnFocus(true);
        nv.requestFocus();
        // show keyboard
        InputMethodManager inputMethodManager = (InputMethodManager) iView.getContext().getSystemService(Context.INPUT_METHOD_SERVICE);
        inputMethodManager.toggleSoftInput(InputMethodManager.SHOW_FORCED, 0);
    }

    private void setupDrawerProjectMembers(final String selectedItem) {
        itemAll = new NavigationAdapter.NavigationItem(ADAPTER_KEY_ALL, getString(R.string.label_all_bills), null, R.drawable.ic_allgrey_24dp, false);

        adapterMembers = new NavigationAdapter(new NavigationAdapter.ClickListener() {
            @Override
            public void onItemClick(NavigationAdapter.NavigationItem item) {
                selectItem(item, true);
            }

            private void selectItem(NavigationAdapter.NavigationItem item, boolean closeNavigation) {
                adapterMembers.setSelectedItem(item.id);

                // update current selection
                if (itemAll == item) {
                    navigationSelection = new Category(null, null);
                } else {
                    navigationSelection = new Category(item.label, Long.valueOf(item.id));
                }

                // auto-close sub-folder in Navigation if selection is outside of that folder
                /*if (navigationOpen != null) {
                    int slashIndex = navigationSelection.category == null ? -1 : navigationSelection.category.indexOf('/');
                    String rootCategory = slashIndex < 0 ? navigationSelection.category : navigationSelection.category.substring(0, slashIndex);
                    if (!navigationOpen.equals(rootCategory)) {
                        navigationOpen = null;
                    }
                }*/

                // update views
                if (closeNavigation) {
                    drawerLayout.closeDrawers();
                }
                refreshLists(true);
            }

            /*@Override
            public void onIconClick(NavigationAdapter.NavigationItem item) {
                if (item.icon == NavigationAdapter.ICON_MULTIPLE && !item.label.equals(navigationOpen)) {
                    navigationOpen = item.label;
                    selectItem(item, false);
                } else if (item.icon == NavigationAdapter.ICON_MULTIPLE || item.icon == NavigationAdapter.ICON_MULTIPLE_OPEN && item.label.equals(navigationOpen)) {
                    navigationOpen = null;
                    refreshLists();
                } else {
                    onItemClick(item);
                }
            }*/

            @Override
            public void onIconClick(NavigationAdapter.NavigationItem item) {
                onItemClick(item);
            }
        });
        adapterMembers.setSelectedItem(selectedItem);
        listDrawerMembers.setAdapter(adapterMembers);
        //listNavigationMembers.setNestedScrollingEnabled(false);
    }


    private class LoadCategoryListTask extends AsyncTask<Void, Void, List<NavigationAdapter.NavigationItem>> {
        @Override
        protected List<NavigationAdapter.NavigationItem> doInBackground(Void... voids) {
            SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
            long selectedProjectId = preferences.getLong("selected_project", 0);

            ArrayList<NavigationAdapter.NavigationItem> items = new ArrayList<>();

            if (selectedProjectId == 0) {
                return items;
            }

            List<DBMember> dbMembers = db.getMembersOfProject(selectedProjectId, null);

            Map<Long, Integer> membersNbBills = new HashMap<>();
            HashMap<Long, Double> membersBalance = new HashMap<>();
            HashMap<Long, Double> membersPaid = new HashMap<>();
            HashMap<Long, Double> membersSpent = new HashMap<>();

            int nbBills = SupportUtil.getStatsOfProject(
                selectedProjectId, db,
                membersNbBills, membersBalance, membersPaid, membersSpent,
                -1000, -1000, null, null
            );

            itemAll.count = null;
            items.add(itemAll);

            NumberFormat weightFormatter = new DecimalFormat("#.##");

            for (DBMember m : dbMembers) {
                double balance = membersBalance.get(m.getId());
                double rBalance = Math.round( balance * 100.0 ) / 100.0;

                // add member in sidebar list if he/she's activated or the balance is not "zero"
                if (m.isActivated() || balance <= -0.01 || balance >= 0.01) {
                    String weightStr = "";
                    if (m.getWeight() != 1) {
                        weightStr = " x" + weightFormatter.format(m.getWeight()).replace(",", ".");
                    }
                    NavigationAdapter.NavigationItem it = new NavigationAdapter.NavigationItem(
                            String.valueOf(m.getId()),
                            m.getName() + weightStr,
                            rBalance,
                            R.drawable.ic_person_grey_24dp,
                            true
                    );

                    items.add(it);
                }
            }
            return items;
        }

        @Override
        protected void onPostExecute(List<NavigationAdapter.NavigationItem> items) {
            adapterMembers.setItems(items);
        }
    }

    public void initList() {
        adapter = new ItemAdapter(this, db);
        listBillItems.setAdapter(adapter);
        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(this);
        listBillItems.setLayoutManager(linearLayoutManager);
        listBillItems.addItemDecoration(new DividerItemDecoration(listBillItems.getContext(),
                linearLayoutManager.getOrientation()));
        ItemTouchHelper touchHelper = new ItemTouchHelper(new SimpleCallback(0, ItemTouchHelper.LEFT | ItemTouchHelper.RIGHT) {
            @Override
            public boolean onMove(RecyclerView recyclerView, RecyclerView.ViewHolder viewHolder, RecyclerView.ViewHolder target) {
                return false;
            }

            /**
             * Disable swipe on sections
             *
             * @param recyclerView RecyclerView
             * @param viewHolder   RecyclerView.ViewHoler
             * @return 0 if section, otherwise super()
             */
            @Override
            public int getSwipeDirs(RecyclerView recyclerView, RecyclerView.ViewHolder viewHolder) {
                if (viewHolder instanceof ItemAdapter.SectionViewHolder) return 0;
                return super.getSwipeDirs(recyclerView, viewHolder);
            }

            /**
             * Delete bill if it is swiped to left or right
             *
             * @param viewHolder RecyclerView.ViewHoler
             * @param direction  int
             */
            @Override
            public void onSwiped(RecyclerView.ViewHolder viewHolder, int direction) {
                switch(direction) {
                    case ItemTouchHelper.LEFT: {
                        final DBBill bill = (DBBill) adapter.getItem(viewHolder.getAdapterPosition());
                        final DBBill dbBill = db.getBill(bill.getId());
                        // check if we can delete bills in this project
                        final DBProject project = db.getProject(dbBill.getProjectId());
                        if (project.isDeletionDisabled()) {
                            showToast(getString(R.string.bill_deletion_is_disabled));
                            refreshLists();
                            return;
                        }
                        // get real original state to potentially restore it
                        final int originalState = dbBill.getState();

                        if (originalState == DBBill.STATE_ADDED) {
                            db.deleteBill(dbBill.getId());
                        } else {
                            db.setBillState(dbBill.getId(), DBBill.STATE_DELETED);
                        }
                        adapter.remove(dbBill);
                        refreshLists();
                        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
                        final boolean offlineMode = preferences.getBoolean(getString(R.string.pref_key_offline_mode), false);
                        Log.v(TAG, "Item deleted through swipe ----------------------------------------------");
                        Snackbar.make(swipeRefreshLayout, R.string.action_bill_deleted, Snackbar.LENGTH_LONG)
                                .setAction(R.string.action_undo, new View.OnClickListener() {
                                    @Override
                                    public void onClick(View v) {
                                        if (originalState == DBBill.STATE_ADDED) {
                                            db.addBill(dbBill);
                                        } else {
                                            db.setBillState(dbBill.getId(), originalState);
                                        }
                                        refreshLists();
                                        Snackbar.make(swipeRefreshLayout, R.string.action_bill_restored, Snackbar.LENGTH_SHORT)
                                                .show();
                                        if (!offlineMode) {
                                            synchronize();
                                        }
                                        //notifyLoggerService(dbBill.getId());
                                    }
                                })
                                .addCallback(new Snackbar.Callback() {

                                    @Override
                                    public void onDismissed(Snackbar snackbar, int event) {
                                        //see Snackbar.Callback docs for event details
                                        Log.v(TAG, "DISMISSED "+event);
                                        if (event == DISMISS_EVENT_TIMEOUT) {
                                            if (!offlineMode) {
                                                synchronize();
                                            }
                                        }
                                    }

                                    @Override
                                    public void onShown(Snackbar snackbar) {
                                        Log.v(TAG, "SHOWN");
                                    }
                                })
                                .show();
                        //notifyLoggerService(dbBill.getId());
                        break;
                    }
                    case ItemTouchHelper.RIGHT: {
                        //final DBBill dbBill = (DBBill) adapter.getItem(viewHolder.getAdapterPosition());

                        refreshLists();

                        break;
                    }
                }
            }

            @Override
            public void onChildDraw(Canvas c, RecyclerView recyclerView, RecyclerView.ViewHolder viewHolder, float dX, float dY, int actionState, boolean isCurrentlyActive) {
                ItemAdapter.BillViewHolder billViewHolder = (ItemAdapter.BillViewHolder) viewHolder;
                // show swipe icon on the side
                billViewHolder.showSwipe(dX>0);
                // move only swipeable part of item (not leave-behind)
                getDefaultUIUtil().onDraw(c, recyclerView, billViewHolder.billSwipeable, dX, dY, actionState, isCurrentlyActive);
            }

            @Override
            public void clearView(RecyclerView recyclerView, RecyclerView.ViewHolder viewHolder) {
                getDefaultUIUtil().clearView(((ItemAdapter.BillViewHolder) viewHolder).billSwipeable);
            }
        });
        touchHelper.attachToRecyclerView(listBillItems);
    }

    private void refreshLists() {
        refreshLists(false);
    }
    public void refreshLists(final boolean scrollToTop) {
        long projId = 0;
        String projName = "";

        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
        long selectedProjectId = preferences.getLong("selected_project", 0);

        ProjectType type = ProjectType.LOCAL;
        if (selectedProjectId != 0) {
            DBProject proj = db.getProject(selectedProjectId);
            if (proj != null) {
                type = proj.getType();
                projId = proj.getId();
                if (proj.isLocal()) {
                    projName = proj.getRemoteId();
                } else {
                    projName = (proj.getName() == null) ? "???" : proj.getName();
                }
            }
        }
        final ProjectType projectType = type;

        String subtitle;
        if (selectedProjectId != 0) {
            if (navigationSelection.memberName != null) {
                subtitle = projName + " - " + navigationSelection.memberName;
            } else {
                subtitle = projName + " - " + getString(R.string.label_all_bills);
            }
        } else {
            subtitle = getString(R.string.app_name);
        }
        // to display correct name on project selector when project was just added
        setSelectedProject(selectedProjectId);

        setTitle(subtitle);
        CharSequence query = null;
        if (searchView != null && !searchView.isIconified() && searchView.getQuery().length() != 0) {
            query = searchView.getQuery();
        }

        int memberCount = db.getMembersOfProject(selectedProjectId, null).size();
        if (memberCount == 0) {
            swipeRefreshLayout.setVisibility(View.GONE);
            noMembersView.setVisibility(View.VISIBLE);
        } else {
            swipeRefreshLayout.setVisibility(View.VISIBLE);
            noMembersView.setVisibility(View.GONE);
        }

        LoadBillsListTask.BillsLoadedListener callback = new LoadBillsListTask.BillsLoadedListener() {
            @Override
            public void onBillsLoaded(List<Item> billItems, boolean showCategory) {
                adapter.setProjectType(projectType);
                adapter.setItemList(billItems);
                if(scrollToTop) {
                    listBillItems.scrollToPosition(0);
                }
            }
        };
        new LoadBillsListTask(getApplicationContext(), callback, navigationSelection, query, projId).executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
        new LoadCategoryListTask().executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
    }

    public ItemAdapter getItemAdapter() {
        return adapter;
    }

    public SwipeRefreshLayout getSwipeRefreshLayout() {
        return swipeRefreshLayout;
    }

    private void displaySearchHelp() {
        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
        boolean noMoreSearchHelp = preferences.getBoolean(AccountActivity.SETTINGS_NO_MORE_SEARCH_HELP, false);

        if (!noMoreSearchHelp) {
            AlertDialog.Builder helpBuilder = new AlertDialog.Builder(new ContextThemeWrapper(this, R.style.AppThemeDialog));
            helpBuilder.setTitle(getString(R.string.search_help_dialog_title));
            helpBuilder.setMessage(getString(R.string.search_help_dialog_content));

            // add OK and Cancel buttons
            helpBuilder.setPositiveButton(getString(R.string.simple_ok), new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                }
            });
            helpBuilder.setNeutralButton(getString(R.string.simple_ok_no_more), new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    preferences.edit().putBoolean(AccountActivity.SETTINGS_NO_MORE_SEARCH_HELP, true).apply();
                }
            });

            AlertDialog selectDialog = helpBuilder.create();
            selectDialog.show();
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle item selection
        switch (item.getItemId()) {
            /*case R.id.search:
                displaySearchHelp();
                return true;*/
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    @Override
    protected void onNewIntent(Intent intent) {
        if (Intent.ACTION_SEARCH.equals(intent.getAction())) {
            searchView.setQuery(intent.getStringExtra(SearchManager.QUERY), true);
        }
        super.onNewIntent(intent);
    }

    private final ActivityResultLauncher<Intent> addProjectLauncher =
            registerForActivityResult(new ActivityResultContracts.StartActivityForResult(),
                    new ActivityResultCallback<ActivityResult>() {
                        @Override
                        public void onActivityResult(ActivityResult result) {
                            Intent data = result.getData();
                            if (result.getResultCode() == RESULT_OK && data != null) {
                                long pid = data.getLongExtra(CREATED_PROJECT, 0);
                                boolean created = true;
                                if (pid == 0) {
                                    created = false;
                                    pid = data.getLongExtra(ADDED_PROJECT, 0);
                                }
                                if (DEBUG) { Log.d(TAG, "BILLS request code : addproject " + pid); }
                                if (pid != 0) {
                                    setSelectedProject(pid);
                                    Log.d(TAG, "CREATED project id: " + pid);
                                    DBProject addedProj = db.getProject(pid);
                                    String message;
                                    String title;
                                    if (created) {
                                        Log.e(TAG, "CREATED !!!");
                                        title = getString(R.string.project_create_success_title);
                                        message = getString(R.string.project_create_success_message, addedProj.getRemoteId());
                                    } else {
                                        Log.e(TAG, "ADDED !!!");
                                        title = getString(R.string.project_add_success_title);
                                        message = getString(R.string.project_add_success_message, addedProj.getRemoteId());
                                    }
                                    showDialog(message, title, R.drawable.ic_add_circle_white_24dp);
                                }
                            }
                            setupDrawerProjects();
                        }
                    });

    private final ActivityResultLauncher<Intent> serverSettingsLauncher =
            registerForActivityResult(new ActivityResultContracts.StartActivityForResult(),
                    new ActivityResultCallback<ActivityResult>() {
                        @Override
                        public void onActivityResult(ActivityResult result) {
                            Intent data = result.getData();
                            if (result.getResultCode() == RESULT_OK && data != null) {
                                updateUsernameInDrawer();
                                db = MoneyBusterSQLiteOpenHelper.getInstance(BillsListViewActivity.this);
                                if (db.getMoneyBusterServerSyncHelper().isSyncPossible()) {
                                    /*adapter.removeAll();
                                    synchronize();*/
                                } else {
                                    if (MoneyBusterServerSyncHelper.isNextcloudAccountConfigured(getApplicationContext())) {
                                        Toast.makeText(getApplicationContext(), getString(R.string.error_sync, getString(CospendClientUtil.LoginStatus.NO_NETWORK.str)), Toast.LENGTH_LONG).show();
                                    }
                                }
                            }
                        }
                    });

    private final ActivityResultLauncher<Intent> createBillLauncher =
            registerForActivityResult(new ActivityResultContracts.StartActivityForResult(),
                    new ActivityResultCallback<ActivityResult>() {
                        @Override
                        public void onActivityResult(ActivityResult result) {
                            Intent data = result.getData();
                            if (result.getResultCode() == RESULT_OK) {
                                if (data != null) {
                                    long createdBillId = data.getLongExtra(SAVED_BILL_ID, 0);
                                    Log.d(TAG, "[ACT RESULT CREATED BILL ] " + createdBillId);
                                    //adapter.add(createdBill);
                                }
                                listBillItems.scrollToPosition(0);
                            }
                        }
                    });

    private final ActivityResultLauncher<Intent> editBillLauncher =
            registerForActivityResult(new ActivityResultContracts.StartActivityForResult(),
                    new ActivityResultCallback<ActivityResult>() {
                        @Override
                        public void onActivityResult(ActivityResult result) {
                            Intent data = result.getData();
                            Log.d(TAG, "EDIT BILL result");
                            if (result.getResultCode() == RESULT_OK) {
                                if (data != null) {
                                    Long editedBillId = data.getLongExtra(SAVED_BILL_ID, 0);
                                    Log.d(TAG, "[ACT RESULT EDITED BILL ] " + editedBillId);
                                    long billId = data.getLongExtra(BILL_TO_DUPLICATE, 0);
                                    Log.d(TAG, "onActivityResult bill edition BILL ID TO DUPLICATE : " + billId);
                                    if (billId != 0) {
                                        duplicateBill(billId);
                                    }
                                }
                            }
                        }
                    });

    private final ActivityResultLauncher<Intent> editProjectLauncher =
            registerForActivityResult(new ActivityResultContracts.StartActivityForResult(),
                    new ActivityResultCallback<ActivityResult>() {
                        @Override
                        public void onActivityResult(ActivityResult result) {
                            Intent data = result.getData();
                            Log.d(TAG, "EDIT project result");
                            if (result.getResultCode() == RESULT_OK) {
                                if (data != null) {
                                    // adapt after project has been deleted
                                    long pid = data.getLongExtra(DELETED_PROJECT, 0);
                                    Log.d(TAG, "onActivityResult editproject DELETED PID : "+pid);
                                    if (pid != 0) {
                                        setSelectedProject(0);
                                    }
                                    // adapt after project has been edited
                                    pid = data.getLongExtra(EDITED_PROJECT, 0);
                                    Log.d(TAG, "onActivityResult editproject EDITED PID : "+pid);
                                    if (pid != 0) {
                                        setSelectedProject(pid);
                                    }
                                }
                            }
                            setupDrawerProjects();
                        }
                    });

    private final ActivityResultLauncher<Intent> saveFileLauncher =
            registerForActivityResult(new ActivityResultContracts.StartActivityForResult(),
                    new ActivityResultCallback<ActivityResult>() {
                        @Override
                        public void onActivityResult(ActivityResult result) {
                            Intent data = result.getData();
                            Log.d(TAG, "SAVE FILE result");
                            if (result.getResultCode() == RESULT_OK) {
                                if (data != null) {
                                    Uri fileUri = data.getData();
                                    Log.v(TAG, "WE SAVE to " + fileUri);
                                    saveToFileUri(contentToExport, fileUri);
                                }
                            }
                        }
                    });

    /**
     * Handles the Results of started Sub Activities
     *
     * @param requestCode int to distinguish between the different Sub Activities
     * @param resultCode  int Return Code
     * @param data        Intent
     */
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (DEBUG) { Log.d(TAG, "[ACT RESULT] requestCode is " + requestCode); }
    }

    private void duplicateBill(Long billId) {
        Intent createIntent = new Intent(getApplicationContext(), EditBillActivity.class);
        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
        long selectedProjectId = preferences.getLong("selected_project", 0);
        if (selectedProjectId != 0) {
            if (db.getActivatedMembersOfProject(selectedProjectId).size() < 1) {
                showToast(getString(R.string.add_bill_impossible_no_member));
            } else {
                createIntent.putExtra(EditBillActivity.PARAM_PROJECT_ID, selectedProjectId);
                createIntent.putExtra(EditBillActivity.PARAM_PROJECT_TYPE, db.getProject(selectedProjectId).getType().getId());
                createIntent.putExtra(EditBillActivity.PARAM_BILL_ID_TO_DUPLICATE, billId);
                createBillLauncher.launch(createIntent);
            }
        }
    }

    private void showDialog(String msg, String title, int icon) {
        android.app.AlertDialog.Builder builder;
        builder = new android.app.AlertDialog.Builder(new ContextThemeWrapper(this, R.style.AppThemeDialog));
        builder.setTitle(title)
                .setMessage(msg)
                .setPositiveButton(android.R.string.ok, (DialogInterface dialog, int which) -> dialog.dismiss())
                .setIcon(icon)
                .show();
    }

    private void updateUsernameInDrawer() {
        if (!MoneyBusterServerSyncHelper.isNextcloudAccountConfigured(this)) {
            configuredAccount.setText(getString(R.string.drawer_no_account));
            updateAvatarInDrawer(false);
        } else {
            String accountServerUrl;
            String accountUser;
            SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(this);
            if (preferences.getBoolean(AccountActivity.SETTINGS_USE_SSO, false)) {
                try {
                    SingleSignOnAccount ssoAccount = SingleAccountHelper.getCurrentSingleSignOnAccount(this);
                    accountServerUrl = ssoAccount.url.replaceAll("/+$", "").replaceAll("^https?://", "");
                    accountUser = ssoAccount.userId;
                } catch (NextcloudFilesAppAccountNotFoundException | NoCurrentAccountSelectedException e) {
                    accountServerUrl = "error";
                    accountUser = "error";
                }
            } else {
                accountServerUrl = preferences.getString(AccountActivity.SETTINGS_URL, AccountActivity.DEFAULT_SETTINGS)
                        .replaceAll("/+$", "")
                        .replaceAll("^https?://", "");
                accountUser = preferences.getString(AccountActivity.SETTINGS_USERNAME, AccountActivity.DEFAULT_SETTINGS);
            }
            configuredAccount.setText(accountUser + "@" + accountServerUrl);
            updateAvatarInDrawer(true);
        }

    }

    private void updateAvatarInDrawer(boolean isAccountConfigured) {
        if (isAccountConfigured) {
            SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(this);
            String avatarB64 = preferences.getString(getString(R.string.pref_key_avatar), "");
            if (!"".equals(avatarB64)) {
                try {
                    byte[] decodedString = Base64.decode(avatarB64, Base64.DEFAULT);
                    Bitmap decodedByte = BitmapFactory.decodeByteArray(decodedString, 0, decodedString.length);
                    Bitmap rounded = ThemeUtils.getRoundedBitmap(decodedByte, decodedByte.getWidth() / 2);
                    avatarView.setImageBitmap(rounded);
                    accountButton.setImageBitmap(rounded);
                } catch (Exception e) {
                    avatarView.setImageDrawable(ContextCompat.getDrawable(this, R.drawable.ic_nextcloud_logo_white));
                    accountButton.setImageDrawable(ContextCompat.getDrawable(this, R.drawable.ic_nextcloud_logo_white));
                }
            } else {
                avatarView.setImageDrawable(ContextCompat.getDrawable(this, R.drawable.ic_nextcloud_logo_white));
                accountButton.setImageDrawable(ContextCompat.getDrawable(this, R.drawable.ic_nextcloud_logo_white));
            }
        } else {
            avatarView.setImageDrawable(ContextCompat.getDrawable(this, R.drawable.ic_nextcloud_logo_white));
            accountButton.setImageDrawable(ContextCompat.getDrawable(this, R.drawable.ic_account_circle_grey_24dp));
        }
    }

    @Override
    public void onBillClick(int position, View v) {
        if (mActionMode != null) {
            if (!adapter.select(position)) {
                v.setSelected(false);
                adapter.deselect(position);
            } else {
                v.setSelected(true);
            }
            int size = adapter.getSelected().size();
            mActionMode.setTitle(String.valueOf(getResources().getQuantityString(R.plurals.ab_selected, size, size)));
            int checkedItemCount = adapter.getSelected().size();
            boolean hasCheckedItems = checkedItemCount > 0;

            if (hasCheckedItems && mActionMode == null) {
                // TODO differ if one or more items are selected
                // if (checkedItemCount == 1) {
                // mActionMode = startActionMode(new
                // SingleSelectedActionModeCallback());
                // } else {
                // there are some selected items, start the actionMode
                mActionMode = startSupportActionMode(new MultiSelectedActionModeCallback());
                // }
            } else if (!hasCheckedItems && mActionMode != null) {
                // there no selected items, finish the actionMode
                mActionMode.finish();
            }
            adapter.notifyDataSetChanged();
        } else {
            DBBill bill = (DBBill) adapter.getItem(position);
            Intent intent;
            intent = new Intent(getApplicationContext(), EditBillActivity.class);
            intent.putExtra(EditBillActivity.PARAM_BILL_ID, bill.getId());
            SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
            long selectedProjectId = preferences.getLong("selected_project", 0);
            intent.putExtra(EditBillActivity.PARAM_PROJECT_TYPE, db.getProject(selectedProjectId).getType().getId());
            //intent.putExtra(EditBillActivity.PARAM_MEMBERS_BALANCE, membersBalance);
            editBillLauncher.launch(intent);
        }
    }

    @Override
    public boolean onBillLongClick(int position, View v) {
        boolean selected = adapter.select(position);
        if (selected) {
            v.setSelected(true);
            mActionMode = startSupportActionMode(new MultiSelectedActionModeCallback());
            int checkedItemCount = adapter.getSelected().size();
            mActionMode.setTitle(getResources().getQuantityString(R.plurals.ab_selected, checkedItemCount, checkedItemCount));
        }
        adapter.notifyDataSetChanged();
        return selected;
    }

    @Override
    public void onBackPressed() {
        if (toolbar.getVisibility() == VISIBLE) {
            updateToolbars(true);
        } else {
            super.onBackPressed();
        }
    }

    private void synchronize() {
        StackTraceElement[] stackTraceElements = Thread.currentThread().getStackTrace();
        if (DEBUG) { Log.d(TAG, "CALLER : " + stackTraceElements[3].getMethodName()); }
        if (db.getMoneyBusterServerSyncHelper().isSyncPossible()) {
            swipeRefreshLayout.setRefreshing(true);
            SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
            long selectedProjectId = preferences.getLong("selected_project", 0);

            if (selectedProjectId != 0) {
                DBProject proj = db.getProject(selectedProjectId);
                if (proj != null && !proj.isLocal()) {
                    if (DEBUG) {
                        Log.d(TAG, "SYNC ASKED : " + selectedProjectId);
                    }
                    db.getMoneyBusterServerSyncHelper().addCallbackPull(syncCallBack);
                    db.getMoneyBusterServerSyncHelper().scheduleSync(false, selectedProjectId);
                } else {
                    swipeRefreshLayout.setRefreshing(false);
                }
            } else {
                swipeRefreshLayout.setRefreshing(false);
            }
            // then sync the nextcloud account projects
            if (MoneyBusterServerSyncHelper.isNextcloudAccountConfigured(getApplicationContext())) {
                db.getMoneyBusterServerSyncHelper().runAccountProjectsSync();
            }
        }
    }

    /**
     * Handler for the MultiSelect Actions
     */
    private class MultiSelectedActionModeCallback implements ActionMode.Callback {

        @Override
        public boolean onCreateActionMode(ActionMode mode, Menu menu) {
            // inflate contextual menu
            mode.getMenuInflater().inflate(R.menu.menu_list_context_multiple, menu);
            return true;
        }

        @Override
        public boolean onPrepareActionMode(ActionMode mode, Menu menu) {
            return false;
        }

        /**
         * @param mode ActionMode - used to close the Action Bar after all work is done.
         * @param item MenuItem - the item in the List that contains the Node
         * @return boolean
         */
        @Override
        public boolean onActionItemClicked(ActionMode mode, MenuItem item) {
            switch (item.getItemId()) {
                case R.id.menu_delete:
                    List<Integer> selection = adapter.getSelected();
                    for (Integer i : selection) {
                        DBBill bill = (DBBill) adapter.getItem(i);

                        // check if we can delete bills
                        final DBProject project = db.getProject(bill.getProjectId());
                        if (project.isDeletionDisabled()) {
                            showToast(getString(R.string.bill_deletion_is_disabled));
                            mode.finish();
                            return true;
                        }

                        // get up to date bill
                        final DBBill dbBill = db.getBill(bill.getId());
                        // get real original state to potentially restore it
                        final int originalState = dbBill.getState();

                        if (originalState == DBBill.STATE_ADDED) {
                            db.deleteBill(dbBill.getId());
                        } else {
                            db.setBillState(dbBill.getId(), DBBill.STATE_DELETED);
                        }
                    }
                    mode.finish();
                    // delete selection has to be cleared
                    searchView.setIconified(true);
                    refreshLists();
                    return true;
                case R.id.menu_select_all:
                    adapter.clearSelection();
                    for (int i=0; i < adapter.getItemCount(); i++) {
                        adapter.select(i);
                    }
                    adapter.notifyDataSetChanged();

                    int checkedItemCount = adapter.getSelected().size();
                    mActionMode.setTitle(getResources().getQuantityString(R.plurals.ab_selected, checkedItemCount, checkedItemCount));

                    return true;
                default:
                    return false;
            }
        }

        @Override
        public void onDestroyActionMode(ActionMode mode) {
            adapter.clearSelection();
            mActionMode = null;
            adapter.notifyDataSetChanged();
        }
    }

    /**
     * Display toast message
     * @param text Message
     */
    private void showToast(CharSequence text) {
        showToast(text, Toast.LENGTH_SHORT);
    }

    /**
     * Display toast message
     * @param text Message
     * @param duration Duration
     */
    private void showToast(CharSequence text, int duration) {
        Toast toast = Toast.makeText(this, text, duration);
        toast.show();
    }

    /**
     * Register broadcast receiver for synchronization
     * and tracking status updates
     */
    private void registerBroadcastReceiver() {
        IntentFilter filter = new IntentFilter();
        filter.addAction(MoneyBusterServerSyncHelper.BROADCAST_PROJECT_SYNC_FAILED);
        filter.addAction(MoneyBusterServerSyncHelper.BROADCAST_PROJECT_SYNCED);
        filter.addAction(MoneyBusterServerSyncHelper.BROADCAST_SYNC_PROJECT);
        filter.addAction(MoneyBusterServerSyncHelper.BROADCAST_NETWORK_AVAILABLE);
        filter.addAction(MoneyBusterServerSyncHelper.BROADCAST_NETWORK_UNAVAILABLE);
        filter.addAction(MoneyBusterServerSyncHelper.BROADCAST_AVATAR_UPDATED);
        filter.addAction(BROADCAST_ACCOUNT_PROJECTS_SYNC_FAILED);
        filter.addAction(BROADCAST_ACCOUNT_PROJECTS_SYNCED);
        registerReceiver(mBroadcastReceiver, filter, Context.RECEIVER_EXPORTED);
    }

    /**
     * Broadcast receiver
     */
    private final BroadcastReceiver mBroadcastReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            if (DEBUG) { Log.d(TAG, "[broadcast received " + intent + "]"); }
            if (intent == null || intent.getAction() == null) {
                return;
            }
            switch (intent.getAction()) {
                case MoneyBusterServerSyncHelper.BROADCAST_PROJECT_SYNC_FAILED:
                    String errorMessage = intent.getStringExtra(BROADCAST_ERROR_MESSAGE);
                    long projectId = intent.getLongExtra(BROADCAST_PROJECT_ID, 0);
                    if (projectId != 0) {
                        DBProject project = db.getProject(projectId);
                        if (project == null) {
                            return;
                        }
                        String dialogContent = getString(R.string.sync_error_dialog_full_content, project.getName(), errorMessage);

                        android.app.AlertDialog.Builder builder;
                        builder = new android.app.AlertDialog.Builder(new ContextThemeWrapper(BillsListViewActivity.this, R.style.AppThemeDialog));
                        builder.setTitle(getString(R.string.sync_error_dialog_title))
                                .setMessage(dialogContent)
                                /*.setPositiveButton(getString(R.string.simple_remove), new DialogInterface.OnClickListener() {
                                    public void onClick(DialogInterface dialog, int which) {
                                        db.deleteProject(projectId);
                                        List<DBProject> dbProjects = db.getProjects();
                                        if (dbProjects.size() > 0) {
                                            setSelectedProject(dbProjects.get(0).getId());
                                            Log.v(TAG, "set selection 0");
                                        } else {
                                            setSelectedProject(0);
                                        }
                                        refreshLists();
                                        synchronize();
                                        showToast(getString(R.string.remove_project_confirmation, project.getName()));
                                    }
                                })*/
                                .setPositiveButton(getString(R.string.simple_close), new DialogInterface.OnClickListener() {
                                    public void onClick(DialogInterface dialog, int which) {
                                    }
                                })
                                .setIcon(R.drawable.ic_sync_grey_24dp)
                                .show();
                    }
                    break;
                case MoneyBusterServerSyncHelper.BROADCAST_PROJECT_SYNCED:
                    String projName = intent.getStringExtra(BROADCAST_EXTRA_PARAM);
                    refreshLists();
                    //showToast(getString(R.string.project_sync_success, projName));

                    // show sync success toast
                    LayoutInflater inflater = getLayoutInflater();
                    View layout = inflater.inflate(R.layout.sync_success_toast,
                            (ViewGroup) findViewById(R.id.custom_toast_container));

                    LinearLayout ll = layout.findViewById(R.id.custom_toast_container);
                    ll.setBackgroundColor(Color.TRANSPARENT);
                    TextView text = (TextView) layout.findViewById(R.id.text);
                    //text.setText(getString(R.string.project_sync_success, projName));
                    text.setText("");

                    Toast toast = new Toast(getApplicationContext());
                    toast.setGravity(Gravity.TOP | Gravity.END, 55, 6);
                    toast.setDuration(Toast.LENGTH_SHORT);
                    toast.setView(layout);
                    toast.show();

                    break;
                case MoneyBusterServerSyncHelper.BROADCAST_SYNC_PROJECT:
                    SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
                    boolean offlineMode = preferences.getBoolean(getString(R.string.pref_key_offline_mode), false);
                    if (!offlineMode) {
                        synchronize();
                    }
                    break;
                case MoneyBusterServerSyncHelper.BROADCAST_NETWORK_AVAILABLE:
                    swipeRefreshLayout.setEnabled(true);
                    break;
                case MoneyBusterServerSyncHelper.BROADCAST_NETWORK_UNAVAILABLE:
                    swipeRefreshLayout.setEnabled(false);
                    break;
                case MoneyBusterServerSyncHelper.BROADCAST_AVATAR_UPDATED:
                    Log.v("AAA", "BROAD AVATAR received");
                    long memberId = intent.getLongExtra(MoneyBusterServerSyncHelper.BROADCAST_AVATAR_UPDATED_MEMBER, 0);
                    // this is the account avatar
                    if (memberId == 0) {
                        Log.v("AAA", "UPDATE avatar of NC account");
                        updateAvatarInDrawer(true);
                    } else {
                        // update avatar for one specific member
                        Log.v("AAA", "UPDATE avatar of project member "+memberId);
                        refreshLists();
                    }
                    break;
                case BROADCAST_ACCOUNT_PROJECTS_SYNCED:
                    // show account projects sync success toast
                    LayoutInflater inflater2 = getLayoutInflater();
                    View layout2 = inflater2.inflate(R.layout.sync_success_toast,
                            (ViewGroup) findViewById(R.id.custom_toast_container));

                    LinearLayout ll2 = layout2.findViewById(R.id.custom_toast_container);
                    ll2.setBackgroundColor(Color.TRANSPARENT);
                    TextView text2 = (TextView) layout2.findViewById(R.id.text);
                    text2.setText("");
                    ImageView im = layout2.findViewById(R.id.toast_icon);
                    im.setImageResource(R.drawable.ic_nextcloud_logo_white);

                    Toast toast2 = new Toast(getApplicationContext());
                    toast2.setGravity(Gravity.TOP | Gravity.END, 55, 62);
                    toast2.setDuration(Toast.LENGTH_SHORT);
                    toast2.setView(layout2);
                    toast2.show();

                    // select a project if there are some and none is selected
                    SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
                    long selectedProjectId = prefs.getLong("selected_project", 0);
                    List<DBProject> dbProjects = db.getProjects();
                    if (selectedProjectId == 0 && dbProjects.size() > 0) {
                        setSelectedProject(dbProjects.get(0).getId());
                        Log.v(TAG, "set selection 0");
                        refreshLists();
                        if (!db.getMoneyBusterServerSyncHelper().isSyncPossible()) {
                            swipeRefreshLayout.setEnabled(false);
                        } else {
                            swipeRefreshLayout.setEnabled(true);
                            db.getMoneyBusterServerSyncHelper().addCallbackPull(syncCallBack);
                            boolean offlineMode2 = prefs.getBoolean(getString(R.string.pref_key_offline_mode), false);
                            if (!offlineMode2) {
                                synchronize();
                            }
                        }
                    }
                    break;
            }
        }
    };

}
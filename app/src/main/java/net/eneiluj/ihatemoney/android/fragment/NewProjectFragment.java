package net.eneiluj.ihatemoney.android.fragment;

import android.content.Context;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
//import android.preference.EditTextPreference;
import android.support.v7.preference.CheckBoxPreference;
import android.support.v7.preference.EditTextPreference;
//import android.preference.ListPreference;
//import android.preference.Preference;
import android.support.v7.preference.Preference;
//import android.preference.PreferenceFragment;
import android.support.v7.preference.PreferenceFragmentCompat;
import android.support.annotation.Nullable;
import android.support.v7.widget.DividerItemDecoration;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.WindowManager;

import butterknife.ButterKnife;
import net.eneiluj.ihatemoney.R;
import net.eneiluj.ihatemoney.model.DBProject;
import net.eneiluj.ihatemoney.persistence.IHateMoneySQLiteOpenHelper;
import net.eneiluj.ihatemoney.util.ICallback;

public class NewProjectFragment extends PreferenceFragmentCompat {

    private static final String SAVEDKEY_PROJECT = "project";

    public interface NewProjectFragmentListener {
        void close(long pid);
    }

    @Nullable
    protected IHateMoneySQLiteOpenHelper db;
    protected NewProjectFragmentListener listener;

    private Handler handler;

    protected EditTextPreference newProjectId;
    protected EditTextPreference newProjectIHMUrl;
    protected EditTextPreference newProjectPassword;
    protected CheckBoxPreference newProjectCreate;
    protected EditTextPreference newProjectEmail;
    protected EditTextPreference newProjectName;

    public static NewProjectFragment newInstance() {
        NewProjectFragment f = new NewProjectFragment();
        Bundle b = new Bundle();
        //b.putLong(PARAM_PROJECT_ID, projectId);
        //f.setArguments(b);
        return f;
    }

    @Override
    public void onCreatePreferences(Bundle savedInstanceState, String rootkey) {

    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        RecyclerView recyclerView = getListView();
        DividerItemDecoration dividerItemDecoration = new DividerItemDecoration(recyclerView.getContext(),
                DividerItemDecoration.VERTICAL);
        recyclerView.addItemDecoration(dividerItemDecoration);
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        addPreferencesFromResource(R.xml.activity_new_project);

        Preference idPref = findPreference("id");
        idPref.setOnPreferenceChangeListener(new Preference.OnPreferenceChangeListener() {

            @Override
            public boolean onPreferenceChange(Preference preference,
                                              Object newValue) {
                EditTextPreference pref = (EditTextPreference) findPreference("id");
                pref.setSummary((CharSequence) newValue);
                return true;
            }

        });
        Preference URLPref = findPreference("url");
        URLPref.setOnPreferenceChangeListener(new Preference.OnPreferenceChangeListener() {

            @Override
            public boolean onPreferenceChange(Preference preference,
                                              Object newValue) {
                EditTextPreference pref = (EditTextPreference) findPreference("url");
                pref.setSummary((CharSequence) newValue);
                return true;
            }

        });
        Preference namePref = findPreference("name");
        namePref.setOnPreferenceChangeListener(new Preference.OnPreferenceChangeListener() {

            @Override
            public boolean onPreferenceChange(Preference preference,
                                              Object newValue) {
                EditTextPreference pref = (EditTextPreference) findPreference("name");
                pref.setSummary((CharSequence) newValue);
                return true;
            }

        });
        Preference passwordPref = findPreference("password");
        passwordPref.setOnPreferenceChangeListener(new Preference.OnPreferenceChangeListener() {

            @Override
            public boolean onPreferenceChange(Preference preference,
                                              Object newValue) {
                EditTextPreference pref = (EditTextPreference) findPreference("password");
                //pref.setSummary((CharSequence) newValue);
                return true;
            }

        });
        Preference emailPref = findPreference("email");
        emailPref.setOnPreferenceChangeListener(new Preference.OnPreferenceChangeListener() {

            @Override
            public boolean onPreferenceChange(Preference preference,
                                              Object newValue) {
                EditTextPreference pref = (EditTextPreference) preference;
                pref.setSummary((CharSequence) newValue);
                return true;
            }

        });

        Preference createPref = findPreference("createonserver");
        createPref.setOnPreferenceChangeListener(new Preference.OnPreferenceChangeListener() {

            @Override
            public boolean onPreferenceChange(Preference preference,
                                              Object newValue) {
                CheckBoxPreference pref = (CheckBoxPreference) findPreference("createonserver");
                EditTextPreference emailPref = (EditTextPreference) findPreference("email");
                emailPref.setVisible((Boolean) newValue);
                EditTextPreference namePref = (EditTextPreference) findPreference("name");
                namePref.setVisible((Boolean) newValue);
                //pref.setChecked((Boolean) newValue);
                return true;
            }

        });

        handler = new Handler(Looper.getMainLooper());

        System.out.println("PROJECT on create : ");

        setHasOptionsMenu(true);
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        try {
            listener = (NewProjectFragmentListener) context;
        } catch (ClassCastException e) {
            throw new ClassCastException(context.getClass() + " must implement " + NewProjectFragmentListener.class);
        }
        db = IHateMoneySQLiteOpenHelper.getInstance(context);
    }

    @Override
    public void onResume() {
        super.onResume();
        //listener.onProjectUpdated(project);
    }

    @Override
    public void onPause() {
        super.onPause();
        //saveLogjob(null);
    }

    @Override
    public void onDetach() {
        super.onDetach();
        listener = null;
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        System.out.println("PROJECT SAVE INSTANCE STATEEEEEEEE");
        //saveLogjob(null);
        //outState.putSerializable(SAVEDKEY_PROJECT, project);
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        inflater.inflate(R.menu.menu_new_project_fragment, menu);
    }

    @Override
    public void onPrepareOptionsMenu(Menu menu) {
        super.onPrepareOptionsMenu(menu);
        //menu.findItem(R.id.menu_delete_remote).setVisible(false);
    }

    /**
     * Main-Menu-Handler
     */
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.menu_create:
                long pid = saveProject(null);
                listener.close(pid);
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    public void onCloseProject() {
        Log.d(getClass().getSimpleName(), "onCLOSE()");
    }

    /**
     * Save the current state in the database and schedule synchronization if needed.
     *
     * @param callback Observer which is called after save/synchronization
     */
    protected long saveProject(@Nullable ICallback callback) {
        // TODO create remote
        String remoteId = getRemoteId();
        String ihmUrl = getIhmUrl();
        String password = getPassword();
        String email = getEmail();
        String name = getName();
        boolean createRemote = getCreateRemote();

        DBProject newProject = new DBProject(0, remoteId, password, name, ihmUrl, email);
        long pid = db.addProject(newProject);
        System.out.println("PROJECT local id : "+pid);
        // TODO update billsview project selector
        return pid;
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        System.out.println("ACT CREATEDDDDDDD");
        ButterKnife.bind(this, getView());

        // hide the keyboard when this window gets the focus
        getActivity().getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_HIDDEN);

        newProjectEmail = (EditTextPreference) this.findPreference("email");
        newProjectEmail.setVisible(false);
        newProjectName = (EditTextPreference) this.findPreference("name");
        newProjectName.setVisible(false);

        newProjectId = (EditTextPreference) this.findPreference("id");
        newProjectPassword = (EditTextPreference) this.findPreference("password");
        newProjectIHMUrl = (EditTextPreference) this.findPreference("url");
        newProjectCreate = (CheckBoxPreference) this.findPreference("createonserver");
    }

    protected String getRemoteId() {
        return newProjectId.getText();
    }
    protected String getIhmUrl() {
        return newProjectIHMUrl.getText();
    }
    protected String getPassword() {
        return newProjectPassword.getText();
    }
    protected boolean getCreateRemote() {
        return newProjectCreate.isChecked();
    }
    protected String getName() {
        return newProjectName.getText();
    }
    protected String getEmail() {
        return newProjectEmail.getText();
    }

}
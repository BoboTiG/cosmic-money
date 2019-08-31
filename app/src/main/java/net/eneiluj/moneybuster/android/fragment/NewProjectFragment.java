package net.eneiluj.moneybuster.android.fragment;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.ColorStateList;
import android.graphics.Color;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.SimpleAdapter;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.view.ContextThemeWrapper;
import androidx.fragment.app.Fragment;
import androidx.preference.PreferenceManager;

import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.textfield.TextInputLayout;

import net.eneiluj.moneybuster.R;
import net.eneiluj.moneybuster.android.activity.EditBillActivity;
import net.eneiluj.moneybuster.android.activity.NewProjectActivity;
import net.eneiluj.moneybuster.android.activity.QrCodeScanner;
import net.eneiluj.moneybuster.model.DBAccountProject;
import net.eneiluj.moneybuster.model.DBBill;
import net.eneiluj.moneybuster.model.DBProject;
import net.eneiluj.moneybuster.model.ProjectType;
import net.eneiluj.moneybuster.persistence.MoneyBusterSQLiteOpenHelper;
import net.eneiluj.moneybuster.persistence.MoneyBusterServerSyncHelper;
import net.eneiluj.moneybuster.util.ICallback;
import net.eneiluj.moneybuster.util.MoneyBuster;
import net.eneiluj.moneybuster.util.SupportUtil;
import net.eneiluj.moneybuster.util.ThemeUtils;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static android.webkit.URLUtil.isValidUrl;

public class NewProjectFragment extends Fragment {
    private static final String TAG = NewProjectFragment.class.getSimpleName();

    private static final String SAVEDKEY_PROJECT = "project";
    public static final String PARAM_DEFAULT_IHM_URL = "defaultIhmUrl";
    public static final String PARAM_DEFAULT_NC_URL = "defaultNcUrl";
    public static final String PARAM_DEFAULT_PROJECT_ID = "defaultProjectId";
    public static final String PARAM_DEFAULT_PROJECT_PASSWORD = "defaultProjectPassword";
    public static final String PARAM_DEFAULT_PROJECT_TYPE = "defaultProjectType";
    public static final String PARAM_IS_IMPORT = "isImport";

    private final static int scan_qrcode_import_cmd = 33;

    public interface NewProjectFragmentListener {
        void close(long pid);
    }

    @Nullable
    protected MoneyBusterSQLiteOpenHelper db;
    protected NewProjectFragmentListener listener;

    private Handler handler;

    protected EditText newProjectId;
    protected EditText newProjectUrl;
    protected EditText newProjectPassword;
    protected Spinner whatTodoSpinner;
    protected Spinner whereSpinner;
    protected EditText newProjectEmail;
    protected EditText newProjectName;

    protected ImageView whereIcon;

    protected LinearLayout newProjectIdLayout;
    protected LinearLayout newProjectUrlLayout;
    protected TextInputLayout newProjectUrlInputLayout;
    protected LinearLayout newProjectPasswordLayout;
    protected LinearLayout newProjectNameLayout;
    protected LinearLayout newProjectEmailLayout;

    protected ImageView scanButton;
    protected ImageView nextcloudButton;

    protected FloatingActionButton fabOk;

    protected String defaultIhmUrl;
    protected String defaultNcUrl;

    private boolean isSpinnerWhereAction = false;
    private boolean isSpinnerWhatTodoAction = false;

    public static NewProjectFragment newInstance(String defaultIhmUrl, String defaultNCUrl,
                                                 @Nullable String defaultProjectId,
                                                 @Nullable String defaultProjectPassword,
                                                 ProjectType defaultProjectType,
                                                 Boolean isImport) {
        NewProjectFragment f = new NewProjectFragment();
        Bundle b = new Bundle();
        b.putString(PARAM_DEFAULT_IHM_URL, defaultIhmUrl);
        b.putString(PARAM_DEFAULT_NC_URL, defaultNCUrl);
        b.putString(PARAM_DEFAULT_PROJECT_ID, defaultProjectId);
        b.putString(PARAM_DEFAULT_PROJECT_PASSWORD, defaultProjectPassword);
        b.putString(PARAM_DEFAULT_PROJECT_TYPE, defaultProjectType.getId());
        b.putBoolean(PARAM_IS_IMPORT, isImport);
        f.setArguments(b);
        return f;
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        Log.d(TAG, "onCreateView");
        View view = inflater.inflate(R.layout.activity_new_project_form, container, false);
        whatTodoSpinner = view.findViewById(R.id.whatTodoSpinner);
        whereSpinner = view.findViewById(R.id.whereSpinner);
        whereIcon = view.findViewById(R.id.whereIcon);

        newProjectId = view.findViewById(R.id.editProjectId);
        newProjectUrl = view.findViewById(R.id.editProjectUrl);
        newProjectPassword = view.findViewById(R.id.editProjectPassword);
        newProjectEmail = view.findViewById(R.id.editProjectEmail);
        newProjectName = view.findViewById(R.id.editProjectName);

        newProjectIdLayout = view.findViewById(R.id.editProjectIdLayout);
        newProjectUrlLayout = view.findViewById(R.id.editProjectUrlLayout);
        newProjectUrlInputLayout = view.findViewById(R.id.editProjectUrlInputLayout);
        newProjectPasswordLayout = view.findViewById(R.id.editProjectPasswordLayout);
        newProjectEmailLayout = view.findViewById(R.id.editProjectEmailLayout);
        newProjectNameLayout = view.findViewById(R.id.editProjectNameLayout);

        scanButton = view.findViewById(R.id.scanButton);
        nextcloudButton = view.findViewById(R.id.nextcloudButton);

        fabOk = view.findViewById(R.id.fab_new_ok);

        fabOk.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                onPressOk();
            }
        });

        boolean darkTheme = MoneyBuster.getAppTheme(getContext());
        // if dark theme and main color is black, make fab button lighter/gray
        if (darkTheme && ThemeUtils.primaryColor(getContext()) == Color.BLACK) {
            fabOk.setBackgroundTintList(ColorStateList.valueOf(Color.DKGRAY));
        } else {
            fabOk.setBackgroundTintList(ColorStateList.valueOf(ThemeUtils.primaryColor(getContext())));
        }
        fabOk.setRippleColor(ThemeUtils.primaryDarkColor(getContext()));

        scanButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Log.d(TAG, "Scan button pressed");
                Intent createIntent = new Intent(getContext(), QrCodeScanner.class);
                startActivityForResult(createIntent, scan_qrcode_import_cmd);
            }
        });

        nextcloudButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                List<DBAccountProject> accountProjects = db.getAccountProjects();
                List<String> acProjNameList = new ArrayList<>();
                final List<Long> acProjIdList = new ArrayList<>();
                for (DBAccountProject accountProject : accountProjects) {
                    acProjNameList.add(accountProject.getName());
                    acProjIdList.add(accountProject.getId());
                }
                // manage account projects list DIALOG
                AlertDialog.Builder selectBuilder = new AlertDialog.Builder(new ContextThemeWrapper(view.getContext(), R.style.AppThemeDialog));
                selectBuilder.setTitle(getString(R.string.choose_account_project_dialog_title));

                if (acProjNameList.size() > 0) {

                    CharSequence[] entcs = acProjNameList.toArray(new CharSequence[acProjNameList.size()]);

                    selectBuilder.setSingleChoiceItems(entcs, -1, new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            long pid = acProjIdList.get(which);
                            DBAccountProject p = db.getAccountProject(pid);
                            newProjectId.setText(p.getRemoteId());
                            newProjectUrl.setText(p.getncUrl());
                            dialog.dismiss();
                            newProjectPassword.requestFocus();
                        }
                    });
                    selectBuilder.setNegativeButton(getString(R.string.simple_cancel), null);

                    AlertDialog selectDialog = selectBuilder.create();
                    selectDialog.show();
                }
                else {
                    showToast(getString(R.string.choose_account_project_dialog_impossible), Toast.LENGTH_LONG);
                }
            }
        });

        whatTodoSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parentView, View selectedItemView, int position, long id) {
                Log.d(TAG, "WHAT TODO spinner");
                Log.d(TAG, position + " ----> " + id);
                if (isSpinnerWhatTodoAction) {
                    showHideInputFields();
                    showHideValidationButtons();
                }

                isSpinnerWhatTodoAction = true;
            }

            @Override
            public void onNothingSelected(AdapterView<?> parentView) {
                Log.d(TAG, "WHAT TODO spinner NOTHING");
                showHideValidationButtons();
            }

        });

        whereSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parentView, View selectedItemView, int position, long id) {
                Log.d(TAG, "WHERE spinner");
                Log.d(TAG, position + " ----> " + id);
                if (isSpinnerWhereAction) {
                    showHideInputFields();
                    showHideValidationButtons();
                }

                isSpinnerWhereAction = true;
            }

            @Override
            public void onNothingSelected(AdapterView<?> parentView) {
                Log.d(TAG, "where spinner NOTHING");
                showHideValidationButtons();
            }

        });

        newProjectId.addTextChangedListener(new TextWatcher() {
            public void afterTextChanged(Editable s) {
                Log.d(TAG, "project id change");
                showHideValidationButtons();
            }

            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            public void onTextChanged(CharSequence s, int start, int before, int count) {
            }
        });

        newProjectUrl.addTextChangedListener(new TextWatcher() {
            public void afterTextChanged(Editable s) {
                Log.d(TAG, "project url change");
                showHideValidationButtons();
            }

            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            public void onTextChanged(CharSequence s, int start, int before, int count) {
            }
        });

        newProjectName.addTextChangedListener(new TextWatcher() {
            public void afterTextChanged(Editable s) {
                Log.d(TAG, "project name change");
                showHideValidationButtons();
            }

            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            public void onTextChanged(CharSequence s, int start, int before, int count) {
            }
        });
        newProjectPassword.addTextChangedListener(new TextWatcher() {
            public void afterTextChanged(Editable s) {
                Log.d(TAG, "project password change");
                showHideValidationButtons();
            }

            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            public void onTextChanged(CharSequence s, int start, int before, int count) {
            }
        });
        newProjectEmail.addTextChangedListener(new TextWatcher() {
            public void afterTextChanged(Editable s) {
                Log.d(TAG, "project email change");
                showHideValidationButtons();
            }

            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            public void onTextChanged(CharSequence s, int start, int before, int count) {
            }
        });

        return view;
    }

    private void showHideValidationButtons() {
        if (isFormValid()) {
            fabOk.show();
        }
        else {
            fabOk.hide();
        }
    }

    private boolean isFormValid() {
        boolean todoCreate = getTodoCreate();
        ProjectType type = getProjectType();

        String projectId = getRemoteId();
        String projectUrl = getUrl();
        String projectPassword = getPassword();
        String projectName = getName();
        String projectEmail = getEmail();

        // always check project ID
        if (projectId.equals("")) {
            return false;
        }

        // first, what is independent from creation/join
        if (!type.equals(ProjectType.LOCAL)) {
            if (projectUrl.equals("") || !isValidUrl(projectUrl) || projectPassword.equals("")) {
                return false;
            }
        }

        // create
        if (todoCreate) {
            if (!type.equals(ProjectType.LOCAL)) {
                if (projectName.equals("") || !SupportUtil.isValidEmail(projectEmail)) {
                    return false;
                }
            }
        }
        // join
        else {
            if (type.equals(ProjectType.LOCAL)) {
                return false;
            }
        }

        return true;
    }

    private void showHideInputFields() {
        boolean todoCreate = getTodoCreate();
        ProjectType type = getProjectType();

        // change 'where' selection if we want to join a project and 'where' is local
        if (!todoCreate && type.equals(ProjectType.LOCAL)) {
            whereSpinner.setSelection(1);
            type = getProjectType();
        }

        newProjectUrlLayout.setVisibility(!type.equals(ProjectType.LOCAL) ? View.VISIBLE : View.GONE);
        newProjectPasswordLayout.setVisibility(!type.equals(ProjectType.LOCAL) ? View.VISIBLE : View.GONE);

        boolean showNameEmail = (todoCreate && !type.equals(ProjectType.LOCAL));
        newProjectNameLayout.setVisibility(showNameEmail ? View.VISIBLE : View.GONE);
        newProjectEmailLayout.setVisibility(showNameEmail ? View.VISIBLE : View.GONE);

        scanButton.setVisibility(todoCreate ? View.GONE : View.VISIBLE);

        if (type.equals(ProjectType.LOCAL)) {
            whereIcon.setImageResource(R.drawable.ic_cellphone_grey_24dp);
            nextcloudButton.setVisibility(View.GONE);
        }
        else if (type.equals(ProjectType.IHATEMONEY)) {
            whereIcon.setImageResource(R.drawable.ic_ihm_grey_24dp);
            newProjectUrl.setText(defaultIhmUrl);
            newProjectUrlInputLayout.setHint(getString(R.string.setting_ihm_project_url));
            nextcloudButton.setVisibility(View.GONE);
        }
        else if (type.equals(ProjectType.COSPEND)) {
            whereIcon.setImageResource(R.drawable.ic_cospend_grey_24dp);
            newProjectUrl.setText(defaultNcUrl);
            newProjectUrlInputLayout.setHint(getString(R.string.setting_cospend_project_url));
            boolean isNCC = MoneyBusterServerSyncHelper.isNextcloudAccountConfigured(getContext());
            nextcloudButton.setVisibility((!todoCreate && isNCC) ? View.VISIBLE : View.GONE);
        }
    }

    private void hideKeyboard(Context context) {
        // hide keyboard
        InputMethodManager inputMethodManager = (InputMethodManager) context.getSystemService(Context.INPUT_METHOD_SERVICE);
        inputMethodManager.toggleSoftInput(InputMethodManager.HIDE_IMPLICIT_ONLY, 0);
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        handler = new Handler(Looper.getMainLooper());

        Log.d(TAG, "PROJECT on create : ");

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
        db = MoneyBusterSQLiteOpenHelper.getInstance(context);
    }

    @Override
    public void onResume() {
        super.onResume();
        //listener.onProjectUpdated(project);
    }

    @Override
    public void onPause() {
        super.onPause();
        //saveBill(null);
    }

    @Override
    public void onDetach() {
        super.onDetach();
        listener = null;
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        Log.d(TAG, "PROJECT SAVE INSTANCE STATE");
        //saveBill(null);
        //outState.putSerializable(SAVEDKEY_PROJECT, project);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        Log.d(TAG, "[ACT RESULT]");
        // Check which request we're responding to
        if (requestCode == scan_qrcode_import_cmd) {
            if (data != null) {
                // adapt after project has been deleted
                String scannedUrl = data.getStringExtra(QrCodeScanner.KEY_QR_CODE);
                Log.d(TAG, "onActivityResult SCANNED URL : "+scannedUrl);
                applyMBLink(Uri.parse(scannedUrl));
            }
        }
    }

    private void applyMBLink(Uri data) {
        String password;
        String url;
        String pid;
        if (data.getHost().equals("net.eneiluj.moneybuster.cospend") && data.getPathSegments().size() >= 2) {
            if (data.getPath().endsWith("/")) {
                password = "";
                pid = data.getLastPathSegment();
            }
            else {
                password = data.getLastPathSegment();
                pid = data.getPathSegments().get(data.getPathSegments().size() - 2);
            }
            url = "https:/" +
                    data.getPath().replaceAll("/"+pid+"/" + password + "$", "");
            newProjectPassword.setText(password);
            newProjectId.setText(pid);
            newProjectUrl.setText(url);
            whereSpinner.setSelection(2);
            showHideInputFields();
            showHideValidationButtons();
        }
        else if (data.getHost().equals("net.eneiluj.moneybuster.ihatemoney") && data.getPathSegments().size() >= 2) {
            if (data.getPath().endsWith("/")) {
                password = "";
                pid = data.getLastPathSegment();
            }
            else {
                password = data.getLastPathSegment();
                pid = data.getPathSegments().get(data.getPathSegments().size() - 2);
            }
            url = "https:/" +
                    data.getPath().replaceAll("/" + pid + "/" + password + "$", "");
            newProjectPassword.setText(password);
            newProjectId.setText(pid);
            newProjectUrl.setText(url);
            whereSpinner.setSelection(1);
            showHideInputFields();
            showHideValidationButtons();
        }
        else if (data.getHost().equals("ihatemoney.org") && data.getPathSegments().size() == 1) {
            password = "";
            pid = data.getLastPathSegment();
            url = "https://ihatemoney.org";
            newProjectPassword.setText(password);
            newProjectId.setText(pid);
            newProjectUrl.setText(url);
            whereSpinner.setSelection(1);
            showHideInputFields();
            showHideValidationButtons();
        }
        else {
            showToast(getString(R.string.import_bad_url), Toast.LENGTH_LONG);
        }
    }

    private void onPressOk() {
        String rid = getRemoteId();
        if (rid == null || rid.equals("")) {
            //showToast(getString(R.string.error_invalid_project_remote_id), Toast.LENGTH_LONG);
            return;
        }

        ProjectType type = getProjectType();

        if (!ProjectType.LOCAL.equals(type)) {
            // check values
            String url = getUrl();
            if (!isValidUrl(url)) {
                //showToast(getString(R.string.error_invalid_url), Toast.LENGTH_LONG);
                return;
            }
            String pwd = getPassword();
            if (url != null && !url.equals("") && (pwd == null || pwd.equals(""))) {
                //showToast(getString(R.string.error_invalid_project_password), Toast.LENGTH_LONG);
                return;
            }
        }

        // join or create local
        boolean todoCreate = getTodoCreate();
        if (!todoCreate || ProjectType.LOCAL.equals(type)) {
            long pid = saveProject(null);
            listener.close(pid);
        }
        // create remote project (we know the type is not local)
        // the callback will quit this activity
        else {
            String name = getName();
            if (name == null || name.equals("")) {
                //showToast(getString(R.string.error_invalid_project_name), Toast.LENGTH_LONG);
                return;
            }
            if (!SupportUtil.isValidEmail(getEmail())) {
                //showToast(getString(R.string.error_invalid_email), Toast.LENGTH_LONG);
                return;
            }
            if (!db.getMoneyBusterServerSyncHelper().createRemoteProject(getRemoteId(), getName(), getEmail(), getPassword(), getUrl(), getProjectType(), createRemoteCallBack)) {
                //showToast(getString(R.string.remote_project_operation_no_network), Toast.LENGTH_LONG);
            }
        }
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        /*inflater.inflate(R.menu.menu_new_project_fragment, menu);
        //ImageView addButton = getActivity().findViewById(R.id.menu_create);
        final ImageView addButton = (ImageView) menu.findItem(R.id.menu_create).getActionView();
        addButton.setImageResource(R.drawable.ic_add_circle_white_24dp);
        addButton.setPadding(40,0,40,0);
        addButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Animation animation1 =
                        AnimationUtils.loadAnimation(
                                getActivity().getApplicationContext(),
                                R.anim.rotation
                        );
                addButton.startAnimation(animation1);



            }
        });*/

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
        ProjectType type = getProjectType();
        String remoteId = getRemoteId();
        String url = null;
        String password = null;
        String email = null;
        String name = null;
        if (!type.equals(ProjectType.LOCAL)) {
            url = getUrl();
            password = getPassword();
            email = getEmail();
            name = getName();
        } else {

        }

        DBProject newProject = new DBProject(0, remoteId, password, name, url, email, null, type);
        long pid = db.addProject(newProject);

        // to make it the selected project even if we got here because of a VIEW intent
        // this is supposed to be done in activity result in billListViewActivity
        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(getActivity().getApplicationContext());
        preferences.edit().putLong("selected_project", pid).apply();

        showToast(getString(R.string.project_added_success), Toast.LENGTH_LONG);

        Log.i(TAG, "PROJECT local id : "+pid+" : "+newProject);
        return pid;
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        Log.d(TAG, "ACT CREATED");
        //ButterKnife.bind(this, getView());

        // hide the keyboard when this window gets the focus
        getActivity().getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_HIDDEN);

        // what to do spinner
        List<String> whatTodoNameList = new ArrayList<>();
        whatTodoNameList.add(getString(R.string.todo_join));
        whatTodoNameList.add(getString(R.string.todo_create));

        String[] whatTodoNames = whatTodoNameList.toArray(new String[whatTodoNameList.size()]);

        String[] whatTodoIds = getResources().getStringArray(R.array.whatTodoValues);
        //int index = Arrays.asList(whatTodoIds).indexOf(bill.getRepeat());

        ArrayList<Map<String, String>> data = new ArrayList<>();
        for (int i = 0; i < whatTodoNames.length; i++) {
            HashMap<String, String> hashMap = new HashMap<>();
            hashMap.put("name", whatTodoNames[i]);
            hashMap.put("id", whatTodoIds[i]);
            Log.d(TAG, "TODO add "+whatTodoIds[i]);
            data.add(hashMap);
        }
        String[] from = {"name", "id"};
        int[] to = new int[]{android.R.id.text1};
        SimpleAdapter simpleAdapter = new SimpleAdapter(this.getContext(), data, android.R.layout.simple_spinner_item, from, to);
        simpleAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        whatTodoSpinner.setAdapter(simpleAdapter);

        whatTodoSpinner.setSelection(0);

        // where spinner
        List<String> whereNameList = new ArrayList<>();
        whereNameList.add(getString(R.string.where_local));
        whereNameList.add(getString(R.string.where_ihm));
        whereNameList.add(getString(R.string.where_cospend));

        String[] whereNames = whereNameList.toArray(new String[whereNameList.size()]);

        List<String> whereIdsList = new ArrayList<>();
        whereIdsList.add(ProjectType.LOCAL.getId());
        whereIdsList.add(ProjectType.IHATEMONEY.getId());
        whereIdsList.add(ProjectType.COSPEND.getId());
        String[] whereIds = whereIdsList.toArray(new String[whereIdsList.size()]);
        //String[] whereIds = getResources().getStringArray(R.array.whereValues);
        //int index = Arrays.asList(whereIds).indexOf(getArguments().getString(PARAM_DEFAULT_PROJECT_TYPE));
        int index = whereIdsList.indexOf(getArguments().getString(PARAM_DEFAULT_PROJECT_TYPE));

        ArrayList<Map<String, String>> dataWhere = new ArrayList<>();
        for (int i = 0; i < whereNames.length; i++) {
            HashMap<String, String> hashMap = new HashMap<>();
            hashMap.put("name", whereNames[i]);
            hashMap.put("id", whereIds[i]);
            Log.d(TAG, "WHERE add "+whereIds[i]);
            dataWhere.add(hashMap);
        }
        String[] fromWhere = {"name", "id"};
        int[] toWhere = new int[]{android.R.id.text1};
        SimpleAdapter simpleAdapterWhere = new SimpleAdapter(this.getContext(), dataWhere, android.R.layout.simple_spinner_item, fromWhere, toWhere) {
            // Disable click item < month current
            @Override
            public boolean isEnabled(int position) {
                boolean todoCreate = getTodoCreate();
                if (!todoCreate && position == 0) {
                    return false;
                }
                return true;
            }
            // Change item appearance
            @Override
            public View getDropDownView(int position, View convertView,
                                        ViewGroup parent) {
                View v = null;
                boolean todoCreate = getTodoCreate();
                if (!todoCreate && position == 0) {
                    TextView tv = new TextView(getContext());
                    tv.setVisibility(View.GONE);
                    tv.setHeight(0);
                    v = tv;
                } else {
                    v = super.getDropDownView(position, null, parent);
                }
                return v;
            }
        };
        simpleAdapterWhere.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        whereSpinner.setAdapter(simpleAdapterWhere);

        if (index == -1) {
            whereSpinner.setSelection(0);
        }
        else {
            whereSpinner.setSelection(index);
        }

        defaultIhmUrl = getArguments().getString(PARAM_DEFAULT_IHM_URL);
        defaultNcUrl = getArguments().getString(PARAM_DEFAULT_NC_URL);

        showHideInputFields();

        newProjectId.setText(getArguments().getString(PARAM_DEFAULT_PROJECT_ID));

        String defaultPassword = getArguments().getString(PARAM_DEFAULT_PROJECT_PASSWORD);
        if (defaultPassword != null) {
            newProjectPassword.setText(getArguments().getString(PARAM_DEFAULT_PROJECT_PASSWORD));
        }
    }

    protected ProjectType getProjectType() {
        Map<String, String> item = (Map<String, String>) whereSpinner.getSelectedItem();
        Log.d(TAG, "project type item id "+item.get("id"));
        Log.d(TAG, "project type item name "+item.get("name"));
        return ProjectType.getTypeById(item.get("id"));
    }
    protected String getRemoteId() {
        return newProjectId.getText().toString();
    }
    protected String getUrl() {
        String url = newProjectUrl.getText().toString().trim();
        ProjectType type = getProjectType();
        if (ProjectType.COSPEND.equals(type)) {
            url = url.replaceAll("/+$", "") + "/index.php/apps/cospend";
        }
        return url;
    }
    protected String getPassword() {
        return newProjectPassword.getText().toString();
    }
    protected boolean getTodoCreate() {
        Map<String, String> item = (Map<String, String>) whatTodoSpinner.getSelectedItem();
        return (item.get("id").equals("c"));
    }
    protected String getName() {
        return newProjectName.getText().toString();
    }
    protected String getEmail() {
        return newProjectEmail.getText().toString();
    }

    private ICallback createRemoteCallBack = new ICallback() {
        @Override
        public void onFinish() {
        }

        public void onFinish(String result, String message) {
            if (message.isEmpty()) {
                long pid = saveProject(null);
                listener.close(pid);
            }
            else {
                showToast(getString(R.string.error_create_remote_project_helper, message), Toast.LENGTH_LONG);
                // TODO stop animation
            }
        }

        @Override
        public void onScheduled() {
        }
    };

    protected void showToast(CharSequence text, int duration) {
        Context context = getActivity();
        Toast toast = Toast.makeText(context, text, duration);
        toast.show();
    }

}

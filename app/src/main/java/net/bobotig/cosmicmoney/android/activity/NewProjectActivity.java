package net.bobotig.cosmicmoney.android.activity;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.MenuItem;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import net.bobotig.cosmicmoney.R;
import net.bobotig.cosmicmoney.android.fragment.NewProjectFragment;
import net.bobotig.cosmicmoney.model.ProjectType;

public class NewProjectActivity extends AppCompatActivity implements NewProjectFragment.NewProjectFragmentListener {

    protected NewProjectFragment fragment;

    @Override
    protected void onCreate(final Bundle savedInstanceState) {
        Log.d(getClass().getSimpleName(), "onCreate: ");
        super.onCreate(savedInstanceState);

        if (savedInstanceState == null) {
            launchNewProjectFragment();
        } else {
            fragment = (NewProjectFragment) getSupportFragmentManager().findFragmentById(android.R.id.content);
        }
    }

    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
        Log.d(getClass().getSimpleName(), "onNewIntent: ");
        setIntent(intent);
        if (fragment != null) {
            getSupportFragmentManager().beginTransaction().detach(fragment).commit();
            fragment = null;
        }
        launchNewProjectFragment();
    }

    protected String getDefaultIhmUrl() {
        return getIntent().getStringExtra(NewProjectFragment.PARAM_DEFAULT_IHM_URL);
    }

    protected String getDefaultNcUrl() {
        return getIntent().getStringExtra(NewProjectFragment.PARAM_DEFAULT_NC_URL);
    }

    private void launchNewProjectFragment() {
        String defaultIhmUrl = getDefaultIhmUrl();
        String defaultNcUrl = getDefaultNcUrl();
        String defaultProjectId = null;
        String defaultProjectPassword = null;
        ProjectType defaultProjectType = ProjectType.LOCAL;

        Boolean shouldCloseActivity = false;

        if (Intent.ACTION_VIEW.equals(getIntent().getAction())) {
            Uri data = getIntent().getData();
            if (data == null) {
                showToast(getString(R.string.import_no_data), Toast.LENGTH_LONG);
                shouldCloseActivity = true;
            } else if (
                    (data.getScheme().equals("cospend") || data.getScheme().equals("cospend+http"))
                    && data.getPathSegments().size() >= 1
            ) {
                if (data.getPath().endsWith("/")) {
                    defaultProjectPassword = "";
                    defaultProjectId = data.getLastPathSegment();
                } else {
                    defaultProjectPassword = data.getLastPathSegment();
                    defaultProjectId = data.getPathSegments().get(data.getPathSegments().size() - 2);
                }
                String protocol = "https";
                if (data.getScheme().equals("cospend+http")) {
                    protocol = "http";
                }
                defaultNcUrl = protocol + "://" + data.getHost();
                if (data.getPort() != -1) {
                    defaultNcUrl += ":" + data.getPort();
                }
                defaultNcUrl += data.getPath().replaceAll("/"+defaultProjectId+"/" + defaultProjectPassword + "$", "");
                defaultProjectType = ProjectType.COSPEND;
            } else if (
                    (data.getScheme().equals("ilovemoney") || data.getScheme().equals("ilovemoney+http"))
                    && data.getPathSegments().size() >= 1
            ) {
                // invitation link
                if (data.getPathSegments().size() >= 3
                    && "join".equals(data.getPathSegments().get(data.getPathSegments().size() - 2))) {
                    String protocol = "https";
                    if (data.getScheme().equals("ilovemoney+http")) {
                        protocol = "http";
                    }
                    defaultIhmUrl = protocol + "://" + data.getHost();
                    if (data.getPort() != -1) {
                        defaultIhmUrl += ":" + data.getPort();
                    }
                    defaultIhmUrl += data.getPath();
                    Log.e("PORT", "port is " + data.getPort());
                } else {
                    if (data.getPath().endsWith("/")) {
                        defaultProjectPassword = "";
                        defaultProjectId = data.getLastPathSegment();
                    } else {
                        defaultProjectPassword = data.getLastPathSegment();
                        defaultProjectId = data.getPathSegments().get(data.getPathSegments().size() - 2);
                    }
                    String protocol = "https";
                    if (data.getScheme().equals("ilovemoney+http")) {
                        protocol = "http";
                    }
                    defaultIhmUrl = protocol + "://" + data.getHost();
                    if (data.getPort() != -1) {
                        defaultIhmUrl += ":" + data.getPort();
                    }
                    defaultIhmUrl += data.getPath().replaceAll("/" + defaultProjectId + "/" + defaultProjectPassword + "$", "");
                }
                defaultProjectType = ProjectType.ILOVEMONEY;
            } else {
                showToast(getString(R.string.import_bad_url), Toast.LENGTH_LONG);
                shouldCloseActivity = true;
            }
        }
        fragment = NewProjectFragment.newInstance(
                defaultIhmUrl, defaultNcUrl,
                defaultProjectId, defaultProjectPassword, defaultProjectType,
                (Intent.ACTION_VIEW.equals(getIntent().getAction()))
        );

        getSupportFragmentManager().beginTransaction().replace(android.R.id.content, fragment).commit();

        if (shouldCloseActivity) {
            close(0, false);
        }
    }

    @Override
    public void onBackPressed() {
        close(0, false);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                close(0, false);
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    /**
     * Send result and closes the Activity
     */
    public void close(long pid, boolean justAdded) {
        fragment.onCloseProject();
        final Intent data = new Intent();
        if (justAdded) {
            data.putExtra(BillsListViewActivity.ADDED_PROJECT, pid);
        } else {
            data.putExtra(BillsListViewActivity.CREATED_PROJECT, pid);
        }
        setResult(RESULT_OK, data);
        finish();
    }

    protected void showToast(CharSequence text, int duration) {
        Context context = getApplicationContext();
        Toast toast = Toast.makeText(context, text, duration);
        toast.show();
    }
}
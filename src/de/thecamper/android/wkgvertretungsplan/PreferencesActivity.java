package de.thecamper.android.wkgvertretungsplan;

import com.actionbarsherlock.app.ActionBar;
import com.actionbarsherlock.app.SherlockPreferenceActivity;
import com.actionbarsherlock.view.MenuItem;

import de.thecamper.android.androidtools.UpdateChecker;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.preference.Preference;
import android.preference.Preference.OnPreferenceClickListener;

public class PreferencesActivity extends SherlockPreferenceActivity {
    
    Context context;
    
    @Override
    protected void onCreate(Bundle savedInstanceState) {
            super.onCreate(savedInstanceState);
            addPreferencesFromResource(R.xml.preferences);
            
            // set up ActionBar
            ActionBar actionBar = getSupportActionBar();
            actionBar.setDisplayHomeAsUpEnabled(true);
            
            context = this;
            
            // Add Listener for Preferences
            Preference checkForUpdate = findPreference("checkForUpdate");
            checkForUpdate.setOnPreferenceClickListener(new OnPreferenceClickListener() {
                
                public boolean onPreferenceClick(Preference preference) {
                    String versionURL = getString(R.string.versionURL);
                    String appURL = getString(R.string.appURL);
                    new UpdateChecker(context, versionURL, appURL, true).execute();
                    return true;
                }
            });
            
            Preference showChangelog = findPreference("showChangelog");
            showChangelog.setOnPreferenceClickListener(new OnPreferenceClickListener() {
                
                public boolean onPreferenceClick(Preference preference) {
                    Intent intent = new Intent(context, MyChangelog.class);
                    startActivity(intent);
                    return true;
                }
            });
    }
    
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                // app icon in action bar clicked; go home
                Intent intent = new Intent(this, WKGVertretungsplanActivity.class);
                intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                startActivity(intent);
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }
}

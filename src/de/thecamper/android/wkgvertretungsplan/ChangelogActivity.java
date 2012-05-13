package de.thecamper.android.wkgvertretungsplan;

import android.content.Intent;
import android.os.Bundle;
import android.widget.TextView;

import com.actionbarsherlock.app.ActionBar;
import com.actionbarsherlock.app.SherlockActivity;
import com.actionbarsherlock.view.MenuItem;


/**
 * Shows the changelog located in /res/values/changelog.xml
 * as a String-Array
 * @author Daniel
 *
 */
public class ChangelogActivity extends SherlockActivity {
    
    
    /** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.changelog);
        
        // set up ActionBar
        ActionBar actionBar = getSupportActionBar();
        actionBar.setDisplayHomeAsUpEnabled(true);

        // fill TextView with changelog
        TextView textView = (TextView) findViewById(R.id.changelogText);
        
        String[] changelogArray = getResources().getStringArray(R.array.changelog);
        StringBuilder sb = new StringBuilder();
        String delim = "";
        for (String s : changelogArray) {
            sb.append(delim).append(s);
            delim = "\n\n";
        }
        
        textView.setText(sb.toString());
    }
    
    
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                // app icon in action bar clicked; go home
                Intent intent = new Intent(this, PreferencesActivity.class);
                intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                startActivity(intent);
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }
    
}

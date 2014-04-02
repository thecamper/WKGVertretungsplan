/*
 * Copyright 2014 Daniel Grothe
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package de.thecamper.android.wkgvertretungsplan;

import android.content.Intent;
import android.os.Bundle;
import android.preference.PreferenceManager;

import com.actionbarsherlock.app.ActionBar;
import com.actionbarsherlock.view.MenuItem;
import com.google.android.gms.analytics.HitBuilders;
import com.google.android.gms.analytics.Tracker;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GooglePlayServicesUtil;

import de.thecamper.android.androidtools.ChangelogActivity;

/**
 * Shows the changelog located in /res/values/changelog.xml as a String-Array
 * 
 * @author Daniel
 * 
 */
public class MyChangelog extends ChangelogActivity {

	/** Called when the activity is first created. */
	@Override
	public void onCreate(Bundle savedInstanceState) {
		setTheme(PreferencesActivity.getTheme(this));

		super.onCreate(savedInstanceState);

		// set up ActionBar
		ActionBar actionBar = getSupportActionBar();
		actionBar.setDisplayHomeAsUpEnabled(true);

		setChangelogText(R.array.changelog);
	}

	@Override
	public void onResume() {
		super.onResume();
		if (PreferenceManager.getDefaultSharedPreferences(this).getBoolean(
				"enableAnalytics", false)
				&& GooglePlayServicesUtil.isGooglePlayServicesAvailable(this) == ConnectionResult.SUCCESS) {
			Tracker tracker = ((MyApplication) getApplication()).getTracker();
			tracker.setScreenName(getClass().getSimpleName());
			tracker.send(new HitBuilders.AppViewBuilder().build());
		}
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

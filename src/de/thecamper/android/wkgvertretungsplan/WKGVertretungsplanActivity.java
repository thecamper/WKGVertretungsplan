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

import java.util.ArrayList;
import java.util.List;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.DialogInterface.OnClickListener;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.view.ViewPager;
import android.util.SparseArray;
import android.view.ViewGroup;
import android.widget.ProgressBar;

import com.actionbarsherlock.app.ActionBar;
import com.actionbarsherlock.app.ActionBar.Tab;
import com.actionbarsherlock.app.SherlockFragmentActivity;
import com.actionbarsherlock.view.Menu;
import com.actionbarsherlock.view.MenuInflater;
import com.actionbarsherlock.view.MenuItem;
import com.google.analytics.tracking.android.EasyTracker;

import de.thecamper.android.androidtools.UpdateChecker;
import de.thecamper.android.wkgvertretungsplan.fragments.KlausurplanFragment;
import de.thecamper.android.wkgvertretungsplan.fragments.ScheduleFragment;
import de.thecamper.android.wkgvertretungsplan.fragments.TaskFragment;
import de.thecamper.android.wkgvertretungsplan.fragments.VertretungsplanFragment;

/**
 * Main Activity of the App extends SherlockActivity in order to get the
 * ActionBar on devices before Honeycomb
 * 
 * @author Daniel
 * 
 */
public class WKGVertretungsplanActivity extends SherlockFragmentActivity implements
		TaskFragment.DownloadTaskCallbacks, TaskFragment.SaveTaskCallbacks {

	private SharedPreferences preferences;
	private MenuItem menuItemRefresh;

	private MyPagerAdapter pagerAdapter;
	public List<String> fragments = new ArrayList<String>();
	private TaskFragment taskFragment;
	private ViewPager viewPager;
	private ProgressBar refreshProgressBar;
	private boolean refreshProgressBarVisible = false;

	@Override
	public void onSaveInstanceState(Bundle outState) {
		super.onSaveInstanceState(outState);
		outState.putBoolean("menuProgressBarVisible",
				(menuItemRefresh.getActionView() != null));
	}

	/** Called when the activity is first created. */
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setTheme(PreferencesActivity.getTheme(this));
		setContentView(R.layout.main);

		if (savedInstanceState != null) {
			refreshProgressBarVisible = savedInstanceState
					.getBoolean("menuProgressBarVisible");
		}

		FragmentManager fm = getSupportFragmentManager();
		taskFragment = (TaskFragment) fm.findFragmentByTag("task");

		if (taskFragment == null) {
			taskFragment = new TaskFragment();
			fm.beginTransaction().add(taskFragment, "task").commit();
		}

		final ActionBar actionBar = getSupportActionBar();
		actionBar.setNavigationMode(ActionBar.NAVIGATION_MODE_TABS);
		pagerAdapter = new MyPagerAdapter(fm);
		viewPager = (ViewPager) findViewById(R.id.pager);
		viewPager.setAdapter(pagerAdapter);
		viewPager.setOnPageChangeListener(new MyViewPagerListener());
		ActionBar.TabListener tl = new MyTabListener();

		actionBar.addTab(actionBar.newTab().setText(R.string.tabVertretungsplanName)
				.setTabListener(tl));
		actionBar.addTab(actionBar.newTab().setText(R.string.tabKlausurplanName)
				.setTabListener(tl));

		preferences = PreferenceManager.getDefaultSharedPreferences(this);
		// ask for analytics if it is the first time
		if (preferences.getBoolean("analyticsFirstTime", true)) {
			askForAnalytics();
		}

		// Check for updates if preference is set
		if (preferences.getBoolean("checkForUpdateOnCreate", false)) {
			checkForUpdate();
		}
	}

	@Override
	public void onDestroy() {
		super.onDestroy();
	}

	@Override
	public void onStart() {
		super.onStart();
		if (preferences.getBoolean("enableAnalytics", false)) {
			EasyTracker.getInstance(this).activityStart(this);
		}

	}

	@Override
	public void onStop() {
		super.onStop();
		if (preferences.getBoolean("enableAnalytics", false)) {
			EasyTracker.getInstance(this).activityStop(this);
		}
	}

	public boolean onCreateOptionsMenu(Menu menu) {
		MenuInflater inflater = getSupportMenuInflater();
		inflater.inflate(R.menu.menu, menu);
		menuItemRefresh = menu.findItem(R.id.refresh);
		refreshProgressBar = new ProgressBar(this);
		if (refreshProgressBarVisible) {
			menuItemRefresh.setActionView(refreshProgressBar);
		}
		return true;
	}

	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
		case R.id.preferences:
			// Launch Preference activity
			Intent i = new Intent(WKGVertretungsplanActivity.this,
					PreferencesActivity.class);
			startActivity(i);
			break;
		case R.id.refresh:
			// Update Image
			this.updateImage();
		}
		return true;
	}

	/**
	 * update the image of the schedule from the internet the update is done in
	 * an asynchronous background task
	 */
	private void updateImage() {
		int id = (pagerAdapter.getFragment(viewPager.getCurrentItem()) instanceof VertretungsplanFragment) ? TaskFragment.VERTRETUNGSPLAN
				: TaskFragment.KLAUSURPLAN;
		taskFragment.new DownloadFileTask(this, id)
				.execute(preferences.getString("login", ""),
						preferences.getString("password", ""));
	}

	/**
	 * checks for an update of the app the check is done in an asynchronous
	 * background task
	 */
	private void checkForUpdate() {
		String versionURL = getString(R.string.versionURL);
		String appURL = getString(R.string.appURL);
		// no notification toast in the case of no available update
		new UpdateChecker(this, versionURL, appURL, false, true).execute();
	}

	private void askForAnalytics() {

		AlertDialog.Builder builder = new AlertDialog.Builder(this);
		builder.setMessage(R.string.askForAnalyticsMessage);
		builder.setCancelable(false);
		builder.setTitle(R.string.askForAnalyticsTitle);
		builder.setPositiveButton(R.string.askForAnalyticsYes, new OnClickListener() {

			public void onClick(DialogInterface dialog, int which) {
				preferences.edit().putBoolean("enableAnalytics", true)
						.putBoolean("analyticsFirstTime", false).commit();
			}
		});
		builder.setNegativeButton(R.string.askForAnalyticsNo, new OnClickListener() {

			public void onClick(DialogInterface dialog, int which) {
				preferences.edit().putBoolean("enableAnalytics", false)
						.putBoolean("analyticsFirstTime", false).commit();
			}
		});
		builder.show();
	}

	// /////////////////////////////////////////////////////////////////////
	// /////////////////////////////////////////////////////////////////////

	public class MyPagerAdapter extends FragmentPagerAdapter {
		SparseArray<Fragment> registeredFragments = new SparseArray<Fragment>();

		public MyPagerAdapter(FragmentManager fm) {
			super(fm);
			fragments.add(VertretungsplanFragment.class.getName());
			fragments.add(KlausurplanFragment.class.getName());
		}

		@Override
		public Fragment getItem(int position) {
			return Fragment.instantiate(getBaseContext(), fragments.get(position));
		}

		@Override
		public Object instantiateItem(ViewGroup container, int position) {
			Fragment fragment = (Fragment) super.instantiateItem(container, position);
			registeredFragments.put(position, fragment);
			return fragment;
		}

		@Override
		public void destroyItem(ViewGroup container, int position, Object object) {
			registeredFragments.remove(position);
			super.destroyItem(container, position, object);
		}

		@Override
		public int getCount() {
			return 2;
		}

		public Fragment getFragment(int position) {
			return registeredFragments.get(position);
		}

	}

	private class MyTabListener implements ActionBar.TabListener {

		public void onTabSelected(Tab tab, FragmentTransaction ft) {
			viewPager.setCurrentItem(tab.getPosition());
		}

		public void onTabUnselected(Tab tab, FragmentTransaction ft) {

		}

		public void onTabReselected(Tab tab, FragmentTransaction ft) {

		}

	}

	private class MyViewPagerListener implements ViewPager.OnPageChangeListener {

		public void onPageScrollStateChanged(int arg0) {

		}

		public void onPageScrolled(int arg0, float arg1, int arg2) {

		}

		public void onPageSelected(int position) {
			getSupportActionBar().setSelectedNavigationItem(position);
		}

	}

	// ////////////////////////////////////////////////////////////////
	// ////////////////////////////////////////////////////////////////

	public void onPostExecuteSaveTask(Boolean retVal) {
		if (menuItemRefresh != null) {
			menuItemRefresh.setActionView(null);
			menuItemRefresh.setEnabled(true);
		}
	}

	public void onCancelledSaveTask() {
		if (menuItemRefresh != null) {
			menuItemRefresh.setActionView(null);
			menuItemRefresh.setEnabled(true);
		}
	}

	public void onPreExecuteDownloadTask(int id) {
		if (menuItemRefresh != null) {
			menuItemRefresh.setEnabled(false);
			menuItemRefresh.setActionView(refreshProgressBar);
		}
		((ScheduleFragment) pagerAdapter.getFragment(id)).onPreExecuteDownloadTask(id);
	}

	public void onPostExecuteDownloadTask(int id, String html) {
		if (html != null && preferences.getBoolean("saveBmp", true)) {
			taskFragment.new SaveBitmapTask(id).execute(html);
		}
		if (menuItemRefresh != null) {
			menuItemRefresh.setActionView(null);
			menuItemRefresh.setEnabled(true);
		}
		try {
			((ScheduleFragment) pagerAdapter.getFragment(id)).onPostExecuteDownloadTask(
					id, html);
		} catch (NullPointerException e) {
			e.printStackTrace();
		}

	}

	public void onCancelledDownloadTask(int id) {
		if (menuItemRefresh != null) {
			menuItemRefresh.setActionView(null);
			menuItemRefresh.setEnabled(true);
		}
		((ScheduleFragment) pagerAdapter.getFragment(id)).onCancelledDownloadTask(id);
	}
}

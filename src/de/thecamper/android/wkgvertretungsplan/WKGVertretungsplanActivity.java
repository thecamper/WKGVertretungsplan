package de.thecamper.android.wkgvertretungsplan;

import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;

import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.message.BasicNameValuePair;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.DialogInterface.OnClickListener;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.Bitmap.CompressFormat;
import android.graphics.Point;
import android.os.AsyncTask;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.view.ViewPager;
import android.view.Display;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.actionbarsherlock.app.ActionBar;
import com.actionbarsherlock.app.ActionBar.Tab;
import com.actionbarsherlock.app.SherlockFragmentActivity;
import com.actionbarsherlock.view.Menu;
import com.actionbarsherlock.view.MenuInflater;
import com.actionbarsherlock.view.MenuItem;
import com.google.analytics.tracking.android.EasyTracker;

import de.thecamper.android.androidtools.ScalingHelper;
import de.thecamper.android.androidtools.ScalingHelper.ScalingLogic;
import de.thecamper.android.androidtools.UpdateChecker;
import de.thecamper.android.wkgvertretungsplan.fragments.KlausurplanFragment;
import de.thecamper.android.wkgvertretungsplan.fragments.ScheduleFragment;
import de.thecamper.android.wkgvertretungsplan.fragments.VertretungsplanFragment;
import de.thecamper.android.wkgvertretungsplan.helper.MyBitmap;
import de.thecamper.android.wkgvertretungsplan.touch.TouchImageViewPager;

/**
 * Main Activity of the App extends SherlockActivity in order to get the
 * ActionBar on devices before Honeycomb
 * 
 * @author Daniel
 * 
 */
public class WKGVertretungsplanActivity extends SherlockFragmentActivity {

	public static final int VERTRETUNGSPLAN = 0, KLAUSURPLAN = 1;
	public int MAX_WIDTH, MAX_HEIGHT = 4096;
	private final float MAX_SIZE_MULTIPLIER = 2.0f;

	SharedPreferences preferences;
	MenuItem menuItemRefresh;

	MyPagerAdapter pagerAdapter;
	public List<String> fragments = new ArrayList<String>();
	TouchImageViewPager viewPager;

	/** Called when the activity is first created. */
	@Override
	public void onCreate(Bundle savedInstanceState) {
		Display display = getWindowManager().getDefaultDisplay();
		if (android.os.Build.VERSION.SDK_INT >= 13) {
			Point size = new Point();
			display.getSize(size);
			MAX_WIDTH = Math.round(size.x * MAX_SIZE_MULTIPLIER);

		} else {
			MAX_WIDTH = Math.round(display.getWidth() * MAX_SIZE_MULTIPLIER);
		}

		super.onCreate(savedInstanceState);
		setTheme(PreferencesActivity.getTheme(this));
		setContentView(R.layout.main);

		final ActionBar actionBar = getSupportActionBar();
		actionBar.setNavigationMode(ActionBar.NAVIGATION_MODE_TABS);
		pagerAdapter = new MyPagerAdapter(getSupportFragmentManager());
		viewPager = (TouchImageViewPager) findViewById(R.id.pager);
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
		if (menuItemRefresh != null) {
			menuItemRefresh.setEnabled(false);
			menuItemRefresh.setActionView(new ProgressBar(this));
		}
		// TODO: prevent switching orientation while loading

		ScheduleFragment fragment = (ScheduleFragment) getCurrentFragment(viewPager,
				pagerAdapter);
		int id;
		if (fragment instanceof VertretungsplanFragment) {
			id = VERTRETUNGSPLAN;
		} else {
			id = KLAUSURPLAN;
		}

		try {
			fragment.freeImage();
		} catch (NullPointerException ex) {
		}

		new DownloadFileTask(this, id, fragment)
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

	/**
	 * Downloads the schedule image from the internet in an asynchronous task.
	 * As params there are needed the login and the password in this order
	 * 
	 * @author Daniel
	 * 
	 */
	public class DownloadFileTask extends AsyncTask<String, Void, MyBitmap> {

		private final int id;
		private ScheduleFragment fragment;
		private Context context; // activity context for the display of
									// notification toasts

		public DownloadFileTask(Context context, int id, ScheduleFragment fragment) {
			this.context = context;
			this.id = id;
			this.fragment = fragment;
		}

		protected MyBitmap doInBackground(String... params) {
			// abort, if the params are set false
			if (params.length != 2)
				return null;

			// set params
			String login = params[0];
			String password = params[1];

			// build the Post-Request
			HttpClient httpClient = new DefaultHttpClient();
			HttpPost httpPost = new HttpPost(
					getString((id == VERTRETUNGSPLAN) ? R.string.vertretungsplanURL
							: R.string.klausurplanURL));

			// Add data
			try {
				List<NameValuePair> nameValuePairs = new ArrayList<NameValuePair>(2);
				nameValuePairs.add(new BasicNameValuePair("login", login));
				nameValuePairs.add(new BasicNameValuePair("password", password));
				httpPost.setEntity(new UrlEncodedFormEntity(nameValuePairs));

				// Execute HTTP Post Request
				HttpResponse response = httpClient.execute(httpPost);

				// check status code
				if (response.getStatusLine().getStatusCode() == 200) {
					// get bitmap from InputStream
					InputStream is = response.getEntity().getContent();
					// get bitmap and resize if necessary (maximum width/height
					// of 2048 because of OpenGL rendering on ICS devices)
					Bitmap bmp = generateBitmap(is, MAX_WIDTH, MAX_HEIGHT);
					is.close();

					return new MyBitmap(id, bmp);
				}
			} catch (ClientProtocolException e) {
				e.printStackTrace();
			} catch (NullPointerException e1) {
				return null;
			} catch (IOException e2) {
				e2.printStackTrace();
			}
			return null;
		}

		protected void onPreExecute() {
			// update layout before downloading
			fragment.setUpdating(true);
		}

		protected void onPostExecute(MyBitmap myBmp) {
			// set layout and new image after downloading
			fragment.setUpdating(false);

			if (myBmp != null) {
				fragment.setImage(myBmp.bmp);

				// save bitmap if preference is set
				if (preferences.getBoolean("saveBmp", true)) {
					new SaveBitmapTask().execute(myBmp);
				} else if (menuItemRefresh != null) {
					menuItemRefresh.setActionView(null);
					menuItemRefresh.setEnabled(true);
				}
			} else {
				fragment.setError();
				Toast.makeText(context, getString(R.string.errorAccessSchedule),
						Toast.LENGTH_SHORT).show();
				if (menuItemRefresh != null) {
					menuItemRefresh.setActionView(null);
					menuItemRefresh.setEnabled(true);
				}
			}

		}

		/**
		 * get bitmap from InputStream and resize if necessary
		 * 
		 * @param is
		 *            InputStream of the bitmap
		 * @param maxHeight
		 *            maximum height of the bitmap
		 * @param maxWidth
		 *            maximum width of the bitmap
		 * @return the bitmap
		 * @throws IOException
		 * @throws NullPointerException
		 */
		private Bitmap generateBitmap(InputStream is, double maxHeight, double maxWidth)
				throws IOException, NullPointerException {

			// return ScalingHelper.decodeFile(is);

			Bitmap unscaledBitmap = ScalingHelper.decodeFile(is, MAX_WIDTH, MAX_HEIGHT,
					ScalingLogic.FIT);
			if (unscaledBitmap.getHeight() > MAX_HEIGHT
					|| unscaledBitmap.getWidth() > MAX_WIDTH) {
				Bitmap scaledBitmap = ScalingHelper.createScaledBitmap(unscaledBitmap,
						MAX_WIDTH, MAX_HEIGHT, ScalingLogic.FIT);
				unscaledBitmap.recycle();
				return scaledBitmap;
			} else
				return unscaledBitmap;

		}
	}

	/**
	 * Saves the Bitmap to the internal storage in an asynchronous task
	 * 
	 * @author Daniel
	 * 
	 */
	public class SaveBitmapTask extends AsyncTask<MyBitmap, Void, Boolean> {

		@Override
		protected Boolean doInBackground(MyBitmap... params) {

			FileOutputStream fos = null;
			boolean retVal = false;

			try {
				for (MyBitmap param : params) {
					fos = openFileOutput("data" + param.id + ".png", Context.MODE_PRIVATE);
					retVal = param.bmp.compress(CompressFormat.PNG, 90, fos);
					if (!retVal) {
						break;
					}
				}
			} catch (IOException e) {
				retVal = false;
			} finally {
				if (fos != null) {
					try {
						fos.close();
					} catch (IOException e) {
						e.printStackTrace();
					}
				}
			}
			return retVal;
		}

		protected void onPostExecute(Boolean retVal) {
			if (menuItemRefresh != null) {
				menuItemRefresh.setActionView(null);
				menuItemRefresh.setEnabled(true);
			}
		}
	}

	// /////////////////////////////////////////////////////////////////////
	// /////////////////////////////////////////////////////////////////////

	public class MyPagerAdapter extends FragmentPagerAdapter {

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
		public int getCount() {
			return 2;
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

	public static Fragment getCurrentFragment(ViewPager pager,
			FragmentPagerAdapter adapter) {
		try {
			Method m = adapter.getClass().getSuperclass()
					.getDeclaredMethod("makeFragmentName", int.class, long.class);
			Field f = adapter.getClass().getSuperclass()
					.getDeclaredField("mFragmentManager");
			f.setAccessible(true);
			FragmentManager fm = (FragmentManager) f.get(adapter);
			m.setAccessible(true);
			String tag = null;
			tag = (String) m.invoke(null, pager.getId(), (long) pager.getCurrentItem());
			return fm.findFragmentByTag(tag);
		} catch (NoSuchMethodException e) {
			e.printStackTrace();
		} catch (IllegalArgumentException e) {
			e.printStackTrace();
		} catch (IllegalAccessException e) {
			e.printStackTrace();
		} catch (InvocationTargetException e) {
			e.printStackTrace();
		} catch (NoSuchFieldException e) {
			e.printStackTrace();
		}
		return null;
	}
}

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
package de.thecamper.android.wkgvertretungsplan.fragments;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;

import org.apache.commons.io.IOUtils;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.View;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.widget.ProgressBar;
import android.widget.Toast;
import de.thecamper.android.wkgvertretungsplan.R;

public abstract class ScheduleFragment extends Fragment implements
		TaskFragment.DownloadTaskCallbacks {

	protected ProgressBar progressBar;
	protected WebView webView;

	public ScheduleFragment() {

	}

	@Override
	public void onSaveInstanceState(Bundle outState) {
		super.onSaveInstanceState(outState);
		webView.saveState(outState);
		outState.putInt("webViewVisibility", webView.getVisibility());
		outState.putInt("progressBarVisibility", progressBar.getVisibility());
	}

	public void setUpdating(boolean isUpdating) {
		if (isUpdating) {
			progressBar.setVisibility(View.VISIBLE);
			webView.setVisibility(View.INVISIBLE);
		} else {
			progressBar.setVisibility(View.INVISIBLE);
			webView.setVisibility(View.VISIBLE);
		}
	}

	public void setError() {
		progressBar.setVisibility(View.INVISIBLE);
		webView.setVisibility(View.INVISIBLE);
	}

	protected void setWebViewSettings(WebView view) {
		view.setInitialScale(1);
		WebSettings settings = view.getSettings();
		settings.setBuiltInZoomControls(true);
		settings.setUseWideViewPort(true);
		if (android.os.Build.VERSION.SDK_INT >= 11) {
			settings.setDisplayZoomControls(false);
		}
	}

	/**
	 * loads a bitmap from data.png from the internal storage
	 * 
	 * @return the loaded bitmap
	 * @throws FileNotFoundException
	 * @throws IOException
	 */
	public void loadHtml(int id) throws FileNotFoundException, IOException {
		FileInputStream fis = getActivity().openFileInput("data" + id + ".html");
		setHtml(IOUtils.toString(fis));
	}

	public void setHtml(String html) {
		webView.loadData(html, "text/html", "UTF-8");
	}

	public void onPreExecuteDownloadTask(int id) {
		setUpdating(true);
	}

	public void onPostExecuteDownloadTask(int id, String html) {
		setUpdating(false);

		if (html != null) {
			setHtml(html);
		} else {
			setError();
			Toast.makeText(getActivity(), getString(R.string.errorAccessSchedule),
					Toast.LENGTH_SHORT).show();
		}
	}

	public void onCancelledDownloadTask(int id) {
		setError();
		Toast.makeText(getActivity(), getString(R.string.errorAccessSchedule),
				Toast.LENGTH_SHORT).show();
	}
}

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

import java.io.FileNotFoundException;
import java.io.IOException;

import android.os.Bundle;
import android.preference.PreferenceManager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.WebView;
import android.widget.ProgressBar;
import de.thecamper.android.wkgvertretungsplan.R;

public class KlausurplanFragment extends ScheduleFragment {

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		View view = inflater.inflate(R.layout.klausurplan_view, container, false);

		return view;
	}

	@Override
	public void onActivityCreated(Bundle savedInstanceState) {
		super.onActivityCreated(savedInstanceState);

		progressBar = (ProgressBar) getView().findViewById(R.id.progressBarKlausurplan);
		progressBar.setVisibility(View.INVISIBLE);

		webView = (WebView) getView().findViewById(R.id.webViewKlausurplan);
		setWebViewSettings(webView);

		if (savedInstanceState != null) {
			progressBar.setVisibility(savedInstanceState.getInt("progressBarVisibility"));
			webView.restoreState(savedInstanceState);
			webView.setVisibility(savedInstanceState.getInt("webViewVisibility"));
		} else if (PreferenceManager.getDefaultSharedPreferences(getActivity())
				.getBoolean("saveBmp", true)) {
			try {
				loadHtml(TaskFragment.KLAUSURPLAN);
			} catch (FileNotFoundException e) {
				webView.setVisibility(View.INVISIBLE);
			} catch (IOException e) {
				webView.setVisibility(View.INVISIBLE);
			}
		}
	}
}

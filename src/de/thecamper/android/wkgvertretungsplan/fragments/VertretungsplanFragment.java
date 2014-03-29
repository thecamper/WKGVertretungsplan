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

public class VertretungsplanFragment extends ScheduleFragment {

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		View view = inflater.inflate(R.layout.vertretungsplan_view, container, false);

		return view;
	}

	@Override
	public void onActivityCreated(Bundle savedInstanceState) {
		super.onActivityCreated(savedInstanceState);

		progressBar = (ProgressBar) getView().findViewById(
				R.id.progressBarVertretungsplan);
		progressBar.setVisibility(View.INVISIBLE);

		webView = (WebView) getView().findViewById(R.id.webViewVertretungsplan);
		setWebViewSettings(webView);
		if (savedInstanceState != null) {
			progressBar.setVisibility(savedInstanceState.getInt("progressBarVisibility"));
			webView.restoreState(savedInstanceState);
			webView.setVisibility(savedInstanceState.getInt("webViewVisibility"));
		} else if (PreferenceManager.getDefaultSharedPreferences(getActivity())
				.getBoolean("saveBmp", true)) {
			try {
				loadHtml(TaskFragment.VERTRETUNGSPLAN);
			} catch (FileNotFoundException e) {
				webView.setVisibility(View.INVISIBLE);
			} catch (IOException e) {
				webView.setVisibility(View.INVISIBLE);
			}
		}
	}
}

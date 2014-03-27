package de.thecamper.android.wkgvertretungsplan.fragments;

import java.io.FileNotFoundException;
import java.io.IOException;

import android.os.Bundle;
import android.preference.PreferenceManager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ProgressBar;
import de.thecamper.android.wkgvertretungsplan.R;
import de.thecamper.android.wkgvertretungsplan.WKGVertretungsplanActivity;
import de.thecamper.android.wkgvertretungsplan.touch.TouchImageView;

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

		imageView = (TouchImageView) getView().findViewById(R.id.imageViewKlausurplan);
		// load saved Bitmap if preference is set
		if (PreferenceManager.getDefaultSharedPreferences(getActivity()).getBoolean(
				"saveBmp", true)) {
			try {
				setImage(loadBmp(WKGVertretungsplanActivity.KLAUSURPLAN));
			} catch (FileNotFoundException e) {
				imageView.setVisibility(View.INVISIBLE);
			} catch (IOException e) {
				imageView.setVisibility(View.INVISIBLE);
			}
		}
	}
}

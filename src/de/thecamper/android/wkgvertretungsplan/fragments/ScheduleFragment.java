package de.thecamper.android.wkgvertretungsplan.fragments;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;

import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.support.v4.app.Fragment;
import android.view.View;
import android.widget.ProgressBar;
import de.thecamper.android.androidtools.ScalingHelper;
import de.thecamper.android.wkgvertretungsplan.touch.TouchImageView;

public abstract class ScheduleFragment extends Fragment {
	protected ProgressBar progressBar;
	protected TouchImageView imageView;
	private final float baseMinZoom = .8f;
	private final float baseMaxZoom = 2.0f;

	public ScheduleFragment() {
	}

	public void setUpdating(boolean isUpdating) {
		if (isUpdating) {
			progressBar.setVisibility(View.VISIBLE);
			imageView.setVisibility(View.INVISIBLE);
		} else {
			progressBar.setVisibility(View.INVISIBLE);
			imageView.setVisibility(View.VISIBLE);
		}
	}

	public void setError() {
		progressBar.setVisibility(View.INVISIBLE);
		imageView.setVisibility(View.INVISIBLE);
	}

	/**
	 * loads a bitmap from data.png from the internal storage
	 * 
	 * @return the loaded bitmap
	 * @throws FileNotFoundException
	 * @throws IOException
	 */
	protected Bitmap loadBmp(int id) throws FileNotFoundException, IOException {
		FileInputStream fis = getActivity().openFileInput("data" + id + ".png");
		return ScalingHelper.decodeFile(fis);
	}

	public void setImage(Bitmap bmp) {
		imageView.setImageBitmap(bmp);
		imageView.setMinZoom(baseMinZoom / bmp.getWidth() * bmp.getHeight());
		imageView.setMaxZoom(baseMaxZoom / bmp.getWidth() * bmp.getHeight());
		imageView.setZoom(imageView.getMinZoom(), 0.5f, 0);
	}

	public void freeImage() throws NullPointerException {
		((BitmapDrawable) imageView.getDrawable()).getBitmap().recycle();
	}
}

package de.thecamper.android.wkgvertretungsplan.fragments;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStreamWriter;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.io.IOUtils;
import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.message.BasicNameValuePair;

import android.app.Activity;
import android.content.Context;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.util.Base64;
import de.thecamper.android.wkgvertretungsplan.R;

public class TaskFragment extends Fragment {

	public static final int VERTRETUNGSPLAN = 0, KLAUSURPLAN = 1;

	private DownloadTaskCallbacks downloadTaskCallbacks;
	private SaveTaskCallbacks saveTaskCallbacks;

	@Override
	public void onAttach(Activity activity) {
		super.onAttach(activity);
		downloadTaskCallbacks = (DownloadTaskCallbacks) activity;
		saveTaskCallbacks = (SaveTaskCallbacks) activity;
	}

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setRetainInstance(true);
	}

	@Override
	public void onDetach() {
		super.onDetach();
		downloadTaskCallbacks = null;
		saveTaskCallbacks = null;
	}

	public static interface DownloadTaskCallbacks {
		void onPreExecuteDownloadTask(int id);

		void onPostExecuteDownloadTask(int id, String html);

		void onCancelledDownloadTask(int id);
	}

	public static interface SaveTaskCallbacks {
		void onPostExecuteSaveTask(Boolean retVal);

		void onCancelledSaveTask();
	}

	/**
	 * Downloads the schedule image from the internet in an asynchronous task.
	 * As params there are needed the login and the password in this order
	 * 
	 * @author Daniel
	 * 
	 */
	public class DownloadFileTask extends AsyncTask<String, Void, String> {

		private final int id;
		private Context context; // activity context for the display of
									// notification toasts

		public DownloadFileTask(Context context, int id) {
			this.id = id;
			this.context = context;
		}

		protected String doInBackground(String... params) {
			// abort, if the params are set false
			if (params.length != 2) {
				return null;
			}

			// set params
			String login = params[0];
			String password = params[1];

			// build the Post-Request
			HttpClient httpClient = new DefaultHttpClient();
			HttpPost httpPost = new HttpPost(
					context.getString((id == VERTRETUNGSPLAN) ? R.string.vertretungsplanURL
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
				int statusCode = response.getStatusLine().getStatusCode();
				String contentType = response.getEntity().getContentType().getValue()
						.split(";")[0].trim();
				if (statusCode == 200 && contentType.equals("image/gif")) {
					InputStream is = response.getEntity().getContent();
					byte[] image = IOUtils.toByteArray(is);
					is.close();

					return context.getString(R.string.htmlBodyTop)
							+ Base64.encodeToString(image, Base64.DEFAULT)
							+ context.getString(R.string.htmlBodyBot);
				}
			} catch (ClientProtocolException e) {
				e.printStackTrace();
			} catch (IOException e2) {
				e2.printStackTrace();
			} catch (NullPointerException e3) {
				// return null
			}
			return null;
		}

		@Override
		protected void onPreExecute() {
			if (downloadTaskCallbacks != null) {
				downloadTaskCallbacks.onPreExecuteDownloadTask(id);
			}
		}

		@Override
		protected void onPostExecute(String html) {
			if (downloadTaskCallbacks != null) {
				downloadTaskCallbacks.onPostExecuteDownloadTask(id, html);
			}
		}

		@Override
		protected void onCancelled() {
			if (downloadTaskCallbacks != null) {
				downloadTaskCallbacks.onCancelledDownloadTask(id);
			}
		}
	}

	/**
	 * Saves the Bitmap to the internal storage in an asynchronous task
	 * 
	 * @author Daniel
	 * 
	 */
	public class SaveBitmapTask extends AsyncTask<String, Void, Boolean> {

		private final int id;

		public SaveBitmapTask(int id) {
			this.id = id;
		}

		@Override
		protected Boolean doInBackground(String... params) {

			if (params.length != 1) {
				return false;
			}

			OutputStreamWriter writer = null;
			boolean retVal = true;

			try {
				writer = new OutputStreamWriter(getActivity().openFileOutput(
						"data" + id + ".html", Context.MODE_PRIVATE));
				writer.write(params[0]);
			} catch (IOException e) {
				retVal = false;
			} finally {
				if (writer != null) {
					try {
						writer.close();
					} catch (IOException e) {
						e.printStackTrace();
					}
				}
			}
			return retVal;
		}

		@Override
		protected void onPostExecute(Boolean retVal) {
			if (saveTaskCallbacks != null) {
				saveTaskCallbacks.onPostExecuteSaveTask(retVal);
			}
		}

		@Override
		protected void onCancelled() {
			if (saveTaskCallbacks != null) {
				saveTaskCallbacks.onCancelledSaveTask();
			}
		}
	}

}

package de.thecamper.android.wkgvertretungsplan;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
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

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.Bitmap.CompressFormat;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.actionbarsherlock.app.SherlockActivity;
import com.actionbarsherlock.view.Menu;
import com.actionbarsherlock.view.MenuInflater;
import com.actionbarsherlock.view.MenuItem;

import de.thecamper.android.androidtools.TouchImageView;

/**
 * Main Activity of the App
 * extends SherlockActivity in order to get the ActionBar on devices before Honeycomb
 * @author Daniel
 *
 */
public class WKGVertretungsplanActivity extends SherlockActivity {
    
    SharedPreferences preferences;
    ProgressBar progressBar;
    TouchImageView imageView;
    
    /** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main);

        preferences = PreferenceManager.getDefaultSharedPreferences(this);
        
        // Check for updates if preference is set
        if (preferences.getBoolean("checkForUpdateOnCreate", false))
            checkForUpdate();
        
        // layout settings
        progressBar = (ProgressBar) findViewById(R.id.progressBar1);
        progressBar.setVisibility(View.INVISIBLE);
        imageView = (TouchImageView) findViewById(R.id.imageView);
        imageView.setMaxZoom(4f);
        
        // load saved Bitmap if preference is set
        if (preferences.getBoolean("saveBmp", true)) {
            try {
                imageView.setImageBitmap(loadBmp());
            } catch (FileNotFoundException e) {
                imageView.setVisibility(View.INVISIBLE);
            }
        }
    }
    
    public void onResume() {
        super.onResume();
        // refresh Image if preference is set
        if (preferences.getBoolean("autoRefresh", false))
            updateImage();
    }

    
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getSupportMenuInflater();
        inflater.inflate(R.menu.menu, menu);
        return true;
    }

    
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
        case R.id.preferences:
            // Launch Preference activity
            Intent i = new Intent(WKGVertretungsplanActivity.this, PreferencesActivity.class);
            startActivity(i);
            break;
        case R.id.refresh:
            // Update Image
            this.updateImage();
        }
        return true;
    }
    
    /**
     * update the image of the schedule from the internet
     * the update is done in an asynchronous background task
     */
    private void updateImage() {
        new DownloadFileTask(this).execute(preferences.getString("login", ""), preferences.getString("password", ""));
    }

    /**
     * Saves a bitmap to data.png into the internal storage
     * @param bmp the bitmap to save
     * @throws IOException
     */
    public void saveBmp(Bitmap bmp) throws IOException {
        FileOutputStream fos = openFileOutput("data.png", Context.MODE_PRIVATE);
        bmp.compress(CompressFormat.PNG, 90, fos);
        fos.close();
    }
    
    /**
     * loads a bitmap from data.png from the internal storage
     * @return the loaded bitmap
     * @throws FileNotFoundException 
     */
    private Bitmap loadBmp() throws FileNotFoundException {
        FileInputStream fis = openFileInput("data.png");
        return BitmapFactory.decodeStream(fis);
    }
    
    
    /**
     * checks for an update of the app
     * the check is done in an asynchronous background task
     */
    private void checkForUpdate() {
        // no notification toast in the case of no available update
        new UpdateChecker(this, false).execute(getString(R.string.versionURL));
    }
    
    /**
     * Downloads the schedule image from the internet in an asynchronous task
     * As params there are needed the login and the password in this order
     * @author Daniel
     *
     */
    private class DownloadFileTask extends AsyncTask<String, Void, Bitmap> {
        
        private Context context;        // activity context for the display
                                        // of notification toasts
        
        
        public DownloadFileTask(Context context) {
            this.context = context;
        }
        
        
        protected Bitmap doInBackground(String... params) {
            // abort, if the params are set false
            if (params.length != 2)
                return null;
            
            // set params
            String login = params[0];
            String password = params[1];
            
            // build the Post-Request
            HttpClient httpClient = new DefaultHttpClient();
            HttpPost httpPost = new HttpPost(getString(R.string.scheduleURL));
            
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
                    // get bitmap and resize if necessary (maximum width/height of 2048 because of OpenGL rendering on ICS devices)
                    Bitmap bmp = generateBitmap(is, 2048, 2048);
                    is.close();
                    
                    return bmp;
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
        
        protected void onPreExecute () {
            // set layout before downloading
            progressBar.setVisibility(View.VISIBLE);
            imageView.setVisibility(View.INVISIBLE);
        }
        
        protected void onPostExecute (Bitmap bmp) {
            // set layout and new image after downloading
            progressBar.setVisibility(View.INVISIBLE);
            if (bmp != null) {                
                imageView.setImageBitmap(bmp);
                imageView.setVisibility(View.VISIBLE);
                
                // save bitmap if preference is set
                if (preferences.getBoolean("saveBmp", true)) {
                    try {
                        saveBmp(bmp);
                    } catch (IOException e) {
                        Toast.makeText(context, getString(R.string.errorSaveBMP), Toast.LENGTH_SHORT).show();
                    }
                }
            }
            else {
                Toast.makeText(context, getString(R.string.errorAccessSchedule), Toast.LENGTH_SHORT).show();
            }
        }
        
        /**
         * get bitmap from InputStream and resize if necessary
         * 
         * @param is InputStream of the bitmap
         * @param maxHeight maximum height of the bitmap
         * @param maxWidth maximum width of the bitmap
         * @return the bitmap
         * @throws IOException
         * @throws NullPointerException
         */
        private Bitmap generateBitmap(InputStream is, double maxHeight,
                double maxWidth) throws IOException, NullPointerException {
            
            // read InputStream into a byte-Array
            byte[] bmpArray = IOUtils.toByteArray(is);
            
            BitmapFactory.Options o = new BitmapFactory.Options();
            o.inJustDecodeBounds = true;
            BitmapFactory.decodeByteArray(bmpArray, 0, bmpArray.length, o);

            // The new size we want to scale to
            int REQUIRED_SIZE = 0;
            if (maxWidth > maxHeight) {
                REQUIRED_SIZE = (int) maxWidth;
            } else {
                REQUIRED_SIZE = (int) maxHeight;
            }

            // Find the correct scale value. It should be the power of 2.
            int width_tmp = o.outWidth;
            int height_tmp = o.outHeight;
            int sampleSize = 1;
            while (true) {
                if (width_tmp / 2 < REQUIRED_SIZE || height_tmp / 2 < REQUIRED_SIZE)
                    break;
                width_tmp /= 2;
                height_tmp /= 2;
                sampleSize++;
            }

            // Decode with inSampleSize
            BitmapFactory.Options o2 = new BitmapFactory.Options();
            o2.inSampleSize = sampleSize;

            Bitmap originalBmp = BitmapFactory.decodeByteArray(bmpArray, 0, bmpArray.length, o2);

            // Find the correct scale value. It should be the power of 2.
            int origHeight = originalBmp.getHeight();
            int origWidth = originalBmp.getWidth();

            double scale = 1.0;

            double tmpScaleHeight = maxHeight / (double) origHeight;
            double tmpScaleWidth = maxWidth / (double) origWidth;

            if (tmpScaleHeight < tmpScaleWidth) {
                scale = tmpScaleHeight;
            } else {
                scale = tmpScaleWidth;
            }

            int scaledW = (int) (scale * origWidth);
            int scaledH = (int) (scale * origHeight);

            Bitmap tmpBmp = Bitmap.createScaledBitmap(originalBmp, scaledW,
                    scaledH, true);
            originalBmp.recycle();

            return tmpBmp;
        }
    }
}
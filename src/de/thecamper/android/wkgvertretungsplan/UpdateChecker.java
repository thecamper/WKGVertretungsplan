/**
 * 
 */
package de.thecamper.android.wkgvertretungsplan;

import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;

import org.apache.commons.io.IOUtils;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager.NameNotFoundException;
import android.net.Uri;
import android.os.AsyncTask;
import android.widget.Toast;


/**
 * Checks if there is an update of the app from the internet 
 * in an asynchronous task
 * 
 * As param there is needed the URL of the version-file
 * @author Daniel
 *
 */
public class UpdateChecker extends AsyncTask<String, Void, Boolean> {
    
    private Context context;            // activity context for the display
                                        // of notification toasts
    private boolean showNoUpdateToast;  // boolean if a notification toast
                                        // should be shown
    
    
    public UpdateChecker(Context context, boolean showNoUpdateToast) {
        this.context = context;
        this.showNoUpdateToast = showNoUpdateToast;
    }
    
    
    protected Boolean doInBackground(String... params) {
        // abort, if the param is set false
        if (params.length != 1)
            return null;
        
        try {
            // get the version code from a Get-Request
            URL url = new URL(params[0]);
            URLConnection con = url.openConnection();
            InputStream is = con.getInputStream();
            String encoding = con.getContentEncoding();
            encoding = encoding == null ? "UTF-8" : encoding;
            String body = IOUtils.toString(is, encoding);
            
            // if it is a newer version out there return true
            if (Integer.parseInt(body) > context.getPackageManager().getPackageInfo(context.getPackageName(), 0).versionCode) {
                return true;
            }
            else return false;
            
        } catch (MalformedURLException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        } catch (NumberFormatException e) {
            e.printStackTrace();
        } catch (NameNotFoundException e) {
            e.printStackTrace();
        }
        
        
        return false;
    }
    
    protected void onPostExecute (Boolean b) {
        if (b)
            // show Alert to inform the user of the update
            showUpdateAlert();
        else if (showNoUpdateToast)
            // show a notification toast if the preference is set
            Toast.makeText(context, context.getString(R.string.updateNotAvailable), Toast.LENGTH_SHORT).show();
    }
    
    /**
     * show a AlertDialog to inform the user of the update and let him download
     * the new version of the app
     */
    public void showUpdateAlert() {
        AlertDialog.Builder builder = new AlertDialog.Builder(context);
        builder.setTitle(context.getString(R.string.updateTitle));
        builder.setMessage(context.getString(R.string.updateAvailable));
        builder.setCancelable(false);
        builder.setPositiveButton(context.getString(R.string.yes), new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
                Intent intent = new Intent(Intent.ACTION_VIEW ,Uri.parse(context.getString(R.string.appURL)));
                context.startActivity(intent);               
                ((Activity) context).finish();
            }
        });
        builder.setNegativeButton(context.getString(R.string.later), new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
                dialog.cancel();
            }
        });
        AlertDialog alert = builder.create();
        alert.show();
    }
}

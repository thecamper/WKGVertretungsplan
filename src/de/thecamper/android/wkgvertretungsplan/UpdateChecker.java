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
 * @author Daniel
 *
 */
public class UpdateChecker extends AsyncTask<String, Void, Boolean> {
    
    private Context context;
    private boolean showNoUpdateToast;
    
    public UpdateChecker(Context context, boolean showNoUpdateToast) {
        this.context = context;
        this.showNoUpdateToast = showNoUpdateToast;
    }
    
    protected Boolean doInBackground(String... params) {
        if (params.length != 1)
            return null;
        
        try {
            URL url = new URL(params[0]);
            URLConnection con = url.openConnection();
            InputStream is = con.getInputStream();
            String encoding = con.getContentEncoding();
            encoding = encoding == null ? "UTF-8" : encoding;
            String body = IOUtils.toString(is, encoding);
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
            showUpdateAlert();
        else if (showNoUpdateToast)
            Toast.makeText(context, context.getString(R.string.updateNotAvailable), Toast.LENGTH_SHORT).show();
    }
    
    /**
     * 
     */
    public void showUpdateAlert() {
        AlertDialog.Builder builder = new AlertDialog.Builder(context);
        builder.setTitle(context.getString(R.string.updateTitle));
        builder.setMessage(context.getString(R.string.updateAvailable));
//        builder.setIcon(R.drawable.icon);
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

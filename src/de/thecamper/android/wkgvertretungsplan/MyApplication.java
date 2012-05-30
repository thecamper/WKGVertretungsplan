/**
 * 
 */
package de.thecamper.android.wkgvertretungsplan;

import org.acra.ACRA;
import org.acra.annotation.ReportsCrashes;

import android.app.Application;


/**
 * @author Daniel
 *
 */
@ReportsCrashes(formKey = "dGNQbmNLTWFEYmhHRzgxM18zZGVlT3c6MQ")
public class MyApplication extends Application {
    @Override
    public void onCreate() {
        // The following line triggers the initialization of ACRA
        ACRA.init(this);
        setTheme(R.style.Theme_Light);
        super.onCreate();
    }
}

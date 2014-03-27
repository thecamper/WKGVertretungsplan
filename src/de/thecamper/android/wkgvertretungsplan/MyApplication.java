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
// @formatter:off
@ReportsCrashes(formKey = "",
				formUri = "https://grothe6.pictor.uberspace.de/acra-wkg/_design/acra-storage/_update/report",
				reportType = org.acra.sender.HttpSender.Type.JSON,
				httpMethod = org.acra.sender.HttpSender.Method.PUT,
				formUriBasicAuthLogin = "wkgvertretungsplan",
				formUriBasicAuthPassword = "f3684dAc")
// @formatter:on
public class MyApplication extends Application {
	@Override
	public void onCreate() {
		super.onCreate();
		// The following line triggers the initialization of ACRA
		ACRA.init(this);
	}
}

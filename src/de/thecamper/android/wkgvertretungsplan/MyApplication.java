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
package de.thecamper.android.wkgvertretungsplan;

import org.acra.ACRA;
import org.acra.annotation.ReportsCrashes;

import android.app.Application;

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

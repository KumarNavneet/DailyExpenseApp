package com.example.hisaabapp;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.os.Handler;
import android.widget.Toast;

import com.salesforce.androidsdk.app.ForceApp;
import com.salesforce.androidsdk.rest.ClientManager;
import com.salesforce.androidsdk.rest.ClientManager.LoginOptions;
import com.salesforce.androidsdk.rest.ClientManager.RestClientCallback;
import com.salesforce.androidsdk.rest.RestClient;
import com.salesforce.androidsdk.security.PasscodeManager;
import com.salesforce.androidsdk.util.TokenRevocationReceiver;

public class SplashActivity extends Activity {
	private boolean mIsBackButtonPressed;
	boolean haveConnectedWifi = false;
	boolean haveConnectedMobile = false;
	boolean synchComplete = false;
	private TokenRevocationReceiver tokenRevocationReceiver;
	private PasscodeManager passcodeManager;
	Handler handler;
	// private RestClient client;
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		super.onCreate(savedInstanceState);
		setContentView(R.layout.splashscreen);
		passcodeManager = ForceApp.APP.getPasscodeManager();
		tokenRevocationReceiver = new TokenRevocationReceiver(this);
		ConnectivityManager cm = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
		NetworkInfo[] netInfo = cm.getAllNetworkInfo();
		for (NetworkInfo ni : netInfo) {
			if (ni.getTypeName().equalsIgnoreCase("WIFI"))
				if (ni.isConnected())
					haveConnectedWifi = true;
			if (ni.getTypeName().equalsIgnoreCase("MOBILE"))
				if (ni.isConnected())
					haveConnectedMobile = true;
		}
		if (haveConnectedWifi || haveConnectedMobile) {
			// synch the old data on database.com
			synchComplete = true;
		} else {
			synchComplete = true;
		}
	}

	@Override
	public void onResume() {
		super.onResume();
		registerReceiver(tokenRevocationReceiver, new IntentFilter(ClientManager.ACCESS_TOKEN_REVOKE_INTENT));
		if(haveConnectedWifi == false &&  haveConnectedMobile == false)
		{
				new Handler().postDelayed(new Runnable() {				
		        @Override    
				public void run() {
		            	Toast.makeText(SplashActivity.this, "No Network Connectivity..Exiting!!", Toast.LENGTH_LONG).show();
		            	finish();
		            } 
		        }, 2000);
		}
		if (haveConnectedWifi || haveConnectedMobile) {
			if (passcodeManager.onResume(this)){
			String accountType = getString(R.string.account_type);
			LoginOptions loginOptions = new LoginOptions(
					null, // gets overridden by LoginActivity based on server
							// picked by uuser
					ForceApp.APP.getPasscodeHash(),
					getString(R.string.oauth_callback_url),
					getString(R.string.oauth_client_id), new String[] { "api" });
			new ClientManager(this, accountType, loginOptions,
					ForceApp.APP.shouldLogoutWhenTokenRevoked()).getRestClient(
					this, new RestClientCallback() {

						@Override
						public void authenticatedRestClient(RestClient client) {
							if (client == null) {
								ForceApp.APP.logout(SplashActivity.this);
								return;
							}
							MainActivity.client = client;
							startActivity(new Intent(SplashActivity.this,
									MainActivity.class));
						}
					});
			}
			
		}

	}

	@Override
	public void onBackPressed() {
		// set the flag to true so the next activity won't start up
		mIsBackButtonPressed = true;
		super.onBackPressed();

	}

	@Override
	protected void onPause() {
		// TODO Auto-generated method stub
		super.onPause();
		finish();
	}

}

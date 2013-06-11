package com.example.hisaabapp;

import android.app.Activity;
import android.content.IntentFilter;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;

import com.salesforce.androidsdk.app.ForceApp;
import com.salesforce.androidsdk.rest.ClientManager;
import com.salesforce.androidsdk.rest.ClientManager.LoginOptions;
import com.salesforce.androidsdk.rest.ClientManager.RestClientCallback;
import com.salesforce.androidsdk.rest.RestClient;
import com.salesforce.androidsdk.ui.NativeMainActivity;
import com.salesforce.androidsdk.util.TokenRevocationReceiver;

public class MainActivity extends Activity {
	public String LOG_TAG = "HisaabApp Tag"; 
	private TokenRevocationReceiver tokenRevocationReceiver;
	private RestClient client;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);
		Log.d(LOG_TAG, "OnCreate() of the MainActivity");
	}
	@Override
	public void onResume(){
		super.onResume();
//		// Hide everything until we are logged in
//				findViewById(R.id.root).setVisibility(View.INVISIBLE);
		// Login options
		String accountType = getString(R.string.account_type);
		LoginOptions loginOptions = new LoginOptions(
				null, // gets overridden by LoginActivity based on server picked by uuser 
				ForceApp.APP.getPasscodeHash(),
				getString(R.string.oauth_callback_url),
				getString(R.string.oauth_client_id),
				new String[] {"api"});
		new ClientManager(this, accountType, loginOptions, ForceApp.APP.shouldLogoutWhenTokenRevoked()).getRestClient(this, new RestClientCallback() {

			@Override
			public void authenticatedRestClient(RestClient client) {
				if (client == null) {
					ForceApp.APP.logout(MainActivity.this);
					return;
				}
				MainActivity.this.client = client;
				//getAlbums();
			}
		});
		tokenRevocationReceiver = new TokenRevocationReceiver(this);
		registerReceiver(tokenRevocationReceiver, new IntentFilter(ClientManager.ACCESS_TOKEN_REVOKE_INTENT));
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.main, menu);
		return true;
	}
//	@Override
//	protected LoginOptions getLoginOptions() {
//		// TODO Auto-generated method stub
//		LoginOptions loginOptions = new LoginOptions(
//    			null, // login host is chosen by user through the server picker 
//    			ForceApp.APP.getPasscodeHash(),
//    			getString(R.string.oauth_callback_url),
//    			getString(R.string.oauth_client_id),
//    			new String[] {"api"});
//    	return loginOptions;
//		
//	}
//	@Override
//	protected void onResume(RestClient client) {
//		// TODO Auto-generated method stub
//		// Keeping reference to rest client
//        this.client = client; 
//
////		// Show everything
////		findViewById(R.id.root).setVisibility(View.VISIBLE);
//	}

}

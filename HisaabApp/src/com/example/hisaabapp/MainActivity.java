package com.example.hisaabapp;

import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import android.app.Activity;
import android.content.Context;
import android.content.IntentFilter;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.text.Editable;
import android.util.Log;
import android.view.Menu;
import android.view.View;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.Toast;

import com.salesforce.androidsdk.app.ForceApp;
import com.salesforce.androidsdk.rest.ClientManager;
import com.salesforce.androidsdk.rest.RestClient;
import com.salesforce.androidsdk.rest.RestRequest;
import com.salesforce.androidsdk.rest.RestResponse;
import com.salesforce.androidsdk.rest.RestClient.AsyncRequestCallback;
import com.salesforce.androidsdk.security.PasscodeManager;
import com.salesforce.androidsdk.util.EventsObservable;
import com.salesforce.androidsdk.util.TokenRevocationReceiver;
import com.salesforce.androidsdk.util.EventsObservable.EventType;



public class MainActivity extends Activity {
	boolean haveConnectedWifi = false;
    boolean haveConnectedMobile = false;
	private DatePicker dp;
	private Button okButton;
	private Button resetButton;
	private RadioButton radioButton;
	private RadioGroup radioGroup;
	private EditText details = null;
	private EditText amount = null;
	private int year;
	private int month;
	private int day;
	private String selectedName = null;
	private boolean readyToStore = false;
	private String toastMessgae = "Please Enter ";
	public String LOG_TAG = "HisaabApp Tag"; 
	private TokenRevocationReceiver tokenRevocationReceiver;
	public static RestClient client;
	public  final Calendar c = Calendar.getInstance();
	private final String objectType = "Db_Table_1__c";
	private String apiVersion;
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		Log.d(LOG_TAG, "OnCreate() of the MainActivity");
		// ApiVersion
		apiVersion = getString(R.string.api_version);
        setContentView(R.layout.main);
       
        //Toast.makeText(getApplicationContext(), "No Network!!! Fill the details in offline mode only...", Toast.LENGTH_LONG).show();
        dp = (DatePicker)findViewById(R.id.datePicker1);
        okButton = (Button)findViewById(R.id.button1);
        resetButton = (Button)findViewById(R.id.button2);
        details = (EditText)findViewById(R.id.editText1);
        amount = (EditText)findViewById(R.id.editText2);
        radioGroup = (RadioGroup)findViewById(R.id.radioGroup);       
		year = c.get(Calendar.YEAR);
		month = c.get(Calendar.MONTH);
		day = c.get(Calendar.DAY_OF_MONTH);
		//dp.setCalendarViewShown(false);
		dp.init(year, month, day, null);
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
	}
	@Override
	public void onResume(){
		super.onResume();
		 	if(client != null)
				Toast.makeText(getBaseContext(), "Made Connection with db.com", Toast.LENGTH_LONG).show();
			else
				Toast.makeText(getBaseContext(), "Failed Connection with db.com", Toast.LENGTH_LONG).show();
			okButton.setOnClickListener(new View.OnClickListener() {
				
				public void onClick(View v) {
					// TODO Auto-generated method stub
					toastMessgae = "Please Enter ";
					Date d = new Date();
					d.setDate(dp.getDayOfMonth());
					d.setMonth(dp.getMonth());
					d.setYear(dp.getYear());
					Editable detail =  details.getText();
					Editable amt =  amount.getText();
					
					if(radioGroup.getCheckedRadioButtonId()!=-1 )
						{
							radioButton = (RadioButton)findViewById(radioGroup.getCheckedRadioButtonId());
							selectedName = (String) radioButton.getText();
						}
					else
						toastMessgae += "your name "; 
						//Toast.makeText(TestProjectActivity.this, "Please select your name", Toast.LENGTH_SHORT).show();
					
					if(details.getText().length() == 0)
						toastMessgae += "the Details ";
					
					if(amount.getText().length() == 0)
						toastMessgae += "the Amount ";
					
					if(toastMessgae.equals("Please Enter "))
					{
						readyToStore = true;
					}
					//Log.d(LOG_TAG, ""+client);
					
					//Toast.makeText(getBaseContext(), d+"  "+ detail+"  "+ amt +" "+selectedName , Toast.LENGTH_LONG).show();
					if(readyToStore)
					{
						if( haveConnectedWifi || haveConnectedMobile)
						   {
							Map<String, Object> fields = new HashMap<String, Object>();
							fields.put("Amount__c",amt.toString());
							fields.put("Date_And_Time__c",d);
							fields.put("Details__c", detail.toString());
							fields.put("Name__c", selectedName);
							fields.put("Serial_Number__c","");
							RestRequest request = null;
							try {
								request = RestRequest.getRequestForUpdate(apiVersion, objectType,"",fields);
							} catch (Exception e) {
								e.printStackTrace();
								return;
							}
							sendRequest(request);
							   	System.out.println(haveConnectedMobile +" "+ haveConnectedWifi);
						   }
						   else
						   {
							   System.out.println(haveConnectedMobile +" "+ haveConnectedWifi);
						   }
					}
					else
						Toast.makeText(getBaseContext(), toastMessgae, Toast.LENGTH_LONG).show();
					
				}
			});
			
			resetButton.setOnClickListener(new View.OnClickListener() {
				
				public void onClick(View v) {
					// TODO Auto-generated method stub
					toastMessgae = "Please Enter ";
					details.setText("");
					amount.setText("");
					year = c.get(Calendar.YEAR);
					month = c.get(Calendar.MONTH);
					day = c.get(Calendar.DAY_OF_MONTH);
					//dp.setCalendarViewShown(false);
					dp.init(year, month, day, null);
					radioGroup.clearCheck();
				}
			});
	}
	private void sendRequest(RestRequest request) {
		try {
			sendFromUIThread(request);
			// response is printed by RestCallTask:onPostExecute
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	private void sendFromUIThread(RestRequest restRequest) {
		client.sendAsync(restRequest, new AsyncRequestCallback() {
			private long start = System.nanoTime();

			@Override
			public void onSuccess(RestRequest request, RestResponse result) {
				Toast.makeText(getBaseContext(), "Successfully inserted the record", Toast.LENGTH_LONG).show();
				try {
					long duration = System.nanoTime() - start;
					int size = result.asString().length();
					int statusCode = result.getStatusCode();
				} catch (Exception e) {
					e.printStackTrace();
				}
			
				EventsObservable.get().notifyEvent(EventType.RenditionComplete);
			}
			
			@Override
			public void onError(Exception exception) {
				EventsObservable.get().notifyEvent(EventType.RenditionComplete);				
			}
		});
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.main, menu);
		return true;
	}

}

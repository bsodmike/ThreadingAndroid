/* Author: 	Michael de Silva
 * Date:	27th December 2010
 * Email:	michael@mwdesilva.com
 * Blog:	bsodmike.com
 *
 * Adapted https://github.com/bsodmike/ThreadingAndroid/tree/threading4
 * to implement pipeline thread pattern.
 * 
 * ref 1. http://mindtherobot.com/blog/159/android-guts-intro-to-loopers-and-handlers/
 * ref 2. http://codinghard.wordpress.com/2009/05/16/android-thread-messaging/
 * ref 3. http://www.aviyehuda.com/2010/12/android-multithreading-in-a-ui-environment/
 */

package com.threading;

import android.app.Activity;
import android.app.ProgressDialog;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

public class ThreadingActivity extends Activity implements OnClickListener {
	private TextView textOutput;
	private Button buttonPush;
	private Handler innerHandler;
	private ProgressDialog backgroundTask;
	
	private int mResults;
	
	final Handler mHandler = new Handler();

	/*
	 * Create this only once!
	 */
	// Create runnable for posting
	final Runnable mUpdateResults = new Runnable() {
		public void run() {
			updateResultsInUi(mResults);
		}
	};
	
    /** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.main);
		
		textOutput = (TextView)findViewById(R.id.textOutput);
		buttonPush = (Button)findViewById(R.id.buttonPush); // Get button from xml
		buttonPush.setOnClickListener(this); 
		
		new PipelineThread().start();
	}


    public void onClick(View v) {
    	switch(v.getId()){
    	case R.id.buttonPush:
    		if (innerHandler!=null){
    			try{
    				backgroundTask = ProgressDialog.show(this, "", "Loading", true);
    				Message msg = innerHandler.obtainMessage();
		            innerHandler.sendMessage(msg);
		            Log.i(Thread.currentThread().getName(), "Sending a message to the child thread - " + (String)msg.obj);
    			}catch (Exception e){
    				//@todo
    				e.printStackTrace();
    			}
    		}
    	default:
    		break;
    	}
    }
    
    public class PipelineThread extends Thread {
		public void run() {
			final String TAG = Thread.currentThread().getName();
			
			Looper.prepare();
			
			innerHandler = new Handler(){
				public void handleMessage(Message msg) {
					
				    Message message = mHandler.obtainMessage();
				    mHandler.sendMessage(message);
				    Log.d(TAG,"sending message to "+mHandler.getLooper().getThread().getName()); 
				    Log.d(TAG, "bound to "+innerHandler.getLooper().getThread().getName());

				    try {				    
				    	mResults = doSomethingExpensive();
				    	
				    	/*
				    	 * This method creates a new object
				    	 * each time a runnable is posted;
				    	 * hence it has been commented...
				    	 */
				        // Create runnable for posting
//				        mHandler.post(new Runnable() {
//							
//							@Override
//							public void run() {
//								// TODO Auto-generated method stub
//								updateResultsInUi(mResults);
//							}
//						});		
				    	
				    	//...in favour of this.
				    	mHandler.post(mUpdateResults);
				    	
					}catch (Exception e){
						//TODO
						e.printStackTrace();
					}			
				}
			};
			Log.d(TAG, "Looper.loop()");
		    Looper.loop();
		}
    }

    private void updateResultsInUi(int mResults) {
        // Back in the UI thread -- update our UI elements based on the data in mResults
    	backgroundTask.dismiss();
    	textOutput.setText("Received: " + mResults);
    	Toast.makeText(getApplicationContext(), "Whooo: " + mResults, Toast.LENGTH_LONG).show();
    }    
    
    public int doSomethingExpensive(){
    	final String TAG = Thread.currentThread().getName();
    	try {
    		Log.d(TAG, "background operation starting");
			Thread.sleep(1000);
    		Log.d(TAG, "background operation done");
    	} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
    	return(RESULT_OK);
    }

    //prevent looper from looping ad infinitum
    @Override
	protected void onDestroy() {
		innerHandler.getLooper().quit();
		super.onDestroy();
	} 
    
}
 
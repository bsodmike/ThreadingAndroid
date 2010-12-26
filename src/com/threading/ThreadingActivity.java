/* Author: 	Michael de Silva
 * Date:	26nd December 2010
 * Email:	michael@mwdesilva.com
 * Blog:	bsodmike.com
 *
 * Example on threading as per Android documentation,
 * http://developer.android.com/resources/faq/commontasks.html#threading  
 */

package com.threading;

import android.app.Activity;
import android.app.ProgressDialog;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

public class ThreadingActivity extends Activity implements OnClickListener {
	private int mResults;
	private TextView textOutput;
	private Button buttonPush;
	final Handler mHandler = new Handler();
	
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
	}


    public void onClick(View v) {
    	switch(v.getId()){
    	case R.id.buttonPush:
    		startLongRunningOperation();
    	default:
    		break;
    	}

    }
    protected void startLongRunningOperation() {

    	// DISPLAYING UR PROGRESS DIALOG
    	final ProgressDialog backgroundTask = ProgressDialog.show(this, "", "Loading", true);

	    // Fire off a thread to do some work that we shouldn't do directly in the UI thread
	    Thread t = new Thread() {
	        public void run() {
	        	try {
		        	Log.d(this.getName(),"bound to "+mHandler.getLooper().getThread().getName());
		            mResults = doSomethingExpensive();
		            mHandler.post(mUpdateResults);
		              //ENDING YOUR PROGRESSDIALOG UPON COMPLETION
		            backgroundTask.dismiss();
	        	}catch (Exception e){
	        		//TODO
	        	}
	        	Log.d(this.getName(),"sending message to "+threadHandler.getLooper().getThread().getName());
	        	threadHandler.sendEmptyMessage(0);
	        }
	    };
	    t.start();
	}
    
	private Handler threadHandler = new Handler() {
		public void handleMessage(Message msg) {
			if (msg.what==0){
				Log.d(this.getLooper().getThread().getName(), "threadHandler");
				Toast.makeText(getApplicationContext(), "Whooo: " + msg.what, Toast.LENGTH_LONG).show();
			}
		}
	};    
    
    private void updateResultsInUi(int mResults) {
        // Back in the UI thread -- update our UI elements based on the data in mResults
    	textOutput.setText("Received: " + mResults);
    }    
    
    private int doSomethingExpensive(){
    	try {
    		Log.d(Thread.currentThread().getName(), "background operation starting");
			Thread.sleep(1000);
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
    	return(RESULT_OK);
    }
		
}
 
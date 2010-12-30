/* Author: 	Michael de Silva
 * Date:	26th December 2010
 * Email:	michael@mwdesilva.com
 * Blog:	bsodmike.com
 *
 * Example on threading as per Android documentation,
 * http://developer.android.com/resources/faq/commontasks.html#threading  
 * 
 * ref: http://www.aviyehuda.com/2010/12/android-multithreading-in-a-ui-environment/
 * 
 * Implemented  looper in child thread. 
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
	private int mResults;
	private TextView textOutput;
	private Button buttonPush;
	private Handler innerHandler;
	
	// Create runnable for posting
	final Runnable mUpdateResults = new Runnable() {
		public void run() {
			updateResultsInUi(mResults);
		}
	};	
	
	final Handler mHandler = new Handler(){
		public void handleMessage(Message msg) {
			if (msg.what==0){
				Log.d(this.getLooper().getThread().getName(), "mHandler");
				Toast.makeText(getApplicationContext(), "Whooo: " + msg.what, Toast.LENGTH_LONG).show();
			}
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
	        	
	        	Looper.prepare();
	        		
	        	innerHandler = new Handler();
                Message message = innerHandler.obtainMessage();
                innerHandler.dispatchMessage(message);
                Log.d(innerHandler.getLooper().getThread().getName(), "bound to "+ innerHandler.getLooper().getThread().getName());
                
	        	try {
		        	Log.d(this.getName(), "bound to "+mHandler.getLooper().getThread().getName());
		            
		        	mResults = doSomethingExpensive();
		        	
		        	//post runnable to message queue
		            mHandler.post(mUpdateResults);
		            
		            /*
		             * quit each 'spawned' thread on completion.
		             */
		            innerHandler.getLooper().quit();
		            backgroundTask.dismiss();
		            
	        	}catch (Exception e){
	        		//TODO
	        	}
	        	Log.d(this.getName(),"sending message to "+mHandler.getLooper().getThread().getName());
	        	mHandler.sendEmptyMessage(0);
	        	
                Looper.loop();
	        }
	    };
	    t.start();
	}  
    
    public void updateResultsInUi(int mResults) {
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

    //prevent looper from looping ad infinitum
    @Override
	protected void onDestroy() {
		innerHandler.getLooper().quit();
		super.onDestroy();
	} 
    
}
 
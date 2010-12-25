package com.threading;

import android.app.Activity;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.TextView;

public class ThreadingActivity extends Activity implements OnClickListener, Runnable {

    /** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.main);
		
		TextView textOutput = (TextView)findViewById(R.id.textOutput);
		Button buttonPush = (Button)findViewById(R.id.buttonPush); // Get button from xml
        ProgressBar progressBar = (ProgressBar) findViewById(R.id.progressBar);		
		
		buttonPush.setOnClickListener(this); 
	}


    public void onClick(View v) {
                          
	    Thread pipelineThread = new Thread() {
	    	/*
	    	 * 1. uncomment this and "Runnable" from above
	    	 * 2. comment lines 68-80
	    	 */
//	        public void run(){
//	        	final String TAG = "pipelineThread";
//	                try {
//	                	Log.i(TAG, "before sleep");
//	                	Thread.sleep(1500); //just to show you that it works
//	                	Log.i(TAG, "after sleep");
//	                } catch (Exception e) {
//	                        // @todo: Show error message
//	                }
//	                showResults.sendEmptyMessage(0);                                        
//	        }
	    };
	    pipelineThread.start();
    }


    private Handler showResults = new Handler() {
    	public static final String TAG = "handler_showResults";
    	
    	@Override
    	public void handleMessage(Message msg) {
    		Log.i(TAG, "received message: "+ (Object)msg.obj);
    	
    		super.handleMessage(msg);
    	}
    };


    /*
     * ThreadingActivity implements Runnable
     */
	@Override
	public void run() {
		// TODO Auto-generated method stub
    	final String TAG = "pipelineThread";
        try {
        	Log.i(TAG, "before sleep");
        	Thread.sleep(1500); //just to show you that it works
        	Log.i(TAG, "after sleep");
        } catch (Exception e) {
                // @todo: Show error message
        }
        showResults.sendEmptyMessage(0);  			
	} 
		
}
 
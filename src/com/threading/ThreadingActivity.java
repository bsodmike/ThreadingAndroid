/* Author: 	Michael de Silva
 * Date:	26th December 2010
 * Email:	michael@mwdesilva.com
 * Blog:	bsodmike.com
 *
 * Simple example of threading as per
 * http://www.anddev.org/post90900.html#p90900 
 */

package com.threading;

import android.app.Activity;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.TextView;

public class ThreadingActivity extends Activity implements Runnable, OnClickListener {
	private Thread pipelineThread;
	private TextView textOutput;
	private Button buttonPush;

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
                          
    	pipelineThread = new Thread(this) {
	    	/*
	    	 * 1. uncomment this and remove "implements Runnable" from ThreadingActivity
	    	 * 2. change new Thread(this) {} to new Thread() {} above
	    	 * 3. comment lines 68-80
	    	 */
//	        public void run(){
//	        	final String TAG = "pipelineThread";
//	                try {
//	                	Log.i(TAG, "before sleep (" + this.getName() + ")");
//	                	Thread.sleep(1500); //just to show you that it works
//	                	Log.i(TAG, "after sleep (" + this.getName() + ")");
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
    		Log.i(TAG, "received message: "+ (Object)msg.obj +  " (" + this.getLooper().getThread().getName() + ")");
    		textOutput.setText("received message: "+ (Object)msg.obj +  " (" + this.getLooper().getThread().getName() + ")");
    		super.handleMessage(msg);
    	}
    };


    /*
     * ThreadingActivity implements Runnable
     * 1. comment this and add "implements Runnable" to ThreadingActivity
     * 2. change new Thread() {} to new Thread(this) {} above
     * 3. comment lines 48-58
     */
    @Override
	public void run() {
		// TODO Auto-generated method stub
    	final String TAG = "pipelineThread";
        try {
        	Log.i(TAG, "before sleep (" + pipelineThread.getName() + ")");
        	Thread.sleep(1500); //just to show you that it works
        	Log.i(TAG, "after sleep (" + pipelineThread.getName() + ")");
        } catch (Exception e) {
            // @todo: Show error message
        }
    showResults.sendEmptyMessage(0);			
	} 
		
}
 
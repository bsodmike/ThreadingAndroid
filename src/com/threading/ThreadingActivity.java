/* Author: 	Michael de Silva
 * Date:	26nd December 2010
 * Email:	michael@mwdesilva.com
 * Blog:	bsodmike.com
 *
 * Pipelined thread execution demo as per
 * http://mindtherobot.com/blog/159/android-guts-intro-to-loopers-and-handlers/ 
 */

package com.threading;

import java.util.Random;

import android.app.Activity;
import android.app.ProgressDialog;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.TextView;

public class ThreadingActivity extends Activity implements TaskThreadListener, OnClickListener {
	private TaskThread pipelineThread;
	private Handler handler;

	private TextView textOutput;
	private Button buttonPush;
	private ProgressBar progressBar;
	private ProgressDialog pd;
	
    /** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.main);

        // Create and launch the download thread
        pipelineThread = new TaskThread(this);
        pipelineThread.start();
        
        // Create the Handler. It will implicitly bind to the Looper
        // that is internally created for this thread (since it is the UI thread)
        handler = new Handler();		
		
		textOutput = (TextView)findViewById(R.id.textOutput);
		buttonPush = (Button)findViewById(R.id.buttonPush); // Get button from xml
        progressBar = (ProgressBar) findViewById(R.id.progressBar);		
		
		buttonPush.setOnClickListener(this); 
	}


    public void onClick(View v) {
    	switch(v.getId()){
    	case R.id.buttonPush:
			int totalTasks = new Random().nextInt(5) + 1;

    		//pd = ProgressDialog.show(this, "", "Loading", true); //just for fun
    		buttonPush.setClickable(false);
    		
			for (int i = 0; i < totalTasks; ++i) {
				pipelineThread.enqueueDownload(new ScheduledTask());
			}
		default:
			break;
    	}
    }
    
    public final class TaskThread extends Thread {

    	private final String TAG = TaskThread.class.getSimpleName();
    	
    	private Handler handler;
    	private int totalQueued;
    	private int totalCompleted;

    	private TaskThreadListener listener;
    	
    	public TaskThread(TaskThreadListener listener) {
    		this.listener = listener;
    	}
    	
    	@Override
    	public void run() {
    		try {
    			// preparing a looper on current thread			
    			// the current thread is being detected implicitly
    			Looper.prepare();

    			Log.i(TAG, "TaskThread entering the loop");

    			this.setName("pipelineThread");
    			
    			// now, the handler will automatically bind to the
    			// Looper that is attached to the current thread
    			// You don't need to specify the Looper explicitly
    			handler = new Handler();
    			
    			// After the following line the thread will start
    			// running the message loop and will not normally
    			// exit the loop unless a problem happens or you
    			// quit() the looper (see below)
    			Looper.loop();
    			
    			Log.i(TAG, "TaskThread exiting gracefully");
    		} catch (Throwable t) {
    			Log.e(TAG, "TaskThread halted due to an error", t);
    		} 
    	}
    	
    	// This method is allowed to be called from any thread
    	public synchronized void requestStop() {
    		// using the handler, post a Runnable that will quit()
    		// the Looper attached to our DownloadThread
    		// obviously, all previously queued tasks will be executed
    		// before the loop gets the quit Runnable
    		handler.post(new Runnable() {
    			@Override
    			public void run() {
    				// This is guaranteed to run on the DownloadThread
    				// so we can use myLooper() to get its looper
    				Log.i(TAG, "TaskThread loop quitting by request");
    				
    				Looper.myLooper().quit();
    			}
    		});
    	}
    	
    	public synchronized void enqueueDownload(final ScheduledTask task) {   		
    		// Wrap DownloadTask into another Runnable to track the statistics
    		handler.post(new Runnable() {
    			@Override
    			public void run() {
    				try {
    					task.run();
    					} finally {					
    					// register task completion
    					synchronized (TaskThread.this) {
    						totalCompleted++;
    					}
    					// tell the listener something has happened
    					signalUpdate();
    				}				
    			}
    		});
    		
    		totalQueued++;
    		// tell the listeners the queue is now longer
    		signalUpdate();
    	}
    	
    	public synchronized int getTotalQueued() {
    		return totalQueued;
    	}
    	
    	public synchronized int getTotalCompleted() {
    		return totalCompleted;
    	}
    	
    	// Please note! This method will normally be called from the download thread.
    	// Thus, it is up for the listener to deal with that (in case it is a UI component,
    	// it has to execute the signal handling code in the UI thread using Handler - see
    	// DownloadQueueActivity for example).
    	private void signalUpdate() {
    		if (listener != null) {
    			listener.handleTaskThreadUpdate();
    		}
    	}
    }   

    public void handleTaskThreadUpdate() {
		// we want to modify the progress bar so we need to do it from the UI thread 
		// how can we make sure the code runs in the UI thread? use the handler!
		handler.post(new Runnable() {
			@Override
			public void run() {
				final String TAG = "handleTaskThreadUpdate";
				int total = pipelineThread.getTotalQueued();
				int completed = pipelineThread.getTotalCompleted();
				
				progressBar.setMax(total);
				
				progressBar.setProgress(0); // need to do it due to a ProgressBar bug
				progressBar.setProgress(completed);
				
				textOutput.setText(String.format("Pipeline tasks completed %d/%d", completed, total));
				
				if (completed == total){
					//to simulate a one click scheduling of tasks.
					buttonPush.setClickable(true);
					//pd.dismiss(); //just for fun
				}
				
				Log.d(TAG, "update ui");
			}
		});
	}

    
    /**
     * This is not a real download task.
     * It just sleeps for some random time when it's launched. 
     * The idea is not to require a connection and not to eat it.
     * 
     */
    public static class ScheduledTask implements Runnable {

    	private static final String TAG = ScheduledTask.class.getSimpleName();
    	
    	private static final Random random = new Random();
    	
    	private int lengthSec;
    	
    	public ScheduledTask() {
    		lengthSec = random.nextInt(3) + 1;
    	}
    	
    	@Override
    	public void run() {
    		try {   			
    			Thread.sleep(lengthSec * 1000);
    			
    			// it's a good idea to always catch Throwable
    			// in isolated "codelets" like Runnable or Thread
    			// otherwise the exception might be sunk by some
    			// agent that actually runs your Runnable - you
    			// never know what it might be.
    		} catch (Throwable t) {
    			Log.e(TAG, "Error in ScheduleTask", t);
    		}
    	}
    }    
    
		
}
 
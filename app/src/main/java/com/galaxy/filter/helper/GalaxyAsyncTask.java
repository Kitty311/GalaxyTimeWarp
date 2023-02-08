package com.galaxy.filter.helper;

import android.os.Handler;
import android.os.Looper;

import java.io.IOException;

public abstract class GalaxyAsyncTask extends Thread {
    private String[] mInput;
    private String[] mOutput;
    private Handler mMainHandler;

    public GalaxyAsyncTask() {
        // Using this handler to update UI
        mMainHandler = new Handler(Looper.getMainLooper());
    }

    abstract protected void onPreExecute();

    abstract protected void onPostExecute(int progress);

    abstract protected void onProgressUpdate(int progress);

    protected void publishProgress(int progress) {
        // This will run in UI thread.
        mMainHandler.post(new Runnable() {
            @Override
            public void run() {
                onProgressUpdate(progress);
            }
        });
    }

    abstract protected int doInBackground(String[] input, String[] output) throws IOException;

    protected void execute(String[] input, String[] ouput) {
        mInput = input;
        mOutput = ouput;
        start();
    }

    public void run() {
        // This will run in UI thread.
        mMainHandler.post(new Runnable() {
            @Override
            public void run() {
                onPreExecute();
            }
        });

        int result = 0;
        try {
            // This will run in the background thread.
            result = doInBackground(mInput, mOutput);
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            // This will run in UI thread.
            final int finalResult = result;
            mMainHandler.post(new Runnable() {
                @Override
                public void run() {
                    onPostExecute(finalResult);
                }
            });
        }
    }
}
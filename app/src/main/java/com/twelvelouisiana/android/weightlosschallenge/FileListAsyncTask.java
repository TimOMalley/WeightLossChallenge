package com.twelvelouisiana.android.weightlosschallenge;

import android.app.Activity;
import android.os.AsyncTask;

import java.io.File;
import java.io.FilenameFilter;
import java.lang.ref.WeakReference;

/**
 * Background task to list the saved data files in the data directory.
 */

public class FileListAsyncTask extends AsyncTask<Void, Void, File[]> implements FilenameFilter
{
    ActivityCallback activityCallback;
    WeakReference<Activity> weakReference;

    public FileListAsyncTask(Activity activity)
    {
        activityCallback = (ActivityCallback) activity;
        weakReference = new WeakReference<Activity>(activity);
    }

    @Override
    protected File[] doInBackground(Void... voids) {
        Activity activity = weakReference.get();
        if (activity != null)
        {
            return activity.getFilesDir().listFiles(this);
        }
        return new File[0];
    }

    @Override
    protected void onPostExecute(File[] files) {
        activityCallback.sendData(files);
        super.onPostExecute(files);
    }

    @Override
    public boolean accept(File file, String name) {
        String lowercaseName = name.toLowerCase();
        if (lowercaseName.endsWith(Constants.DATA_FILENAME_EXT))
        {
            return true;
        }
        else
        {
            return false;
        }
    }
}

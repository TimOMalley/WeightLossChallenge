package com.twelvelouisiana.android.weightlosschallenge;

import android.app.Activity;
import android.os.AsyncTask;
import android.widget.TextView;

import java.io.File;
import java.io.FilenameFilter;
import java.lang.ref.WeakReference;

/**
 * Background task to list the saved data files in the data directory.
 */

public class FileListAsyncTask extends AsyncTask<Void, Void, String[]> implements FilenameFilter
{
    ActivityCallback fileListActivityCallback;
    WeakReference<Activity> weakReference;

    public FileListAsyncTask(Activity activity)
    {
        fileListActivityCallback = (ActivityCallback) activity;
        weakReference = new WeakReference<Activity>(activity);
    }

    @Override
    protected String[] doInBackground(Void... voids) {
        Activity activity = weakReference.get();
        if (activity != null)
        {
            return activity.getFilesDir().list(this);
        }
        return new String[0];
    }

    @Override
    protected void onPostExecute(String[] strings) {
        fileListActivityCallback.sendData(strings);
        super.onPostExecute(strings);
    }

    @Override
    public boolean accept(File file, String name) {
        String lowercaseName = name.toLowerCase();
        if (lowercaseName.endsWith(WeightLossChallengeActivity.DATA_FILENAME_EXT)
                && lowercaseName.startsWith(WeightLossChallengeActivity.DATA_FILENAME_PREFIX))
        {
            return true;
        }
        else
        {
            return false;
        }
    }
}

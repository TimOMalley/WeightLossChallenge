package com.twelvelouisiana.android.weightlosschallenge;

import android.app.Activity;
import android.content.Context;
import android.os.AsyncTask;
import android.util.Log;
import android.widget.Toast;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.List;

/**
 * Background task to read, write, and delete a file.
 */

public class FileOperationsAsyncTask extends AsyncTask<String, Void, String[]>
{
    private static final String TAG = FileOperationsAsyncTask.class.getName();
    private static final String LINE_SEPARATOR = System.getProperty("line.separator");

    private ActivityCallback activityCallback;
    private WeakReference<Activity> weakReference;
    private int operation;
    private String filename;

    public FileOperationsAsyncTask(Activity activity, int operation, String filename)
    {
        this.activityCallback = (ActivityCallback) activity;
        this.weakReference = new WeakReference<Activity>(activity);
        this.operation = operation;
        this.filename = filename;
    }

    @Override
    protected String[] doInBackground(String... strings)
    {
        String[] result = null;
        try {
            Activity activity = weakReference.get();

            if (activity != null && filename != null) {
                switch (operation) {
                    case Constants.FILE_READ:
                        result = readFile(activity, filename);
                        break;
                    case Constants.FILE_WRITE:
                        writeFile(activity, filename, strings);
                        break;
                    case Constants.FILE_DELETE:
                        deleteFile(activity.getFilesDir(), filename);
                        break;
                    default:
                        break;
                }
            }
        }
        catch(Exception e)
        {
            Log.e(TAG, "Error on file operation", e);
        }
        return result;
    }

    @Override
    protected void onPostExecute(String[] results)
    {
        switch (operation)
        {
            case Constants.FILE_READ:
                activityCallback.sendData(operation, results);
                break;
            case Constants.FILE_WRITE:
                Toast.makeText(weakReference.get(), weakReference.get().getString(R.string.file_saved), Toast.LENGTH_SHORT).show();
                break;
            case Constants.FILE_DELETE:
                Toast.makeText(weakReference.get(), weakReference.get().getString(R.string.file_deleted), Toast.LENGTH_SHORT).show();
                activityCallback.sendData(operation, results);
                break;
            default:
                break;
        }

        super.onPostExecute(results);
    }

    private String[] readFile(Context context, String filename)
    {
        List<String> lines = new ArrayList<String>();
        File file = new File(context.getFilesDir(), filename);
        if (file.exists()) {
            InputStream inputStream = null;
            try {
                inputStream = context.openFileInput(filename);
                InputStreamReader inputStreamReader = new InputStreamReader(inputStream);
                BufferedReader bufferedReader = new BufferedReader(inputStreamReader);
                String line;
                while ((line = bufferedReader.readLine()) != null) {
                    lines.add(line);
                }
            } catch (Exception e) {
                Log.e(TAG, "Error reading file : " + filename, e);
            } finally {
                try {
                    if (inputStream != null) {
                        inputStream.close();
                    }
                } catch (Exception e) {}
            }
        }
        String[] result = new String[lines.size()];
        return lines.toArray(result);
    }

    private void writeFile(Context context, String filename, String[] lines)
    {
        if (lines == null)
        {
            Log.d(TAG, "No data to write");
            return;
        }
        FileOutputStream fos = null;
        try
        {
            fos = context.openFileOutput(filename, Context.MODE_PRIVATE);
            for (String line : lines) {
                fos.write(line.getBytes());
                fos.write(LINE_SEPARATOR.getBytes());
            }
        }
        catch (Exception e)
        {
            Log.e(TAG, "Error writing file : " + filename, e);
        }
        finally
        {
            try {
                if (fos !=null)
                    fos.close();
            } catch (Exception e) {}
        }
    }

    private boolean deleteFile(File dir, String filename)
    {
        File file = new File(dir, filename);
        return file.delete();
    }
}

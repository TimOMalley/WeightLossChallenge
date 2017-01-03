package com.twelvelouisiana.android.weightlosschallenge;

import java.io.File;
import java.util.Comparator;

/**
 * Comparator to sort the files.
 */

public class FileComparator implements Comparator<File>
{
    private static final String TAG = FileComparator.class.getName();

    private boolean ascending = false;

    /**
     * Constructor with sort order parameter.
     *
     * @param ascending If true sort order is ascending, if false sort order is descending.
     */
    public FileComparator(boolean ascending)
    {
         this.ascending = ascending;
    }

    @Override
    public int compare(File file1, File file2) {
        long date1 = file1.lastModified();
        long date2 = file2.lastModified();
        if (ascending)
        {
            return ascending(date1, date2);
        }
        else
        {
            return descending(date1, date2);
        }
    }

    private int descending(long long1, long long2)
    {
        if (long1 > long2)
        {
            return -1;
        }
        else if (long1 < long2)
        {
            return 1;
        }
        else
        {
            return 0;
        }
    }

    private int ascending(long long1, long long2)
    {
        if (long1 > long2)
        {
            return 1;
        }
        else if (long1 < long2)
        {
            return -1;
        }
        else
        {
            return 0;
        }
    }
}

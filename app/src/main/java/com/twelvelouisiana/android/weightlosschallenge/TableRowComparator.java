package com.twelvelouisiana.android.weightlosschallenge;

import android.util.Log;

import java.text.ParseException;
import java.util.Comparator;

/**
 * Comparator to sort the table rows.
 */

public class TableRowComparator implements Comparator<String>
{
    private static final String TAG = TableRowComparator.class.getName();
    @Override
    public int compare(String row1, String row2) {
        try
        {
            String[] arr1 = row1.split(";");
            String date1 = arr1[0];
            String[] arr2 = row2.split(";");
            String date2 = arr2[0];
            return Constants.DATE_FORMAT.parse(date1).compareTo(Constants.DATE_FORMAT.parse(date2));
        } catch (ParseException e)
        {
            Log.d(TAG, "Error sorting table rows", e);
        }

        return -1;
    }
}

package com.twelvelouisiana.android.weightlosschallenge;

import android.util.Log;
import android.view.View;
import android.widget.TableLayout;
import android.widget.TableRow;
import android.widget.TextView;

import java.util.Calendar;
import java.util.Date;

/**
 * Class of utility or helper methods.
 */

public class Utilities
{
    private static final String TAG = Utilities.class.getName();

    public static String calculateLoss(String startingWeight, String weight)
    {
        String lossString = "0";
        if (startingWeight == null || startingWeight.equals(weight))
        {
            return lossString;
        }
        try
        {
            double start = Double.valueOf(startingWeight);
            double current = Double.valueOf(weight);
            double loss = start - current;
            lossString = String.format("%3.1f", loss);
        }
        catch(Exception e){
            Log.d(TAG, "Error calculating loss", e);
        }
        return lossString;
    }

    public static String calculatePercentage(String startingWeight, String loss)
    {
        String pctString = "0";
        if (startingWeight == null)
        {
            return pctString;
        }
        try
        {
            double start = Double.valueOf(startingWeight);
            double amount = Double.valueOf(loss);
            double pct = amount/start;
            pctString = String.format("%3.2f", pct*100);;
        }
        catch(Exception e){
            Log.d(TAG, "Error calculating percentage", e);
        }
        return pctString;
    }

    public static String formatWeight(String w)
    {
        if (w == null || w.length() == 0)
            return "";
        return String.format("%9.1f", Double.valueOf(w));
    }

    public static boolean validate(String entry)
    {
        boolean validated = false;
        if (entry != null && entry.length() > 0)
        {
            validated = true;
        }
        return validated;
    }

    public static String getCellText(TableRow tableRow, int position)
    {
        String text = "";
        if (tableRow == null)
        {
            return text;
        }
        View view = tableRow.getChildAt(position);
        if (view != null && view instanceof TextView)
        {
            TextView textView = (TextView) view;
            text = textView.getText().toString();
        }

        return text;
    }

    public static void clearTableData(TableLayout tableLayout)
    {
        if (tableLayout != null)
        {
            int rows = tableLayout.getChildCount();
            tableLayout.removeViews(1, rows - 1);
        }
    }

    public static String getCurrentDate()
    {
        try
        {
            return Constants.DATE_FORMAT.format(Calendar.getInstance().getTime());
        } catch (Exception e){}

        return new String();
    }


}

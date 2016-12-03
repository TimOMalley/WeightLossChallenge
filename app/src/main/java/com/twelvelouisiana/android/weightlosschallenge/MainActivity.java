package com.twelvelouisiana.android.weightlosschallenge;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.view.ContextMenu;
import android.view.Gravity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.TableLayout;
import android.widget.TableRow;
import android.widget.TextView;

public class MainActivity extends Activity implements ActivityCallback {
    private static final int REQUEST_CODE_NEW = 1;

    private boolean alternateBackgroundColor = false;
    private int rowId = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        refreshFilelist();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {

        if (requestCode == REQUEST_CODE_NEW) {
            if(resultCode == Activity.RESULT_OK)
            {
//                String result = data.getStringExtra("filename");
                refreshFilelist();
            }
            else if (resultCode == Activity.RESULT_CANCELED)
            {
                //Do something?
            }
        }
    }

    @Override
    public void sendData(String[] results) {
        if (results != null) {
            for (String result : results)
            {
                addTableRow(result);
            }
        }
    }

    @Override
    public void onCreateContextMenu(ContextMenu menu, View v, ContextMenu.ContextMenuInfo menuInfo)
    {
        super.onCreateContextMenu(menu, v, menuInfo);
        menu.setHeaderTitle(getString(R.string.menu_header_title));
        menu.add(0, v.getId(), 0, getString(R.string.delete_file));
        menu.add(0, v.getId(), 0, getString(R.string.update_file));
        menu.add(0, v.getId(), 0, getString(R.string.cancel));
    }

    @Override
    public boolean onContextItemSelected(MenuItem item)
    {
        if (item.getTitle().equals(getString(R.string.delete_file))) {
            removeRow(item.getItemId());
        } else if (item.getTitle().equals(getString(R.string.update_file))) {
            //resetData();
        } else {
            return false;
        }
        return true;
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu)
    {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item)
    {
        if (item.getTitle().equals(getString(R.string.menu_new)))
        {
            Intent intent = new Intent(this, WeightLossChallengeActivity.class);
            startActivityForResult(intent, REQUEST_CODE_NEW);
        }
        else
        {
            return false;
        }

        return true;
    }

    private void addTableRow(String filename)
    {
        if (filename != null)
        {
            TableLayout tl = (TableLayout) findViewById(R.id.tableLayout1);
            TableRow tr = new TableRow(this);
            if (alternateBackgroundColor)
            {
                tr.setBackgroundColor(Color.LTGRAY);
                alternateBackgroundColor = false;
            }
            else
            {
                alternateBackgroundColor = true;
            }
            TextView dateText = new TextView(this);
            dateText.setGravity(Gravity.LEFT);
            dateText.setText(filename);
            dateText.setLayoutParams(new TableRow.LayoutParams(TableRow.LayoutParams.MATCH_PARENT,
                    TableRow.LayoutParams.WRAP_CONTENT, 1f));
            tr.addView(dateText);


            tr.setId(++rowId);
            tl.addView(tr);
            registerForContextMenu(tr);
        }
    }

    private void removeRow(int id)
    {
        TableLayout tl = (TableLayout) findViewById(R.id.tableLayout1);
        TableRow tr = null;

        // Loop through the table to find he row
        for (int i = 1; i < tl.getChildCount(); i++)
        {
            View view = tl.getChildAt(i);
            if (view.getId() == id)
                tr = (TableRow) view;
        }
        if (tr != null)
        {
            // Remove from table
            tl.removeView(tr);

            // Remove  file
            // TODO

            // alternateBackgroundColor
            resetAlternateBackgroundColor(tl);
        }
    }

    private void resetAlternateBackgroundColor(TableLayout tableLayout)
    {
        alternateBackgroundColor = false;
        for (int i = 1; i < tableLayout.getChildCount(); i++)
        {
            View view = tableLayout.getChildAt(i);
            if (alternateBackgroundColor)
            {
                view.setBackgroundColor(Color.LTGRAY);
                alternateBackgroundColor = false;
            }
            else
            {
                view.setBackgroundColor(Color.WHITE);
                alternateBackgroundColor = true;
            }
        }
    }

    private void refreshFilelist()
    {
        FileListAsyncTask task = new FileListAsyncTask(this);
        task.execute();
    }
}

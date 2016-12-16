package com.twelvelouisiana.android.weightlosschallenge;

import android.app.ActionBar;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.DatePickerDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.support.v4.app.FragmentActivity;
import android.util.Log;
import android.view.ContextMenu;
import android.view.ContextMenu.ContextMenuInfo;
import android.view.Gravity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.RelativeLayout;
import android.widget.TableLayout;
import android.widget.TableRow;
import android.widget.TableRow.LayoutParams;
import android.widget.TextView;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;

public class WeightLossChallengeActivity extends FragmentActivity implements ActivityCallback
{
    private static final String TAG = WeightLossChallengeActivity.class.getName();
    private static final SimpleDateFormat DATE_FORMAT = new SimpleDateFormat("MM/dd/yyyy");

	private String _startingWeight = null;
	private EditText _editText1 = null;
	private EditText _editText2 = null;
	private boolean alternateBackgroundColor = false;
	private int rowId = 0;
	private boolean modified = false;
    EditText editTextFilename;
	
	@Override
	protected void onCreate(Bundle savedInstanceState)
	{
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_challenge);

        ActionBar actionBar = getActionBar();
        String filename = getIntent().getStringExtra("filename");
        if (filename == null)
        {
            if (actionBar != null) {
                actionBar.setCustomView(R.layout.custom_actionbar_view);
                editTextFilename = (EditText) actionBar.getCustomView().findViewById(R.id.editTextFilename);
                actionBar.setDisplayOptions(ActionBar.DISPLAY_SHOW_CUSTOM);
                actionBar.setDisplayHomeAsUpEnabled(false);
            }
        }
        else
        {
            setTitle(filename);
            if (actionBar != null) {
                getActionBar().setDisplayShowHomeEnabled(false);
                getActionBar().setDisplayHomeAsUpEnabled(false);
            }
        }
		readChallengeFile(normalizeFilename(filename));
		addListenerOnDateText();
		addListenerOnButtonEnter();

	}

	@Override
	public void onCreateContextMenu(ContextMenu menu, View v, ContextMenuInfo menuInfo)
	{
		super.onCreateContextMenu(menu, v, menuInfo);
		menu.setHeaderTitle(getString(R.string.menu_header_title));
		menu.add(0, v.getId(), 0, getString(R.string.remove_row));
		menu.add(0, v.getId(), 0, getString(R.string.remove_all_data));
		menu.add(0, v.getId(), 0, getString(R.string.cancel));
	}

	@Override
	public boolean onContextItemSelected(MenuItem item)
	{
		if (item.getTitle().equals(getString(R.string.remove_row)))
        {
			if (removeRow(item.getItemId()))
            {
                // Update the file
                if (!modified)
                {
                    modified = true;
                }
            }
		}
        else if (item.getTitle().equals(getString(R.string.remove_all_data)))
        {
			resetData();
		}
        else
        {
			return false;
		}
		return true;
	}
	
	@Override
	public boolean onCreateOptionsMenu(Menu menu)
	{
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.menu_challange, menu);
        if (editTextFilename != null)
        {
            MenuItem item = menu.findItem(R.id.menu_delete);
            item.setEnabled(false);
        }
		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item)
	{
        switch (item.getItemId()) {
            case R.id.menu_back:
                onBackPressed();
                return true;
            case R.id.menu_cancel:
                goBack();
                return true;
            case R.id.menu_delete:
                if (editTextFilename == null) {
                    deleteChallengeFile(normalizeFilename(getTitle().toString()));
                }
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
	}
	
	@Override
	protected void onStop() {
		super.onStop();
		this.finish();
	}

	@Override
	public void onBackPressed()
	{
        if (modified)
        {
            String filename;
            if (editTextFilename == null)
            {
				filename = getTitle().toString();
            }
            else
            {
				filename = editTextFilename.getText().toString();
            }
            if (filename == null || filename.length() == 0)
            {
                showMessageAlert(null, getString(R.string.name_required));
            }
            else {
                writeChallengeFile(normalizeFilename(filename));
                goBack();
            }
        }
        else {
            goBack();
        }
	}

    @Override
    public void sendData(File[] results)
    {
        // Do nothing.
    }

    @Override
    public void sendData(int operation, String[] results)
    {
        switch (operation)
        {
            case Constants.FILE_READ:
                loadData(results);
                break;
            case Constants.FILE_DELETE:
                goBack();
                break;
            default:
                break;
        }
    }

    private void goBack()
    {
        Intent returnIntent = new Intent();
        setResult(Activity.RESULT_OK, returnIntent);
        this.finish();
    }

    private String normalizeFilename(String name)
    {
        if (name == null || name.endsWith(Constants.DATA_FILENAME_EXT))
        {
            return name;
        }
        return name + Constants.DATA_FILENAME_EXT;
    }

	private void readChallengeFile(String filename)
	{
		FileOperationsAsyncTask task = new FileOperationsAsyncTask(this, Constants.FILE_READ, filename);
		task.execute();
	}

	private void writeChallengeFile(String filename)
    {
        FileOperationsAsyncTask task = new FileOperationsAsyncTask(this, Constants.FILE_WRITE, filename);
        task.execute(getTableData());
    }

    private void deleteChallengeFile(String filename)
    {
        FileOperationsAsyncTask task = new FileOperationsAsyncTask(this, Constants.FILE_DELETE, filename);
        task.execute();
    }

    private void addListenerOnDateText()
	{
		_editText1 = (EditText) findViewById(R.id.editText1);
		_editText1.setOnClickListener(new DateTextOnClickListener());
	}

	private void addListenerOnButtonEnter()
	{
		Button button = (Button) findViewById(R.id.buttonEnter);
		button.setOnClickListener(new EnterButtonOnClickListener());
	}
	
	private String calculateLoss(String weight)
	{
		String lossString = "0";
		if (_startingWeight == null || _startingWeight.equals(weight))
		{
			return lossString;
		}
		try
		{
			double start = Double.valueOf(_startingWeight);
			double current = Double.valueOf(weight);
			double loss = start - current;
			lossString = String.format("%3.1f", loss);
		}
		catch(Exception e){
            Log.d(TAG, "Error calculating loss", e);
        }
		return lossString;
	}
	
	private String calculatePercentage(String loss)
	{
		String pctString = "0";
		if (_startingWeight == null)
		{
			return pctString;
		}
		try
		{
			double start = Double.valueOf(_startingWeight);
			double amount = Double.valueOf(loss);
			double pct = amount/start;
			pctString = String.format("%3.2f", pct*100);;
		}
		catch(Exception e){
            Log.d(TAG, "Error calculating percentage", e);
        }
		return pctString;
	}
	
	private boolean validate(String entry)
	{
		boolean validated = false;
		if (entry != null && entry.length() > 0)
		{
			validated = true;
		}
		return validated;
	}

    private String[] getTableData()
    {
        ArrayList<String> rows = new ArrayList<String>();
        TableLayout tableLayout = (TableLayout) findViewById(R.id.tableLayout1);
        if (tableLayout != null) {
            for (int i = 1; i < tableLayout.getChildCount(); i++) // skip header row
            {
                TableRow tableRow = (TableRow) tableLayout.getChildAt(i);
                StringBuilder row = new StringBuilder();
                row.append(getCellText(tableRow, 0));
                row.append(";");
                row.append(getCellText(tableRow, 1));
                row.append(";");
                row.append(getCellText(tableRow, 2));
                row.append(";");
                row.append(getCellText(tableRow, 3));
                rows.add(row.toString());
            }
        }

        return rows.toArray(new String[rows.size()]);
    }

    private String getCellText(TableRow tableRow, int position)
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

    private void loadData(String[] lines)
    {
        if (lines == null || lines.length == 0)
        {
            Log.d(TAG, "No data to load");
            return;
        }
        try
        {
            for (String line : lines)
            {
                String[] row = line.split(";");
                if (row.length == 4)
                {
                    String date = row[0];
                    String weight = row[1];
                    String loss = row[2];
                    String pct = row[3];
                    setStartingWeight(weight);
                    addTableRow(date, weight, loss, pct, false);
                }
            }
        }
        catch (Exception e)
        {
            Log.e(TAG, "Error loading table data", e);
        }
    }

    private boolean addTableRow(String date, String weight, String loss, String pct, boolean alert)
	{
        boolean added = false;
		if (validate(date) && validate(weight))
		{
			TableLayout tl = (TableLayout) findViewById(R.id.tableLayout1);
			TableRow tr = new TableRow(WeightLossChallengeActivity.this);
			if (alternateBackgroundColor)
			{
				tr.setBackgroundColor(Color.LTGRAY);
				alternateBackgroundColor = false;
			}
			else
			{
				alternateBackgroundColor = true;
			}
			TextView dateText = new TextView(WeightLossChallengeActivity.this);
			dateText.setGravity(Gravity.CENTER_HORIZONTAL);
			dateText.setText(date);
			dateText.setLayoutParams(new LayoutParams(LayoutParams.MATCH_PARENT,
					LayoutParams.WRAP_CONTENT, 1f));
			tr.addView(dateText);
			
			TextView weightText = new TextView(WeightLossChallengeActivity.this);
			weightText.setGravity(Gravity.CENTER_HORIZONTAL);
			weightText.setText(weight);
			weightText.setLayoutParams(new LayoutParams(LayoutParams.MATCH_PARENT,
					LayoutParams.WRAP_CONTENT, 1f));
			tr.addView(weightText);
			
			TextView lossText = new TextView(WeightLossChallengeActivity.this);
			lossText.setGravity(Gravity.CENTER_HORIZONTAL);
			lossText.setText(loss);
			lossText.setLayoutParams(new LayoutParams(LayoutParams.MATCH_PARENT,
					LayoutParams.WRAP_CONTENT, 1f));
			tr.addView(lossText);
			
			TextView pctText = new TextView(WeightLossChallengeActivity.this);
			pctText.setGravity(Gravity.CENTER_HORIZONTAL);
			pctText.setText(pct);
			pctText.setLayoutParams(new LayoutParams(LayoutParams.MATCH_PARENT,
					LayoutParams.WRAP_CONTENT, 1f));
			tr.addView(pctText);
			tr.setId(++rowId);
			tl.addView(tr);
            added = true;
			registerForContextMenu(tr);
		}
		else
		{
			if (alert) {
                showMessageAlert(getString(R.string.title_invalid), getString(R.string.invalid_message));
            }
		}
        return added;
	}
	
	private boolean removeRow(int id)
	{
        boolean removed = true;
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
            removed = true;
			
			// alternateBackgroundColor
			resetAlternateBackgroundColor(tl);
		}
        return removed;
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
	
	private void resetData()
	{
		try
		{
			// Clear initial setting
			_startingWeight = null;
			alternateBackgroundColor = false;

			// Clear Table
			TableLayout tl = (TableLayout) findViewById(R.id.tableLayout1);
			int rows = tl.getChildCount();
			tl.removeViews(1, rows-1);
		}
		catch (Exception e)
		{
			showExceptionAlert(e);
		}
	}

	private void showExceptionAlert(Exception e)
	{
		String message = e.getMessage();
		if (message == null || message.length() == 0)
			message = e.toString();
		showMessageAlert("Error", message);
	}
	
	private void showMessageAlert(String title, String message)
	{
		AlertDialog.Builder dialog = new AlertDialog.Builder(this);
        if (title != null) {
            dialog.setTitle(title);
        }
        dialog.setMessage(message);
        dialog.setNeutralButton("Ok", null);
        dialog.create().show();
	}
	
	private void setStartingWeight(String weight)
	{
		if (_startingWeight == null)
		{
			_startingWeight = weight;
		}
	}

	private String formatWeight(String w)
	{
		if (w == null || w.length() == 0)
			return "";
		return String.format("%9.1f", Double.valueOf(w));
	}

	public static class DatePickerDialogFragment extends DialogFragment implements	DatePickerDialog.OnDateSetListener
	{
		@Override
		public Dialog onCreateDialog(Bundle savedInstanceState) {
			final Calendar calendar = Calendar.getInstance();
			int yy = calendar.get(Calendar.YEAR);
			int mm = calendar.get(Calendar.MONTH);
			int dd = calendar.get(Calendar.DAY_OF_MONTH);
			return new DatePickerDialog(getActivity(), this, yy, mm, dd);
		}

		public void onDateSet(DatePicker view, int yy, int mm, int dd) {
			EditText editText1 = (EditText) getActivity().findViewById(R.id.editText1);
            String dateText = String.format("%02d/%02d/%d", mm + 1, dd, yy);
            editText1.setText(dateText);
		}
	}

    class DateTextOnClickListener implements OnClickListener
	{
		@Override
        public void onClick(View v)
		{
			DialogFragment newFragment = new DatePickerDialogFragment();
			newFragment.show(getSupportFragmentManager(), "DatePickerDialog");
        }
	}

	class EnterButtonOnClickListener implements OnClickListener
	{
		public void onClick(View v)
		{
			_editText1 = (EditText) findViewById(R.id.editText1);
			_editText2 = (EditText) findViewById(R.id.editText2);
			
			String date = _editText1.getText().toString();
			if (date.length() == 0)
				return;
			
			String weight = formatWeight(_editText2.getText().toString());
			if (weight.length() == 0)
				return;
			
			setStartingWeight(weight);
			String loss = calculateLoss(weight);
			String pct = calculatePercentage(loss);
			if (addTableRow(date, weight, loss, pct, true))
            {
                // Update the file
                if (!modified) {
                    modified = true;
                }
            }

			// Clear text areas
			_editText1.setText("");
			_editText2.setText("");
			
			// Give focus back to the layout.
			RelativeLayout mainLayout = (RelativeLayout)findViewById(R.id.mainLayout);
			mainLayout.requestFocus();
			
			// Remove the keyboard.
			InputMethodManager imm = (InputMethodManager)getSystemService(Context.INPUT_METHOD_SERVICE);
			imm.hideSoftInputFromWindow(mainLayout.getWindowToken(), 0);
		}
	}

}

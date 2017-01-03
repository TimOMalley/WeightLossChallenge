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
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Date;

public class WeightLossChallengeActivity extends FragmentActivity implements ActivityCallback
{
    private static final String TAG = WeightLossChallengeActivity.class.getName();

	private String _startingWeight;
    private String _startDate;
    private RelativeLayout _mainLayout;
	private EditText _editTextDate;
	private EditText _editTextWeight;
    private EditText _editTextFilename;
    private TableLayout _tableLayoutResults;
	private boolean _alternateBackgroundColor = false;
	private int _rowId = 0;
	private boolean _modified = false;
	
	@Override
	protected void onCreate(Bundle savedInstanceState)
	{
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_challenge);

        ActionBar actionBar = getActionBar();
        String filename = getIntent().getStringExtra(Constants.FILENAME_KEY);
        if (filename == null)
        {
            if (actionBar != null) {
                actionBar.setCustomView(R.layout.custom_actionbar_view);
                _editTextFilename = (EditText) actionBar.getCustomView().findViewById(R.id.editTextFilename);
                actionBar.setDisplayOptions(ActionBar.DISPLAY_SHOW_CUSTOM | ActionBar.DISPLAY_HOME_AS_UP);
            }
        }
        else
        {
            if (actionBar != null)
            {
                actionBar.setDisplayHomeAsUpEnabled(true);
                actionBar.setDisplayShowHomeEnabled(false);
            }
            setTitle(filename);
        }
        if (savedInstanceState == null)
        {
            readChallengeFile(normalizeFilename(filename));
        }

        _mainLayout = (RelativeLayout)findViewById(R.id.mainLayout);
        _editTextDate = (EditText) findViewById(R.id.editTextDate);
        _editTextWeight = (EditText) findViewById(R.id.editTextWeight);
        _tableLayoutResults = (TableLayout) findViewById(R.id.tableLayoutResults);

        addListenerOnDateText();
		addListenerOnButtonEnter();
        setCurrentDate();
	}

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        if (_editTextFilename == null)
        {
            outState.putString("filename", getTitle().toString());
        }
        else
        {
            outState.putString("filename", _editTextFilename.getText().toString());
        }
        outState.putStringArray("data", getTableData(_tableLayoutResults));
        outState.putBoolean("modified", _modified);
        outState.putInt("rowId", _rowId);
    }

    @Override
    protected void onRestoreInstanceState(Bundle savedInstanceState) {
        super.onRestoreInstanceState(savedInstanceState);
        if (savedInstanceState != null)
        {
            String[] data = savedInstanceState.getStringArray("data");
            if (data != null)
            {
                loadTableData(data);
            }
            _modified = savedInstanceState.getBoolean("modified");
            _rowId = savedInstanceState.getInt("rowId");
        }
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
			if (removeRow(_tableLayoutResults, item.getItemId()))
            {
                // Update the file
                if (!_modified)
                {
                    _modified = true;
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
		getMenuInflater().inflate(R.menu.menu_challange, menu);
        if (_editTextFilename != null)
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
            case android.R.id.home:
            case R.id.menu_back:
                onBackPressed();
                return true;
            case R.id.menu_cancel:
                goBack();
                return true;
            case R.id.menu_delete:
                if (_editTextFilename == null) {
                    deleteChallengeFile(normalizeFilename(getTitle().toString()));
                }
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
	}
	
	@Override
	public void onBackPressed()
	{
        if (_modified)
        {
            String filename;
            if (_editTextFilename == null)
            {
				filename = getTitle().toString();
            }
            else
            {
				filename = _editTextFilename.getText().toString();
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
                loadTableData(results);
                break;
            case Constants.FILE_DELETE:
                goBack();
                break;
            default:
                break;
        }
    }

    @Override
    public void sendText(String text)
    {
        if (_editTextDate != null)
        {
            _editTextDate.setText(text);
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
        task.execute(getTableData(_tableLayoutResults));
    }

    private void deleteChallengeFile(String filename)
    {
        FileOperationsAsyncTask task = new FileOperationsAsyncTask(this, Constants.FILE_DELETE, filename);
        task.execute();
    }

    private void addListenerOnDateText()
	{
		_editTextDate.setOnClickListener(new DateTextOnClickListener());
	}

	private void addListenerOnButtonEnter()
	{
		Button button = (Button) findViewById(R.id.buttonEnter);
		button.setOnClickListener(new EnterButtonOnClickListener());
	}

    private void setCurrentDate()
    {
        if (_editTextDate == null)
        {
            return;
        }

        if (_editTextDate.getText().length() == 0)
        {
            _editTextDate.setText(Utilities.getCurrentDate());
        }
    }
	
	private String[] getTableData(TableLayout tableLayout)
    {
        ArrayList<String> rows = new ArrayList<String>();
        if (tableLayout != null) {
            for (int i = 1; i < tableLayout.getChildCount(); i++) // skip header row
            {
                TableRow tableRow = (TableRow) tableLayout.getChildAt(i);
                StringBuilder row = new StringBuilder();
                row.append(Utilities.getCellText(tableRow, 0));
                row.append(";");
                row.append(Utilities.getCellText(tableRow, 1));
                row.append(";");
                row.append(Utilities.getCellText(tableRow, 2));
                row.append(";");
                row.append(Utilities.getCellText(tableRow, 3));
                rows.add(row.toString());
            }
        }

        return rows.toArray(new String[rows.size()]);
    }

    private void loadTableData(String[] lines)
    {
        loadTableData(lines, false);
    }

    private void loadTableData(String[] lines, boolean reset)
    {
        if (lines == null || lines.length == 0)
        {
            Log.d(TAG, "No data to load");
            return;
        }
        try
        {
            // Clear data rows
            Utilities.clearTableData(_tableLayoutResults);

            for (String line : lines)
            {
                String[] row = line.split(";");
                if (row.length == 4)
                {
                    String date = row[0];
                    String weight = row[1];
                    setStartValues(date, weight);
                    String loss = row[2];
                    String pct = row[3];
                    if (reset)
                    {
                        // Calculate new loss and percentage values on a reset.
                        loss = Utilities.calculateLoss(_startingWeight, weight);
                        pct = Utilities.calculatePercentage(_startingWeight, loss);
                    }
                    addTableRow(_tableLayoutResults, date, weight, loss, pct, false);
                }
            }
        }
        catch (Exception e)
        {
            Log.e(TAG, "Error loading table data", e);
        }
    }

    private boolean addTableRow(TableLayout tableLayout, String date, String weight, String loss, String pct, boolean alert)
	{
        boolean added = false;
		if (Utilities.validate(date) && Utilities.validate(weight))
		{
			TableRow tableRow = new TableRow(WeightLossChallengeActivity.this);
			if (_alternateBackgroundColor)
			{
				tableRow.setBackgroundColor(Color.LTGRAY);
				_alternateBackgroundColor = false;
			}
			else
			{
				_alternateBackgroundColor = true;
			}
			TextView dateText = new TextView(WeightLossChallengeActivity.this);
			dateText.setGravity(Gravity.CENTER_HORIZONTAL);
			dateText.setText(date);
			dateText.setLayoutParams(new LayoutParams(LayoutParams.MATCH_PARENT,
					LayoutParams.WRAP_CONTENT, 1f));
			tableRow.addView(dateText);
			
			TextView weightText = new TextView(WeightLossChallengeActivity.this);
			weightText.setGravity(Gravity.CENTER_HORIZONTAL);
			weightText.setText(weight);
			weightText.setLayoutParams(new LayoutParams(LayoutParams.MATCH_PARENT,
					LayoutParams.WRAP_CONTENT, 1f));
			tableRow.addView(weightText);
			
			TextView lossText = new TextView(WeightLossChallengeActivity.this);
			lossText.setGravity(Gravity.CENTER_HORIZONTAL);
			lossText.setText(loss);
			lossText.setLayoutParams(new LayoutParams(LayoutParams.MATCH_PARENT,
					LayoutParams.WRAP_CONTENT, 1f));
			tableRow.addView(lossText);
			
			TextView pctText = new TextView(WeightLossChallengeActivity.this);
			pctText.setGravity(Gravity.CENTER_HORIZONTAL);
			pctText.setText(pct);
			pctText.setLayoutParams(new LayoutParams(LayoutParams.MATCH_PARENT,
					LayoutParams.WRAP_CONTENT, 1f));
			tableRow.addView(pctText);
			tableRow.setId(++_rowId);
			tableLayout.addView(tableRow);
            added = true;
			registerForContextMenu(tableRow);
		}
		else
		{
			if (alert) {
                showMessageAlert(getString(R.string.title_invalid), getString(R.string.invalid_message));
            }
		}
        return added;
	}
	
	private boolean removeRow(TableLayout tableLayout, int id)
	{
        boolean removed = true;
		TableRow tableRow = null;

        if (tableLayout != null)
        {
            // Loop through the table to find the row
            for (int i = 1; i < tableLayout.getChildCount(); i++)
            {
                View view = tableLayout.getChildAt(i);
                if (view.getId() == id)
                    tableRow = (TableRow) view;
            }
            if (tableRow != null)
            {
                // Remove from table
                tableLayout.removeView(tableRow);
                removed = true;

                resetAlternateBackgroundColor(tableLayout);
            }
        }
        return removed;
	}

    private void resetAlternateBackgroundColor(TableLayout tableLayout)
	{
		_alternateBackgroundColor = false;
		for (int i = 1; i < tableLayout.getChildCount(); i++)
		{
			View view = tableLayout.getChildAt(i);
			if (_alternateBackgroundColor)
			{
				view.setBackgroundColor(Color.LTGRAY);
				_alternateBackgroundColor = false;
			}
			else
			{
				view.setBackgroundColor(Color.TRANSPARENT);
				_alternateBackgroundColor = true;
			}
		}
	}
	
	private void resetData()
	{
		try
		{
			// Clear initial setting
			_startingWeight = null;
			_alternateBackgroundColor = false;
            _rowId = 0;

			// Clear Table
			Utilities.clearTableData(_tableLayoutResults);

            if(!_modified)
            {
                _modified = true;
            }
		}
		catch (Exception e)
		{
			showExceptionAlert(e);
		}
	}

    private void resetTable()
    {
        // Get current rows
        String[] rows = getTableData(_tableLayoutResults);
        // Sort by date
        Arrays.sort(rows, new TableRowComparator());
        // Add new rows to table
        loadTableData(rows, true);
    }

	private void showExceptionAlert(Exception e)
	{
		String message = e.getMessage();
		if (message == null || message.length() == 0)
			message = e.toString();
		showMessageAlert(getString(R.string.error_text), message);
	}
	
	private void showMessageAlert(String title, String message)
	{
		AlertDialog.Builder dialog = new AlertDialog.Builder(this);
        if (title != null) {
            dialog.setTitle(title);
        }
        dialog.setMessage(message);
        dialog.setNeutralButton(R.string.ok_text, null);
        dialog.create().show();
	}

    private void setStartValues(String date, String weight)
    {
        // Start Date
        if (_startDate == null)
        {
            _startDate = date;
        }

        // Start Weight
        if (_startingWeight == null)
        {
            _startingWeight = weight;
        }
    }

   private boolean checkStartDate(String date, String weight)
    {
        if (date == null || weight == null)
        {
            return false;
        }

        boolean weightChanged = false;
        try
        {
            Date newDate = Constants.DATE_FORMAT.parse(date);
            Date startDate = Constants.DATE_FORMAT.parse(_startDate);
            if (newDate.before(startDate))
            {
                _startDate = date;
                _startingWeight = weight;
                weightChanged = true;
            }
        }
        catch (Exception e)
        {
            Log.d(TAG, "Error comparing start date.", e);
        }
        return weightChanged;
    }

    public static class DatePickerDialogFragment extends DialogFragment implements	DatePickerDialog.OnDateSetListener
	{
        private ActivityCallback activityCallback;

		@Override
		public Dialog onCreateDialog(Bundle savedInstanceState) {
			final Calendar calendar = Calendar.getInstance();
			int yy = calendar.get(Calendar.YEAR);
			int mm = calendar.get(Calendar.MONTH);
			int dd = calendar.get(Calendar.DAY_OF_MONTH);
			return new DatePickerDialog(getActivity(), this, yy, mm, dd);
		}

        @Override
        public void onAttach(Activity activity) {
            super.onAttach(activity);
            if (activity instanceof ActivityCallback)
            {
                activityCallback = (ActivityCallback) activity;
            }
        }

        public void onDateSet(DatePicker view, int yy, int mm, int dd) {
			String dateText = String.format("%02d/%02d/%d", mm + 1, dd, yy);
            if (activityCallback == null)
            {
                EditText editText1 = (EditText) getActivity().findViewById(R.id.editTextDate);
                editText1.setText(dateText);
            }
            else
            {
                activityCallback.sendText(dateText);
            }
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
			String date = _editTextDate.getText().toString();
			if (date.length() == 0)
				return;
			
			String weight = Utilities.formatWeight(_editTextWeight.getText().toString());
			if (weight.length() == 0)
				return;

            setStartValues(date, weight);
			boolean startValuesChanged = checkStartDate(date, weight);
			String loss = Utilities.calculateLoss(_startingWeight, weight);
			String pct = Utilities.calculatePercentage(_startingWeight, loss);
            if (addTableRow(_tableLayoutResults, date, weight, loss, pct, true))
            {
                // Update the file
                if (!_modified) {
                    _modified = true;
                }
            }

            if (startValuesChanged)
            {
                resetTable();
            }

			// Clear text areas
			_editTextDate.setText("");
			_editTextWeight.setText("");
            setCurrentDate();
			
			// Give focus back to the layout.
			_mainLayout.requestFocus();
			
			// Remove the keyboard.
			InputMethodManager imm = (InputMethodManager)getSystemService(Context.INPUT_METHOD_SERVICE);
			imm.hideSoftInputFromWindow(_mainLayout.getWindowToken(), 0);
		}
	}

}

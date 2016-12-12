package com.twelvelouisiana.android.weightlosschallenge;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.DatePickerDialog;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.v4.app.DialogFragment;
import android.support.v4.app.FragmentActivity;
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

public class WeightLossChallengeActivity extends FragmentActivity implements ActivityCallback
{
	public static final String DATA_FILENAME_PREFIX = "wlc_";
    public static final String DATA_FILENAME_EXT = ".dat";
	private static final SimpleDateFormat DATE_FORMAT = new SimpleDateFormat("mm/dd/yyyy");

	private String _startingWeight = null;
	private Date _startDate = null;
	private EditText _editText1 = null;
	private EditText _editText2 = null;
	private boolean alternateBackgroundColor = false;
	private int rowId = 0;
	private FileManager _fileManager = null;
	private ProgressDialog _progressDialog;
	private Executor _executor = null;
	private boolean modified = false;
    private String filename;
	
	@Override
	protected void onCreate(Bundle savedInstanceState)
	{
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_challenge);

        filename = getIntent().getStringExtra("filename");
        if (filename == null)
        {
            filename = generateFilename();
        }
		_fileManager = new FileManager(getApplication(), filename);
		_progressDialog = new ProgressDialog(this);
        _progressDialog.setMessage(getString(R.string.progress_message));
        _executor = Executors.newSingleThreadExecutor();
		loadFile();
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
		if (item.getTitle().equals(getString(R.string.remove_row))) {
			removeRow(item.getItemId());
		} else if (item.getTitle().equals(getString(R.string.remove_all_data))) {
			resetData();
		} else {
			return false;
		}
		return true;
	}
	
	public Dialog getFailedAlertDialog()
	{
		return new AlertDialog.Builder(WeightLossChallengeActivity.this)
        .setTitle(R.string.alert_dialog_failed)
        .setNeutralButton(R.string.alert_dialog_ok, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int whichButton) {
            	return;
            }
        })
        .create();
	}
	
	@Override
	public boolean onCreateOptionsMenu(Menu menu)
	{
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.menu_challange, menu);
		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item)
	{
		if (item.getTitle().equals(getString(R.string.menu_back)))
		{
			onBackPressed();
		}
        else if (item.getTitle().equals(getString(R.string.menu_delete)))
        {
            deleteFile();
        }
		else
		{
			return false;
		}

		return true;
	}
	
	@Override
	protected void onStop() {
		super.onStop();
		this.finish();
	}

	@Override
	public void onBackPressed()
	{
		Intent returnIntent = new Intent();
		returnIntent.putExtra("filename", _fileManager.getFilename());
		setResult(Activity.RESULT_OK, returnIntent);
		this.finish();
	}

    @Override
    public void sendData(File[] results) {
        //TODO
    }

    @Override
    public void sendData(String[] results) {
        if (results == null)
        {
            onBackPressed();
        }
    }

    private String generateFilename()
    {
        SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMddHHmmss");
        String currentTime = sdf.format(Calendar.getInstance().getTime());
        return DATA_FILENAME_PREFIX + currentTime + DATA_FILENAME_EXT;
    }

	private void loadFile()
	{
		try
		{
			List<String> lines = _fileManager.readFile();
			for (String line : lines)
			{
				String[] row = line.split(";");
				if (row.length == 4)
				{
					String date = row[0];
					String weight = row[1];
					String loss = row[2];
					String pct = row[3];
					setStartingWeight(date, weight);
					addTableRow(date, weight, loss, pct, false);
				}
			}
		}
		catch (Exception e)
		{
			e.printStackTrace();
		}
	}

    private void deleteFile()
    {
        FileOperationsAsyncTask task = new FileOperationsAsyncTask(this, Constants.FILE_DELETE);
        task.execute(filename);
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
		catch(Exception e){}
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
		catch(Exception e){}
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
	
	private void addTableRow(String date, String weight, String loss, String pct, boolean alert)
	{
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
			pctText.setText(pct + "%");
			pctText.setLayoutParams(new LayoutParams(LayoutParams.MATCH_PARENT,
					LayoutParams.WRAP_CONTENT, 1f));
			tr.addView(pctText);
			tr.setId(++rowId);
			tl.addView(tr);
			registerForContextMenu(tr);
		}
		else
		{
			if (alert)
				showMessageAlert("Invalid Entry", "Entry not added to table.");
			return;
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
			
			// Remove from file
			_fileManager.removeFileRow(tr);
			
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
	
	private void resetData()
	{
		try
		{
			// Clear initial setting
			_startDate = null;
			_startingWeight = null;
			alternateBackgroundColor = false;

			// Clear Table
			TableLayout tl = (TableLayout) findViewById(R.id.tableLayout1);
			int rows = tl.getChildCount();
			tl.removeViews(1, rows-1);

			// Delete File
			_fileManager.deleteDataFile();
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
        dialog.setTitle(title);
        dialog.setMessage(message);
        dialog.setNeutralButton("Ok", null);
        dialog.create().show();
	}
	
	public void showDatePickerDialog(View view)
	{
		DialogFragment newFragment = new DatePickerDialogFragment();
		newFragment.show(getSupportFragmentManager(), "DatePickerDialog");
	}
	
	private void setStartingWeight(String date, String weight)
	{
		if (_startingWeight == null)
		{
			_startingWeight = weight;
			return;
		}
		try
		{
			Date newDate = DATE_FORMAT.parse(date);
			if (newDate.before(_startDate))
			{
				_startDate = newDate;
				_startingWeight = weight;
			}
		}
		catch (Exception e)
		{
			return;
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
			
			if (_startingWeight == null)
				_startingWeight = weight;
			String loss = calculateLoss(weight);
			String pct = calculatePercentage(loss);
			addTableRow(date, weight, loss, pct, true);
			
			_progressDialog.show();
			
			_executor.execute(new AddEntryToFile(date, weight, loss, pct));

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
	
	private Handler handler = new Handler()
	{
		@Override
    	public void handleMessage(Message msg)
		{
			_progressDialog.dismiss();
		}
	};
	
	class AddEntryToFile implements Runnable
	{
		String m_date;
		String m_weight;
		String m_loss;
		String m_pct;
		
		public AddEntryToFile(String date, String weight, String loss, String pct)
		{
			m_date = date;
			m_weight = weight;
			m_loss = loss;
			m_pct = pct;
		}

		public void run()
		{
			try
			{
				_fileManager.addFileRow(m_date, m_weight, m_loss, m_pct);	
			}
			catch (Exception e)
			{
				e.printStackTrace();
			}
			finally
			{
				handler.sendEmptyMessage(0);
			}
		}
	}

}

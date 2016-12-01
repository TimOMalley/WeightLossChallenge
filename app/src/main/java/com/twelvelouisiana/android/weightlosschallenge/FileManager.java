package com.twelvelouisiana.android.weightlosschallenge;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;

import android.content.Context;
import android.widget.TableRow;
import android.widget.TextView;

public class FileManager
{
	public static String NEW_LINE = System.getProperty("line.separator");
	private String _filename = "weightloss.dat";
	private Context _context = null;
	
	public FileManager(Context context, String filename)
	{
		_context = context;
		_filename = filename;
	}

    public String getFilename() {
        return _filename;
    }

    public void addFileRow(String date, String weight, String loss, String pct) throws Exception
	{
		String fileRow = String.format("%s;%s;%s;%s" + NEW_LINE, date, weight, loss, pct);
		writeLineToFile(fileRow);
	}
	
	public void removeFileRow(TableRow tr)
	{
		// Just get the first 2 - Date & Weight
		if (tr.getChildCount() > 1)
		{
			StringBuilder sb = new StringBuilder();
			TextView tvDate = (TextView) tr.getChildAt(0);
			TextView tvWeight = (TextView) tr.getChildAt(1);
			sb.append(tvDate.getText());
			sb.append(";");
			sb.append(tvWeight.getText());
			removeFromFile(sb.toString());
		}
	}
	
	public List<String> readFile()
	{
		List<String> lines = new ArrayList<String>();
        File file = new File(_context.getFilesDir(), _filename);
        if (file.exists()) {
            InputStream inputStream = null;
            try {
                inputStream = _context.openFileInput(_filename);
                InputStreamReader inputStreamReader = new InputStreamReader(inputStream);
                BufferedReader bufferedReader = new BufferedReader(inputStreamReader);
                String line;
                while ((line = bufferedReader.readLine()) != null) {
                    lines.add(line);
                }
            } catch (Exception e) {
                e.printStackTrace();
            } finally {
                try {
                    if (inputStream != null) {
                        inputStream.close();
                    }
                } catch (Exception e) {
                }
            }
        }
		return lines;
	}
	
	public void deleteDataFile()
	{
		File dir = _context.getFilesDir();
		File file = new File(dir, _filename);
		file.delete();
	}
	
	private void writeLineToFile(String line)
	{
		FileOutputStream fos = null;
		try
		{
			fos = _context.openFileOutput(_filename, Context.MODE_APPEND);
			fos.write(line.getBytes());
		}
		catch (Exception e)
		{
			e.printStackTrace();
		}
		finally
		{
		    try {
		    	if (fos !=null)
		    		fos.close();
		    } catch (Exception e) {}
		}
	}
	
	private void removeFromFile(String line2remove)
	{
		try
		{
			List<String> lines2keep = getLines2Keep(line2remove);
			
			// Delete the file
			deleteDataFile();
			
			for (String line2keep : lines2keep)
			{
				writeLineToFile(line2keep + NEW_LINE);
			}
		}
		catch (Exception e)
		{
			e.printStackTrace();
		}
	}
	
	private List<String> getLines2Keep(String line2remove)
	{
		List<String> lines2keep = new ArrayList<String>();
		List<String> lines = readFile();
		for (String line : lines)
		{
			if (!line.startsWith(line2remove))
			{
				lines2keep.add(line);
			}
		}
		return lines2keep;
	}
	
}

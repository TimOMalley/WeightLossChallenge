package com.twelvelouisiana.android.weightlosschallenge;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.TextView;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Locale;

public class MainActivity extends Activity implements ActivityCallback
{
    private SimpleDateFormat sdf = new SimpleDateFormat("MMM d", Locale.US);
    private ChallengeListAdapter listAdapter;
    private ArrayList<ChallengeItem> challengeList = new ArrayList<ChallengeItem>();

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        listAdapter = new ChallengeListAdapter(this.getApplicationContext(), challengeList);

        ListView listView = (ListView) findViewById(R.id.challengeList);
        listView.setAdapter(listAdapter);

        listView.setOnItemClickListener(new ListViewOnItemClickListener());

        refreshFilelist();
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
            startActivityForResult(intent, Constants.REQUEST_CODE_NEW);
        }
        else
        {
            return false;
        }

        return true;
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {

        if (requestCode == Constants.REQUEST_CODE_NEW) {
            refreshFilelist();
        }
    }

    @Override
    public void sendData(File[] results) {
        if (results != null) {
            challengeList.clear();
            for (File file : results) {
                Date date = new Date(file.lastModified());
                String lastModified = sdf.format(date);
                challengeList.add(new ChallengeItem(file.getName().replaceFirst(Constants.DATA_FILENAME_EXT, ""), lastModified));
            }
            listAdapter.notifyDataSetChanged();
        }
    }

    @Override
    public void sendData(int operatio, String[] results) {
        // Do nothing
    }

    @Override
    public void sendText(String text) {
        // Do nothing
    }

    private void refreshFilelist()
    {
        FileListAsyncTask task = new FileListAsyncTask(this);
        task.execute();
    }

    class ListViewOnItemClickListener implements AdapterView.OnItemClickListener
    {
        @Override
        public void onItemClick(AdapterView<?> adapterView, View view, int i, long l)
        {
            String name = ((TextView) view.findViewById(R.id.name)).getText().toString();
            Intent intent = new Intent(getApplicationContext(), WeightLossChallengeActivity.class);
            intent.putExtra(Constants.FILENAME_KEY, name);
            startActivityForResult(intent, Constants.REQUEST_CODE_NEW);
        }
    }
}

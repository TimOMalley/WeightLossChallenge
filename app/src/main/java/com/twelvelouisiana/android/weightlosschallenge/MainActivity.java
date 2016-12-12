package com.twelvelouisiana.android.weightlosschallenge;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListAdapter;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;

public class MainActivity extends Activity implements ActivityCallback {
    private static final int REQUEST_CODE_NEW = 1;

    private SimpleDateFormat sdf = new SimpleDateFormat("MMM d");
    private ChallengeListAdapter listAdapter;
    private ArrayList<ChallengeItem> challengeList = new ArrayList<ChallengeItem>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        listAdapter = new ChallengeListAdapter(this.getApplicationContext(), challengeList);

        ListView listView = (ListView) findViewById(R.id.challengeList);
        listView.setAdapter(listAdapter);

        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            public void onItemClick(AdapterView<?> parent, View view,
                                    int position, long id) {
                String name = ((TextView) view.findViewById(R.id.name)).getText().toString();
                Intent intent = new Intent(getApplicationContext(), WeightLossChallengeActivity.class);
                intent.putExtra("filename", name);
                startActivityForResult(intent, REQUEST_CODE_NEW);
            }
        });
        refreshFilelist();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {

        if (requestCode == REQUEST_CODE_NEW) {
            if(resultCode == Activity.RESULT_OK)
            {
//                String result = data.getStringExtra("filename");
//                if (result != null)
//                {
//                    arrayAdapter.add(result);
//                }
                refreshFilelist();
            }
            else if (resultCode == Activity.RESULT_CANCELED)
            {
                //Do something?
            }
        }
    }

    @Override
    public void sendData(File[] results) {
        if (results != null) {
            challengeList.clear();
            for (File file : results) {
                Date date = new Date(file.lastModified());
                String lastModified = sdf.format(date);
                challengeList.add(new ChallengeItem(file.getName(), lastModified));
            }
            listAdapter.notifyDataSetChanged();
        }
    }

    @Override
    public void sendData(String[] results) {
        if (results != null)
        {
            //TODO
        }
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

    private void refreshFilelist()
    {
        FileListAsyncTask task = new FileListAsyncTask(this);
        task.execute();
    }
}

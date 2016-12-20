package com.twelvelouisiana.android.weightlosschallenge;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import java.util.ArrayList;

/**
 * Extension of ArrayAdapter that uses the ViewHolder pattern.
 */

public class ChallengeListAdapter extends ArrayAdapter<ChallengeItem>
{
    public ArrayList<ChallengeItem> list;
    private Context context;

    public ChallengeListAdapter(Context context, ArrayList<ChallengeItem> list)
    {
        super(context, R.layout.challenge_list, list);
        this.context = context;
        this.list = list;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        ChallengeItem item = getItem(position);
        ViewHolder holder;
        if (convertView == null)
        {
            holder = new ViewHolder();
            LayoutInflater inflater = LayoutInflater.from(getContext());
            convertView = inflater.inflate(R.layout.challenge_list, parent, false);
            holder.name = (TextView) convertView.findViewById(R.id.name);
            holder.date = (TextView) convertView.findViewById(R.id.date);
            // Cache the ViewHolder object inside the view
            convertView.setTag(holder);
        } else {
            holder = (ViewHolder) convertView.getTag();
        }
        holder.name.setText(item.getName());
        holder.date.setText(item.getLastModifiedDate());

        return convertView;

    }

    private static class ViewHolder {
        TextView name;
        TextView date;
    }

}

package com.twelvelouisiana.android.weightlosschallenge;

/**
 * Interface to send results back to the activity.
 */

public interface ActivityCallback
{
    void sendData(String[] results);
}

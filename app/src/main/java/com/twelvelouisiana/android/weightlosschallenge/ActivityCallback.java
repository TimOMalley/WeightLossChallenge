package com.twelvelouisiana.android.weightlosschallenge;

import java.io.File;

/**
 * Interface to send results back to the activity.
 */

public interface ActivityCallback
{
    void sendData(File[] results);
    void sendData(String[] results);
}

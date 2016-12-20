package com.twelvelouisiana.android.weightlosschallenge;

import java.io.File;

/**
 * Interface to send results back to the activity.
 */

public interface ActivityCallback
{
    void sendData(File[] results);
    void sendData(int operation, String[] results);
    void sendText(String text);
}

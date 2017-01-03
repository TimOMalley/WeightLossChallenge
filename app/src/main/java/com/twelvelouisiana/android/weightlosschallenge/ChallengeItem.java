package com.twelvelouisiana.android.weightlosschallenge;

/**
 * Class to hold file information.
 */

public class ChallengeItem
{
    private String name;
    private String lastModifiedDate;

    public ChallengeItem(String name, String lastModifiedDate)
    {
        this.name = name;
        this.lastModifiedDate = lastModifiedDate;
    }

    public String getName() {
        return name;
    }

    public String getLastModifiedDate() {
        return lastModifiedDate;
    }

}

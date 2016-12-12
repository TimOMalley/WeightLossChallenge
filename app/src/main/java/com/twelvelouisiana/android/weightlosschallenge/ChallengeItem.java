package com.twelvelouisiana.android.weightlosschallenge;

/**
 * Created by tomal on 12/8/2016.
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

package com.example.android.Tubeddit.data;

import org.parceler.Parcel;

@Parcel
public class Gildings {

    public String mSilverCount;
    public String mGoldCount;
    public String mPlatinumCount;

    public Gildings() {}

    public Gildings(String silverCount, String goldCount, String platinumCount) {

        mSilverCount = silverCount;
        mGoldCount = goldCount;
        mPlatinumCount = platinumCount;
    }
}

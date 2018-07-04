package com.example.ckpcamsensorsignalgenerator;

import android.os.Parcel;
import android.os.Parcelable;

import java.util.ArrayList;

public class ArrToIntent implements Parcelable {
    private String [] frontsStringArr = new String [4];
    public ArrayList<String []> fronts;

    public ArrToIntent(ArrayList<String[]> i) {
        fronts = i;
    }

    @Override
    public int describeContents() {
        return hashCode();
    }

    public void writeToParcel(Parcel dest, int flags) {

        dest.writeStringArray(frontsStringArr);
        dest.writeInt(fronts.size());

        for (String[] array : fronts) {
            dest.writeStringArray(array);
        }
    };

    public static final Parcelable.Creator<ArrToIntent> CREATOR
            = new Parcelable.Creator<ArrToIntent>() {
        public ArrToIntent createFromParcel(Parcel in) {
            return new ArrToIntent(in);
        }

        public ArrToIntent[] newArray(int size) {
            return new ArrToIntent[size];
        }
    };

    /**
     * Specific constructor for Parcelable support
     * @param in
     */
    public ArrToIntent(Parcel in) {
        in.readStringArray(frontsStringArr);

        final int arraysCount = in.readInt();
        fronts = new ArrayList<String[]>(arraysCount);

        for (int i = 0; i < arraysCount; i++) {
            fronts.add(in.createStringArray());
        }
    }
}
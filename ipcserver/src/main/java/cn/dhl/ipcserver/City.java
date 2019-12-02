package cn.dhl.ipcserver;

import android.os.Parcel;
import android.os.Parcelable;

/**
 * Author: duanhl
 * Create: 2019-12-02 11:33
 * Description:
 */
public class City implements Parcelable {

    String name;
    String weather;

    public City() {

    }

    protected City(Parcel in) {
        name = in.readString();
        weather = in.readString();
    }

    public static final Creator<City> CREATOR = new Creator<City>() {
        @Override
        public City createFromParcel(Parcel in) {
            return new City(in);
        }

        @Override
        public City[] newArray(int size) {
            return new City[size];
        }
    };

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(name);
        dest.writeString(weather);
    }
}

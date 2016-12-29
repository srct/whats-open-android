package srct.whatsopen.model;

import com.google.gson.annotations.SerializedName;

import io.realm.RealmList;
import io.realm.RealmObject;
import io.realm.annotations.PrimaryKey;

public class Facility extends RealmObject {

    @PrimaryKey
    @SerializedName("name")
    private String mName;

    @SerializedName("location")
    private String mLocation;

    @SerializedName("main_schedule")
    private MainSchedule mMainSchedule;

    private boolean isFavorited;

    public String getName() {
        return mName;
    }

    public void setName(String name) {
        this.mName = name;
    }

    public boolean isFavorited() {
        return isFavorited;
    }

    public void setFavorited(boolean favorited) {
        isFavorited = favorited;
    }

    public String getLocation() {
        return mLocation;
    }

    public void setLocation(String location) {
        mLocation = location;
    }

    public MainSchedule getMainSchedule() {
        return mMainSchedule;
    }

    public void setMainSchedule(MainSchedule mainSchedule) {
        mMainSchedule = mainSchedule;
    }
}

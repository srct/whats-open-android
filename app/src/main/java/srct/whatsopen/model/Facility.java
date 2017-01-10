package srct.whatsopen.model;

import com.google.gson.annotations.SerializedName;

import io.realm.RealmList;
import io.realm.RealmObject;
import io.realm.annotations.PrimaryKey;

public class Facility extends RealmObject {

    public Facility(String name, String location, MainSchedule mainSchedule,
                    RealmList<SpecialSchedule> specialSchedules, boolean isOpen,
                    boolean isFavorited) {
        mName = name;
        mLocation = location;
        mMainSchedule = mainSchedule;
        mSpecialSchedules = specialSchedules;
        this.isOpen = isOpen;
        this.isFavorited = isFavorited;
    }

    public Facility() {
    }

    @PrimaryKey
    @SerializedName("name")
    private String mName;

    @SerializedName("location")
    private String mLocation;

    @SerializedName("main_schedule")
    private MainSchedule mMainSchedule;

    @SerializedName("special_schedules")
    private RealmList<SpecialSchedule> mSpecialSchedules;

    private boolean isOpen;
    private boolean isFavorited;

    public RealmList<SpecialSchedule> getSpecialSchedules() {
        return mSpecialSchedules;
    }

    public void setSpecialSchedules(RealmList<SpecialSchedule> specialSchedules) {
        mSpecialSchedules = specialSchedules;
    }

    public boolean isOpen() {

        return isOpen;
    }

    public void setOpen(boolean open) {
        isOpen = open;
    }

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

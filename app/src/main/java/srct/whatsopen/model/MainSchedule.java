package srct.whatsopen.model;

import com.google.gson.annotations.SerializedName;

import io.realm.RealmList;
import io.realm.RealmObject;

public class MainSchedule extends RealmObject {

    public MainSchedule(RealmList<OpenTimes> openTimesList) {
        mOpenTimesList = openTimesList;
    }

    public MainSchedule() {
    }

    @SerializedName("open_times")
    private RealmList<OpenTimes> mOpenTimesList;

    public RealmList<OpenTimes> getOpenTimesList() {
        return mOpenTimesList;
    }

    public void setOpenTimesList(RealmList<OpenTimes> openTimesList) {
        mOpenTimesList = openTimesList;
    }
}

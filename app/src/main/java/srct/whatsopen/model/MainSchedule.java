package srct.whatsopen.model;

import com.google.gson.annotations.SerializedName;

import io.realm.RealmList;
import io.realm.RealmObject;

public class MainSchedule extends RealmObject implements Schedule{

    public MainSchedule(RealmList<OpenTimes> openTimesList, String validStart, String validEnd) {
        mOpenTimesList = openTimesList;
        this.validStart = validStart;
        this.validEnd = validEnd;
    }

    public MainSchedule() {
    }

    @SerializedName("open_times")
    private RealmList<OpenTimes> mOpenTimesList;

    @SerializedName("valid_start")
    private String validStart;

    @SerializedName("valid_end")
    private String validEnd;

    public String getValidStart() {
        return validStart;
    }

    public void setValidStart(String validStart) {
        this.validStart = validStart;
    }

    public String getValidEnd() {
        return validEnd;
    }

    public void setValidEnd(String validEnd) {
        this.validEnd = validEnd;
    }

    public RealmList<OpenTimes> getOpenTimesList() {
        return mOpenTimesList;
    }

    public void setOpenTimesList(RealmList<OpenTimes> openTimesList) {
        mOpenTimesList = openTimesList;
    }
}

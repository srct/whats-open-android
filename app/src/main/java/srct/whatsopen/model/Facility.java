package srct.whatsopen.model;

import com.google.gson.annotations.SerializedName;

import io.realm.RealmList;
import io.realm.RealmObject;
import io.realm.annotations.PrimaryKey;

public class Facility extends RealmObject {

    @PrimaryKey
    private String name;

    private boolean isFavorited;
    private RealmList<OpenTimes> mOpenTimes;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public boolean isFavorited() {
        return isFavorited;
    }

    public void setFavorited(boolean favorited) {
        isFavorited = favorited;
    }

    public RealmList<OpenTimes> getOpenTimes() {
        return mOpenTimes;
    }

    public void setOpenTimes(RealmList<OpenTimes> openTimes) {
        this.mOpenTimes = openTimes;
    }
}

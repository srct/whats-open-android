package srct.whatsopen.model;


import io.realm.RealmList;

public interface Schedule {

    RealmList<OpenTimes> getOpenTimesList();

    String getValidEnd();
}

package srct.whatsopen.model;


import io.realm.RealmObject;
import io.realm.annotations.PrimaryKey;

public class NotificationSettings extends RealmObject {

    public NotificationSettings() {
    }

    public NotificationSettings(String name, boolean opening, boolean closing, boolean interval_on, boolean interval_15, boolean interval_30, boolean interval_hour) {
        this.name = name;
        this.opening = opening;
        this.closing = closing;
        this.interval_on = interval_on;
        this.interval_15 = interval_15;
        this.interval_30 = interval_30;
        this.interval_hour = interval_hour;
    }

    @PrimaryKey
    public String name;

    public boolean opening;
    public boolean closing;
    public boolean interval_on;
    public boolean interval_15;
    public boolean interval_30;
    public boolean interval_hour;

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof NotificationSettings)) return false;

        NotificationSettings that = (NotificationSettings) o;

        if (opening != that.opening) return false;
        if (closing != that.closing) return false;
        if (interval_on != that.interval_on) return false;
        if (interval_15 != that.interval_15) return false;
        if (interval_30 != that.interval_30) return false;
        return interval_hour == that.interval_hour;

    }

    @Override
    public int hashCode() {
        int result = (opening ? 1 : 0);
        result = 31 * result + (closing ? 1 : 0);
        result = 31 * result + (interval_on ? 1 : 0);
        result = 31 * result + (interval_15 ? 1 : 0);
        result = 31 * result + (interval_30 ? 1 : 0);
        result = 31 * result + (interval_hour ? 1 : 0);
        return result;
    }
}

package srct.whatsopen.model;


public class NotificationSettings {

    public NotificationSettings() {
    }

    public NotificationSettings(boolean opening, boolean closing, boolean interval_on, boolean interval_15, boolean interval_30, boolean interval_hour) {
        this.opening = opening;
        this.closing = closing;
        this.interval_on = interval_on;
        this.interval_15 = interval_15;
        this.interval_30 = interval_30;
        this.interval_hour = interval_hour;
    }

    public boolean opening;
    public boolean closing;
    public boolean interval_on;
    public boolean interval_15;
    public boolean interval_30;
    public boolean interval_hour;
}

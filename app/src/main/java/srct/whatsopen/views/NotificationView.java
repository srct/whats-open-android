package srct.whatsopen.views;


import android.content.Context;

public interface NotificationView {

    public Context getContext();

    public void setNotificationChecks(boolean opening, boolean closing,
                                      boolean intervalOn, boolean interval_15,
                                      boolean interval_30, boolean intervalHour);

    public void dismiss();
}

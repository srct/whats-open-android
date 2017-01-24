package srct.whatsopen.views;


import android.content.Context;

import srct.whatsopen.model.NotificationSettings;

public interface NotificationView {

    Context getContext();

    void setNotificationChecks(NotificationSettings n);

    void dismiss();
}

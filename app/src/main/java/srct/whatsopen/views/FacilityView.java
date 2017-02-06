package srct.whatsopen.views;


import android.content.Context;

public interface FacilityView {

    Context getContext();

    void changeFavoriteIcon(boolean favoriteStatus);
}

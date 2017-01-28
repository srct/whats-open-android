package srct.whatsopen.widget;

import android.appwidget.AppWidgetManager;
import android.content.Context;
import android.content.Intent;
import android.widget.RemoteViews;
import android.widget.RemoteViewsService;

import srct.whatsopen.R;


public class FacilityListAppWidgetService extends RemoteViewsService {

    @Override
    public RemoteViewsFactory onGetViewFactory(Intent intent) {
        return new FacilityListAppWidgetRemoteViewsFactory(this.getApplicationContext(), intent);
    }

    class FacilityListAppWidgetRemoteViewsFactory implements RemoteViewsFactory {

        private Context context;
        private Intent intent;
        private int appWidgetId;

        private int count = 12;

        FacilityListAppWidgetRemoteViewsFactory(Context context, Intent intent) {
            this.context = context;
            this.intent = intent;
            this.appWidgetId = intent.getIntExtra(AppWidgetManager.EXTRA_APPWIDGET_ID,
                    AppWidgetManager.INVALID_APPWIDGET_ID);
        }

        @Override
        public void onCreate() {

        }

        @Override
        public void onDataSetChanged() {

        }

        @Override
        public void onDestroy() {

        }

        @Override
        public int getCount() {
            return this.count;
        }

        @Override
        public RemoteViews getViewAt(int i) {
            RemoteViews rv = new RemoteViews(context.getPackageName(), R.layout.item_facility);
            rv.setTextViewText(R.id.facility_name, String.format("Facility %d", i));
            return rv;
        }

        @Override
        public RemoteViews getLoadingView() {
            return null;
        }

        @Override
        public int getViewTypeCount() {
            return 0;
        }

        @Override
        public long getItemId(int i) {
            return 0;
        }

        @Override
        public boolean hasStableIds() {
            return false;
        }
    }
}

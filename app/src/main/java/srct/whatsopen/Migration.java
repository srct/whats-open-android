package srct.whatsopen;

import io.realm.DynamicRealm;
import io.realm.RealmMigration;
import io.realm.RealmObjectSchema;
import io.realm.RealmSchema;
import srct.whatsopen.model.Location;


public class Migration implements RealmMigration {

    @Override
    public void migrate(DynamicRealm realm, long oldVersion, long newVersion) {
        RealmSchema schema = realm.getSchema();

        if(oldVersion == -1) {
            RealmObjectSchema facilitySchema = schema.get("Facility");
            facilitySchema
                    .renameField("mName", "name")
                    .renameField("mMainSchedule", "mainSchedule")
                    .renameField("mSpecialSchedules", "specialSchedules")
                    .removeField("mLocation")
                    .addField("location", Location.class);

            RealmObjectSchema mainScheduleSchema = schema.get("MainSchedule");
            mainScheduleSchema.addField("openTwentyFourHours", Boolean.class);

            RealmObjectSchema specialScheduleSchema = schema.get("SpecialSchedule");
            specialScheduleSchema.addField("openTwentyFourHours", Boolean.class);

            oldVersion++;
        }
    }
}

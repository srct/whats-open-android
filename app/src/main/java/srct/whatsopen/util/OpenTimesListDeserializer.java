package srct.whatsopen.util;

import com.google.gson.JsonArray;
import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonParseException;

import java.lang.reflect.Type;

import io.realm.RealmList;
import srct.whatsopen.model.OpenTimes;


// Json Deserializer for a list of OpenTimes
public class OpenTimesListDeserializer implements JsonDeserializer<RealmList<OpenTimes>> {

    @Override
    public RealmList<OpenTimes> deserialize(JsonElement json, Type type, JsonDeserializationContext
                                               jdc) throws JsonParseException {

        RealmList<OpenTimes> openTimes = new RealmList<>();
        JsonArray ja = json.getAsJsonArray();

        for(JsonElement je : ja) {
            openTimes.add((OpenTimes) jdc.deserialize(je, OpenTimes.class));
        }
        return openTimes;
    }
}

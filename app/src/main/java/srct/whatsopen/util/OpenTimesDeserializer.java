package srct.whatsopen.util;

import com.google.gson.Gson;
import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonParseException;

import java.lang.reflect.Type;

import srct.whatsopen.model.OpenTimes;


// Deserializer for a nested Json object, OpenTimes
public class OpenTimesDeserializer implements JsonDeserializer<OpenTimes> {

    @Override
    public OpenTimes deserialize(JsonElement je, Type type, JsonDeserializationContext jdc)
            throws JsonParseException
    {
        JsonElement openTimes = je.getAsJsonObject().get("open_time");

        return new Gson().fromJson(openTimes, OpenTimes.class);
    }
}

package me.vadim.airportsimulation.simulation.core.helper.adapters;

import com.google.gson.*;
import javafx.scene.paint.Color;
import me.vadim.airportsimulation.simulation.core.helper.ValueRange;

import java.lang.reflect.Type;

public class ValueRangeTypeAdapter implements JsonSerializer<ValueRange<Long>>, JsonDeserializer<ValueRange<Long>> {
    @Override
    public ValueRange<Long> deserialize(JsonElement jsonElement, Type type, JsonDeserializationContext jsonDeserializationContext) throws JsonParseException {
        long min = jsonElement.getAsJsonObject().get("min").getAsLong();
        long max = jsonElement.getAsJsonObject().get("max").getAsLong();

        return new ValueRange<>(min, max);
    }

    @Override
    public JsonElement serialize(ValueRange<Long> color, Type type, JsonSerializationContext jsonSerializationContext) {
        JsonObject obj = new JsonObject();
        obj.addProperty("min", color.getMin());
        obj.addProperty("max", color.getMax());
        return obj;
    }
}

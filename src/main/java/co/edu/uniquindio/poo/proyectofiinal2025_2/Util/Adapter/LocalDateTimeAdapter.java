package co.edu.uniquindio.poo.proyectofiinal2025_2.Util.Adapter;

import com.google.gson.*;

import java.lang.reflect.Type;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

/**
 * A Gson TypeAdapter for the {@link java.time.LocalDateTime} class.
 * <p>
 * This adapter serializes LocalDateTime objects to a string in ISO-8601 format
 * and deserializes them back from a string. This is necessary because Gson's
 * default reflection-based approach cannot handle the new Java 8 date/time types.
 * </p>
 */
public class LocalDateTimeAdapter implements JsonSerializer<LocalDateTime>, JsonDeserializer<LocalDateTime> {

    private static final DateTimeFormatter FORMATTER = DateTimeFormatter.ISO_LOCAL_DATE_TIME;

    /**
     * Serializes a {@link LocalDateTime} object into a {@link JsonElement}.
     *
     * @param src       The source LocalDateTime object to serialize.
     * @param typeOfSrc The specific genericized type of src.
     * @param context   The context for serialization.
     * @return A JsonElement representing the serialized date-time.
     */
    @Override
    public JsonElement serialize(LocalDateTime src, Type typeOfSrc, JsonSerializationContext context) {
        return new JsonPrimitive(FORMATTER.format(src));
    }

    /**
     * Deserializes a {@link JsonElement} into a {@link LocalDateTime} object.
     *
     * @param json    The JsonElement to deserialize.
     * @param typeOfT The specific genericized type of src.
     * @param context The context for deserialization.
     * @return A deserialized LocalDateTime object.
     * @throws JsonParseException if the JsonElement does not contain a valid date-time string.
     */
    @Override
    public LocalDateTime deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context) throws JsonParseException {
        return FORMATTER.parse(json.getAsString(), LocalDateTime::from);
    }
}

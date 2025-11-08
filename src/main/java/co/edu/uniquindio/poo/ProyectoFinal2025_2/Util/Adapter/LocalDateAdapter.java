package co.edu.uniquindio.poo.ProyectoFinal2025_2.Util.Adapter;

import com.google.gson.*;

import java.lang.reflect.Type;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

/**
 * A Gson TypeAdapter for the {@link java.time.LocalDate} class.
 * <p>
 * This adapter serializes LocalDate objects to a string in ISO-8601 format
 * and deserializes them back from a string. This is necessary because Gson's
 * default reflection-based approach cannot handle the new Java 8 date/time types.
 * </p>
 */
public class LocalDateAdapter implements JsonSerializer<LocalDate>, JsonDeserializer<LocalDate> {

    private static final DateTimeFormatter FORMATTER = DateTimeFormatter.ISO_LOCAL_DATE;

    /**
     * Serializes a {@link LocalDate} object into a {@link JsonElement}.
     *
     * @param src       The source LocalDate object to serialize.
     * @param typeOfSrc The specific genericized type of src.
     * @param context   The context for serialization.
     * @return A JsonElement representing the serialized date.
     */
    @Override
    public JsonElement serialize(LocalDate src, Type typeOfSrc, JsonSerializationContext context) {
        return new JsonPrimitive(FORMATTER.format(src));
    }

    /**
     * Deserializes a {@link JsonElement} into a {@link LocalDate} object.
     *
     * @param json    The JsonElement to deserialize.
     * @param typeOfT The specific genericized type of src.
     * @param context The context for deserialization.
     * @return A deserialized LocalDate object.
     * @throws JsonParseException if the JsonElement does not contain a valid date string.
     */
    @Override
    public LocalDate deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context) throws JsonParseException {
        return FORMATTER.parse(json.getAsString(), LocalDate::from);
    }
}

package tv.ender.itemparser.adapters

import com.google.gson.*
import org.bukkit.Color.fromRGB
import org.bukkit.FireworkEffect
import tv.ender.itemparser.modal.data.FireworkEffectData
import java.lang.reflect.Type

class FireworkEffectDataAdapter : JsonSerializer<FireworkEffectData>, JsonDeserializer<FireworkEffectData> {

    override fun serialize(src: FireworkEffectData, typeOfSrc: Type, context: JsonSerializationContext): JsonElement {
        val jsonObject = JsonObject().apply {
            addProperty("type", src.type.name)
            add("colors", JsonArray().apply {
                src.colors.forEach { color ->
                    add(color.asRGB())
                }
            })
            addProperty("flicker", src.flicker)
            addProperty("trail", src.trail)
        }
        return jsonObject
    }

    override fun deserialize(
        json: JsonElement,
        typeOfT: Type,
        context: JsonDeserializationContext
    ): FireworkEffectData {
        val jsonObject = json.asJsonObject

        val type = FireworkEffect.Type.valueOf(jsonObject.get("type").asString)
        val colorsArray = jsonObject.getAsJsonArray("colors")
        val colors = colorsArray.map { elem -> fromRGB(elem.asInt) }
        val flicker = jsonObject.get("flicker").asBoolean
        val trail = jsonObject.get("trail").asBoolean

        return FireworkEffectData(type, colors, flicker, trail)
    }
}

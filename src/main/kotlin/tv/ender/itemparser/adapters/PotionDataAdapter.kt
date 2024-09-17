package tv.ender.itemparser.adapters

import com.google.gson.*
import org.bukkit.Color
import org.bukkit.potion.PotionType
import tv.ender.itemparser.modal.data.PotionData
import java.lang.reflect.Type

class PotionDataAdapter : JsonSerializer<PotionData>, JsonDeserializer<PotionData> {

    override fun serialize(src: PotionData, typeOfSrc: Type, context: JsonSerializationContext): JsonElement {
        val jsonObject = JsonObject().apply {
            addProperty("type", src.type.name)

            src.color?.also {
                add("color", JsonObject().apply {
                    addProperty("red", src.color!!.red)
                    addProperty("green", src.color!!.green)
                    addProperty("blue", src.color!!.blue)
                    addProperty("alpha", src.color!!.alpha)
                })
            }

            addProperty("extended", src.extended)
            addProperty("upgraded", src.upgraded)
        }
        return jsonObject
    }

    override fun deserialize(json: JsonElement, typeOfT: Type, context: JsonDeserializationContext): PotionData {
        val jsonObject = json.asJsonObject

        var type = PotionType.AWKWARD
        val color: Color?

        runCatching {
            type = PotionType.valueOf(jsonObject.get("type").asString)
        }

        if (jsonObject.has("color")
            && !jsonObject.get("color").isJsonArray
        ) {
            val colorObject = jsonObject.getAsJsonObject("color")
            val r = Math.clamp(colorObject.get("red").asLong, 0, 255)
            val g = Math.clamp(colorObject.get("green").asLong, 0, 255)
            val b = Math.clamp(colorObject.get("blue").asLong, 0, 255)
            val a = Math.clamp(colorObject.get("alpha").asLong, 0, 255)
            color = Color.fromARGB(a, r, g, b)
        } else {
            color = type.effectType?.color
        }

        val extended = jsonObject.get("extended").asBoolean
        val upgraded = jsonObject.get("upgraded").asBoolean

        return PotionData(type, color, extended, upgraded)
    }
}

package tv.ender.itemparser.adapters

import com.google.gson.JsonArray
import com.google.gson.JsonDeserializationContext
import com.google.gson.JsonDeserializer
import com.google.gson.JsonElement
import com.google.gson.JsonObject
import com.google.gson.JsonSerializationContext
import com.google.gson.JsonSerializer
import net.kyori.adventure.key.Key
import org.bukkit.Color
import org.bukkit.Registry
import org.bukkit.potion.PotionEffect
import tv.ender.itemparser.modal.data.PotionData
import java.lang.reflect.Type

class PotionDataAdapter : JsonSerializer<PotionData>, JsonDeserializer<PotionData> {

    override fun serialize(src: PotionData, typeOfSrc: Type, context: JsonSerializationContext): JsonElement {
        val jsonObject = JsonObject().apply {

            src.type?.let { type -> addProperty("type", type.key().asString()) }

            src.color?.let { color ->
                add("color", JsonObject().apply {
                    addProperty("red", color.red)
                    addProperty("green", color.green)
                    addProperty("blue", color.blue)
                    addProperty("alpha", color.alpha)
                })
            }


            add(
                "customEffects",
                JsonArray().also { arr ->
                    src.customEffects
                        .map { pot -> context.serialize(pot, PotionEffect::class.java) }
                        .forEach { arr.add(it) }
                }
            )
        }

        return jsonObject
    }

    override fun deserialize(json: JsonElement, typeOfT: Type, context: JsonDeserializationContext): PotionData {
        val jsonObject = json.asJsonObject

        val type = if (jsonObject.has("type")) {
            jsonObject.get("type").asString.let {
                Registry.POTION.get(Key.key(it))
            }
        } else null

        val color = if (jsonObject.has("color")) {
            val colorObj = jsonObject.getAsJsonObject("color")
            val red = colorObj.get("red").asInt
            val green = colorObj.get("green").asInt
            val blue = colorObj.get("blue").asInt
            val alpha = colorObj.get("alpha").asInt
            Color.fromARGB(alpha, red, green, blue)
        } else null

        val customEffects = if (jsonObject.has("customEffects")) {
            val effectsArray = jsonObject.getAsJsonArray("customEffects")
            effectsArray.map { effectJson ->
                context.deserialize<PotionEffect>(effectJson, PotionEffect::class.java)
            }
        } else emptyList()

        return PotionData(
            type = type,
            color = color,
            customEffects = customEffects
        )
    }
}

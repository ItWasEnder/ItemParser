package tv.ender.itemparser.adapters

import com.google.gson.JsonDeserializationContext
import com.google.gson.JsonDeserializer
import com.google.gson.JsonElement
import com.google.gson.JsonObject
import com.google.gson.JsonSerializationContext
import com.google.gson.JsonSerializer
import net.kyori.adventure.key.Key
import org.bukkit.Registry
import org.bukkit.potion.PotionEffect
import org.bukkit.potion.PotionEffectType
import java.lang.reflect.Type

class PotionEffectAdapter : JsonSerializer<PotionEffect>, JsonDeserializer<PotionEffect> {
    companion object {
        @JvmStatic
        val ADAPTER = PotionEffectAdapter()
    }

    override fun serialize(src: PotionEffect, typeOfSrc: Type, context: JsonSerializationContext): JsonElement {
        val jsonObject = JsonObject().apply {
            addProperty("type", src.type.key().asString())
            addProperty("duration", src.duration)
            addProperty("amplifier", src.amplifier)
            addProperty("ambient", src.isAmbient)
            addProperty("particles", src.hasParticles())
            addProperty("icon", src.hasIcon())
        }
        return jsonObject
    }

    override fun deserialize(json: JsonElement, typeOfT: Type, context: JsonDeserializationContext): PotionEffect {

        val jsonObject = json.asJsonObject
        val typeName = jsonObject.get("type").asString
        val type = Registry.POTION_EFFECT_TYPE.get(Key.key(typeName))
            ?: return PotionEffect(PotionEffectType.LUCK, 0, 0)
        val duration = jsonObject.get("duration").asInt
        val amplifier = jsonObject.get("amplifier").asInt
        val ambient = jsonObject.get("ambient").asBoolean
        val particles = jsonObject.get("particles").asBoolean
        val icon = jsonObject.get("icon").asBoolean

        return PotionEffect(type, duration, amplifier, ambient, particles, icon)
    }
}
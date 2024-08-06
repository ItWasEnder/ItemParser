package tv.ender.itemparser.adapters

import com.google.gson.*
import io.papermc.paper.registry.RegistryAccess
import io.papermc.paper.registry.RegistryKey
import org.bukkit.NamespacedKey
import tv.ender.itemparser.modal.item.EnchantData
import tv.ender.itemparser.modal.item.WrappedEnchantment
import java.lang.reflect.Type

class EnchantDataAdapter : JsonSerializer<EnchantData>, JsonDeserializer<EnchantData> {
    private val registry = RegistryAccess.registryAccess().getRegistry(RegistryKey.ENCHANTMENT)

    override fun deserialize(jsonElement: JsonElement, type: Type, context: JsonDeserializationContext): EnchantData {
        if (jsonElement.isJsonObject) {
            val jsonObject = jsonElement.asJsonObject

            if (jsonObject.has("enchantments")) {
                return EnchantData(parseEnchantArray(jsonObject.get("enchantments").asJsonArray))
            }

            val enchantment = registry.get(NamespacedKey.minecraft(jsonObject["type"].asString))
                ?: throw JsonParseException("Invalid enchantment type")
            val level = jsonObject.get("level").asInt
            val enchantments = listOf(WrappedEnchantment(enchantment, level))

            return EnchantData(enchantments)
        }

        if (jsonElement.isJsonArray) {
            return EnchantData(parseEnchantArray(jsonElement.asJsonArray))
        }

        throw JsonParseException("Invalid JSON for EnchantData")
    }

    override fun serialize(enchantData: EnchantData, type: Type, context: JsonSerializationContext): JsonElement {
        val jsonArray = JsonArray()

        enchantData.enchantments?.forEach { enchantment ->
            val jsonObject = JsonObject()
            jsonObject.addProperty("type", enchantment.enchantment.key.key)
            jsonObject.addProperty("level", enchantment.level)
            jsonArray.add(jsonObject)
        }

        return jsonArray
    }

    private fun parseEnchantArray(jsonArray: JsonArray): List<WrappedEnchantment> {
        return jsonArray.mapNotNull { element ->
            val jsonObject = element.asJsonObject
            val type = jsonObject.get("type")?.asString
            val level = jsonObject.get("level")?.asInt

            if (type != null && level != null) {
                val enchantment = registry.get(NamespacedKey.minecraft(type))
                if (enchantment != null) {
                    WrappedEnchantment(enchantment, level)
                } else {
                    null
                }
            } else {
                null
            }
        }
    }
}

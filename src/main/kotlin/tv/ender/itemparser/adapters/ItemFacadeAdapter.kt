package tv.ender.itemparser.adapters

import com.google.gson.*
import org.bukkit.Material
import org.bukkit.inventory.ItemStack
import tv.ender.itemparser.modal.item.*
import tv.ender.itemparser.utils.PersistentDataSerializer
import java.lang.reflect.Type
import kotlin.math.max

class ItemFacadeAdapter : JsonSerializer<ItemFacade>, JsonDeserializer<ItemFacade> {

    override fun deserialize(jsonElement: JsonElement, type: Type, context: JsonDeserializationContext): ItemFacade {
        val jsonObject = jsonElement.asJsonObject
        val builder = ItemFacade.builder()

        jsonObject["displayName"]?.asString?.let { builder.displayName(it) }

        jsonObject["material"]?.asString?.let {
            val mat = Material.matchMaterial(it) ?: Material.BARRIER
            builder.material(mat)
        }

        jsonObject["texture"]?.asString?.let { builder.texture(it) }

        jsonObject["lore"]?.asJsonArray?.let { loreArray ->
            val lore = loreArray.map { it.asString }
            builder.lore(lore)
        }

        jsonObject["model"]?.asInt?.let { builder.model(it) }

        jsonObject["count"]?.asDouble?.let { builder.count(max(1.0, it).toInt()) }

        jsonObject["hideEnchants"]?.asBoolean?.let { builder.hideEnchants(it) }

        jsonObject["extra-data"]?.asJsonObject?.let { extraData ->
            extraData["potionData"]?.let {
                val potionData = context.deserialize<PotionData>(it, PotionData::class.java)
                builder.potionData(potionData)
            }

            extraData["enchantData"]?.let {
                val enchantData = context.deserialize<EnchantData>(it, EnchantData::class.java)
                builder.enchantData(enchantData)
            }

            extraData["instrumentData"]?.let {
                val instrumentData = context.deserialize<InstrumentData>(it, InstrumentData::class.java)
                builder.instrumentData(instrumentData)
            }

            extraData["axolotlData"]?.let {
                val axolotlData = context.deserialize<AxolotlData>(it, AxolotlData::class.java)
                builder.axolotlData(axolotlData)
            }

            extraData["fireworkEffectData"]?.let {
                val fireworkEffectData = context.deserialize<FireworkEffectData>(it, FireworkEffectData::class.java)
                builder.fireworkEffectData(fireworkEffectData)
            }

            extraData["pdc"]?.asJsonArray?.let { pdcJson ->
                val pdcSerializedString = pdcJson.toString()
                val persistentDataContainer = PersistentDataSerializer.fromJson(
                    pdcSerializedString,
                    ItemStack.empty().persistentDataContainer.adapterContext.newPersistentDataContainer()
                )
                builder.publicBukkitData(persistentDataContainer)
            }
        }

        // THIS IS DEPRECATED ITEM LAYOUT
        run {
            jsonObject["potionData"]?.let {
                val potionData = context.deserialize<PotionData>(it, PotionData::class.java)
                builder.potionData(potionData)
            }

            jsonObject["enchantData"]?.let {
                val enchantData = context.deserialize<EnchantData>(it, EnchantData::class.java)
                builder.enchantData(enchantData)
            }

            jsonObject["instrumentData"]?.let {
                val instrumentData = context.deserialize<InstrumentData>(it, InstrumentData::class.java)
                builder.instrumentData(instrumentData)
            }

            jsonObject["axolotlData"]?.let {
                val axolotlData = context.deserialize<AxolotlData>(it, AxolotlData::class.java)
                builder.axolotlData(axolotlData)
            }

            jsonObject["fireworkEffectData"]?.let {
                val fireworkEffectData = context.deserialize<FireworkEffectData>(it, FireworkEffectData::class.java)
                builder.fireworkEffectData(fireworkEffectData)
            }
        }

        val itemFacade = builder.build()

        return itemFacade
    }

    override fun serialize(itemFacade: ItemFacade, typeOfSrc: Type, context: JsonSerializationContext): JsonElement {
        val jsonObject = JsonObject().apply {
            addProperty("displayName", itemFacade.displayName.replace("§", "&"))
            addProperty("material", itemFacade.material.name)
            addProperty("model", itemFacade.model)
            addProperty("count", itemFacade.count)
            addProperty("hideEnchants", itemFacade.hideEnchants)

            itemFacade.texture?.let { addProperty("texture", it) }

            itemFacade.lore.takeIf { it.isNotEmpty() }?.let {
                val loreArray = JsonArray()
                it.forEach { lore -> loreArray.add(lore.replace("§", "&")) }
                add("lore", loreArray)
            }

            val extraData: JsonObject = JsonObject().apply {
                itemFacade.potionData?.let { add("potionData", context.serialize(it)) }
                itemFacade.enchantData?.let { add("enchantData", context.serialize(it)) }
                itemFacade.instrumentData?.let { add("instrumentData", context.serialize(it)) }
                itemFacade.axolotlData?.let { add("axolotlData", context.serialize(it)) }
                itemFacade.fireworkEffectData?.let { add("fireworkEffectData", context.serialize(it)) }
                itemFacade.pdcDelegate?.let { add("pdc", context.serialize(PersistentDataSerializer.toMapList(it))) }
            }

            if (!extraData.isEmpty) add("extra-data", extraData)
        }
        return jsonObject
    }
}
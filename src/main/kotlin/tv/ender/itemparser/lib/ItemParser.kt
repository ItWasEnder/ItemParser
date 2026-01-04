package tv.ender.itemparser.lib

import com.google.gson.Gson
import com.google.gson.GsonBuilder
import com.google.gson.JsonElement
import com.google.gson.stream.JsonReader
import java.io.File
import org.bukkit.inventory.ItemStack
import org.bukkit.potion.PotionEffect
import tv.ender.itemparser.adapters.PotionEffectAdapter
import tv.ender.itemparser.modal.data.EnchantData
import tv.ender.itemparser.modal.data.FireworkEffectData
import tv.ender.itemparser.modal.data.InstrumentData
import tv.ender.itemparser.modal.data.PotionData
import tv.ender.itemparser.modal.interfaces.Parser
import tv.ender.itemparser.modal.item.ItemFacade
import tv.ender.itemparser.modal.item.toFacade

object ItemParser : Parser {
    val gson: Gson

    init {
        val builder =
                GsonBuilder()
                        .setPrettyPrinting()
                        .enableComplexMapKeySerialization()
                        .disableHtmlEscaping()

        registerTypes(builder)

        gson = builder.create()
    }

    override fun fromJSON(json: String): ItemStack {
        return gson.fromJson(json, ItemFacade::class.java).asItem()
    }

    override fun fromJSON(file: File): ItemStack {
        require(file.exists()) { "The given file \"${file.name}\" does not exist" }
        require(file.isFile) { "The given file path does not point to a file (possible folder)" }

        // Ensure the reader is closed (prevents FD leaks on repeated calls).
        return file.reader().use { reader ->
            JsonReader(reader).use { jsonReader ->
                gson.fromJson<ItemFacade>(jsonReader, ItemFacade::class.java).asItem()
            }
        }
    }

    override fun toJSON(stack: ItemStack): String {
        return gson.toJson(stack.toFacade())
    }

    override fun toJSON(facade: ItemFacade): String {
        return gson.toJson(facade)
    }

    // Convert JsonElement to ItemFacade
    fun fromJsonElement(jsonElement: JsonElement): ItemFacade {
        return gson.fromJson(jsonElement, ItemFacade::class.java)
    }

    // Convert ItemFacade to JsonElement
    fun toJsonElement(itemFacade: ItemFacade): JsonElement {
        return gson.toJsonTree(itemFacade)
    }

    // Additional utility function to convert JSON file to ItemFacade
    fun fromFileToItemFacade(file: File): ItemFacade {
        require(file.exists()) { "The given file \"${file.name}\" does not exist" }
        require(file.isFile) { "The given file path does not point to a file (possible folder)" }

        return JsonReader(file.reader()).use { reader ->
            gson.fromJson(reader, ItemFacade::class.java)
        }
    }

    // Additional utility function to write ItemFacade to file as JSON
    fun toFile(itemFacade: ItemFacade, file: File) {
        file.writeText(gson.toJson(itemFacade))
    }

    @JvmStatic
    fun registerTypes(gsonBuilder: GsonBuilder) {
        gsonBuilder.registerTypeAdapter(EnchantData::class.java, EnchantData.ADAPTER)
        gsonBuilder.registerTypeAdapter(FireworkEffectData::class.java, FireworkEffectData.ADAPTER)
        gsonBuilder.registerTypeAdapter(InstrumentData::class.java, InstrumentData.ADAPTER)
        gsonBuilder.registerTypeAdapter(PotionEffect::class.java, PotionEffectAdapter.ADAPTER)
        gsonBuilder.registerTypeAdapter(PotionData::class.java, PotionData.ADAPTER)
        gsonBuilder.registerTypeAdapter(ItemFacade::class.java, ItemFacade.ADAPTER)
    }
}

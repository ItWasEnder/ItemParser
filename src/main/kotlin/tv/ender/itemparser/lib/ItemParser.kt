package tv.ender.itemparser.lib

import com.google.gson.Gson
import com.google.gson.GsonBuilder
import com.google.gson.JsonElement
import com.google.gson.stream.JsonReader
import org.bukkit.inventory.ItemStack
import tv.ender.itemparser.modal.item.*
import java.io.File

object ItemParser : Parser {
    val gson: Gson

    init {
        val builder = GsonBuilder()
            .setPrettyPrinting()
            .enableComplexMapKeySerialization()
            .disableHtmlEscaping()

        builder.registerTypeAdapter(EnchantData::class.java, EnchantData.ADAPTER)
        builder.registerTypeAdapter(FireworkEffectData::class.java, FireworkEffectData.ADAPTER)
        builder.registerTypeAdapter(InstrumentData::class.java, InstrumentData.ADAPTER)
        builder.registerTypeAdapter(PotionData::class.java, PotionData.ADAPTER)
        builder.registerTypeAdapter(ItemFacade::class.java, ItemFacade.ADAPTER)

        gson = builder.create()
    }

    override fun fromJSON(json: String): ItemStack {
        return gson.fromJson(json, ItemFacade::class.java).asItem()
    }

    override fun fromJSON(file: File): ItemStack {
        // make a file reader
        assert(file.exists()) { "The given file \"${file.name}\" does not exist" }
        assert(file.isFile()) { "The given file path does not point to a file (possible folder)" }

        return with(JsonReader(file.reader())) {
            gson.fromJson<ItemFacade>(this, ItemFacade::class.java).asItem()
        }
    }

    override fun toJSON(stack: ItemStack): String {
        return gson.toJson(stack.toFacade())
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
        assert(file.exists()) { "The given file \"${file.name}\" does not exist" }
        assert(file.isFile()) { "The given file path does not point to a file (possible folder)" }

        return JsonReader(file.reader()).use { reader ->
            gson.fromJson(reader, ItemFacade::class.java)
        }
    }

    // Additional utility function to write ItemFacade to file as JSON
    fun toFile(itemFacade: ItemFacade, file: File) {
        file.writeText(gson.toJson(itemFacade))
    }
}
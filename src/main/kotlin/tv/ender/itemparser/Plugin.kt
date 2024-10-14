package tv.ender.itemparser

import org.bukkit.Material
import org.bukkit.inventory.ItemStack
import org.bukkit.inventory.meta.BookMeta
import org.bukkit.inventory.meta.WritableBookMeta
import org.bukkit.plugin.java.JavaPlugin
import tv.ender.itemparser.lib.ItemParser
import tv.ender.itemparser.modal.item.toFacade
import tv.ender.itemparser.text.ColorUtil

class Plugin : JavaPlugin() {
    companion object {
        val DEBUG: Boolean
            get() = System.getenv()["debug"].toBoolean()
    }


    override fun onEnable() {
        // Plugin startup logic
        if (DEBUG) {
            val itemJson =
                "{\"material\":\"POTION\",\"model\":0,\"count\":1,\"hideEnchants\":false,\"extra-data\":{\"potionData\":{\"color\":{\"red\":0,\"green\":142,\"blue\":0,\"alpha\":255},\"customEffects\":[{\"type\":\"minecraft:strength\",\"duration\":69,\"amplifier\":69,\"ambient\":false,\"particles\":false,\"icon\":false}]}}}"

            try {
                ItemParser.fromJSON(itemJson).also {
                    println(ItemParser.toJSON(it.toFacade()))
                    println("similar: ${it.toFacade().isSimilar(it)}")
                    println(it.itemMeta)
                }

                println("------------------")

                val item = ItemStack(Material.WRITTEN_BOOK, 1)

                item.editMeta { meta ->
                    val bookMeta = meta as BookMeta

                    bookMeta.setTitle(ColorUtil.color("&atest"))
                    bookMeta.pages = listOf("test1", "test2")
                    bookMeta.author = "dumb"

                    println(meta)
                }

                println("isBook: " + (item.itemMeta is BookMeta))
                println("isBook2: " + (item.itemMeta is WritableBookMeta))

                println(ItemParser.toJSON(item))
            } catch (e: Exception) {
                e.printStackTrace()
                server.shutdown()
            }
        }
    }

    override fun onDisable() {
        // Plugin shutdown logic
    }
}

private infix fun String.and(any: Any?): Any {
    return this + " " + any.toString()
}

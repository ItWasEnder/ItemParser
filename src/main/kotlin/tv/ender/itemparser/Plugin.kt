package tv.ender.itemparser

import org.bukkit.plugin.java.JavaPlugin
import tv.ender.itemparser.lib.ItemParser
import tv.ender.itemparser.modal.item.toFacade

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

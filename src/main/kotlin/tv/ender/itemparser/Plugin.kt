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
                "{\"displayName\":\"&#FF369ATest Item\",\"material\":\"ENDER_EYE\",\"model\":1,\"count\":1,\"hideEnchants\":false,\"lore\":[\"Obtained from deliveries.\"],\"extra-data\":{\"enchantData\":[{\"type\":\"unbreaking\",\"level\":1}],\"pdc\":[{\"key\":\"pyrofishingpro:first\",\"type\":\"DOUBLE\",\"value\":4.1},{\"key\":\"pyrofishingpro:second\",\"type\":\"STRING\",\"value\":\"test\"},{\"key\":\"pyrofishingpro:third\",\"type\":\"INTEGER\",\"value\":9}]}}"

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

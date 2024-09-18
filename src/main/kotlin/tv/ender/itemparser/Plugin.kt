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
                "{\"displayName\":\"&b&lEntropy Booster &8(&31.0x&8)\",\"material\":\"ENDER_EYE\",\"model\":1,\"count\":1,\"hideEnchants\":false,\"lore\":[\"&7Obtained from deliveries.\",\"&7Boosts all entropy you get from\",\"&7catching fish. This does not affect\",\"&agutting&7 or &aperception&7.\",\"\",\"&7This booster will last &b30 minutes&7.\",\"\",\"&aRight-Click &7to use.\"],\"extra-data\":{\"enchantData\":[{\"type\":\"unbreaking\",\"level\":1}],\"pdc\":[{\"key\":\"pyrofishingpro:first\",\"type\":\"DOUBLE\",\"value\":4.1},{\"key\":\"pyrofishingpro:second\",\"type\":\"STRING\",\"value\":\"test\"},{\"key\":\"pyrofishingpro:third\",\"type\":\"INTEGER\",\"value\":9}]}}"

            try {
                ItemParser.fromJSON(itemJson).also {
                    println(ItemParser.toJSON(it.toFacade()))
                    println("similar: ${it.toFacade().isSimilar(it)}")
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

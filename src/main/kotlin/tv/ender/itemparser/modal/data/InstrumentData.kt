package tv.ender.itemparser.modal.data

import net.kyori.adventure.key.Key
import org.bukkit.MusicInstrument
import org.bukkit.NamespacedKey
import org.bukkit.Registry
import org.bukkit.inventory.ItemStack
import org.bukkit.inventory.meta.ItemMeta
import org.bukkit.inventory.meta.MusicInstrumentMeta
import tv.ender.itemparser.adapters.InstrumentDataAdapter
import tv.ender.itemparser.modal.interfaces.ItemData

data class InstrumentData(
    var instrument: String = ""
) : ItemData<MusicInstrumentMeta> {

    override fun isSimilar(stack: ItemStack): Boolean {
        if (!stack.hasItemMeta()) return false

        val meta: ItemMeta = stack.itemMeta

        if (meta !is MusicInstrumentMeta) return false

        val it: MusicInstrument = meta.instrument ?: return false

        val key: NamespacedKey? = Registry.INSTRUMENT.getKey(it)

        return instrument.equals(key?.value().orEmpty(), ignoreCase = true)
    }

    override fun apply(meta: MusicInstrumentMeta) {
        if (instrument.isBlank()) {
            return
        }

        val received = Registry.INSTRUMENT[Key.key(this.instrument)] ?: return
        meta.instrument = received
    }

    fun name(): String {
        return instrument.substringAfter(":")
            .split("_")
            .firstOrNull()
            ?.replaceFirstChar { if (it.isLowerCase()) it.titlecase() else it.toString() }
            ?: "Unknown"
    }

    companion object {
        @JvmStatic val ADAPTER = InstrumentDataAdapter()

        inline fun build(block: InstrumentData.() -> Unit): InstrumentData {
            return InstrumentData().apply(block)
        }
    }
}

fun MusicInstrumentMeta.toInstrumentData(): InstrumentData {
    val instrument = this.instrument
        ?.let { Registry.INSTRUMENT.getKey(it)?.value().orEmpty() }
        .orEmpty()

    return InstrumentData(instrument)
}

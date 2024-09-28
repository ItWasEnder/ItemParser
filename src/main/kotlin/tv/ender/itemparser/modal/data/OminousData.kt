package tv.ender.itemparser.modal.data

import org.bukkit.inventory.ItemStack
import org.bukkit.inventory.meta.ItemMeta
import org.bukkit.inventory.meta.OminousBottleMeta
import tv.ender.itemparser.modal.interfaces.ItemData

data class OminousData(
    var amplifier: Int = 0,
) : ItemData<OminousBottleMeta> {

    override fun isSimilar(stack: ItemStack): Boolean {
        val meta: ItemMeta = stack.itemMeta ?: return false

        if (meta !is OminousBottleMeta) return false
        val metaAmp = if (meta.hasAmplifier()) meta.amplifier else 0

        return this.amplifier == metaAmp
    }

    override fun apply(meta: OminousBottleMeta) {
        meta.amplifier = this.amplifier
    }
}

fun OminousBottleMeta.toOminousData(): OminousData {
    return OminousData(
        amplifier = if (this.hasAmplifier()) this.amplifier else 0,
    )
}

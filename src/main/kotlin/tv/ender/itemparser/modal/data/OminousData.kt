package tv.ender.itemparser.modal.data

import org.bukkit.inventory.ItemStack
import org.bukkit.inventory.meta.ItemMeta
import org.bukkit.inventory.meta.OminousBottleMeta
import tv.ender.itemparser.modal.interfaces.ItemData

data class OminousData(
    var amplifier: Int = 1,
) : ItemData<OminousBottleMeta> {

    override fun isSimilar(stack: ItemStack): Boolean {
        val meta: ItemMeta = stack.itemMeta ?: return false

        if (meta !is OminousBottleMeta) return false

        return this.amplifier == meta.amplifier
    }

    override fun apply(meta: OminousBottleMeta) {
        meta.amplifier = this.amplifier
    }
}

fun OminousBottleMeta.toOminousData(): OminousData {
    return OminousData(
        amplifier = this.amplifier,
    )
}

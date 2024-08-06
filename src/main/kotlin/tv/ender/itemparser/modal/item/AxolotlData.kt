package tv.ender.itemparser.modal.item

import org.bukkit.Material
import org.bukkit.entity.Axolotl
import org.bukkit.inventory.ItemStack
import org.bukkit.inventory.meta.AxolotlBucketMeta
import org.bukkit.inventory.meta.ItemMeta

data class AxolotlData(var variant: Int = 0) : ItemData<AxolotlBucketMeta> {
    override fun isSimilar(stack: ItemStack): Boolean {
        val meta: ItemMeta = stack.getItemMeta() ?: return false

        if (stack.type != Material.AXOLOTL_BUCKET) {
            return false
        }

        if (meta is AxolotlBucketMeta) {
            return this.variant == meta.variant.ordinal
        }

        return false
    }

    override fun apply(meta: AxolotlBucketMeta) {
        meta.variant = Axolotl.Variant.entries[this.variant]
    }

    companion object {
        inline fun build(block: AxolotlData.() -> Unit): AxolotlData {
            return AxolotlData().apply(block)
        }
    }
}

fun AxolotlBucketMeta.toAxolotlData(): AxolotlData {
    return AxolotlData.build {
        variant = this@toAxolotlData.variant.ordinal
    }
}



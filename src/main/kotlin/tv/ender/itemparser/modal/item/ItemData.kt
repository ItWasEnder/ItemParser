package tv.ender.itemparser.modal.item

import org.bukkit.inventory.ItemStack

interface ItemData<T> {
    fun isSimilar(stack: ItemStack): Boolean

    fun apply(meta: T)
}
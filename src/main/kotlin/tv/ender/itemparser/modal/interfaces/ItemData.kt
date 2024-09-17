package tv.ender.itemparser.modal.interfaces

import org.bukkit.inventory.ItemStack

interface ItemData<T> {
    fun isSimilar(stack: ItemStack): Boolean

    fun apply(meta: T)
}
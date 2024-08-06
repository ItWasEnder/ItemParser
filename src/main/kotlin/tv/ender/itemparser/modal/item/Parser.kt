package tv.ender.itemparser.modal.item

import org.bukkit.inventory.ItemStack
import java.io.File

interface Parser {
    fun fromJSON(json: String): ItemStack

    fun fromJSON(file: File): ItemStack

    fun toJSON(stack: ItemStack): String
}
package tv.ender.itemparser.modal.interfaces

import org.bukkit.inventory.ItemStack
import tv.ender.itemparser.modal.item.ItemFacade
import java.io.File

interface Parser {
    fun fromJSON(json: String): ItemStack

    fun fromJSON(file: File): ItemStack

    fun toJSON(stack: ItemStack): String

    fun toJSON(facade: ItemFacade): String
}
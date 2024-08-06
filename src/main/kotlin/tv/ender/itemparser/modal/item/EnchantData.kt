package tv.ender.itemparser.modal.item

import org.bukkit.ChatColor
import org.bukkit.enchantments.Enchantment
import org.bukkit.inventory.ItemStack
import org.bukkit.inventory.meta.EnchantmentStorageMeta
import org.bukkit.inventory.meta.ItemMeta
import tv.ender.itemparser.adapters.EnchantDataAdapter
import java.util.*

data class WrappedEnchantment(val enchantment: Enchantment, val level: Int)

data class EnchantData(var enchantments: List<WrappedEnchantment>? = null) : ItemData<ItemMeta> {

    override fun isSimilar(stack: ItemStack): Boolean {
        val meta: ItemMeta = stack.itemMeta ?: return false
        return enchantCheck(meta)
    }

    override fun apply(meta: ItemMeta) {
        val lore = LinkedList<String>()

        enchantments?.forEach { enchant ->
            if (meta is EnchantmentStorageMeta) {
                meta.addStoredEnchant(enchant.enchantment, enchant.level, true)
            } else {
                meta.addEnchant(enchant.enchantment, enchant.level, true)
            }
            lore.add(
                ChatColor.translateAlternateColorCodes(
                    '&',
                    "&7${enchant.enchantment.displayName(enchant.level)}"
                )
            )
        }

        meta.lore?.let { lore.addAll(it) }
    }

    private fun enchantCheck(meta: ItemMeta): Boolean {
        return enchantments?.all { enchant ->
            if (meta is EnchantmentStorageMeta) {
                meta.hasStoredEnchant(enchant.enchantment) &&
                        meta.getStoredEnchantLevel(enchant.enchantment) == enchant.level
            } else {
                meta.hasEnchant(enchant.enchantment) &&
                        meta.getEnchantLevel(enchant.enchantment) == enchant.level
            }
        } ?: false
    }

    companion object {
        val ADAPTER = EnchantDataAdapter()

        inline fun build(block: EnchantData.() -> Unit): EnchantData {
            return EnchantData().apply(block)
        }
    }
}

fun ItemMeta.toEnchantData(): EnchantData {
    val enchants = mutableListOf<WrappedEnchantment>()
    if (this is EnchantmentStorageMeta) {
        this.storedEnchants.entries.mapTo(enchants) {
            WrappedEnchantment(it.key, it.value)
        }
    } else if (this.hasEnchants()) {
        this.enchants.entries.mapTo(enchants) {
            WrappedEnchantment(it.key, it.value)
        }
    }

    return EnchantData(enchants)
}
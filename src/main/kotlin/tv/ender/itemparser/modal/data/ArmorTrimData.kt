package tv.ender.itemparser.modal.data

import io.papermc.paper.registry.RegistryAccess
import io.papermc.paper.registry.RegistryKey
import net.kyori.adventure.key.Key
import org.bukkit.inventory.ItemStack
import org.bukkit.inventory.meta.ArmorMeta
import org.bukkit.inventory.meta.ItemMeta
import org.bukkit.inventory.meta.trim.ArmorTrim
import tv.ender.itemparser.modal.interfaces.ItemData

data class ArmorTrimData(
    var pattern: String,
    var material: String,
) : ItemData<ArmorMeta> {

    override fun isSimilar(stack: ItemStack): Boolean {
        val meta: ItemMeta = stack.itemMeta ?: return false

        if (meta !is ArmorMeta) return false
        if (!meta.hasTrim()) return false
        val trim = meta.trim ?: return false

        return (this.pattern == trim.pattern.key().asString()) && (this.material == trim.material.key().asString())
    }

    override fun apply(meta: ArmorMeta) {
        TRIM_MATERIALS.get(Key.key(material))?.also { mat ->
            TRIM_PATTERNS.get(Key.key(pattern))?.also { pat ->
                meta.trim = ArmorTrim(mat, pat)
            }
        }
    }

    companion object {
        val TRIM_PATTERNS = RegistryAccess.registryAccess().getRegistry(RegistryKey.TRIM_PATTERN)
        val TRIM_MATERIALS = RegistryAccess.registryAccess().getRegistry(RegistryKey.TRIM_MATERIAL)

        inline fun build(block: AxolotlData.() -> Unit): AxolotlData {
            return AxolotlData().apply(block)
        }
    }
}

fun ArmorMeta.toArmorTrimData(): ArmorTrimData {
    val armorTrim = this.trim ?: throw IllegalStateException("Cannot call this method will null armor trim")
    return ArmorTrimData(armorTrim.pattern.key().asString(), armorTrim.material.key().asString())
}


package tv.ender.itemparser.modal.data

import org.bukkit.Color
import org.bukkit.inventory.ItemStack
import org.bukkit.inventory.meta.ItemMeta
import org.bukkit.inventory.meta.PotionMeta
import org.bukkit.potion.PotionType
import tv.ender.itemparser.adapters.PotionDataAdapter
import tv.ender.itemparser.modal.extensions.pretty
import tv.ender.itemparser.modal.interfaces.ItemData

@Suppress("DEPRECATION")
data class PotionData(
    var type: PotionType = PotionType.AWKWARD,
    var color: Color? = null,
    var extended: Boolean = false,
    var upgraded: Boolean = false
) : ItemData<PotionMeta> {

    override fun isSimilar(stack: ItemStack): Boolean {
        val meta: ItemMeta = stack.itemMeta ?: return false

        if (!(meta is PotionMeta)) return false

        val base = meta.basePotionData ?: return false

        return this.type == base.type &&
                this.extended == base.isExtended &&
                this.upgraded == base.isUpgraded
    }

    override fun apply(meta: PotionMeta) {
        color?.let { meta.color = it }

        meta.basePotionData = org.bukkit.potion.PotionData(this.type, extended, upgraded)
    }

    fun name(): String {
        val typeName = type?.name?.lowercase()?.pretty() ?: "Unknown"
        val extension = if (extended) "(8:00)" else "(3:00)"
        val upgrade = if (upgraded) " II " else " "
        return "$typeName$upgrade$extension"
    }

    companion object {
        val ADAPTER = PotionDataAdapter()

        inline fun build(block: PotionData.() -> Unit): PotionData {
            return PotionData().apply(block)
        }
    }
}

fun PotionMeta.toPotionData(): PotionData {
    val base = this.basePotionData ?: return PotionData()

    val baseColor: Color = this.color ?: base.type.effectType?.color ?: Color.WHITE

    return PotionData(
        type = base.type,
        color = baseColor,
        extended = base.isExtended,
        upgraded = base.isUpgraded
    )
}

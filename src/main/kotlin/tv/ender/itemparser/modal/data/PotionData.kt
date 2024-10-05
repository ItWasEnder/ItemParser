package tv.ender.itemparser.modal.data

import org.bukkit.Color
import org.bukkit.inventory.ItemStack
import org.bukkit.inventory.meta.ItemMeta
import org.bukkit.inventory.meta.PotionMeta
import org.bukkit.potion.PotionEffect
import org.bukkit.potion.PotionType
import tv.ender.itemparser.adapters.PotionDataAdapter
import tv.ender.itemparser.modal.interfaces.ItemData

@Suppress("DEPRECATION")
data class PotionData(
    var type: PotionType? = null,
    var color: Color? = null,
    var customEffects: List<PotionEffect> = emptyList(),
) : ItemData<PotionMeta> {
    override fun isSimilar(stack: ItemStack): Boolean {
        val meta: ItemMeta = stack.itemMeta ?: return false
        if (meta !is PotionMeta) return false

//        println("---===---")
//        println("Comparing Potion Meta")
//        println("meta type: ${meta.basePotionType}")
//        println("this type: $type")
//        println("meta color: ${meta.color}")
//        println("this color: $color")
//        println("meta effects: ${meta.customEffects}")
//        println("this effects: $customEffects")
//        print("--")

        // https://hub.spigotmc.org/javadocs/spigot/org/bukkit/potion/PotionType.html
        if (!meta.hasBasePotionType() && type != null) return false
        else if (meta.hasBasePotionType() && type != null && meta.basePotionType != type) return false

        if (meta.hasColor()) {
            if (color == null) return false
            if (meta.color != color) return false
        } else if (color != null) return false

        if (meta.hasCustomEffects()) {
            if (meta.customEffects.isEmpty() && customEffects.isNotEmpty()) return false
            for (mcF in meta.customEffects) {
                if (!customEffects.any {
                        it.type == mcF.type && it.duration == mcF.duration && it.amplifier == mcF.amplifier
                    }) return false
            }
        } else if (customEffects.isNotEmpty()) return false

//        println("--- Similar Found ---")

        return true
    }

    override fun apply(meta: PotionMeta) {
        color?.let { meta.color = it }
        type?.let { meta.basePotionType = this.type }

        if (this.customEffects.isNotEmpty()) {
            this.customEffects.forEach { meta.addCustomEffect(it, false) }
        }
    }

    companion object {
        @JvmStatic
        val ADAPTER = PotionDataAdapter()

        inline fun build(block: PotionData.() -> Unit): PotionData {
            return PotionData().apply(block)
        }
    }
}

fun PotionMeta.toPotionData(): PotionData {
    return PotionData(
        type = this.basePotionType,
        color = this.color,
        customEffects = if (this.hasCustomEffects()) this.customEffects else emptyList()
    )
}

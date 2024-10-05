package tv.ender.itemparser.modal.data

import org.bukkit.Color
import org.bukkit.FireworkEffect
import org.bukkit.inventory.ItemStack
import org.bukkit.inventory.meta.FireworkEffectMeta
import org.bukkit.inventory.meta.ItemMeta
import tv.ender.itemparser.adapters.FireworkEffectDataAdapter
import tv.ender.itemparser.modal.extensions.pretty
import tv.ender.itemparser.modal.interfaces.ItemData

data class FireworkEffectData(
    var type: FireworkEffect.Type = FireworkEffect.Type.BURST,
    var colors: List<Color> = emptyList(),
    var flicker: Boolean = false,
    var trail: Boolean = false
) : ItemData<FireworkEffectMeta> {

    val effect: FireworkEffect
        get() = FireworkEffect.builder()
            .with(this.type)
            .withColor(this.colors)
            .flicker(this.flicker)
            .trail(this.trail)
            .build()

    override fun isSimilar(stack: ItemStack): Boolean {
        val meta: ItemMeta = stack.itemMeta ?: return false

        if (meta is FireworkEffectMeta) {
            val fireworkEffect: FireworkEffect = meta.effect ?: return false

            return (fireworkEffect.type == this.type && fireworkEffect.colors == colors && fireworkEffect.hasFlicker() == flicker) && fireworkEffect.hasTrail() == this.trail
        }

        return false
    }

    override fun apply(meta: FireworkEffectMeta) {
        meta.effect = effect
    }

    fun name(): String {
        return (type?.name?.lowercase() ?: "Unknown").also { it.pretty() }
    }

    companion object {
        @JvmStatic val ADAPTER = FireworkEffectDataAdapter()

        inline fun build(block: FireworkEffectData.() -> Unit): FireworkEffectData {
            return FireworkEffectData().apply(block)
        }
    }
}

fun FireworkEffectMeta.toFireworkEffectData(): FireworkEffectData {
    val eff: FireworkEffect = this.effect ?: return FireworkEffectData()

    return FireworkEffectData(
        type = eff.type,
        colors = eff.colors,
        flicker = eff.hasFlicker(),
        trail = eff.hasTrail()
    )
}

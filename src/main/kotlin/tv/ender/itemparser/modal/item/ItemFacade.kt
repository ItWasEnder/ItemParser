package tv.ender.itemparser.modal.item

import net.kyori.adventure.text.format.TextDecoration
import org.bukkit.ChatColor
import org.bukkit.Material
import org.bukkit.NamespacedKey
import org.bukkit.inventory.ItemFlag
import org.bukkit.inventory.ItemStack
import org.bukkit.inventory.meta.ArmorMeta
import org.bukkit.inventory.meta.AxolotlBucketMeta
import org.bukkit.inventory.meta.EnchantmentStorageMeta
import org.bukkit.inventory.meta.FireworkEffectMeta
import org.bukkit.inventory.meta.ItemMeta
import org.bukkit.inventory.meta.MusicInstrumentMeta
import org.bukkit.inventory.meta.PotionMeta
import org.bukkit.inventory.meta.SkullMeta
import org.bukkit.persistence.PersistentDataContainer
import tv.ender.itemparser.Plugin
import tv.ender.itemparser.adapters.ItemFacadeAdapter
import tv.ender.itemparser.modal.data.ArmorTrimData
import tv.ender.itemparser.modal.data.AxolotlData
import tv.ender.itemparser.modal.data.EnchantData
import tv.ender.itemparser.modal.data.FireworkEffectData
import tv.ender.itemparser.modal.data.InstrumentData
import tv.ender.itemparser.modal.data.PotionData
import tv.ender.itemparser.modal.data.toArmorTrimData
import tv.ender.itemparser.modal.data.toAxolotlData
import tv.ender.itemparser.modal.data.toEnchantData
import tv.ender.itemparser.modal.data.toFireworkEffectData
import tv.ender.itemparser.modal.data.toInstrumentData
import tv.ender.itemparser.modal.data.toPotionData
import tv.ender.itemparser.modal.extensions.prettyName
import tv.ender.itemparser.persistent.PersistentDataSerializer
import tv.ender.itemparser.text.ColorUtil
import tv.ender.itemparser.text.ColorUtil.LEGACY
import tv.ender.itemparser.utils.MetaUtils
import kotlin.math.max

data class ItemFacade(
    var material: Material,
    var displayName: String,
    var lore: List<String> = emptyList(),
    var model: Int = 0,
    var count: Int = 1,
    var hideEnchants: Boolean = false,
    var texture: String? = null,
    var potionData: PotionData? = null,
    var enchantData: EnchantData? = null,
    var instrumentData: InstrumentData? = null,
    var axolotlData: AxolotlData? = null,
    var fireworkEffectData: FireworkEffectData? = null,
    var armorTrimData: ArmorTrimData? = null,
    var pdcMapList: List<Map<*, *>>? = null,
) {

    fun isSimilar(stack: ItemStack): Boolean {
        if (stack.type != material) return false

        val meta: ItemMeta = stack.itemMeta ?: return false

        if (texture != null && MetaUtils.getTexture(meta).isNotBlank()) {
            return texture.equals(MetaUtils.getTexture(meta), ignoreCase = true)
        }

        // TODO: Check if meta is present on item and if so compare to faced
        if (!meta.hasCustomModelData() && model != 0) return false

        if (meta.hasCustomModelData() && model != meta.customModelData) return false

        if (potionData?.isSimilar(stack) == false) return false

        if (meta.hasEnchants() && enchantData == null) return false

        if (enchantData?.isSimilar(stack) == false) return false

        if (instrumentData?.isSimilar(stack) == false) return false

        if (axolotlData?.isSimilar(stack) == false) return false

        if (fireworkEffectData?.isSimilar(stack) == false) return false

        if (armorTrimData?.isSimilar(stack) == false) return false

        if (pdcMapList?.isNotEmpty() == true) {
            val currentPdc = meta.persistentDataContainer

            for (map in pdcMapList!!) {
                val key = NamespacedKey.fromString(map["key"].toString()) ?: continue
                val type =
                    PersistentDataSerializer.getNativePersistentDataTypeByFieldName(map["type"].toString())
                val value = map["value"]

                if (Plugin.DEBUG) {
                    println("Checking PDC | $key, $type, $value")
                }

                currentPdc[key, type]?.also {
                    if (Plugin.DEBUG) {
                        println("value exists: $it")
                        println("compare: ${it == value}")
                    }
                    if (it != value) return false
                } ?: return false
            }
        }

        return true
    }

    fun asItem(): ItemStack {
        val stack = ItemStack(material, max(1, count))
        val meta = stack.itemMeta ?: return stack

        if (model != 0) meta.setCustomModelData(model)
        meta.displayName(LEGACY.deserialize(ColorUtil.hex(displayName)).decoration(TextDecoration.ITALIC, false))

        if (lore.isNotEmpty()) {
            meta.lore = lore.map { ChatColor.translateAlternateColorCodes('&', it) }
        }

        texture?.also { MetaUtils.applyTexture(meta, it) }

        // Apply metadata
        enchantData?.apply(meta)
        potionData?.takeIf { meta is PotionMeta }?.apply(meta as PotionMeta)
        axolotlData?.takeIf { meta is AxolotlBucketMeta }?.apply(meta as AxolotlBucketMeta)
        fireworkEffectData?.takeIf { meta is FireworkEffectMeta }?.apply(meta as FireworkEffectMeta)
        armorTrimData?.takeIf { meta is ArmorMeta }?.apply(meta as ArmorMeta)
        instrumentData?.takeIf { meta is MusicInstrumentMeta }?.apply(meta as MusicInstrumentMeta)

        if (hideEnchants) {
            meta.addItemFlags(ItemFlag.HIDE_ENCHANTS)
        }

        MetaUtils.fixName(meta)

        // Applying public Bukkit data
        pdcMapList?.apply {
            PersistentDataSerializer.fromMapList(this, meta.persistentDataContainer)
        }

        stack.itemMeta = meta

        return stack
    }

    companion object {
        val ADAPTER: ItemFacadeAdapter = ItemFacadeAdapter()

        fun of(stack: ItemStack): ItemFacade {
            val meta = stack.itemMeta ?: return ItemFacade(material = stack.type, displayName = stack.type.prettyName)
            val metaDisplayName = meta.displayName()?.let { LEGACY.serialize(it) } ?: ""

            val builder = builder()
                .material(stack.type)
                .count(stack.amount)
                .displayName(
                    metaDisplayName.ifBlank { stack.type.prettyName }
                )

            if (meta.hasCustomModelData()) builder.model(meta.customModelData)
            if (meta.hasLore()) builder.lore(meta.lore ?: emptyList())
            if (meta is SkullMeta) meta.playerProfile?.properties?.firstOrNull { it.name == "textures" }
                ?.let { builder.texture(it.value) }
            if (meta is PotionMeta) builder.potionData(meta.toPotionData())
            if (meta is EnchantmentStorageMeta || meta.hasEnchants()) builder.enchantData(meta.toEnchantData())
            if (meta is AxolotlBucketMeta) builder.axolotlData(meta.toAxolotlData())
            if (meta is FireworkEffectMeta) builder.fireworkEffectData(meta.toFireworkEffectData())
            if (meta is MusicInstrumentMeta) builder.instrumentData(meta.toInstrumentData())
            if (meta is ArmorMeta && meta.hasTrim()) builder.armorTrimData(meta.toArmorTrimData())
            if (meta.persistentDataContainer.keys.isNotEmpty()) {
                builder.publicBukkitData(meta.persistentDataContainer)
            }

            return builder.build()
        }

        fun builder() = ItemFacadeBuilder()
    }

    class ItemFacadeBuilder {
        private var displayName: String = ""
        private var material: Material = Material.AIR
        private var lore: List<String> = emptyList()
        private var model: Int = 0
        private var count: Int = 1
        private var hideEnchants: Boolean = false
        private var texture: String? = null
        private var potionData: PotionData? = null
        private var enchantData: EnchantData? = null
        private var instrumentData: InstrumentData? = null
        private var axolotlData: AxolotlData? = null
        private var fireworkEffectData: FireworkEffectData? = null
        private var armorTrimData: ArmorTrimData? = null
        private var pdcMapList: List<Map<*, *>>? = null

        fun displayName(displayName: String) = apply { this.displayName = displayName }
        fun material(material: Material) = apply { this.material = material }
        fun lore(lore: List<String>) = apply { this.lore = lore }
        fun model(model: Int) = apply { this.model = model }
        fun count(count: Int) = apply { this.count = count }
        fun hideEnchants(hideEnchants: Boolean) = apply { this.hideEnchants = hideEnchants }
        fun texture(texture: String?) = apply { this.texture = texture }
        fun potionData(potionData: PotionData?) = apply { this.potionData = potionData }
        fun enchantData(enchantData: EnchantData?) = apply { this.enchantData = enchantData }
        fun instrumentData(instrumentData: InstrumentData?) = apply { this.instrumentData = instrumentData }
        fun axolotlData(axolotlData: AxolotlData?) = apply { this.axolotlData = axolotlData }
        fun armorTrimData(armorTrimData: ArmorTrimData?) = apply { this.armorTrimData = armorTrimData }
        fun fireworkEffectData(fireworkEffectData: FireworkEffectData?) =
            apply { this.fireworkEffectData = fireworkEffectData }

        fun publicBukkitData(container: PersistentDataContainer) =
            apply { this.pdcMapList = PersistentDataSerializer.toMapList(container) }

        fun build() = ItemFacade(
            displayName = displayName,
            material = material,
            lore = lore,
            model = model,
            count = count,
            hideEnchants = hideEnchants,
            texture = texture,
            potionData = potionData,
            enchantData = enchantData,
            instrumentData = instrumentData,
            axolotlData = axolotlData,
            fireworkEffectData = fireworkEffectData,
            armorTrimData = armorTrimData,
            pdcMapList = pdcMapList
        )
    }
}

fun ItemStack.toFacade(): ItemFacade {
    return ItemFacade.of(this)
}


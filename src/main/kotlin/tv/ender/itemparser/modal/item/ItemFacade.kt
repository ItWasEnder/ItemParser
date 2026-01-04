package tv.ender.itemparser.modal.item

import java.util.TreeMap
import kotlin.math.max
import net.kyori.adventure.text.format.TextDecoration
import org.bukkit.Material
import org.bukkit.NamespacedKey
import org.bukkit.inventory.ItemFlag
import org.bukkit.inventory.ItemRarity
import org.bukkit.inventory.ItemStack
import org.bukkit.inventory.meta.ArmorMeta
import org.bukkit.inventory.meta.AxolotlBucketMeta
import org.bukkit.inventory.meta.BookMeta
import org.bukkit.inventory.meta.EnchantmentStorageMeta
import org.bukkit.inventory.meta.FireworkEffectMeta
import org.bukkit.inventory.meta.ItemMeta
import org.bukkit.inventory.meta.MusicInstrumentMeta
import org.bukkit.inventory.meta.OminousBottleMeta
import org.bukkit.inventory.meta.PotionMeta
import org.bukkit.inventory.meta.SkullMeta
import org.bukkit.persistence.PersistentDataContainer
import tv.ender.itemparser.adapters.ItemFacadeAdapter
import tv.ender.itemparser.modal.config.Options
import tv.ender.itemparser.modal.data.ArmorTrimData
import tv.ender.itemparser.modal.data.AxolotlData
import tv.ender.itemparser.modal.data.BookData
import tv.ender.itemparser.modal.data.EnchantData
import tv.ender.itemparser.modal.data.FireworkEffectData
import tv.ender.itemparser.modal.data.InstrumentData
import tv.ender.itemparser.modal.data.OminousData
import tv.ender.itemparser.modal.data.PotionData
import tv.ender.itemparser.modal.data.toArmorTrimData
import tv.ender.itemparser.modal.data.toAxolotlData
import tv.ender.itemparser.modal.data.toBookData
import tv.ender.itemparser.modal.data.toEnchantData
import tv.ender.itemparser.modal.data.toFireworkEffectData
import tv.ender.itemparser.modal.data.toInstrumentData
import tv.ender.itemparser.modal.data.toOminousData
import tv.ender.itemparser.modal.data.toPotionData
import tv.ender.itemparser.persistent.PersistentDataSerializer
import tv.ender.itemparser.text.ColorUtil
import tv.ender.itemparser.text.ColorUtil.LEGACY
import tv.ender.itemparser.utils.MetaUtils

data class ItemFacade(
        var material: Material,
        var displayName: String? = null,
        var lore: List<String> = emptyList(),
        var model: Int = 0,
        var count: Int = 1,
        var hideEnchants: Boolean = false,
        var rarity: Int? = null,
        var texture: String? = null,
        var potionData: PotionData? = null,
        var enchantData: EnchantData? = null,
        var instrumentData: InstrumentData? = null,
        var axolotlData: AxolotlData? = null,
        var fireworkEffectData: FireworkEffectData? = null,
        var armorTrimData: ArmorTrimData? = null,
        var pdcMapList: List<Map<*, *>>? = null,
        var ominousData: OminousData? = null,
        var bookData: BookData? = null,
) {
    private fun normalize(obj: Any?): Any? {
        return when (obj) {
            null -> null
            is Map<*, *> -> {
                val tm = TreeMap<String, Any?>()
                for ((k, v) in obj) {
                    if (k == null) continue
                    tm[k.toString()] = normalize(v)
                }
                tm
            }
            is List<*> -> {
                val normed = obj.map { normalize(it) }
                // If this is a "maplist" (PDC serialized list), sort by "key"+"type" to ignore
                // order instability.
                val allMaps = normed.all { it is Map<*, *> && (it as Map<*, *>).containsKey("key") }
                if (allMaps) {
                    normed.sortedBy {
                        val m = it as Map<*, *>
                        (m["key"]?.toString() ?: "") + ":" + (m["type"]?.toString() ?: "")
                    }
                } else {
                    normed
                }
            }
            else -> obj
        }
    }

    @JvmOverloads
    fun isSimilar(stack: ItemStack, options: Options = Options()): Boolean {
        if (stack.type != material) return false

        val meta: ItemMeta = stack.itemMeta ?: return false

        if (texture != null && MetaUtils.getTexture(meta).isNotBlank()) {
            return texture.equals(MetaUtils.getTexture(meta), ignoreCase = true)
        }

        if (options.modelData && !meta.hasCustomModelData() && model != 0) return false
        if (options.modelData && meta.hasCustomModelData() && model != meta.customModelData)
                return false

        if (options.potionData && potionData?.isSimilar(stack) == false) return false

        if (options.enchantData && enchantData?.isSimilar(stack) == false) return false

        if (options.rarity && !meta.hasRarity() && rarity != null) return false
        if (options.rarity && meta.hasRarity() && rarity != meta.rarity.ordinal) return false

        if (instrumentData?.isSimilar(stack) == false) return false

        if (axolotlData?.isSimilar(stack) == false) return false

        if (fireworkEffectData?.isSimilar(stack) == false) return false

        if (armorTrimData?.isSimilar(stack) == false) return false

        if (ominousData?.isSimilar(stack) == false) return false

        if (options.bookData && bookData?.isSimilar(stack) == false) return false

        if (options.nbtKeys && pdcMapList?.isNotEmpty() == true) {
            for (map in pdcMapList!!) {
                val key = NamespacedKey.fromString(map["key"].toString()) ?: continue
                val type =
                        PersistentDataSerializer.getNativePersistentDataTypeByFieldName(
                                map["type"].toString()
                        )
                val value = map["value"]

                if (!meta.persistentDataContainer.has(key, type)) {
                    return false
                }

                if (options.nbtValues) {
                    val actual = meta.persistentDataContainer[key, type]

                    val typeName = map["type"]?.toString()
                    if (typeName == "TAG_CONTAINER") {
                        val actualNorm =
                                normalize(
                                        if (actual is PersistentDataContainer)
                                                PersistentDataSerializer.toMapList(actual)
                                        else null
                                )
                        val expectedNorm = normalize(value)
                        val eq = (actualNorm == expectedNorm)
                        if (!eq) return false
                    } else if (typeName == "TAG_CONTAINER_ARRAY") {
                        val serialized: List<List<Map<*, *>>>? =
                                if (actual is Array<*>) {
                                    actual.filterIsInstance<PersistentDataContainer>().map {
                                        PersistentDataSerializer.toMapList(it)
                                    }
                                } else {
                                    null
                                }
                        val actualNorm = normalize(serialized)
                        val expectedNorm = normalize(value)
                        val eq = (actualNorm == expectedNorm)
                        if (!eq) return false
                    } else {
                        if (actual != value) return false
                    }
                }
            }
        }

        return true
    }

    fun asItem(): ItemStack {
        val stack = ItemStack(material, max(1, count))
        val meta = stack.itemMeta ?: return stack

        if (model != 0) meta.setCustomModelData(model)

        if (displayName != null) {
            val colored = ColorUtil.color(displayName ?: "%%ERR%%")
            meta.displayName(LEGACY.deserialize(colored).decoration(TextDecoration.ITALIC, false))
        }

        if (lore.isNotEmpty()) {
            meta.lore = lore.map { ColorUtil.color(it) }
        }

        texture?.also { MetaUtils.applyTexture(meta, it) }

        // Apply metadata
        enchantData?.apply(meta)
        potionData?.takeIf { meta is PotionMeta }?.apply(meta as PotionMeta)
        axolotlData?.takeIf { meta is AxolotlBucketMeta }?.apply(meta as AxolotlBucketMeta)
        fireworkEffectData?.takeIf { meta is FireworkEffectMeta }?.apply(meta as FireworkEffectMeta)
        armorTrimData?.takeIf { meta is ArmorMeta }?.apply(meta as ArmorMeta)
        instrumentData?.takeIf { meta is MusicInstrumentMeta }?.apply(meta as MusicInstrumentMeta)
        ominousData?.takeIf { meta is OminousBottleMeta }?.apply(meta as OminousBottleMeta)
        bookData?.takeIf { meta is BookMeta }?.apply(meta as BookMeta)
        rarity?.also { meta.setRarity(ItemRarity.values()[it]) }

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
        @JvmStatic val ADAPTER: ItemFacadeAdapter = ItemFacadeAdapter()

        fun of(stack: ItemStack): ItemFacade {
            val meta = stack.itemMeta ?: return ItemFacade(stack.type)
            val metaDisplayName = meta.displayName()?.let { LEGACY.serialize(it) } ?: ""

            val builder = builder().material(stack.type).count(stack.amount)

            if (meta.hasDisplayName()) builder.displayName(metaDisplayName)
            if (meta.hasCustomModelData()) builder.model(meta.customModelData)
            if (meta.hasLore()) builder.lore(meta.lore ?: emptyList())
            if (meta.hasRarity()) builder.rarity(meta.rarity.ordinal)
            if (meta is SkullMeta)
                    meta.playerProfile?.properties?.firstOrNull { it.name == "textures" }?.let {
                        builder.texture(it.value)
                    }
            if (meta is PotionMeta) builder.potionData(meta.toPotionData())
            if (meta is EnchantmentStorageMeta || meta.hasEnchants())
                    builder.enchantData(meta.toEnchantData())
            if (meta is AxolotlBucketMeta) builder.axolotlData(meta.toAxolotlData())
            if (meta is FireworkEffectMeta) builder.fireworkEffectData(meta.toFireworkEffectData())
            if (meta is MusicInstrumentMeta) builder.instrumentData(meta.toInstrumentData())
            if (meta is ArmorMeta && meta.hasTrim()) builder.armorTrimData(meta.toArmorTrimData())
            if (meta is OminousBottleMeta) builder.ominousData(meta.toOminousData())
            if (meta is BookMeta) builder.bookData(meta.toBookData())

            if (meta.persistentDataContainer.keys.isNotEmpty()) {
                builder.publicBukkitData(meta.persistentDataContainer)
            }

            return builder.build()
        }

        fun builder() = ItemFacadeBuilder()
    }

    class ItemFacadeBuilder {
        private var displayName: String? = null
        private var material: Material = Material.AIR
        private var lore: List<String> = emptyList()
        private var model: Int = 0
        private var count: Int = 1
        private var hideEnchants: Boolean = false
        private var rarity: Int? = null
        private var texture: String? = null
        private var potionData: PotionData? = null
        private var enchantData: EnchantData? = null
        private var instrumentData: InstrumentData? = null
        private var axolotlData: AxolotlData? = null
        private var fireworkEffectData: FireworkEffectData? = null
        private var armorTrimData: ArmorTrimData? = null
        private var pdcMapList: List<Map<*, *>>? = null
        private var ominousData: OminousData? = null
        private var bookData: BookData? = null

        fun displayName(displayName: String?) = apply { this.displayName = displayName }
        fun material(material: Material) = apply { this.material = material }
        fun lore(lore: List<String>) = apply { this.lore = lore }
        fun model(model: Int) = apply { this.model = model }
        fun count(count: Int) = apply { this.count = count }
        fun hideEnchants(hideEnchants: Boolean) = apply { this.hideEnchants = hideEnchants }
        fun rarity(rarity: Int) = apply { this.rarity = rarity }
        fun texture(texture: String?) = apply { this.texture = texture }
        fun potionData(potionData: PotionData?) = apply { this.potionData = potionData }
        fun enchantData(enchantData: EnchantData?) = apply { this.enchantData = enchantData }
        fun instrumentData(instrumentData: InstrumentData?) = apply {
            this.instrumentData = instrumentData
        }
        fun axolotlData(axolotlData: AxolotlData?) = apply { this.axolotlData = axolotlData }
        fun armorTrimData(armorTrimData: ArmorTrimData?) = apply {
            this.armorTrimData = armorTrimData
        }
        fun ominousData(data: OminousData?) = apply { this.ominousData = data }
        fun bookData(data: BookData?) = apply { this.bookData = data }
        fun fireworkEffectData(fireworkEffectData: FireworkEffectData?) = apply {
            this.fireworkEffectData = fireworkEffectData
        }

        fun publicBukkitData(container: PersistentDataContainer) = apply {
            this.pdcMapList = PersistentDataSerializer.toMapList(container)
        }

        fun build() =
                ItemFacade(
                        displayName = displayName,
                        material = material,
                        lore = lore,
                        model = model,
                        count = count,
                        hideEnchants = hideEnchants,
                        texture = texture,
                        potionData = potionData,
                        enchantData = enchantData,
                        rarity = rarity,
                        instrumentData = instrumentData,
                        axolotlData = axolotlData,
                        fireworkEffectData = fireworkEffectData,
                        armorTrimData = armorTrimData,
                        pdcMapList = pdcMapList,
                        ominousData = ominousData,
                        bookData = bookData,
                )
    }
}

fun ItemStack.toFacade(): ItemFacade {
    return ItemFacade.of(this)
}

package tv.ender.itemparser.utils

import com.destroystokyo.paper.profile.PlayerProfile
import com.destroystokyo.paper.profile.ProfileProperty
import org.bukkit.Bukkit
import org.bukkit.ChatColor
import org.bukkit.NamespacedKey
import org.bukkit.inventory.ItemStack
import org.bukkit.inventory.meta.Damageable
import org.bukkit.inventory.meta.EnchantmentStorageMeta
import org.bukkit.inventory.meta.ItemMeta
import org.bukkit.inventory.meta.SkullMeta
import org.bukkit.persistence.PersistentDataContainer
import org.bukkit.persistence.PersistentDataType
import java.util.UUID

object MetaUtils {
    fun applyTexture(meta: ItemMeta?, url: String, name: String? = null) {
        if (name != null) assert(name.length < 16) { "Name must be equal to or less than 16 characters." }

        /* this will also handle null meta */
        if (meta !is SkullMeta) {
            return
        }

        val fixedName = name?.replace(" ", "")
        val playerProfile: PlayerProfile = Bukkit.createProfile(UUID.randomUUID(), fixedName)
        playerProfile.setProperty(ProfileProperty("textures", url))

        meta.setPlayerProfile(playerProfile)
    }

    fun getTexture(meta: ItemMeta?): String {
        /* this will also handle null meta */
        if (meta !is SkullMeta) {
            return ""
        }

        val props: Set<ProfileProperty> = meta.getPlayerProfile()?.getProperties() ?: emptySet()

        if (props.isEmpty()) {
            return ""
        }

        for (prop in props) {
            if (prop.getName() == "textures") {
                return prop.getValue()
            }
        }

        return ""
    }

    fun fixName(meta: ItemMeta) {
        if (meta.hasDisplayName()) {
            var itemColor = "&f"

            if (meta is Damageable && meta.hasEnchants()) {
                itemColor = "&b"
            } else if (meta is EnchantmentStorageMeta || meta is SkullMeta) {
                itemColor = "&e"
            }

            meta.setDisplayName(ChatColor.translateAlternateColorCodes('&', (itemColor + meta.getDisplayName())))
        }
    }

    fun getType(container: PersistentDataContainer, key: NamespacedKey): PersistentDataType<*, *>? {
        if (container.has(key, PersistentDataType.BYTE)) {
            return PersistentDataType.BYTE
        } else if (container.has(key, PersistentDataType.BYTE_ARRAY)) {
            return PersistentDataType.BYTE_ARRAY
        } else if (container.has(key, PersistentDataType.DOUBLE)) {
            return PersistentDataType.DOUBLE
        } else if (container.has(key, PersistentDataType.FLOAT)) {
            return PersistentDataType.FLOAT
        } else if (container.has(key, PersistentDataType.INTEGER)) {
            return PersistentDataType.INTEGER
        } else if (container.has(key, PersistentDataType.INTEGER_ARRAY)) {
            return PersistentDataType.INTEGER_ARRAY
        } else if (container.has(key, PersistentDataType.LONG)) {
            return PersistentDataType.LONG
        } else if (container.has(key, PersistentDataType.LONG_ARRAY)) {
            return PersistentDataType.LONG_ARRAY
        } else if (container.has(key, PersistentDataType.SHORT)) {
            return PersistentDataType.SHORT
        } else if (container.has(key, PersistentDataType.STRING)) {
            return PersistentDataType.STRING
        } else if (container.has(
                key,
                PersistentDataType.TAG_CONTAINER
            )
        ) {
            return PersistentDataType.TAG_CONTAINER
        } else if (container.has<Array<PersistentDataContainer>, Array<PersistentDataContainer>>(
                key,
                PersistentDataType.TAG_CONTAINER_ARRAY
            )
        ) {
            return PersistentDataType.TAG_CONTAINER_ARRAY
        }

        return null
    }

    fun readPBV(stack: ItemStack, key: NamespacedKey): String {
        val meta: ItemMeta = stack.getItemMeta()
        val container: PersistentDataContainer = meta.getPersistentDataContainer()
        var value = ""

        if (container.has(key, PersistentDataType.BYTE)) {
            value = container.get(key, PersistentDataType.BYTE).toString()
        } else if (container.has(key, PersistentDataType.BYTE_ARRAY)) {
            value = container.get(key, PersistentDataType.BYTE_ARRAY).contentToString()
        } else if (container.has(key, PersistentDataType.DOUBLE)) {
            value = container.get(key, PersistentDataType.DOUBLE).toString()
        } else if (container.has(key, PersistentDataType.FLOAT)) {
            value = container.get(key, PersistentDataType.FLOAT).toString()
        } else if (container.has(key, PersistentDataType.INTEGER)) {
            value = container.get(key, PersistentDataType.INTEGER).toString()
        } else if (container.has(key, PersistentDataType.INTEGER_ARRAY)) {
            value = container.get(key, PersistentDataType.INTEGER_ARRAY).contentToString()
        } else if (container.has(key, PersistentDataType.LONG)) {
            value = container.get(key, PersistentDataType.LONG).toString()
        } else if (container.has(key, PersistentDataType.LONG_ARRAY)) {
            value = container.get(key, PersistentDataType.LONG_ARRAY).contentToString()
        } else if (container.has(key, PersistentDataType.SHORT)) {
            value = container.get(key, PersistentDataType.SHORT).toString()
        } else if (container.has(key, PersistentDataType.STRING)) {
            value = container.get(key, PersistentDataType.STRING).toString()
        }

        return value
    }

    fun <T, Z : Any> writePVB(
        stack: ItemStack,
        key: NamespacedKey,
        value: Z,
        type: PersistentDataType<T, Z>,
    ): ItemStack {
        val meta: ItemMeta = stack.getItemMeta()
        val container: PersistentDataContainer = meta.getPersistentDataContainer()

        container.set(key, type, value)

        stack.setItemMeta(meta)

        return stack
    }
}

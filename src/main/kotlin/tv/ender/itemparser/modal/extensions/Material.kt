package tv.ender.itemparser.modal.extensions

import org.bukkit.Material
import java.util.*


private val prettyNameMap: EnumMap<Material, String> = EnumMap(Material::class.java)

val Material.prettyName: String
    get() = prettyNameMap.computeIfAbsent(this) { this.name.lowercase().pretty() }
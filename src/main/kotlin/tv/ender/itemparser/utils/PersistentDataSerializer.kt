package tv.ender.itemparser.utils

import com.google.gson.JsonSyntaxException
import com.google.gson.reflect.TypeToken
import org.bukkit.NamespacedKey
import org.bukkit.persistence.PersistentDataAdapterContext
import org.bukkit.persistence.PersistentDataContainer
import org.bukkit.persistence.PersistentDataType
import org.bukkit.persistence.PersistentDataType.PrimitivePersistentDataType
import org.jetbrains.annotations.Contract
import tv.ender.itemparser.lib.ItemParser
import java.util.*

/*
* Copyright (c) 2023. JEFF Media GbR / mfnalex et al.
*
* This program is free software: you can redistribute it and/or modify
* it under the terms of the GNU General Public License as published by
* the Free Software Foundation, either version 3 of the License, or
* (at your option) any later version.
*
* This program is distributed in the hope that it will be useful,
* but WITHOUT ANY WARRANTY; without even the implied warranty of
* MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
* GNU General Public License for more details.
*
* You should have received a copy of the GNU General Public License
* along with this program.  If not, see <https://www.gnu.org/licenses/>.
*/

/**
 * Utility class to serialize and deserialize for [PersistentDataContainer]s
 */
class PersistentDataSerializer private constructor() {
    /**
     * Private constructor to prevent instantiation
     */
    init {
        throw UnsupportedOperationException("This class cannot be instantiated")
    }

    companion object {
        /**
         * All native primitive [PersistentDataType]s declared in [PersistentDataType]
         */
        private val NATIVE_PRIMITIVE_PERSISTENT_DATA_TYPES: MutableSet<PersistentDataType<*, *>> = HashSet()

        /**
         * All native primitive [PersistentDataType]s mapped by their name
         */
        private val NATIVE_PRIMITIVE_PERSISTENT_DATA_TYPES_BY_NAME: MutableMap<String, PersistentDataType<*, *>> =
            HashMap()

        /**
         * All native primitive [PersistentDataType] mapped by their class
         */
        private val NATIVE_PRIMITIVE_PERSISTENT_DATA_TYPES_BY_CLASS: MutableMap<PersistentDataType<*, *>, String> =
            HashMap()

        /**
         * TypeToken for [List] of [Map]s with [String] keys and [Object] values
         */
        private val LIST_MAP_TYPE_TOKEN: TypeToken<List<Map<String?, Any?>?>?> =
            object : TypeToken<List<Map<String?, Any?>?>?>() {
            }

        // Cache the native primitive PersistentDataTypes
        init {
            for (field in PersistentDataType::class.java.fields) {
                // Ignore non-PersistentDataType fields

                if (!PersistentDataType::class.java.isAssignableFrom(field.type)) {
                    continue
                }

                try {
                    val type =
                        Objects.requireNonNull(field[null] as PersistentDataType<*, *>)!! as? PrimitivePersistentDataType<*>
                            ?: continue

                    // Ignore non-primitive PersistentDataTypes, such as PersistentDataType.BOOLEAN

                    val name = field.name

                    NATIVE_PRIMITIVE_PERSISTENT_DATA_TYPES.add(type)
                    NATIVE_PRIMITIVE_PERSISTENT_DATA_TYPES_BY_NAME[name] = type
                    NATIVE_PRIMITIVE_PERSISTENT_DATA_TYPES_BY_CLASS[type] = name
                } catch (exception: IllegalAccessException) {
                    throw RuntimeException(
                        "Could not access native persistent data type field: " + field.type.name,
                        exception
                    )
                }
            }
        }

        /**
         * Gets the proper [org.bukkit.persistence.PersistentDataType] for the given [NamespacedKey]
         *
         * @param pdc PersistentDataContainer
         * @param key NamespacedKey
         * @return PrimitivePersistentDataType
         * @throws IllegalArgumentException if no native PrimitivePersistentDataType was found. (This should never happen.)
         */
        @Throws(IllegalArgumentException::class)
        fun getPrimitivePersistentDataType(
            pdc: PersistentDataContainer,
            key: NamespacedKey
        ): PersistentDataType<*, *> {
            Objects.requireNonNull(pdc, "pdc cannot be null")
            Objects.requireNonNull(key, "key cannot be null")

            for (type in NATIVE_PRIMITIVE_PERSISTENT_DATA_TYPES) {
                if (pdc.has(key, type)) {
                    return type
                }
            }
            throw IllegalArgumentException(
                "Could not find a native PrimitivePersistentDataType for key " + key +
                        " in PersistentDataContainer " + pdc + ". Available native datatypes are " + java.lang.String.join(
                    ", ",
                    NATIVE_PRIMITIVE_PERSISTENT_DATA_TYPES_BY_NAME.keys
                )
            )
        }

        /**
         * Serializes a [PersistentDataContainer] to a list of maps
         *
         * @param pdc PersistentDataContainer
         * @return serialized PersistentDataContainer
         */
        @Contract(value = "_ -> new", pure = true)
        fun toMapList(
            pdc: PersistentDataContainer
        ): List<Map<*, *>> {
            Objects.requireNonNull(pdc, "pdc cannot be null")
            val list: MutableList<Map<*, *>> = ArrayList()

            for (key in pdc.keys) {
                val map: MutableMap<String, Any?> = LinkedHashMap()
                val type = getPrimitivePersistentDataType(pdc, key)
                var value = pdc[key, type]
                Objects.requireNonNull(value, "value cannot be null")

                if (type == PersistentDataType.TAG_CONTAINER) {
                    value = toMapList((value as PersistentDataContainer?)!!)
                } else if (type == PersistentDataType.TAG_CONTAINER_ARRAY) {
                    val containers = value as Array<PersistentDataContainer>?
                    Objects.requireNonNull(containers, "containers cannot be null")
                    val serializedContainers: MutableList<List<Map<*, *>>> = ArrayList()
                    for (container in containers!!) {
                        serializedContainers.add(toMapList(container))
                    }
                    value = serializedContainers
                }

                map["key"] = key.toString()
                map["type"] = getNativePersistentDataTypeFieldName(type)
                map["value"] = value

                list.add(map)
            }

            return list
        }

        /**
         * Deserializes a [PersistentDataContainer] from a list of maps and saves it to the given target [PersistentDataContainer]
         *
         * @param serializedPdc serialized PersistentDataContainer
         * @param targetPdc     target PersistentDataContainer
         * @return deserialized PersistentDataContainer
         */
        @Contract(value = "_, _ -> param2")
        fun fromMapList(
            serializedPdc: List<Map<*, *>>,
            targetPdc: PersistentDataContainer
        ): PersistentDataContainer {
            Objects.requireNonNull(targetPdc, "targetPdc cannot be null")
            Objects.requireNonNull(serializedPdc, "serializedPdc cannot be null")

            val context = targetPdc.adapterContext

            //final PersistentDataContainer pdc = context.newPersistentDataContainer();
            for (map in serializedPdc) {
                val key = NamespacedKey.fromString((map["key"] as String?)!!)

                Objects.requireNonNull(key, "key cannot be null")
                var value = map["value"]

                val type =
                    getNativePersistentDataTypeByFieldName((map["type"] as String?)!!) as PersistentDataType<Any, Any?>

                if (type == PersistentDataType.TAG_CONTAINER) {
                    value = fromMapList((value as List<Map<*, *>>?)!!, context.newPersistentDataContainer())
                } else if (type == PersistentDataType.TAG_CONTAINER_ARRAY) {
                    val serializedContainers = value as List<List<Map<*, *>>>?
                    val containers = arrayOfNulls<PersistentDataContainer>(
                        serializedContainers!!.size
                    )
                    for (i in serializedContainers.indices) {
                        containers[i] = fromMapList(serializedContainers[i], context.newPersistentDataContainer())
                    }
                    value = containers
                } else {
                    value = cast(value, type)
                }

                value?.also { targetPdc.set(key!!, type, it) }
            }

            return targetPdc
        }

        /**
         * Deserializes a [PersistentDataContainer] from a list of maps and saves it to the given target [PersistentDataContainer]
         *
         * @param serializedPdc serialized PersistentDataContainer
         * @param context       PersistentDataAdapterContext
         * @return deserialized PersistentDataContainer
         */
        @Contract(value = "_, _ -> new", pure = true)
        fun fromMapList(
            serializedPdc: List<Map<*, *>>,
            context: PersistentDataAdapterContext
        ): PersistentDataContainer {
            Objects.requireNonNull(serializedPdc, "serializedPdc cannot be null")
            Objects.requireNonNull(context, "context cannot be null")

            return fromMapList(serializedPdc, context.newPersistentDataContainer())
        }

        /**
         * Casts a value to the given [PersistentDataType]'s primitive type
         *
         * @param value value to cast
         * @param type  PersistentDataType
         * @return casted value
         */
        private fun cast(
            value: Any?,
            type: PersistentDataType<*, *>
        ): Any? {
            if (value == null) {
                return null
            }

            Objects.requireNonNull(type, "type cannot be null")
            val primitiveType = type.primitiveType

            if (primitiveType == Float::class.java) {
                return (value as Number).toFloat()
            } else if (primitiveType == Int::class.java) {
                return (value as Number).toInt()
            } else if (primitiveType == Double::class.java) {
                return (value as Number).toDouble()
            } else if (primitiveType == Short::class.java) {
                return (value as Number).toShort()
            } else if (primitiveType == Byte::class.java) {
                if (type.complexType == Boolean::class.java) {
                    if (value is Byte) {
                        return value.toInt() == 1
                    } else if (value is Boolean) {
                        return value
                    }
                } else if (value is Boolean) {
                    return (if (value) 1 else 0).toByte()
                } else if (value is Number) {
                    return value.toByte()
                }
            } else if (value is List<*>) {
                val list = value
                val length = list.size
                if (type === PersistentDataType.BYTE_ARRAY) {
                    val arr = ByteArray(length)
                    for (i in 0 until length) {
                        arr[i] = (list[i] as Number).toByte()
                    }
                    return arr
                } else if (type === PersistentDataType.INTEGER_ARRAY) {
                    val arr = IntArray(length)
                    for (i in 0 until length) {
                        arr[i] = (list[i] as Number).toInt()
                    }
                    return arr
                } else if (type === PersistentDataType.LONG_ARRAY) {
                    val arr = LongArray(length)
                    for (i in 0 until length) {
                        arr[i] = (list[i] as Number).toLong()
                    }
                    return arr
                } else {
                    throw IllegalArgumentException("Unknown array type: " + type.primitiveType.componentType.name)
                }
            }
            return value
        }

        /**
         * Serializes a [PersistentDataContainer] to JSON
         *
         * @param pdc PersistentDataContainer
         * @return JSON string
         */
        @Contract(value = "_ -> new", pure = true)
        fun toJson(
            pdc: PersistentDataContainer
        ): String {
            Objects.requireNonNull(pdc, "pdc cannot be null")
            return ItemParser.gson.toJson(toMapList(pdc), LIST_MAP_TYPE_TOKEN.type)
        }

        /**
         * Deserializes a [PersistentDataContainer] from JSON and saves it to the given target [PersistentDataContainer]
         *
         * @param serializedPdc serialized PersistentDataContainer
         * @param targetPdc     target PersistentDataContainer
         * @return deserialized PersistentDataContainer
         * @throws JsonSyntaxException if the JSON is malformed
         */
        @Contract(value = "_, _ -> param2")
        @Throws(JsonSyntaxException::class)
        fun fromJson(
            serializedPdc: String,
            targetPdc: PersistentDataContainer
        ): PersistentDataContainer {
            Objects.requireNonNull(targetPdc, "targetPdc cannot be null")
            Objects.requireNonNull(serializedPdc, "serializedPdc cannot be null")
            return fromMapList(ItemParser.gson.fromJson(serializedPdc, LIST_MAP_TYPE_TOKEN.type), targetPdc)
        }

        /**
         * Deserializes a [PersistentDataContainer] from JSON and saves it to a new [PersistentDataContainer] created in the given context
         *
         * @param serializedPdc serialized PersistentDataContainer
         * @param context       PersistentDataAdapterContext
         * @return deserialized PersistentDataContainer
         * @throws JsonSyntaxException if the JSON is malformed
         */
        @Contract(value = "_, _ -> new", pure = true)
        @Throws(JsonSyntaxException::class)
        fun fromJson(
            serializedPdc: String,
            context: PersistentDataAdapterContext
        ): PersistentDataContainer {
            Objects.requireNonNull(serializedPdc, "serializedPdc cannot be null")
            Objects.requireNonNull(context, "context cannot be null")
            return fromMapList(
                ItemParser.gson.fromJson(serializedPdc, LIST_MAP_TYPE_TOKEN.type),
                context.newPersistentDataContainer()
            )
        }

        /**
         * Gets a native [PersistentDataType] by its field name, e.g. "STRING" or "BYTE_ARRAY" (case-sensitive)
         *
         * @param fieldName field name
         * @return native PersistentDataType
         * @throws IllegalArgumentException if no native PersistentDataType was found with the given field name
         */
        @Throws(IllegalArgumentException::class)
        private fun getNativePersistentDataTypeByFieldName(
            fieldName: String
        ): PersistentDataType<*, *> {
            Objects.requireNonNull(fieldName, "fieldName cannot be null")
            val type = NATIVE_PRIMITIVE_PERSISTENT_DATA_TYPES_BY_NAME[fieldName]!!
                ?: throw IllegalArgumentException("Could not find native PersistentDataType with field name $fieldName")
            return type
        }

        /**
         * Gets the field name for the given native [PersistentDataType]
         *
         * @param type native PersistentDataType
         * @return field name
         * @throws IllegalArgumentException if the given PersistentDataType is not native and therefore does not have a field name
         */
        @Throws(IllegalArgumentException::class)
        private fun getNativePersistentDataTypeFieldName(
            type: PersistentDataType<*, *>
        ): String {
            Objects.requireNonNull(type, "type cannot be null")
            val name = NATIVE_PRIMITIVE_PERSISTENT_DATA_TYPES_BY_CLASS[type]
                ?: throw IllegalArgumentException(
                    "Could not find native field name for PersistentDataType with " + "primitive class " +
                            type.primitiveType.name + " and complex class " +
                            type.complexType.name
                )

            return name
        }
    }
}

package tv.ender.itemparser.modal.config

data class Options(
    val modelData: Boolean = true,
    val enchantData: Boolean = true,
    val potionData: Boolean = true,
    val bookData: Boolean = true,
    val nbtValues: Boolean = true,
    val nbtKeys: Boolean = true,
    val rarity: Boolean = false,
) {
    companion object {
        @JvmStatic
        fun builder() = Builder()

        inline fun build(block: Builder.() -> Unit) = Builder().apply(block).build()
    }

    class Builder {
        private var modelData: Boolean = true
        private var enchantData: Boolean = true
        private var potionData: Boolean = true
        private var bookData: Boolean = true
        private var nbtValues: Boolean = true
        private var nbtKeys: Boolean = true

        fun modelData(value: Boolean) = apply { modelData = value }
        fun enchantData(value: Boolean) = apply { enchantData = value }
        fun potionData(value: Boolean) = apply { potionData = value }
        fun bookData(value: Boolean) = apply { bookData = value }
        fun nbtValues(value: Boolean) = apply { nbtValues = value }
        fun nbtKeys(value: Boolean) = apply { nbtKeys = value }

        fun build() = Options(
            modelData, enchantData, potionData, bookData, nbtValues, nbtKeys
        )
    }
}

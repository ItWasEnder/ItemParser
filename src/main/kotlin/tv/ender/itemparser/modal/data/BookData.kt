package tv.ender.itemparser.modal.data

import org.bukkit.inventory.ItemStack
import org.bukkit.inventory.meta.BookMeta
import org.bukkit.inventory.meta.BookMeta.Generation
import org.bukkit.inventory.meta.ItemMeta
import org.bukkit.inventory.meta.WritableBookMeta
import tv.ender.itemparser.modal.interfaces.ItemData
import tv.ender.itemparser.text.ColorUtil

data class BookData(
    var title: String? = null,
    var author: String? = null,
    var generation: Generation? = null,
    var content: List<String> = listOf(),
) : ItemData<WritableBookMeta> {

    override fun isSimilar(stack: ItemStack): Boolean {
        val meta: ItemMeta = stack.itemMeta ?: return false

        if (meta !is WritableBookMeta) return false
        return meta.pages.all { content.contains(it) }
    }

    override fun apply(meta: WritableBookMeta) {
        when (meta) {
            is BookMeta -> {
                this.content.also { pages -> meta.pages = pages.map { ColorUtil.color(it) } }
                this.title?.also { meta.title = ColorUtil.color(it) }
                this.author?.also { meta.author = it }
                this.generation?.also { meta.generation = it }
            }

            else -> {
                this.content = meta.pages
            }
        }
    }
}

fun WritableBookMeta.toBookData(): BookData {
    val meta = this

    return BookData().apply {
        when (meta) {
            is BookMeta -> {
                this.content = meta.pages
                this.title = meta.title
                this.author = meta.author
                this.generation = meta.generation
            }

            else -> {
                this.content = meta.pages
            }
        }
    }
}

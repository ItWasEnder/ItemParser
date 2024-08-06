package tv.ender.itemparser.modal.extensions

/**
 * Returns a copy of this string replacing all "_" (underscore) with " " (space)
 */
fun String.addSpaces(): String {
    return this.replace("_", " ")
}

/**
 * Returns a copy of this string having the first letter of each substring split by " " (space) capitalized
 */
fun String.capitalizeWords(): String {
    return this.split(" ").joinToString(" ") { word ->
        word.lowercase().replaceFirstChar { char -> char.uppercase() }
    }
}

/**
 * Returns a copy of this string after applying [addSpaces] and [capitalizeWords] in that order
 */
fun String.pretty(): String {
    return this.addSpaces().trim().capitalizeWords()
}
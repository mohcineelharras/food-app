package com.foodrecommender.app.domain.usecases

/**
 * Turns user text into plain text before it is stored or drawn.
 * Angle brackets are removed so a later HTML renderer cannot treat the note as markup.
 */
object PlainText {
    const val QUERY_LIMIT = 80
    const val NOTE_LIMIT = 280
    private const val SCAN_LIMIT = 2_000
    private val TAG = Regex("<[^>]*>")

    fun sanitize(raw: String, maxLength: Int): String {
        val bounded = raw.take(SCAN_LIMIT)
        val withoutTags = TAG.replace(bounded, "")
        val builder = StringBuilder(withoutTags.length)
        for (ch in withoutTags) {
            when {
                ch == '<' || ch == '>' -> Unit
                ch == '\n' || ch == '\r' || ch == '\t' -> builder.append(' ')
                ch.isISOControl() -> Unit
                else -> builder.append(ch)
            }
        }
        return builder.toString().replace(Regex(" {2,}"), " ").trim().take(maxLength)
    }
}

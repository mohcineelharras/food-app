package com.foodrecommender.app.data.local

import com.foodrecommender.app.domain.PreferenceStore
import com.foodrecommender.app.domain.models.Diet
import com.foodrecommender.app.domain.models.UserPreference
import com.foodrecommender.app.domain.usecases.PlainText
import java.io.File
import java.io.IOException

/**
 * Stores the diet filter and kitchen note in app-private storage.
 * The file name is fixed; user text never becomes a path.
 */
class PreferenceFileStore(root: File) : PreferenceStore {
    private val root = root.canonicalFile.apply { mkdirs() }

    override fun read(): UserPreference = synchronized(lock) {
        val file = preferenceFile()
        if (!file.isFile) UserPreference(Diet.ANY, "") else decode(file.readText())
    }

    override fun write(preference: UserPreference) {
        val safe = UserPreference(
            diet = preference.diet,
            note = PlainText.sanitize(preference.note, PlainText.NOTE_LIMIT),
        )
        val payload = encode(safe)
        synchronized(lock) {
            val file = preferenceFile()
            val tmp = SandboxPaths.resolve(root, TEMP_NAME)
            tmp.writeText(payload)
            restrictToOwner(tmp)
            if (!tmp.renameTo(file)) {
                tmp.delete()
                throw IOException("Couldn't store preference")
            }
            restrictToOwner(file)
        }
    }

    private fun preferenceFile(): File = SandboxPaths.resolve(root, FILE_NAME)

    private fun encode(preference: UserPreference): String {
        return "diet=${preference.diet.name}\nnote=${preference.note}\n"
    }

    private fun decode(text: String): UserPreference {
        var diet = Diet.ANY
        var note = ""
        for (line in text.lineSequence()) {
            when {
                line.startsWith(DIET_PREFIX) -> {
                    val raw = line.removePrefix(DIET_PREFIX)
                    diet = Diet.entries.firstOrNull { it.name == raw } ?: Diet.ANY
                }
                line.startsWith(NOTE_PREFIX) -> {
                    note = PlainText.sanitize(line.removePrefix(NOTE_PREFIX), PlainText.NOTE_LIMIT)
                }
            }
        }
        return UserPreference(diet, note)
    }

    private fun restrictToOwner(file: File) {
        file.setReadable(false, false)
        file.setWritable(false, false)
        file.setExecutable(false, false)
        file.setReadable(true, true)
        file.setWritable(true, true)
    }

    private val lock = Any()

    private companion object {
        const val FILE_NAME = "user-preference.txt"
        const val TEMP_NAME = "user-preference.txt.tmp"
        const val DIET_PREFIX = "diet="
        const val NOTE_PREFIX = "note="
    }
}

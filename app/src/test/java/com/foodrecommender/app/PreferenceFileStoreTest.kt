package com.foodrecommender.app

import com.foodrecommender.app.data.local.PreferenceFileStore
import com.foodrecommender.app.domain.models.Diet
import com.foodrecommender.app.domain.models.UserPreference
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import java.nio.file.Files

class PreferenceFileStoreTest {
    @Test
    fun roundTripStripsMarkupAndStaysInsideTheRoot() {
        val root = Files.createTempDirectory("food-prefs").toFile()
        val store = PreferenceFileStore(root)
        store.write(UserPreference(Diet.VEGAN, "<b>no nuts</b>"))
        val saved = store.read()
        assertEquals(Diet.VEGAN, saved.diet)
        assertEquals("no nuts", saved.note)
        store.write(UserPreference(Diet.GLUTEN_FREE, "rice"))
        val again = store.read()
        assertEquals(Diet.GLUTEN_FREE, again.diet)
        assertEquals("rice", again.note)
        val files = root.canonicalFile.walkTopDown().filter { it.isFile }.toList()
        assertEquals(1, files.size)
        assertEquals("user-preference.txt", files.single().name)
        assertTrue(files.single().canonicalPath.startsWith(root.canonicalPath + java.io.File.separator))
    }

    @Test
    fun corruptDietFallsBackToAny() {
        val root = Files.createTempDirectory("food-prefs").toFile()
        val store = PreferenceFileStore(root)
        store.write(UserPreference(Diet.ANY, "ok"))
        val file = root.listFiles()?.single() ?: error("missing preference file")
        file.writeText("diet=NOT_A_DIET\nnote=<i>soup</i>\n")
        val saved = store.read()
        assertEquals(Diet.ANY, saved.diet)
        assertEquals("soup", saved.note)
    }
}

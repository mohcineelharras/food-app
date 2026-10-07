package com.foodrecommender.app

import com.foodrecommender.app.data.local.SandboxPaths
import org.junit.Assert.assertEquals
import org.junit.Assert.assertThrows
import org.junit.Test
import java.io.File
import java.nio.file.Files

class SandboxPathsTest {
    @Test
    fun resolvesAFileInsideTheRoot() {
        val root = Files.createTempDirectory("food-root").toFile()
        val resolved = SandboxPaths.resolve(root, "user-preference.txt")
        assertEquals(File(root.canonicalFile, "user-preference.txt").canonicalFile, resolved)
    }

    @Test
    fun rejectsTraversalAbsoluteAndNull() {
        val root = Files.createTempDirectory("food-root").toFile()
        assertThrows(SecurityException::class.java) {
            SandboxPaths.resolve(root, "../outside.txt")
        }
        assertThrows(SecurityException::class.java) {
            SandboxPaths.resolve(root, "nested/../../outside.txt")
        }
        assertThrows(SecurityException::class.java) {
            SandboxPaths.resolve(root, "/etc/passwd")
        }
        assertThrows(SecurityException::class.java) {
            SandboxPaths.resolve(root, "bad\u0000name")
        }
    }

    @Test
    fun rejectsSymlinkThatLeavesTheRoot() {
        val temp = Files.createTempDirectory("food-link").toFile()
        val root = File(temp, "private").apply { mkdirs() }
        val outside = File(temp, "outside").apply { mkdirs() }
        Files.createSymbolicLink(File(root, "link").toPath(), outside.toPath())
        assertThrows(SecurityException::class.java) {
            SandboxPaths.resolve(root, "link/secret.txt")
        }
    }
}

package com.foodrecommender.app.data.local

import java.io.File

/**
 * Resolves a relative path and rejects anything that leaves [root], including
 * symlink escapes. Callers pass app-private directories only.
 */
object SandboxPaths {
    fun resolve(root: File, relative: String): File {
        if (relative.isBlank() || relative.indexOf('\u0000') >= 0) {
            throw SecurityException("Path is empty")
        }
        val requested = File(relative)
        if (requested.isAbsolute) {
            throw SecurityException("Absolute paths are not allowed")
        }
        val rootCanon = root.canonicalFile
        val target = File(rootCanon, relative).canonicalFile
        val rootPrefix = rootCanon.path + File.separator
        if (target.path != rootCanon.path && !target.path.startsWith(rootPrefix)) {
            throw SecurityException("Path escapes app storage")
        }
        return target
    }
}

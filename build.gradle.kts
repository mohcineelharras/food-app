plugins {
    base
    id("com.android.application") version "8.13.2" apply false
    id("org.jetbrains.kotlin.android") version "2.1.21" apply false
    id("com.google.devtools.ksp") version "2.1.21-2.0.2" apply false
}

tasks.register<Exec>("releaseHygiene") {
    group = "verification"
    description = "Fails the build when secrets, cleartext, or removed risky dependencies reappear."
    workingDir = rootDir
    commandLine("bash", "scripts/check-release-hygiene.sh")
}

tasks.named("check") {
    dependsOn("releaseHygiene")
    dependsOn(":app:check")
}

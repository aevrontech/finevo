plugins {
    // Apply plugins but don't apply them to the root project
    alias(libs.plugins.androidApplication) apply false
    alias(libs.plugins.androidLibrary) apply false
    alias(libs.plugins.kotlinMultiplatform) apply false
    alias(libs.plugins.kotlinSerialization) apply false
    alias(libs.plugins.composeMultiplatform) apply false
    alias(libs.plugins.composeCompiler) apply false
    alias(libs.plugins.sqldelight) apply false
    alias(libs.plugins.ksp) apply false
    // TODO: Add in Phase 4
    // alias(libs.plugins.mokoResources) apply false
}

// Root project tasks
tasks.register("clean", Delete::class) {
    delete(rootProject.layout.buildDirectory)
}

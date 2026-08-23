/**
 * Project-level build configuration.
 *
 * Uses the Gradle Version Catalog (libs.versions.toml) for all plugin versions.
 * All plugins are declared here with `apply false` so they can be applied
 * selectively in sub-module build files.
 */
plugins {
    alias(libs.plugins.android.application) apply false
    alias(libs.plugins.kotlin.android) apply false
    alias(libs.plugins.ksp) apply false
    alias(libs.plugins.hilt) apply false
    alias(libs.plugins.compose.compiler) apply false
    alias(libs.plugins.google.services) apply false
    alias(libs.plugins.kotlin.serialization) apply false
}

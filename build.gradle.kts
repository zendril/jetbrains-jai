import org.jetbrains.kotlin.gradle.tasks.KotlinCompile
import org.jetbrains.kotlin.gradle.dsl.JvmTarget

plugins {
    // Apply the Kotlin JVM plugin and set the version
    kotlin("jvm") version "2.1.20"

    // Apply the IntelliJ Platform plugin for building IDE plugins
    id("org.jetbrains.intellij.platform") version "2.6.0" // Updated plugin ID and version

    // Apply the Java plugin (often useful, and intellij plugin might expect it)
    java
}

group = "com.zendril.jetbrains.jai.lsp" // TODO: Change to your plugin's group
version = "1.0-SNAPSHOT" // TODO: Change to your plugin's version

repositories {
    // Standard repository for most dependencies
    mavenCentral()
    intellijPlatform {
        defaultRepositories()
    }
}

// Configure the IntelliJ Platform plugin
//intellijPlatform {
//    // Configuration for the new plugin will go here.
//    // The way you set the target IDE version, type, sinceBuild, untilBuild, etc.,
//    // will be different with this new plugin.
//    // For example, to set the target IDE version you might use something like:
//    // targetIde("CL", "2025.1.1") // This is an illustrative example, check docs for exact syntax
//
//    // Your old settings:
//    // version.set("2025.1.1") // This will change
//    // type.set("CL") // This will change
//    // updateSinceUntilBuild.set(false) // This concept is handled differently
//}

// Configure Java compatibility
java {
    sourceCompatibility = JavaVersion.VERSION_17
    targetCompatibility = JavaVersion.VERSION_17
}

// Configure Kotlin options
tasks.withType<KotlinCompile>().configureEach {
    compilerOptions {
        jvmTarget.set(JvmTarget.JVM_17)
        // You can add other Kotlin compiler options here if needed
        // freeCompilerArgs.add("-Xcontext-receivers") // Example
    }
}

//dependencies {
//    // Add the Kotlin standard library
//    implementation(kotlin("stdlib"))
//
//    // Add test dependencies if you have tests
//    // testImplementation(kotlin("test"))
//    // testImplementation("org.junit.jupiter:junit-jupiter-api:5.10.0")
//    // testRuntimeOnly("org.junit.jupiter:junit-jupiter-engine:5.10.0")
//}

dependencies {
    implementation(kotlin("stdlib"))

    intellijPlatform {
        clion("2025.1.1")
        bundledPlugin("com.intellij.clion")
    }
}

// (Optional) Configuration for specific tasks
tasks {
    // Example: Disable the buildSearchableOptions task if you don't need it
    // This task can sometimes cause issues or slow down builds if not configured.
    named("buildSearchableOptions") {
        enabled = false
    }

    // (Optional) If you need to configure the runIde task, e.g., to point to a local CLion installation
//     named<org.jetbrains.intellij.tasks.RunIdeTask>("runIde") {
//         ideDir.set(project.file("C:\\Users\\kenne\\AppData\\Local\\Programs\\CLion\\bin\\clion64.exe"))
//     }
}

// (Optional) Patch the plugin.xml file, for example, to set the since-build and until-build
// tasks.named<org.jetbrains.intellij.platform.tasks.PatchPluginXmlTask>("patchPluginXml") { // Note the updated type
//     // Configuration for sinceBuild/untilBuild is often handled in the `pluginConfiguration` block now
//     // For example:
//     // ideaVersion {
//     //     sinceBuild.set("...")
//     //     untilBuild.set("...")
//     // }
//     // Consult the new plugin's documentation for the recommended way.
// }
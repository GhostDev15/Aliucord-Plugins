import com.aliucord.gradle.AliucordExtension
import com.android.build.gradle.BaseExtension

buildscript {
    repositories {
        google()
        mavenCentral()
        // Aliucords Maven repo which contains our tools and dependencies
        maven("https://maven.aliucord.com/snapshots")
        // Shitpack which still contains some Aliucord dependencies for now. TODO: Remove
        maven("https://jitpack.io")
    }

    dependencies {
        classpath("com.android.tools.build:gradle:7.0.4")
        // Aliucord gradle plugin which makes everything work and builds plugins
        classpath("com.aliucord:gradle:main-SNAPSHOT")
        // Kotlin support. Remove if you want to use Java
        classpath("org.jetbrains.kotlin:kotlin-gradle-plugin:1.5.21")
    }
}

allprojects {
    repositories {
        google()
        mavenCentral()
        maven("https://maven.aliucord.com/snapshots")
    }
}

fun Project.aliucord(configuration: AliucordExtension.() -> Unit) = extensions.getByName<AliucordExtension>("aliucord").configuration()

fun Project.android(configuration: BaseExtension.() -> Unit) = extensions.getByName<BaseExtension>("android").configuration()

subprojects {
    apply(plugin = "com.android.library")
    apply(plugin = "com.aliucord.gradle")
    // Remove if using Java
    apply(plugin = "kotlin-android")

    // Fill out with your info
    aliucord {
        author("Namenlosxy", 339303461877186560L)
        github("https://www.github.com/GhostDev15/AliucordPlugins")
        updateUrl.set("https://raw.githubusercontent.com/GhostDev15/Aliucord-Plugins/builds/updater.json")
        buildUrl.set("https://raw.githubusercontent.com/GhostDev15/Aliucord-Plugins/builds/%s.zip")
    }

    android {
        compileSdkVersion(31)

        defaultConfig {
            minSdk = 24
            targetSdk = 31
        }

        compileOptions {
            sourceCompatibility = JavaVersion.VERSION_11
            targetCompatibility = JavaVersion.VERSION_11
        }

        tasks.withType<org.jetbrains.kotlin.gradle.tasks.KotlinCompile> {
            kotlinOptions {
                jvmTarget = "11" // Required
                // Disables some unnecessary features
                freeCompilerArgs = freeCompilerArgs +
                        "-Xno-call-assertions" +
                        "-Xno-param-assertions" +
                        "-Xno-receiver-assertions"
            }
        }
    }

    dependencies {
        val discord by configurations
        val implementation by configurations

        // Stubs for all Discord classes
        discord("com.discord:discord:aliucord-SNAPSHOT")
        implementation("com.aliucord:Aliucord:main-SNAPSHOT")

        implementation("androidx.appcompat:appcompat:1.4.1")
        implementation("com.google.android.material:material:1.5.0")
        implementation("androidx.constraintlayout:constraintlayout:2.1.3")
    }
}

task<Delete>("clean") {
    delete(rootProject.buildDir)
}

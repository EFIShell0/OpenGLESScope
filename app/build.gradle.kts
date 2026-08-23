import com.android.build.api.artifact.SingleArtifact
import com.android.build.api.variant.BuiltArtifactsLoader
import org.gradle.api.DefaultTask
import org.gradle.api.file.DirectoryProperty
import org.gradle.api.provider.Property
import org.gradle.api.tasks.Input
import org.gradle.api.tasks.InputFiles
import org.gradle.api.tasks.Internal
import org.gradle.api.tasks.OutputDirectory
import org.gradle.api.tasks.TaskAction
import java.io.File
import java.util.zip.ZipFile

abstract class VerifyReleaseApksTask : DefaultTask() {
    @get:InputFiles
    abstract val apkFolder: DirectoryProperty

    @get:Internal
    abstract val builtArtifactsLoader: Property<BuiltArtifactsLoader>

    @get:OutputDirectory
    abstract val artifactDir: DirectoryProperty

    @get:Input
    abstract val releaseName: Property<String>

    @TaskAction
    fun verifyReleaseApks() {
        val builtArtifacts = builtArtifactsLoader.get().load(apkFolder.get())
            ?: throw GradleException("Could not load release APK metadata")
        val apks = builtArtifacts.elements.map { element -> File(element.outputFile) }
        val expected = linkedMapOf(
            "universal" to setOf("arm64-v8a", "armeabi-v7a", "x86_64"),
            "arm64-v8a" to setOf("arm64-v8a"),
            "armeabi-v7a" to setOf("armeabi-v7a"),
            "x86_64" to setOf("x86_64")
        )
        fun apkAbis(apk: File): Set<String> {
            if (!apk.isFile) throw GradleException("Release APK does not exist: ${apk.absolutePath}")
            val actualAbis = mutableSetOf<String>()
            ZipFile(apk).use { zip ->
                val entries = zip.entries()
                while (entries.hasMoreElements()) {
                    val entryName = entries.nextElement().name
                    if (entryName.startsWith("lib/") && entryName.endsWith(".so")) {
                        val parts = entryName.split('/')
                        if (parts.size > 2) actualAbis.add(parts[1])
                    }
                }
            }
            return actualAbis
        }
        val classified = linkedMapOf<File, Set<String>>()
        for (apk in apks) classified[apk] = apkAbis(apk)
        if (classified.size != 4) {
            val details = classified.entries.joinToString { entry -> "${entry.key.absolutePath}=${entry.value}" }
            throw GradleException("Expected exactly four release APK artifacts, found ${classified.size}: $details")
        }
        for ((apk, abis) in classified) {
            if (abis.contains("x86")) throw GradleException("x86 must not be present in ${apk.absolutePath}: $abis")
        }
        val selected = linkedMapOf<String, File>()
        for ((key, requiredAbis) in expected) {
            val matches = classified.entries.filter { entry -> entry.value == requiredAbis }.map { entry -> entry.key }
            if (matches.size != 1) {
                val details = classified.entries.joinToString { entry -> "${entry.key.absolutePath}=${entry.value}" }
                throw GradleException("Expected exactly one $key APK with ABIs $requiredAbis, found ${matches.map { it.absolutePath }}. Outputs: $details")
            }
            selected[key] = matches.single()
        }
        val outputDir = artifactDir.get().asFile
        outputDir.deleteRecursively()
        if (!outputDir.mkdirs() && !outputDir.isDirectory) throw GradleException("Could not create ${outputDir.absolutePath}")
        for ((key, sourceApk) in selected) {
            val target = File(outputDir, "OpenGLESScope-${releaseName.get()}-$key-release.apk")
            sourceApk.copyTo(target, overwrite = true)
            val expectedAbis = expected.getValue(key)
            val copiedAbis = apkAbis(target)
            if (copiedAbis != expectedAbis) throw GradleException("${target.name} ABI mismatch: $copiedAbis, expected $expectedAbis")
        }
    }
}

plugins {
    id("com.android.application")
    id("org.jetbrains.kotlin.plugin.compose")
}

val releaseVersionName = "0.3.0"
val releaseVersionCode = 300

android {
    namespace = "com.efishell.openglesscope"
    compileSdk = 37
    defaultConfig {
        applicationId = "com.efishell.openglesscope"
        minSdk = 24
        targetSdk = 37
        versionCode = releaseVersionCode
        versionName = releaseVersionName
        ndkVersion = "29.0.14206865"
        ndk { abiFilters += listOf("arm64-v8a", "armeabi-v7a", "x86_64") }
    }
    splits {
        abi {
            isEnable = true
            reset()
            include("arm64-v8a", "armeabi-v7a", "x86_64")
            isUniversalApk = true
        }
    }
    buildFeatures {
        buildConfig = true
    }
    externalNativeBuild { cmake { path = file("src/main/cpp/CMakeLists.txt") } }
    packaging { jniLibs { useLegacyPackaging = true } }
    buildTypes {
        release {
            isDebuggable = false
            isMinifyEnabled = true
            isShrinkResources = true
            proguardFiles(getDefaultProguardFile("proguard-android-optimize.txt"), "proguard-rules.pro")
        }
        debug { isDebuggable = true }
    }
}

dependencies {
    implementation("androidx.core:core-ktx:1.19.0")
    implementation("androidx.activity:activity-compose:1.13.0")
    implementation("androidx.compose.ui:ui:1.12.0")
    implementation("androidx.compose.foundation:foundation:1.12.0")
    implementation("androidx.compose.animation:animation:1.12.0")
    implementation("androidx.compose.material3:material3:1.5.0-alpha26")
    implementation("androidx.lifecycle:lifecycle-runtime-compose:2.11.0")
    implementation("androidx.lifecycle:lifecycle-runtime-ktx:2.11.0")
    implementation("com.squareup.okhttp3:okhttp:5.3.0")
}

androidComponents {
    onVariants(selector().withBuildType("release")) { variant ->
        val verifyReleaseApks = tasks.register<VerifyReleaseApksTask>("verifyReleaseApks") {
            apkFolder.set(variant.artifacts.get(SingleArtifact.APK))
            builtArtifactsLoader.set(variant.artifacts.getBuiltArtifactsLoader())
            artifactDir.set(layout.buildDirectory.dir("outputs/releaseArtifacts"))
            releaseName.set(releaseVersionName)
        }
        tasks.configureEach {
            if (name == "assembleRelease") dependsOn(verifyReleaseApks)
        }
    }
}

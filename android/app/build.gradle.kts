plugins {
    id("com.android.application")
    id("org.jetbrains.kotlin.android")
    id("org.jetbrains.kotlin.plugin.compose")
    id("org.jetbrains.kotlin.plugin.serialization")
}

// 工作流规则（2026-09-02 定）：每次打包测试（assembleDebug）自动 patch+1，
// 无论是否 commit 都写回本文件，保证每次产出的 APK 版本号唯一。
// 下面两行是版本状态（上次产出的版本），由 bumpVersion 任务在构建时写回新值；
// defaultConfig 使用其 +1 后的值，故本次 APK 直接带上新版本号。
val versionCodeState = 7
val versionNameState = "0.1.6"

val versionScriptFile = file("build.gradle.kts")
val versionScript = versionScriptFile.readText()
val codeMatch = requireNotNull(
    Regex("""^val versionCodeState\s*=\s*(\d+)$""", RegexOption.MULTILINE).find(versionScript)
) { "versionCodeState not found in build.gradle.kts" }
val nameMatch = requireNotNull(
    Regex("""^val versionNameState\s*=\s*"(\d+)\.(\d+)\.(\d+)"$""", RegexOption.MULTILINE).find(versionScript)
) { "versionNameState not found in build.gradle.kts" }
val nextCode = codeMatch.groupValues[1].toInt() + 1
val nextName = "${nameMatch.groupValues[1]}.${nameMatch.groupValues[2]}.${nameMatch.groupValues[3].toInt() + 1}"

tasks.register("bumpVersion") {
    doLast {
        // 整行替换：两行互不重叠，先替换靠后的 name 行，code 行偏移不受影响
        val text = versionScript
            .replaceRange(nameMatch.range, """val versionNameState = "$nextName"""")
            .replaceRange(codeMatch.range, "val versionCodeState = $nextCode")
        versionScriptFile.writeText(text)
        println("bumpVersion: versionName -> $nextName, versionCode -> $nextCode")
    }
}
tasks.matching { it.name == "assembleDebug" }.configureEach {
    dependsOn("bumpVersion")
}

android {
    namespace = "com.showerly.app"
    compileSdk = 35

    defaultConfig {
        applicationId = "com.showerly.app"
        minSdk = 26
        targetSdk = 35
        versionCode = nextCode
        versionName = nextName
    }

    buildTypes {
        release {
            isMinifyEnabled = false
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
        }
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }
    kotlinOptions {
        jvmTarget = "17"
    }
    buildFeatures {
        compose = true
        buildConfig = false
    }
}

dependencies {
    implementation(platform("androidx.compose:compose-bom:2024.12.01"))
    implementation("androidx.compose.ui:ui")
    implementation("androidx.compose.material3:material3")
    implementation("androidx.compose.material:material-icons-core")
    implementation("androidx.compose.ui:ui-tooling-preview")
    debugImplementation("androidx.compose.ui:ui-tooling")

    implementation("androidx.core:core-ktx:1.15.0")
    implementation("androidx.activity:activity-compose:1.9.3")
    implementation("androidx.lifecycle:lifecycle-runtime-ktx:2.8.7")
    implementation("androidx.lifecycle:lifecycle-viewmodel-compose:2.8.7")
    implementation("androidx.navigation:navigation-compose:2.8.5")
    implementation("androidx.datastore:datastore-preferences:1.1.1")

    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-android:1.9.0")
    implementation("org.jetbrains.kotlinx:kotlinx-serialization-json:1.7.3")

    implementation("com.squareup.retrofit2:retrofit:2.11.0")
    implementation("com.squareup.retrofit2:converter-kotlinx-serialization:2.11.0")
    implementation("com.squareup.okhttp3:okhttp:4.12.0")
}

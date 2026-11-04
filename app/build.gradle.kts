import java.util.Properties

plugins {
    alias(libs.plugins.android.application)
}

val dbProperties = Properties().apply {
    val propertiesFile = rootProject.file("local.properties")
    require(propertiesFile.isFile) { "Create local.properties with the required db.* settings." }
    propertiesFile.inputStream().use { load(it) }
}

fun requiredDbProperty(key: String): String =
    dbProperties.getProperty(key)?.takeIf { it.isNotBlank() }
        ?: error("Missing database setting " + key + " in local.properties")

fun buildConfigString(value: String): String =
    "\"" + value.replace("\\", "\\\\").replace("\"", "\\\"") + "\""

android {
    namespace = "com.example.iserveko"

    buildFeatures {
        buildConfig = true
    }
    compileSdk {
        version = release(37)
    }

    defaultConfig {
        applicationId = "com.example.iserveko"
        minSdk = 24
        targetSdk = 37
        versionCode = 1
        versionName = "1.0"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"

        buildConfigField("String", "MSSQL_SERVER", buildConfigString(requiredDbProperty("db.server")))
        buildConfigField("String", "MSSQL_PORT", buildConfigString(requiredDbProperty("db.port")))
        buildConfigField("String", "MSSQL_DATABASE", buildConfigString(requiredDbProperty("db.name")))
        buildConfigField("String", "MSSQL_USERNAME", buildConfigString(requiredDbProperty("db.user")))
        buildConfigField("String", "MSSQL_PASSWORD", buildConfigString(requiredDbProperty("db.password")))
    }

    buildTypes {
        release {
            optimization {
                enable = false
            }
        }
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_11
        targetCompatibility = JavaVersion.VERSION_11
    }
    testOptions {
        unitTests {
            isReturnDefaultValues = true
        }
    }
}

dependencies {
    implementation(libs.activity.ktx)
    implementation(libs.appcompat)
    implementation(libs.constraintlayout)
    implementation(libs.material)
    implementation(libs.recyclerview)
    implementation("androidx.swiperefreshlayout:swiperefreshlayout:1.2.0")
    implementation("com.android.volley:volley:1.2.1")
    implementation("net.sourceforge.jtds:jtds:1.3.1")
    testImplementation(libs.junit)
    androidTestImplementation(libs.espresso.core)
    androidTestImplementation(libs.ext.junit)
}
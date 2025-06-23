plugins {
    id(libs.plugins.android.library.get().pluginId)
    id(libs.plugins.kotlin.android.get().pluginId)
}

android {
    namespace = "com.growingio.android.apm"
    compileSdk = libs.versions.compileSdk.get().toInt()

    defaultConfig {
        minSdk = libs.versions.minSdk.get().toInt()

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
    }

    buildTypes {
        release {
            isMinifyEnabled = false
            proguardFiles(getDefaultProguardFile("proguard-android-optimize.txt"), "proguard-rules.pro")
            consumerProguardFile( "consumer-rules.pro")
        }
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_1_8
        targetCompatibility = JavaVersion.VERSION_1_8
    }
    kotlinOptions {
        jvmTarget = "1.8"
    }
}

dependencies {

    implementation(project(":growingio-tracker-core"))
    debugImplementation(project(":growingio-tools:gmonitor"))
    releaseImplementation(libs.growingio.gmonitor)

    implementation(project(":growingio-annotation"))
    annotationProcessor(project(":growingio-annotation:compiler"))

    testImplementation(libs.bundles.test)
    testImplementation(libs.androidx.test.core)
    testImplementation(libs.androidx.appcompat)
    androidTestImplementation(libs.kotlin.coroutines.test)
}

apply(from = "${rootProject.projectDir}/gradle/publishMavenCentral.gradle")
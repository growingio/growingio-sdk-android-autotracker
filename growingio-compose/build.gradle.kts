plugins {
    id(libs.plugins.android.library.get().pluginId)
    id(libs.plugins.kotlin.android.get().pluginId)
    id(libs.plugins.compose.get().pluginId)
}

android {
    namespace = "com.growingio.android.compose"
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

    compileOnly(platform(libs.androidx.compose.bom))
    compileOnly(libs.androidx.compose.foundation)
    compileOnly(libs.androidx.compose.ui)
    compileOnly(libs.androidx.activity.compose)

    implementation(project(":growingio-autotracker-core"))
    implementation(project(":growingio-annotation"))
    annotationProcessor(project(":growingio-annotation:compiler"))


    testImplementation(libs.junit)
    androidTestImplementation(libs.junit)
    androidTestImplementation(libs.kotlin.coroutines.test)
    androidTestImplementation(platform(libs.androidx.compose.bom))
    androidTestImplementation(libs.androidx.compose.ui.test)
    androidTestImplementation(libs.androidx.compose.ui.test.junit4)
}

apply(from = "${rootProject.projectDir}/gradle/publishMaven.gradle")
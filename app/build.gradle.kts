import java.util.Properties

plugins {
    id("com.android.application")
    id("org.jetbrains.kotlin.android")
    id("com.google.dagger.hilt.android")
}

android {
    namespace = "com.okcaros.minusscreen"
    compileSdk = 34

    defaultConfig {
        applicationId = "com.okcaros.minusscreen"
        minSdk = 24
        targetSdk = 34
        versionCode = 3
        versionName = "1.0.3"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"

        buildFeatures {
            aidl = true
        }
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_1_8
        targetCompatibility = JavaVersion.VERSION_1_8
    }

    val keystorePropertiesFile = rootProject.file("keystore.properties")
    var releaseSigning = signingConfigs.getByName("debug")
    if (keystorePropertiesFile.exists()) {
        val keystoreProperties = Properties()
        keystoreProperties.load(keystorePropertiesFile.inputStream())
        releaseSigning = signingConfigs.maybeCreate("debug")
        releaseSigning.keyAlias(keystoreProperties["keyAlias"] as String)
        releaseSigning.keyPassword(keystoreProperties["keyPassword"] as String)
        releaseSigning.storeFile(keystoreProperties["storeFile"]?.let { rootProject.file(it) } as File)
        releaseSigning.storePassword(keystoreProperties["storePassword"] as String)
    }

    buildTypes {
        release {
            isMinifyEnabled = true
            isShrinkResources = true
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
        }
        debug {
            isMinifyEnabled = false
            isShrinkResources = false
            signingConfig = releaseSigning
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
        }
    }

    android.applicationVariants.all {
        val buildType = this.buildType.name
        if (buildType.equals("release")) {
            outputs.all {
                if (this is com.android.build.gradle.internal.api.ApkVariantOutputImpl) {
                    this.outputFileName = "minusscreen.apk"
                }
            }
        }
    }
}

dependencies {

    implementation("androidx.appcompat:appcompat:1.6.1")
    implementation("com.google.android.material:material:1.9.0")
    testImplementation("junit:junit:4.13.2")
    androidTestImplementation("androidx.test.ext:junit:1.1.5")
    androidTestImplementation("androidx.test.espresso:espresso-core:3.5.1")


    implementation("com.alibaba:fastjson:1.1.72.android")
    implementation("org.greenrobot:eventbus:3.3.1")
    implementation("com.squareup.retrofit2:adapter-rxjava3:2.9.0")
    implementation("com.orhanobut:logger:2.2.0")
    implementation("com.squareup.retrofit2:retrofit:2.9.0")
    implementation("io.reactivex.rxjava3:rxandroid:3.0.2")
    implementation("io.reactivex.rxjava3:rxjava:3.1.6")

    implementation("com.google.dagger:hilt-android:2.44")
    implementation("androidx.preference:preference:1.2.1")
    annotationProcessor("com.google.dagger:hilt-android-compiler:2.44")
}

// Allow references to generated code
//kapt {
//    correctErrorTypes = true
//}

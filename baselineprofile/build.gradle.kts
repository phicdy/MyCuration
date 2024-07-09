import com.android.build.api.dsl.ManagedVirtualDevice

plugins {
    alias(libs.plugins.android.test)
    alias(libs.plugins.kotlin.gradle)
    alias(libs.plugins.baselineprofile)
}

android {
    namespace = "com.phicdy.baselineprofile"
    compileSdk = 34

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_1_8
        targetCompatibility = JavaVersion.VERSION_1_8
    }

    kotlinOptions {
        jvmTarget = "1.8"
    }

    defaultConfig {
        minSdk = 28
        targetSdk = 34

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
    }

    testOptions.managedDevices.devices {
        create<ManagedVirtualDevice>("pixel6Api33") {
            device = "Pixel 6"
            apiLevel = 33
            systemImageSource = "aosp"
        }
    }

    targetProjectPath = ":app"

}

baselineProfile {
    managedDevices += "pixel6Api33"
    useConnectedDevices = false
}

dependencies {
    implementation(libs.androidx.test)
    implementation(libs.espresso)
    implementation(libs.uiautomator)
    implementation(libs.macrobenchmark)
}

androidComponents {
    onVariants { v ->
        val artifactsLoader = v.artifacts.getBuiltArtifactsLoader()
        v.instrumentationRunnerArguments.put(
            "targetAppId",
            v.testedApks.map { artifactsLoader.load(it)?.applicationId }
        )
    }
}
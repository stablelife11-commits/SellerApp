plugins {
    alias(libs.plugins.android.application)
    id("com.google.gms.google-services")
}

android {
    namespace = "com.retail.seller"
    compileSdk = 36

    defaultConfig {
        applicationId = "com.retail.seller"
        minSdk = 26
        targetSdk = 36
        versionCode = 2
        versionName = "1.1"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"

        // Updated Render Live URL with /api/v1/
        buildConfigField("String", "BASE_URL", "\"https://retail-flow-backend-xqud.onrender.com/api/v1/\"")
        buildConfigField("String", "CLOUDINARY_CLOUD_NAME", "\"kfapwod1\"")
        buildConfigField("String", "CLOUDINARY_UPLOAD_PRESET", "\"retail_flow_unsigned\"")
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
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_11
        targetCompatibility = JavaVersion.VERSION_11
    }
    buildFeatures {
        buildConfig = true
    }
}

dependencies {
    implementation(libs.androidx.appcompat)
    implementation(libs.androidx.constraintlayout)
    implementation(libs.material)

    // Retrofit & Gson
    implementation("com.squareup.retrofit2:retrofit:2.11.0")
    implementation("com.squareup.retrofit2:converter-gson:2.11.0")
    implementation("com.squareup.okhttp3:logging-interceptor:4.12.0")

    // Encrypted SharedPreferences for secure JWT storage
    implementation("androidx.security:security-crypto:1.1.0-alpha06")

    // UI Components
    implementation("androidx.recyclerview:recyclerview:1.3.2")
    implementation("androidx.cardview:cardview:1.0.0")
    implementation("androidx.swiperefreshlayout:swiperefreshlayout:1.1.0")

    // Image Cropping (uCrop) & Fast Image Loading/Caching (Glide)
    implementation("com.github.yalantis:ucrop:2.2.9")
    implementation("com.github.bumptech.glide:glide:4.16.0")

    // Firebase Cloud Messaging (0% Battery Push Notifications)
    implementation(platform("com.google.firebase:firebase-bom:33.7.0"))
    implementation("com.google.firebase:firebase-messaging")

    testImplementation(libs.junit)
    androidTestImplementation(libs.androidx.espresso.core)
    androidTestImplementation(libs.androidx.junit)
}
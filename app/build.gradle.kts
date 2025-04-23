    plugins {
        alias(libs.plugins.android.application)
        id("com.google.gms.google-services")
    }



    android {
        namespace = "com.example.project"
        compileSdk = 35

        defaultConfig {
            applicationId = "com.example.project"
            minSdk = 24
            targetSdk = 32
            versionCode = 1
            versionName = "1.0"

            testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
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
            sourceCompatibility = JavaVersion.VERSION_11
            targetCompatibility = JavaVersion.VERSION_11
        }
    }

    dependencies {

        implementation(libs.appcompat)
        implementation(libs.material)
        implementation(libs.activity)
        implementation(libs.constraintlayout)
        implementation(libs.firebase.auth)
        implementation(libs.credentials)
        implementation(libs.credentials.play.services.auth)
        implementation(libs.googleid)
        testImplementation(libs.junit)
        androidTestImplementation(libs.ext.junit)
        androidTestImplementation(libs.espresso.core)
        implementation(platform("com.google.firebase:firebase-bom:33.10.0"))
        implementation("com.google.firebase:firebase-analytics")
        implementation("com.google.firebase:firebase-auth")
        implementation("com.google.firebase:firebase-firestore:24.9.1")

        implementation("com.google.android.gms:play-services-auth:21.3.0")
        implementation("com.google.android.material:material:1.10.0")

        implementation ("androidx.recyclerview:recyclerview:1.3.1")

        implementation ("androidx.navigation:navigation-fragment-ktx:2.7.5")
        implementation ("androidx.navigation:navigation-ui-ktx:2.7.5")



    }
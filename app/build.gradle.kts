plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.android)
    alias(libs.plugins.kotlin.compose)
    // Adicione o plugin do Protobuf
    id("com.google.protobuf") version "0.9.4" // Verifique a versão mais recente
}

android {
    namespace = "com.example.pokerpote"
    compileSdk = 35

    defaultConfig {
        applicationId = "com.example.pokerpote"
        minSdk = 24
        targetSdk = 35
        versionCode = 1
        versionName = "1.0"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
        vectorDrawables {
            useSupportLibrary = true
        }
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
    kotlinOptions {
        jvmTarget = "11"
    }
    buildFeatures {
        compose = true
    }
    composeOptions {
        kotlinCompilerExtensionVersion = "1.5.3" // Verifique a versão compatível
    }
    packaging {
        resources {
            excludes += "/META-INF/{AL2.0,LGPL2.1}"
        }
    }
}

// Configuração do Protobuf
protobuf {
    protoc {
        artifact = "com.google.protobuf:protoc:3.25.1" // Verifique a versão mais recente
    }
    generateProtoTasks {
        all().forEach { task ->
            task.builtins {
                create("java") {
                    option("lite") // Importante para Android
                }
            }
            // Adicione 'kotlin { option 'lite' }' se quiser gerar stubs Kotlin (opcional)
            /*
            task.builtins {
                 create("kotlin") {
                     option("lite")
                 }
            }
            */
        }
    }
}

dependencies {

    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.lifecycle.runtime.ktx)
    implementation(libs.androidx.activity.compose)
    implementation(platform(libs.androidx.compose.bom))
    implementation(libs.androidx.ui)
    implementation(libs.androidx.ui.graphics)
    implementation(libs.androidx.ui.tooling.preview)
    implementation(libs.androidx.material3)
    implementation(libs.androidx.datastore.core.android)
    testImplementation(libs.junit)
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.espresso.core)
    androidTestImplementation(platform(libs.androidx.compose.bom))
    androidTestImplementation(libs.androidx.ui.test.junit4)
    debugImplementation(libs.androidx.ui.tooling)
    debugImplementation(libs.androidx.ui.test.manifest)
    implementation("androidx.datastore:datastore:1.1.1") // Núcleo do DataStore
    implementation("com.google.protobuf:protobuf-javalite:3.25.1") // Runtime leve do Protobuf
    implementation("androidx.compose.runtime:runtime-livedata:1.6.6") // Ou versão compatível
    implementation("androidx.lifecycle:lifecycle-viewmodel-compose:2.8.0") // Verifique a versão estável mais recente
}
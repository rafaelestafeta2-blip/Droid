package com.example.model

/**
 * Robust default templates for new Android projects in DroidStudio IDE.
 * Pre-configured with essential runtime permissions in AndroidManifest.xml
 * and market-standard dependencies in build.gradle.kts (Compose M3, Retrofit,
 * Moshi, Coroutines, Room, Coil).
 */
object ProjectTemplateDefaults {

  val DEFAULT_MANIFEST_XML = """
<?xml version="1.0" encoding="utf-8"?>
<manifest xmlns:android="http://schemas.android.com/apk/res/android">

    <!-- Conectividade e Rede -->
    <uses-permission android:name="android.intent.action.INTERNET" />
    <uses-permission android:name="android.permission.INTERNET" />
    <uses-permission android:name="android.permission.ACCESS_NETWORK_STATE" />

    <!-- Câmera e Hardware -->
    <uses-permission android:name="android.permission.CAMERA" />
    <uses-feature android:name="android.hardware.camera" android:required="false" />

    <!-- Localização GPS e Rede -->
    <uses-permission android:name="android.permission.ACCESS_FINE_LOCATION" />
    <uses-permission android:name="android.permission.ACCESS_COARSE_LOCATION" />

    <!-- Notificações Push (Android 13+) -->
    <uses-permission android:name="android.permission.POST_NOTIFICATIONS" />

    <!-- Áudio e Microfone -->
    <uses-permission android:name="android.permission.RECORD_AUDIO" />

    <!-- Mídia e Galeria (Compatibilidade Photo Picker / Android 13+) -->
    <uses-permission android:name="android.permission.READ_MEDIA_IMAGES" />
    <uses-permission android:name="android.permission.READ_MEDIA_VIDEO" />

    <application
        android:allowBackup="true"
        android:icon="@mipmap/ic_launcher"
        android:label="@string/app_name"
        android:roundIcon="@mipmap/ic_launcher_round"
        android:supportsRtl="true"
        android:theme="@style/Theme.Material3.Dark">
        <activity
            android:name=".MainActivity"
            android:exported="true">
            <intent-filter>
                <action android:name="android.intent.action.MAIN" />
                <category android:name="android.intent.category.LAUNCHER" />
            </intent-filter>
        </activity>
    </application>
</manifest>
""".trimIndent()

  val DEFAULT_BUILD_GRADLE_KTS = """
plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.compose)
    alias(libs.plugins.ksp)
}

android {
    namespace = "com.example"
    compileSdk = 36

    defaultConfig {
        applicationId = "com.example.droidapp"
        minSdk = 24
        targetSdk = 36
        versionCode = 1
        versionName = "1.0.0"
    }

    buildFeatures {
        compose = true
    }
}

dependencies {
    // Jetpack Compose & Material 3
    implementation(platform(libs.androidx.compose.bom))
    implementation(libs.androidx.compose.ui)
    implementation(libs.androidx.compose.ui.graphics)
    implementation(libs.androidx.compose.ui.tooling.preview)
    implementation(libs.androidx.compose.material3)
    implementation(libs.androidx.compose.material.icons.extended)
    implementation(libs.androidx.activity.compose)
    implementation(libs.androidx.lifecycle.viewmodel.compose)
    implementation(libs.androidx.lifecycle.runtime.compose)

    // Networking: Retrofit & Moshi
    implementation(libs.retrofit)
    implementation(libs.retrofit.converter.moshi)
    implementation(libs.moshi.kotlin)
    ksp(libs.moshi.kotlin.codegen)
    implementation(libs.okhttp)
    implementation(libs.okhttp.logging)

    // Concorrência: Kotlin Coroutines & Flow
    implementation(libs.kotlinx.coroutines.core)
    implementation(libs.kotlinx.coroutines.android)

    // Imagens: Coil AsyncImage
    implementation(libs.coil.compose)

    // Banco de Dados Local: Room DB
    implementation(libs.room.runtime)
    implementation(libs.room.ktx)
    ksp(libs.room.compiler)
}
""".trimIndent()

  val DEFAULT_MAIN_ACTIVITY_KT = """
package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.RocketLaunch
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

class MainActivity : ComponentActivity() {
  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    enableEdgeToEdge()
    setContent {
      MaterialTheme {
        Scaffold(
          modifier = Modifier
            .fillMaxSize()
            .windowInsetsPadding(WindowInsets.safeDrawing),
          contentWindowInsets = WindowInsets.safeDrawing
        ) { innerPadding ->
          Surface(
            modifier = Modifier
              .fillMaxSize()
              .padding(innerPadding),
            color = Color(0xFF13141C)
          ) {
            AppDashboard()
          }
        }
      }
    }
  }
}

@Composable
fun AppDashboard() {
  Column(
    modifier = Modifier
      .fillMaxSize()
      .padding(24.dp),
    verticalArrangement = Arrangement.Center,
    horizontalAlignment = Alignment.CenterHorizontally
  ) {
    Icon(
      imageVector = Icons.Default.RocketLaunch,
      contentDescription = "Ready",
      tint = Color(0xFF8BE9FD),
      modifier = Modifier.size(56.dp)
    )
    Spacer(modifier = Modifier.height(16.dp))
    Text(
      text = "DroidStudio Mobile Ready",
      color = Color.White,
      fontSize = 20.sp,
      fontWeight = FontWeight.Bold
    )
    Spacer(modifier = Modifier.height(8.dp))
    Text(
      text = "Template pré-configurado com Compose, Retrofit, Room, Coil e Permissões Nativo.",
      color = Color(0xFFA6ACCD),
      fontSize = 13.sp
    )
    Spacer(modifier = Modifier.height(24.dp))
    ConfigItem("Permissões:", "Internet, Câmera, GPS, Notificações, Microfone")
    Spacer(modifier = Modifier.height(8.dp))
    ConfigItem("Bibliotecas:", "Material 3, Retrofit, Moshi, Coroutines, Room, Coil")
  }
}

@Composable
private fun ConfigItem(title: String, desc: String) {
  Card(
    modifier = Modifier.fillMaxWidth(),
    shape = RoundedCornerShape(8.dp),
    colors = CardDefaults.cardColors(containerColor = Color(0xFF1E1F29))
  ) {
    Row(
      modifier = Modifier.padding(12.dp),
      verticalAlignment = Alignment.CenterVertically
    ) {
      Icon(Icons.Default.CheckCircle, contentDescription = null, tint = Color(0xFF50FA7B), modifier = Modifier.size(16.dp))
      Spacer(modifier = Modifier.width(8.dp))
      Column {
        Text(title, color = Color.White, fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
        Text(desc, color = Color(0xFF676E95), fontSize = 11.sp)
      }
    }
  }
}
""".trimIndent()

  val DEFAULT_STRINGS_XML = """
<resources>
    <string name="app_name">DroidApp</string>
    <string name="welcome_message">Bem-vindo ao DroidStudio!</string>
</resources>
""".trimIndent()

  val DEFAULT_SETTINGS_GRADLE_KTS = """
rootProject.name = "DroidApp"
include(":app")
""".trimIndent()
}

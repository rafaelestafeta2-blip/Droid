package com.example.data.supabase

import android.content.Context
import android.content.SharedPreferences
import com.example.data.supabase.model.SupabaseConfig

/**
 * Armazenamento persistente de credenciais do Supabase usando SharedPreferences.
 * Isola a responsabilidade de I/O de configurações locais.
 */
class SupabaseConfigStore(context: Context) {
  private val prefs: SharedPreferences =
    context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)

  fun loadConfig(): SupabaseConfig {
    val token = prefs.getString(KEY_ACCESS_TOKEN, "") ?: ""
    val ref = prefs.getString(KEY_PROJECT_REF, "") ?: ""
    val connected = prefs.getBoolean(KEY_IS_CONNECTED, false)
    return SupabaseConfig(
      accessToken = token,
      projectRef = ref,
      isConnected = connected && token.isNotBlank() && ref.isNotBlank()
    )
  }

  fun saveConfig(config: SupabaseConfig) {
    prefs.edit()
      .putString(KEY_ACCESS_TOKEN, config.accessToken.trim())
      .putString(KEY_PROJECT_REF, config.projectRef.trim())
      .putBoolean(KEY_IS_CONNECTED, config.isConnected)
      .apply()
  }

  fun setConnected(connected: Boolean) {
    prefs.edit().putBoolean(KEY_IS_CONNECTED, connected).apply()
  }

  fun clear() {
    prefs.edit().clear().apply()
  }

  companion object {
    private const val PREFS_NAME = "droidstudio_supabase_config"
    private const val KEY_ACCESS_TOKEN = "supabase_access_token"
    private const val KEY_PROJECT_REF = "supabase_project_ref"
    private const val KEY_IS_CONNECTED = "supabase_is_connected"
  }
}

package com.example.promisclamping.data.local

// data/local/TokenStore.kt
import android.content.Context
import androidx.security.crypto.EncryptedSharedPreferences
import androidx.security.crypto.MasterKey

class TokenStore(context: Context) {
    private val masterKey = MasterKey.Builder(context)
        .setKeyScheme(MasterKey.KeyScheme.AES256_GCM)
        .build()

    private val prefs = EncryptedSharedPreferences.create(
        context,
        "auth_prefs",
        masterKey,
        EncryptedSharedPreferences.PrefKeyEncryptionScheme.AES256_SIV,
        EncryptedSharedPreferences.PrefValueEncryptionScheme.AES256_GCM
    )

    var accessToken: String?
        get() = prefs.getString("securityToken", null)
        set(v) {
            prefs.edit().putString("accessToken", v).apply()
        }

    var expiryEpochSec: Long
        get() = prefs.getLong("expiry", 0L)
        set(v) {
            prefs.edit().putLong("expiry", v).apply()
        }

    fun clear() {
        prefs.edit().clear().apply()
    }

    var userName: String?
        get() = prefs.getString("userName", null)
        set(v) {
            prefs.edit().putString("userName", v).apply()
        }

    var userId: String?
        get() = prefs.getString("userId", null)
        set(v) {
            prefs.edit().putString("userId", v).apply()
        }

}

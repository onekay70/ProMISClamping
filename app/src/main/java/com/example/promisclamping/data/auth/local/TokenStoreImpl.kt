package com.example.promisclamping.data.auth.local

import android.content.Context
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.longPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import com.example.promisclamping.domain.auth.AuthToken
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map

private val Context.dataStore by preferencesDataStore(name = "auth_prefs")

class TokenStoreImpl(private val context: Context) {

    private object Keys {
        val TOKEN = stringPreferencesKey("auth_token")
        val EXPIRES_AT = longPreferencesKey("auth_expires_at")
    }

    suspend fun saveToken(token: AuthToken) {
        context.dataStore.edit { prefs ->
            prefs[Keys.TOKEN] = token.token
            prefs[Keys.EXPIRES_AT] = token.expiresAtMillis
        }
    }

    suspend fun getToken(): AuthToken? {
        val prefs = context.dataStore.data.map { it }.first()
        val t = prefs[Keys.TOKEN]
        val exp = prefs[Keys.EXPIRES_AT]
        return if (t != null && exp != null) AuthToken(t, exp) else null
    }

    suspend fun clear() {
        context.dataStore.edit { prefs ->
            prefs.remove(Keys.TOKEN)
            prefs.remove(Keys.EXPIRES_AT)
        }
    }
}

package com.example.myapplication.data.local.session

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map

private val Context.sessionDataStore: DataStore<Preferences> by preferencesDataStore(
    name = "stickrvault_session"
)

class SessionPreferences(private val context: Context) {

    suspend fun saveSession(userId: String, email: String) {
        context.sessionDataStore.edit { prefs ->
            prefs[KEY_USER_ID] = userId
            prefs[KEY_USER_EMAIL] = email
        }
    }

    suspend fun getSavedUserId(): String? =
        context.sessionDataStore.data.map { it[KEY_USER_ID] }.first()

    suspend fun getSavedEmail(): String? =
        context.sessionDataStore.data.map { it[KEY_USER_EMAIL] }.first()

    suspend fun clearSession() {
        context.sessionDataStore.edit { it.clear() }
    }

    private companion object {
        val KEY_USER_ID = stringPreferencesKey("user_id")
        val KEY_USER_EMAIL = stringPreferencesKey("user_email")
    }
}

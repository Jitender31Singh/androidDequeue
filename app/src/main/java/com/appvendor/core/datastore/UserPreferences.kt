package com.appvendor.core.datastore

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject

val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "user_preferences")

/**
 * Manages user preferences using Jetpack DataStore.
 */
class UserPreferences @Inject constructor(
    private val dataStore: DataStore<Preferences>
) {

    companion object {
        private val KEY_IS_LOGGED_IN = booleanPreferencesKey("is_logged_in")
        private val KEY_USER_TOKEN = stringPreferencesKey("user_token")
        private val KEY_THEME_MODE = stringPreferencesKey("theme_mode")
        private val KEY_USER_ID = stringPreferencesKey("user_id")
        private val KEY_EMAIL = stringPreferencesKey("email")
        private val KEY_NAME = stringPreferencesKey("name")
        private val KEY_SHOP_NAME = stringPreferencesKey("shop_name")
        private val KEY_VENDOR_ID = stringPreferencesKey("vendor_id")
        private val KEY_LOGO_URL = stringPreferencesKey("logo_url")
        private val KEY_USER_ROLES = androidx.datastore.preferences.core.stringSetPreferencesKey("user_roles")
        private val KEY_USER_PERMISSIONS = androidx.datastore.preferences.core.stringSetPreferencesKey("user_permissions")
        private val KEY_ORDER_VISIBILITY_STATUSES = androidx.datastore.preferences.core.stringSetPreferencesKey("order_visibility_statuses")
        private val KEY_REMEMBER_ME = booleanPreferencesKey("remember_me")
        private val KEY_SAVED_EMAIL = stringPreferencesKey("saved_email")
    }

    val isLoggedIn: Flow<Boolean> = dataStore.data.map { preferences ->
        preferences[KEY_IS_LOGGED_IN] ?: false
    }

    val userToken: Flow<String?> = dataStore.data.map { preferences ->
        preferences[KEY_USER_TOKEN]
    }
    
    val themeMode: Flow<String> = dataStore.data.map { preferences ->
        preferences[KEY_THEME_MODE] ?: "system"
    }

    val userId: Flow<String?> = dataStore.data.map { preferences -> preferences[KEY_USER_ID] }
    val userEmail: Flow<String?> = dataStore.data.map { preferences -> preferences[KEY_EMAIL] }
    val userName: Flow<String?> = dataStore.data.map { preferences -> preferences[KEY_NAME] }
    val userShopName: Flow<String?> = dataStore.data.map { preferences -> preferences[KEY_SHOP_NAME] }
    val userVendorId: Flow<String?> = dataStore.data.map { preferences -> preferences[KEY_VENDOR_ID] }
    val logoUrl: Flow<String?> = dataStore.data.map { preferences -> preferences[KEY_LOGO_URL] }
    
    val userRoles: Flow<Set<String>> = dataStore.data.map { preferences ->
        preferences[KEY_USER_ROLES] ?: emptySet()
    }
    
    val userPermissions: Flow<Set<String>> = dataStore.data.map { preferences ->
        preferences[KEY_USER_PERMISSIONS] ?: emptySet()
    }
    
    val orderVisibilityStatuses: Flow<Set<String>> = dataStore.data.map { preferences ->
        preferences[KEY_ORDER_VISIBILITY_STATUSES] ?: emptySet()
    }
    
    val rememberMe: Flow<Boolean> = dataStore.data.map { preferences ->
        preferences[KEY_REMEMBER_ME] ?: false
    }
    
    val savedEmail: Flow<String?> = dataStore.data.map { preferences ->
        preferences[KEY_SAVED_EMAIL]
    }
    
    suspend fun setRememberMe(remember: Boolean, email: String? = null) {
        dataStore.edit { preferences ->
            preferences[KEY_REMEMBER_ME] = remember
            if (remember && email != null) {
                preferences[KEY_SAVED_EMAIL] = email
            } else if (!remember) {
                preferences.remove(KEY_SAVED_EMAIL)
            }
        }
    }

    suspend fun setLoggedIn(isLoggedIn: Boolean) {
        dataStore.edit { preferences ->
            preferences[KEY_IS_LOGGED_IN] = isLoggedIn
        }
    }

    suspend fun setUserToken(token: String) {
        dataStore.edit { preferences ->
            preferences[KEY_USER_TOKEN] = token
        }
    }
    
    suspend fun setLogoUrl(url: String?) {
        dataStore.edit { preferences ->
            if (url == null) {
                preferences.remove(KEY_LOGO_URL)
            } else {
                preferences[KEY_LOGO_URL] = url
            }
        }
    }

    suspend fun clearSession() {
        dataStore.edit { preferences ->
            preferences.remove(KEY_IS_LOGGED_IN)
            preferences.remove(KEY_USER_TOKEN)
            preferences.remove(KEY_NAME)
            preferences.remove(KEY_LOGO_URL)
            preferences.remove(KEY_USER_ROLES)
            preferences.remove(KEY_USER_PERMISSIONS)
            preferences.remove(KEY_ORDER_VISIBILITY_STATUSES)
        }
    }
    
    suspend fun setThemeMode(mode: String) {
        dataStore.edit { preferences ->
            preferences[KEY_THEME_MODE] = mode
        }
    }

    suspend fun saveAuthToken(token: String) {
        setUserToken(token)
    }

    suspend fun saveUserSession(
        userId: String,
        name: String,
        email: String,
        shopName: String,
        vendorId: String,
        logoUrl: String? = null,
        roles: Set<String> = emptySet(),
        permissions: Set<String> = emptySet(),
        visibilityStatuses: Set<String> = emptySet()
    ) {
        dataStore.edit { preferences ->
            preferences[KEY_USER_ID] = userId
            preferences[KEY_NAME] = name
            preferences[KEY_EMAIL] = email
            preferences[KEY_SHOP_NAME] = shopName
            preferences[KEY_VENDOR_ID] = vendorId
            preferences[KEY_USER_ROLES] = roles
            preferences[KEY_USER_PERMISSIONS] = permissions
            preferences[KEY_ORDER_VISIBILITY_STATUSES] = visibilityStatuses
            if (logoUrl != null) {
                preferences[KEY_LOGO_URL] = logoUrl
            }
            preferences[KEY_IS_LOGGED_IN] = true
        }
    }
}

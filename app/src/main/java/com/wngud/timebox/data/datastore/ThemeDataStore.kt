package com.wngud.timebox.data.datastore

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

// DataStore 인스턴스 생성
private val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "theme_preferences")

@Singleton
class ThemeDataStore @Inject constructor(
    @ApplicationContext private val context: Context
) {
    private val dataStore: DataStore<Preferences> = context.dataStore

    companion object {
        private val THEME_MODE_KEY = stringPreferencesKey("theme_mode")
        const val DEFAULT_THEME = "라이트"
    }

    /**
     * 테마 모드를 저장합니다.
     * @param mode "라이트" 또는 "다크"
     */
    suspend fun saveThemeMode(mode: String) {
        dataStore.edit { preferences ->
            preferences[THEME_MODE_KEY] = mode
        }
    }

    /**
     * 저장된 테마 모드를 Flow로 반환합니다.
     * 기본값은 "라이트"입니다.
     */
    fun getThemeMode(): Flow<String> {
        return dataStore.data.map { preferences ->
            preferences[THEME_MODE_KEY] ?: DEFAULT_THEME
        }
    }
}

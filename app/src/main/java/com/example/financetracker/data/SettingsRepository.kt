package com.example.financetracker.data

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

/*
 * ямар төрлийн бүртгэл харуулах тохиргоог DataStore-д хадгалж, уншина.
 * DataStore бол жижиг түлхүүр-утга (key-value) хадгалах газар бөгөөд апп хаагдсан ч
 * утга нь үлдэнэ.
 *
 * DataStore = жижиг key-value (түлхүүр-утга) хадгалах газар. Апп хаагдсан ч утга үлдэнэ.
 *   "filter" → "ALL" / "INCOME" / "EXPENSE"
 *
 * Room-оос ялгаа: Room — олон мөртэй хүснэгт, DataStore — цөөн тохиргооны утга.
 */

enum class FilterType {
    ALL,
    INCOME,
    EXPENSE
}

class SettingsRepository(
    private val dataStore: DataStore<Preferences>
) {
    //tulhuuriin nereer utgaa hadgalj unshina
    private companion object {
        val FILTER_KEY = stringPreferencesKey("filter")
    }

    //READ
    val filter: Flow<FilterType> = dataStore.data.map { preferences ->
        val saved = preferences[FILTER_KEY] ?: FilterType.ALL.name
            FilterType.valueOf(saved) //income text iig FilterType.income bolgoo
    }

    //WRITE
    suspend fun saveFilter(filter: FilterType) {
        dataStore.edit { preferences ->
            preferences[FILTER_KEY] = filter.name
        }
    }
}
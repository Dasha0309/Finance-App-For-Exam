package com.example.financetracker.data

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.preferencesDataStore

/*
 * ҮҮРЭГ: Dependency container — Repository-уудыг НЭГ ГАЗАР үүсгэж, бусдад өгөх "агуулах".
 * FinanceApplication үүнийг нэг удаа үүсгэнэ → ViewModel-ууд эндээс Repository-гоо авна.
 */

private val Context.dataStore: DataStore<Preferences> by
        preferencesDataStore(name = "settings")

interface AppContainer {
    val transactionRepository: TransactionRepository
    val settingsRepository: SettingsRepository
}

class DefaultAppContainer(private val context: Context) : AppContainer {

    override val transactionRepository: TransactionRepository by lazy {
        OfflineTransactionRepository(FinanceDatabase.getDatabase(context).transactionDao())
    }
    override val settingsRepository: SettingsRepository by lazy {
        SettingsRepository(context.dataStore)
    }
}

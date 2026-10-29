package com.example.financetracker.ui

import androidx.lifecycle.ViewModelProvider.AndroidViewModelFactory
import androidx.lifecycle.createSavedStateHandle
import androidx.lifecycle.viewmodel.CreationExtras
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import com.example.financetracker.FinanceApplication
import com.example.financetracker.ui.entry.EntryViewModel
import com.example.financetracker.ui.home.HomeViewModel
import com.example.financetracker.ui.settings.SettingsViewModel

/*
 * ҮҮРЭГ: ViewModel ҮЙЛДВЭР (Factory).
 *
 * Асуудал: ViewModel-ууд Repository авах ёстой, гэтэл Android ViewModel-ийг өөрөө үүсгэдэг
 *          тул constructor-т параметр дамжуулж чадахгүй.
 * Шийдэл:  "HomeViewModel хэрэгтэй бол ингэж үүсгэ" гэсэн жорыг энд бичнэ.
 *
 * Дэлгэц дээр:  viewModel(factory = AppViewModelProvider.Factory)
 */

object AppViewModelProvider {
    val Factory = viewModelFactory {

        //Undsen delgets: burtgel + tohirgoo
        initializer{
            HomeViewModel(
                financeApplication().container.transactionRepository,
                financeApplication().container.settingsRepository,
            )
        }

        //Nemeh/Zasah delgets
        initializer {
            EntryViewModel(
                this.createSavedStateHandle(),
                financeApplication().container.transactionRepository
            )
        }

        //Settings display
        initializer {
            SettingsViewModel(
                financeApplication().container.settingsRepository
            )
        }
    }
}

//Application object iig FinanceApplication bolgoj huvirgana
fun CreationExtras.financeApplication(): FinanceApplication =
(this[AndroidViewModelFactory.APPLICATION_KEY] as FinanceApplication)

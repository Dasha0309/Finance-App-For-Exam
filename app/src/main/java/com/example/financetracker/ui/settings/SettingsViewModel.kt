package com.example.financetracker.ui.settings

import androidx.compose.runtime.Composable
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.financetracker.data.FilterType
import com.example.financetracker.data.SettingsRepository
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn

/*
 * ҮҮРЭГ: Тохиргооны дэлгэцийн "тархи" (3-р дэлгэц).
 *   savedFilter  — DataStore-д хадгалагдсан одоогийн тохиргоо (radio button-ыг анх сонгоход)
 *   save()       — шинэ тохиргоог DataStore-д бичнэ
 *
 * Хадгалсны дараа HomeViewModel ч мөн settingsRepository.filter-ийг сонсож байгаа тул
 * үндсэн дэлгэц автоматаар шүүгдэнэ — нэмэлт код хэрэггүй (Flow-ийн давуу тал).
 */

class SettingsViewModel(
    private val settingsRepository: SettingsRepository
) : ViewModel() {
    //DataStore iin flow stateflow bolno.
    //data store oos unshine

    val savedFilter: StateFlow<FilterType> =
        settingsRepository.filter.stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5_000),
            initialValue = FilterType.ALL //anhnii tohirgoog bugdiin haruulna
        )
    //DataStore -d bichne
    suspend fun save(filter: FilterType) {
        settingsRepository.saveFilter(filter)
    }
}
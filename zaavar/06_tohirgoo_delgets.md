# Алхам 6 — Тохиргооны дэлгэц

Шаардлага 1, 6 + шалгуур 3 (DataStore).

Хадгалахад `SettingsRepository.saveFilter()` DataStore-д бичнэ → `HomeViewModel` тэр Flow-г сонсож байгаа тул үндсэн дэлгэц өөрөө шүүгдэнэ.

## `ui/settings/SettingsViewModel.kt`

Файл: [ui/settings/SettingsViewModel.kt](../app/src/main/java/com/example/financetracker/ui/settings/SettingsViewModel.kt)

Маш богино: унших (`savedFilter`) + бичих (`save`).

```kotlin
package com.example.financetracker.ui.settings

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

    val savedFilter: StateFlow<FilterType> = settingsRepository.filter.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5_000),
        initialValue = FilterType.ALL
    )

    // suspend — хадгалж дууссаны дараа дэлгэцээ хаах ёстой тул
    suspend fun save(filter: FilterType) {
        settingsRepository.saveFilter(filter)
    }
}
```

## `ui/settings/SettingsScreen.kt`

Файл: [ui/settings/SettingsScreen.kt](../app/src/main/java/com/example/financetracker/ui/settings/SettingsScreen.kt)

3 RadioButton + ХАДГАЛАХ.

```kotlin
package com.example.financetracker.ui.settings

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.financetracker.R
import com.example.financetracker.data.FilterType
import com.example.financetracker.ui.AppViewModelProvider
import com.example.financetracker.ui.FinanceTopAppBar
import kotlinx.coroutines.launch

/*
 * ҮҮРЭГ: Тохиргооны дэлгэц (3-р дэлгэц). Шаардлага 1, 6.
 *
 *   (•) Орлого, зарлагыг хоёуланг нь харуулах
 *   ( ) Зөвхөн орлогыг харуулах
 *   ( ) Зөвхөн зарлагыг харуулах
 *   [ ХАДГАЛАХ ]
 */
@Composable
fun SettingsScreen(
    navigateBack: () -> Unit,
    viewModel: SettingsViewModel = viewModel(factory = AppViewModelProvider.Factory)
) {
    val savedFilter by viewModel.savedFilter.collectAsState()

    // Хэрэглэгчийн одоо сонгож буй (хадгалаагүй байж болох) утга.
    // rememberSaveable(savedFilter) → DataStore-оос утга ирэхэд түүгээр шинэчлэгдэнэ,
    // утсыг эргүүлэхэд сонголт алдагдахгүй.
    var selected by rememberSaveable(savedFilter) { mutableStateOf(savedFilter) }
    val coroutineScope = rememberCoroutineScope()

    // Radio бүрийн текст
    val options = listOf(
        FilterType.ALL to stringResource(R.string.filter_all),
        FilterType.INCOME to stringResource(R.string.filter_income),
        FilterType.EXPENSE to stringResource(R.string.filter_expense)
    )

    Scaffold(
        topBar = {
            FinanceTopAppBar(
                title = stringResource(R.string.settings),
                canNavigateBack = true,
                navigateUp = navigateBack
            )
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .padding(innerPadding)
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            options.forEach { (filter, label) ->
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        // Мөрийн хаана ч дарсан сонгогдоно
                        .selectable(selected = selected == filter, onClick = { selected = filter })
                        .padding(vertical = 8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    RadioButton(selected = selected == filter, onClick = { selected = filter })
                    Text(label)
                }
            }

            Button(
                onClick = {
                    coroutineScope.launch {
                        viewModel.save(selected)   // DataStore-д бичнэ
                        navigateBack()
                    }
                },
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(stringResource(R.string.save))
            }
        }
    }
}
```

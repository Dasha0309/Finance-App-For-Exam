# Алхам 5 — Бүртгэл нэмэх / засах дэлгэц

Шаардлага 5, 8: НЭМЭХ, ЗАСАХ товч эсвэл удаан дарахад энэ дэлгэц нээгдэнэ.

- `transactionId == -1` → шинэ бүртгэл → `insert`
- `transactionId == 5`  → DB-ээс уншаад талбаруудыг бөглөнө → `update`

`EntryUiState.id != 0` эсэхээр засах горим мөн эсэхийг мэднэ (Room шинэ бүртгэлд id-г өөрөө өгдөг тул шинэ нь 0).

## `ui/entry/EntryViewModel.kt`

Файл: [ui/entry/EntryViewModel.kt](../app/src/main/java/com/example/financetracker/ui/entry/EntryViewModel.kt)

`EntryUiState`, хөрвүүлэх функцууд, `EntryViewModel`.

```kotlin
package com.example.financetracker.ui.entry

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.financetracker.data.Transaction
import com.example.financetracker.data.TransactionRepository
import com.example.financetracker.data.TransactionType
import com.example.financetracker.ui.navigation.EntryDestination
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

/*
 * ҮҮРЭГ: Бүртгэл НЭМЭХ болон ЗАСАХ дэлгэцийн "тархи" (2-р дэлгэц).
 *
 *  - Навигациас ирсэн transactionId-г SavedStateHandle-аас уншина.
 *      -1  → шинэ бүртгэл (талбарууд хоосон)
 *      5   → 5-р бүртгэлийг DB-ээс уншаад талбаруудад бөглөнө
 *  - Хэрэглэгч бичих бүрт uiState шинэчлэгдэнэ
 *  - ХАДГАЛАХ → insert (шинэ) эсвэл update (засах)
 */

// Талбаруудын утга. amount-ийг String болгосон — TextField зөвхөн String авдаг.
data class EntryUiState(
    val id: Int = 0,
    val type: TransactionType = TransactionType.EXPENSE,
    val name: String = "",
    val amount: String = "",
    val date: String = today()
) {
    val isEditMode: Boolean get() = id != 0

    // ХАДГАЛАХ товч идэвхтэй болох нөхцөл
    val isValid: Boolean
        get() = name.isNotBlank() &&
                (amount.toLongOrNull() ?: 0) > 0 &&
                DATE_REGEX.matches(date.trim())
}

// "2026.10.23" хэлбэр шалгах загвар
private val DATE_REGEX = Regex("""\d{4}\.\d{2}\.\d{2}""")

// Өнөөдрийн огноо "2026.10.23" хэлбэрээр
fun today(): String = SimpleDateFormat("yyyy.MM.dd", Locale.US).format(Date())

// UiState ↔ Transaction хөрвүүлэлт
fun EntryUiState.toTransaction() = Transaction(
    id = id,
    type = type,
    name = name.trim(),
    amount = amount.toLong(),
    date = date.trim()
)

fun Transaction.toEntryUiState() = EntryUiState(
    id = id,
    type = type,
    name = name,
    amount = amount.toString(),
    date = date
)

class EntryViewModel(
    savedStateHandle: SavedStateHandle,
    private val transactionRepository: TransactionRepository
) : ViewModel() {

    // Навигацын аргумент. Өгөөгүй бол -1 (NEW_ID)
    private val transactionId: Int =
        savedStateHandle[EntryDestination.ARG_ID] ?: EntryDestination.NEW_ID

    // Compose state — өөрчлөгдөхөд дэлгэц дахин зурагдана. Гаднаас зөвхөн уншина (private set).
    var uiState by mutableStateOf(EntryUiState())
        private set

    init {
        // ЗАСАХ горим бол DB-ээс бүртгэлийг нэг удаа уншаад талбаруудад хийнэ
        if (transactionId != EntryDestination.NEW_ID) {
            viewModelScope.launch {
                uiState = transactionRepository.getByIdStream(transactionId)
                    .filterNotNull()
                    .first()                // Flow-оос эхний утгыг аваад зогсоно
                    .toEntryUiState()
            }
        }
    }

    // TextField-д үсэг бичих бүрт дуудагдана
    fun updateUiState(newState: EntryUiState) {
        uiState = newState
    }

    // suspend — дэлгэц хадгалж ДУУССАНЫ ДАРАА буцах ёстой тул
    suspend fun save() {
        if (!uiState.isValid) return
        if (uiState.isEditMode) {
            transactionRepository.update(uiState.toTransaction())
        } else {
            transactionRepository.insert(uiState.toTransaction())
        }
    }
}
```

## `ui/entry/EntryScreen.kt`

Файл: [ui/entry/EntryScreen.kt](../app/src/main/java/com/example/financetracker/ui/entry/EntryScreen.kt)

Төрөл сонгох 2 товч + 3 TextField + ХАДГАЛАХ.

```kotlin
package com.example.financetracker.ui.entry

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.financetracker.R
import com.example.financetracker.data.TransactionType
import com.example.financetracker.ui.AppViewModelProvider
import com.example.financetracker.ui.FinanceTopAppBar
import kotlinx.coroutines.launch

/*
 * ҮҮРЭГ: Бүртгэл нэмэх / засах дэлгэц (2-р дэлгэц). Шаардлага 5, 8.
 *
 *   [Орлого] [Зарлага]   ← төрөл сонгох
 *   Нэр       ________
 *   Дүн (₮)   ________   ← зөвхөн тоон гар
 *   Огноо     ________   ← анхнаасаа өнөөдрийн огноо
 *   [ ХАДГАЛАХ ]         ← бүх талбар зөв үед л идэвхтэй
 *
 * verticalScroll — хөндлөн (landscape) үед дэлгэцэнд багтахгүй бол гүйлгэнэ (шаардлага 10).
 */
@Composable
fun EntryScreen(
    navigateBack: () -> Unit,
    viewModel: EntryViewModel = viewModel(factory = AppViewModelProvider.Factory)
) {
    val uiState = viewModel.uiState
    // Товч дарахад suspend функц (save) дуудахын тулд coroutine scope хэрэгтэй
    val coroutineScope = rememberCoroutineScope()

    Scaffold(
        topBar = {
            FinanceTopAppBar(
                title = stringResource(if (uiState.isEditMode) R.string.edit_title else R.string.add_title),
                canNavigateBack = true,
                navigateUp = navigateBack
            )
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .padding(innerPadding)
                .verticalScroll(rememberScrollState())
                .padding(16.dp)
                .fillMaxWidth(),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Төрөл сонгох 2 товч
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                TypeButton(
                    text = stringResource(R.string.income),
                    selected = uiState.type == TransactionType.INCOME,
                    onClick = { viewModel.updateUiState(uiState.copy(type = TransactionType.INCOME)) },
                    modifier = Modifier.weight(1f)
                )
                TypeButton(
                    text = stringResource(R.string.expense),
                    selected = uiState.type == TransactionType.EXPENSE,
                    onClick = { viewModel.updateUiState(uiState.copy(type = TransactionType.EXPENSE)) },
                    modifier = Modifier.weight(1f)
                )
            }

            // copy(...) → бусад талбарыг хэвээр нь үлдээж, зөвхөн нэгийг нь солино
            OutlinedTextField(
                value = uiState.name,
                onValueChange = { viewModel.updateUiState(uiState.copy(name = it)) },
                label = { Text(stringResource(R.string.name_label)) },
                singleLine = true,
                modifier = Modifier.fillMaxWidth()
            )
            OutlinedTextField(
                value = uiState.amount,
                // Зөвхөн цифр оруулахыг зөвшөөрнө
                onValueChange = { input ->
                    viewModel.updateUiState(uiState.copy(amount = input.filter { it.isDigit() }))
                },
                label = { Text(stringResource(R.string.amount_label)) },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                singleLine = true,
                modifier = Modifier.fillMaxWidth()
            )
            OutlinedTextField(
                value = uiState.date,
                onValueChange = { viewModel.updateUiState(uiState.copy(date = it)) },
                label = { Text(stringResource(R.string.date_label)) },
                singleLine = true,
                modifier = Modifier.fillMaxWidth()
            )

            Button(
                onClick = {
                    coroutineScope.launch {
                        viewModel.save()     // DB-д бичиж дуустал хүлээнэ
                        navigateBack()       // дараа нь үндсэн дэлгэц рүү буцна
                    }
                },
                enabled = uiState.isValid,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(stringResource(R.string.save))
            }
        }
    }
}

// Сонгогдсон бол дүүрсэн (Button), үгүй бол хүрээтэй (OutlinedButton)
@Composable
private fun TypeButton(
    text: String,
    selected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    if (selected) {
        Button(onClick = onClick, modifier = modifier, colors = ButtonDefaults.buttonColors()) {
            Text(text)
        }
    } else {
        OutlinedButton(onClick = onClick, modifier = modifier) {
            Text(text)
        }
    }
}
```

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

// Талбаруудын утга. amount нь String — TextField зөвхөн String авдаг.
data class EntryUiState(
    val id: Int = 0,
    val type: TransactionType = TransactionType.EXPENSE,
    val name: String = "",
    val amount: String = "",
    val date: String = today()
) {
    // Room шинэ бүртгэлд id-г өөрөө өгдөг тул 0 = шинэ, 0 биш = засах
    val isEditMode: Boolean get() = id != 0

    // ХАДГАЛАХ идэвхтэй болох нөхцөл: нэр бичсэн, дүн > 0, огноо "2026.10.23" хэлбэртэй
    val isValid: Boolean
        get() = name.isNotBlank() &&
                (amount.toLongOrNull() ?: 0) > 0 &&
                DATE_REGEX.matches(date.trim())
}

private val DATE_REGEX = Regex("""\d{4}\.\d{2}\.\d{2}""")

// Өнөөдрийн огноо "yyyy.MM.dd"
fun today(): String = SimpleDateFormat("yyyy.MM.dd", Locale.US).format(Date())

// UiState → Transaction (DB-д хадгалахад)
fun EntryUiState.toTransaction() = Transaction(
    id = id,
    type = type,
    name = name.trim(),
    amount = amount.toLong(),
    date = date.trim()
)

// Transaction → UiState (засах үед талбаруудыг бөглөхөд)
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

    private val transactionId: Int =
        savedStateHandle[EntryDestination.ARG_ID] ?: EntryDestination.NEW_ID

    var uiState by mutableStateOf(EntryUiState())
        private set

    init {
        // Шаардлага 4, 8: ЗАСАХ горим бол DB-ээс уншаад талбаруудыг бөглөнө
        if (transactionId != EntryDestination.NEW_ID) {
            viewModelScope.launch {
                uiState = transactionRepository.getByIdStream(transactionId)
                    .filterNotNull()
                    .first()              // Flow-ийн эхний утгыг аваад зогсоно
                    .toEntryUiState()
            }
        }
    }

    // TextField-д бичих бүрт дуудагдана
    fun updateUiState(newState: EntryUiState) {
        uiState = newState
    }

    // Шинэ бол insert, засах бол update (Room CRUD — Шалгуур 1, 2)
    suspend fun save() {
        if (!uiState.isValid) return
        if (uiState.isEditMode) {
            transactionRepository.update(uiState.toTransaction())
        } else {
            transactionRepository.insert(uiState.toTransaction())
        }
    }
}

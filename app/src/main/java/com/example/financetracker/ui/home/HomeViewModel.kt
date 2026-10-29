package com.example.financetracker.ui.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.financetracker.data.FilterType
import com.example.financetracker.data.SettingsRepository
import com.example.financetracker.data.Transaction
import com.example.financetracker.data.TransactionRepository
import com.example.financetracker.data.TransactionType
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

/*
 * ҮҮРЭГ: Үндсэн дэлгэцийн "тархи". Дэлгэц өөрөө юу ч тооцоолохгүй — бүгдийг энд хийнэ.
 *
 *   - Тохиргооноос (DataStore) шүүлтүүр уншаад зөв бүртгэлүүдийг DB-ээс авна   (шаардлага 1)
 *   - Одоо хэд дэх бүртгэлийг харуулж буйг (index) санана                     (шаардлага 3)
 *   - Дүн нууцлагдсан эсэхийг санана                                          (шаардлага 2)
 *   - Устгах                                                                  (шаардлага 4)
 *
 * Утсыг эргүүлэхэд (landscape) Activity дахин үүсдэг ч ViewModel АМЬД ҮЛДДЭГ
 * тул index, дүн харагдаж буй эсэх алдагдахгүй (шаардлага 10).
 */

data class HomeUiState(
    val transactions: List<Transaction> = emptyList(),
    val currentIndex: Int = 0,
    val filter: FilterType = FilterType.ALL,
    val isAmountVisible: Boolean = false
) {
    val current: Transaction? get() = transactions.getOrNull(currentIndex)
    val isEmpty: Boolean get() = transactions.isEmpty()
    val hasPrevious: Boolean get() = currentIndex > 0
    val hasNext: Boolean get() = currentIndex < transactions.size - 1

    // Дээд талын нийт дүн
    val totalIncome: Long
        get() = transactions.filter { it.type == TransactionType.INCOME }.sumOf { it.amount }
    val totalExpense: Long
        get() = transactions.filter { it.type == TransactionType.EXPENSE }.sumOf { it.amount }

    // all baival income expense zeregtsene ugui baival tusdaa
    val showIncomeTotal: Boolean get() = filter != FilterType.EXPENSE
    val showExpenseTotal: Boolean get() = filter != FilterType.INCOME
}

class HomeViewModel(
    private val transactionRepository: TransactionRepository,
    settingsRepository: SettingsRepository
) : ViewModel() {
    //zuvhun viewModel dotroos uurchluh utguud

    private val currentIndex = MutableStateFlow(0)
    private val isAmountVisible = MutableStateFlow(false)

    //shuultuur uurchluh burd DB query dagaad uurchlugdunu
    @OptIn(ExperimentalCoroutinesApi::class)
    private val transactions: Flow<List<Transaction>> =
        settingsRepository.filter.flatMapLatest{ filter ->
            when (filter) {
                FilterType.ALL -> transactionRepository.getAllStream()
                FilterType.INCOME -> transactionRepository.getByTypeStream(TransactionType.INCOME)
                FilterType.EXPENSE -> transactionRepository.getByTypeStream(TransactionType.EXPENSE)
            }
        }
    //Flow g negtgej UiState bolgono
    val uiState: StateFlow<HomeUiState> =
        combine(transactions, settingsRepository.filter, currentIndex, isAmountVisible) { list, filter, index, visible ->
            HomeUiState(transactions = list,
                currentIndex = index.coerceIn(0,
                    maxOf(list.size - 1, 0)),
                filter = filter,
                isAmountVisible = visible
            )
        }.stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5_000),
            initialValue = HomeUiState()
        )
    //NEXT button
    fun showNext() {
        if (uiState.value.hasNext) {
            currentIndex.value = uiState.value.currentIndex + 1
            isAmountVisible.value = false // shine burtgel deer dun ahin nuutslagdana
        }
    }
    //Previous button
    fun showPrevious() {
        if (uiState.value.hasPrevious) {
            currentIndex.value = uiState.value.currentIndex - 1
            isAmountVisible.value = false
        }
    }
    //hide amount
    fun toggleAmountVisibility() {
        isAmountVisible.value = !isAmountVisible.value
    }

    fun deleteCurrent() {
        val transaction = uiState.value.current ?: return
        isAmountVisible.value = false
        viewModelScope.launch {
            transactionRepository.delete(transaction)
        }
    }
}
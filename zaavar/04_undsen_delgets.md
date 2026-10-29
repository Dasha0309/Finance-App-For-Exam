# Алхам 4 — Үндсэн дэлгэц (Home)

Функциональ шаардлагын ихэнх нь (1, 2, 3, 4, 6, 7, 8, 9, 10) энэ дэлгэц дээр.

Эхлээд **HomeViewModel**-ийг бич (логик), дараа нь **HomeScreen** (харагдах байдал).

Гол санаа: `combine(transactions, currentIndex, isAmountVisible)` — 3 өөр Flow-г нэг `HomeUiState` болгож нэгтгэнэ. Аль нэг нь өөрчлөгдөхөд дэлгэц автоматаар шинэчлэгдэнэ.

## `ui/home/HomeViewModel.kt`

Файл: [ui/home/HomeViewModel.kt](../app/src/main/java/com/example/financetracker/ui/home/HomeViewModel.kt)

`flatMapLatest` — тохиргоо солигдоход өөр query (бүгд / орлого / зарлага) руу шилжинэ.

```kotlin
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

// Дэлгэцэнд хэрэгтэй бүх мэдээлэл нэг объектод
data class HomeUiState(
    val transactions: List<Transaction> = emptyList(),
    val currentIndex: Int = 0,
    val isAmountVisible: Boolean = false
) {
    val current: Transaction? get() = transactions.getOrNull(currentIndex) // жагсаалт хоосон бол null
    val isEmpty: Boolean get() = transactions.isEmpty()
    val hasPrevious: Boolean get() = currentIndex > 0
    val hasNext: Boolean get() = currentIndex < transactions.size - 1
}

class HomeViewModel(
    private val transactionRepository: TransactionRepository,
    settingsRepository: SettingsRepository
) : ViewModel() {

    // Зөвхөн ViewModel дотроос өөрчилдөг утгууд (MutableStateFlow)
    private val currentIndex = MutableStateFlow(0)
    private val isAmountVisible = MutableStateFlow(false)

    // Шүүлтүүр өөрчлөгдөх бүрт (flatMapLatest) DB-ийн өөр query руу шилжинэ.
    // Жишээ: тохиргоо INCOME болоход getByTypeStream(INCOME)-ийг сонсож эхэлнэ.
    @OptIn(ExperimentalCoroutinesApi::class)
    private val transactions: Flow<List<Transaction>> =
        settingsRepository.filter.flatMapLatest { filter ->
            when (filter) {
                FilterType.ALL -> transactionRepository.getAllStream()
                FilterType.INCOME -> transactionRepository.getByTypeStream(TransactionType.INCOME)
                FilterType.EXPENSE -> transactionRepository.getByTypeStream(TransactionType.EXPENSE)
            }
        }

    // 3 Flow-г (жагсаалт, index, дүн харагдах эсэх) нэгтгэж нэг UiState болгоно.
    // stateIn → Flow-г StateFlow болгоно (дэлгэц collectAsState() хийж уншина).
    val uiState: StateFlow<HomeUiState> =
        combine(transactions, currentIndex, isAmountVisible) { list, index, visible ->
            HomeUiState(
                transactions = list,
                // Устгасны дараа index хэтэрсэн байж болох тул [0, size-1] хооронд барина
                currentIndex = index.coerceIn(0, maxOf(list.size - 1, 0)),
                isAmountVisible = visible
            )
        }.stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5_000),
            initialValue = HomeUiState()
        )

    // ДАРААХ товч
    fun showNext() {
        if (uiState.value.hasNext) {
            currentIndex.value = uiState.value.currentIndex + 1
            isAmountVisible.value = false      // шинэ бүртгэл дээр дүн дахин нууцлагдана
        }
    }

    // ӨМНӨХ товч
    fun showPrevious() {
        if (uiState.value.hasPrevious) {
            currentIndex.value = uiState.value.currentIndex - 1
            isAmountVisible.value = false
        }
    }

    // Дүн дээр дарахад "**** ₮" ↔ "15,000 ₮"
    fun toggleAmountVisibility() {
        isAmountVisible.value = !isAmountVisible.value
    }

    // Диалогоор баталгаажуулсны дараа одоо харагдаж буй бүртгэлийг устгана
    fun deleteCurrent() {
        val transaction = uiState.value.current ?: return
        isAmountVisible.value = false
        viewModelScope.launch {
            transactionRepository.delete(transaction)
        }
    }
}
```

## `ui/home/HomeScreen.kt`

Файл: [ui/home/HomeScreen.kt](../app/src/main/java/com/example/financetracker/ui/home/HomeScreen.kt)

Урт файл. Дээрээс доош: `HomeScreen` → `TransactionCard` → `AmountText` → `HomeButtons` → `DeleteConfirmationDialog`.

```kotlin
package com.example.financetracker.ui.home

import android.content.res.Configuration
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.clickable
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.financetracker.R
import com.example.financetracker.data.Transaction
import com.example.financetracker.data.TransactionType
import com.example.financetracker.ui.AppViewModelProvider
import com.example.financetracker.ui.FinanceTopAppBar
import com.example.financetracker.ui.theme.ExpenseBg
import com.example.financetracker.ui.theme.ExpenseRed
import com.example.financetracker.ui.theme.IncomeBg
import com.example.financetracker.ui.theme.IncomeGreen
import java.util.Locale

/*
 * ҮҮРЭГ: Үндсэн дэлгэц (1-р дэлгэц). Зөвхөн ХАРУУЛНА — логик бүгд HomeViewModel-д.
 *
 * Шаардлага → хаана хийгдсэн:
 *   1  Нэг нэгээр харуулах             → TransactionCard (current бүртгэл)
 *   2  "**** ₮", дарахад бодит дүн      → AmountText + toggleAmountVisibility()
 *   3  ӨМНӨХ / ДАРААХ                  → HomeButtons
 *   4  Засах, устгах                   → HomeButtons (ЗАСАХ, УСТГАХ)
 *   5  НЭМЭХ, ЗАСАХ → бүртгэлийн дэлгэц → navigateToAdd / navigateToEdit
 *   6  Цэснээс тохиргоо нээх           → TopAppBar дээрх ⚙ товч
 *   7  Хоосон үед зөвхөн НЭМЭХ идэвхтэй → enabled = ...
 *   8  Удаан дарахад засах дэлгэц       → combinedClickable(onLongClick)
 *   9  Устгахын өмнө диалог            → DeleteConfirmationDialog
 *  10  Landscape                       → isLandscape бол Row, үгүй бол Column
 */
@Composable
fun HomeScreen(
    navigateToAdd: () -> Unit,
    navigateToEdit: (Int) -> Unit,
    navigateToSettings: () -> Unit,
    viewModel: HomeViewModel = viewModel(factory = AppViewModelProvider.Factory)
) {
    // StateFlow-г Compose state болгоно → утга өөрчлөгдөхөд дэлгэц автоматаар дахин зурагдана
    val uiState by viewModel.uiState.collectAsState()

    // Диалог нээлттэй эсэх. rememberSaveable → утсыг эргүүлсэн ч диалог хаагдахгүй
    var showDeleteDialog by rememberSaveable { mutableStateOf(false) }

    // Утас хөндлөн байгаа эсэх (шаардлага 10)
    val isLandscape = LocalConfiguration.current.orientation == Configuration.ORIENTATION_LANDSCAPE

    Scaffold(
        topBar = {
            FinanceTopAppBar(
                title = stringResource(R.string.home_title),
                canNavigateBack = false,
                actions = {
                    // Шаардлага 6: цэснээс тохиргооны дэлгэц нээх
                    IconButton(onClick = navigateToSettings) {
                        Icon(Icons.Filled.Settings, contentDescription = stringResource(R.string.settings))
                    }
                }
            )
        }
    ) { innerPadding ->
        // Карт ба товчнуудыг 2 газар ашиглах тул lambda болгож нэг удаа бичив
        val card = @Composable { modifier: Modifier ->
            TransactionCard(
                uiState = uiState,
                onAmountClick = viewModel::toggleAmountVisibility,
                onLongClick = { id -> navigateToEdit(id) },
                modifier = modifier
            )
        }
        val buttons = @Composable { modifier: Modifier ->
            HomeButtons(
                uiState = uiState,
                onPrevious = viewModel::showPrevious,
                onNext = viewModel::showNext,
                onAdd = navigateToAdd,
                onEdit = { uiState.current?.let { navigateToEdit(it.id) } },
                onDelete = { showDeleteDialog = true },
                modifier = modifier
            )
        }

        if (isLandscape) {
            // ХӨНДЛӨН: карт зүүн талд, товчнууд баруун талд
            Row(
                modifier = Modifier
                    .padding(innerPadding)
                    .fillMaxSize()
                    .padding(16.dp),
                horizontalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                card(Modifier.weight(1f).fillMaxHeight())
                buttons(Modifier.weight(1f).align(Alignment.CenterVertically))
            }
        } else {
            // БОСОО: карт дээр, товчнууд доор
            Column(
                modifier = Modifier
                    .padding(innerPadding)
                    .fillMaxSize()
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                card(Modifier.fillMaxWidth().weight(1f))
                buttons(Modifier.fillMaxWidth())
            }
        }
    }

    // Шаардлага 9: устгахаас өмнө асуух
    if (showDeleteDialog) {
        DeleteConfirmationDialog(
            onConfirm = {
                showDeleteDialog = false
                viewModel.deleteCurrent()
            },
            onDismiss = { showDeleteDialog = false }
        )
    }
}

/*
 * Нэг бүртгэлийг харуулах карт.
 * Удаан дарвал (long click) засах дэлгэц нээгдэнэ (шаардлага 8).
 */
@OptIn(ExperimentalFoundationApi::class)
@Composable
private fun TransactionCard(
    uiState: HomeUiState,
    onAmountClick: () -> Unit,
    onLongClick: (Int) -> Unit,
    modifier: Modifier = Modifier
) {
    val transaction = uiState.current

    // Бүртгэл байхгүй үед
    if (transaction == null) {
        Box(modifier = modifier, contentAlignment = Alignment.Center) {
            Text(stringResource(R.string.no_records), style = MaterialTheme.typography.titleMedium)
        }
        return
    }

    val isIncome = transaction.type == TransactionType.INCOME

    Card(
        modifier = modifier.combinedClickable(
            onClick = {},                                  // энгийн дарахад юу ч болохгүй
            onLongClick = { onLongClick(transaction.id) }  // удаан дарахад засах
        ),
        colors = CardDefaults.cardColors(containerColor = if (isIncome) IncomeBg else ExpenseBg)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())     // хөндлөн үед багтахгүй бол гүйлгэнэ
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(12.dp, Alignment.CenterVertically)
        ) {
            // "2 / 5" — хэд дэх бүртгэл
            Text(
                stringResource(R.string.record_position, uiState.currentIndex + 1, uiState.transactions.size),
                style = MaterialTheme.typography.labelLarge
            )
            // Орлого / Зарлага
            Text(
                text = stringResource(if (isIncome) R.string.income else R.string.expense),
                color = if (isIncome) IncomeGreen else ExpenseRed,
                fontWeight = FontWeight.Bold
            )
            // Нэр
            Text(transaction.name, style = MaterialTheme.typography.headlineMedium)
            // Дүн (нууцлагдсан эсвэл бодит)
            AmountText(
                transaction = transaction,
                isVisible = uiState.isAmountVisible,
                onClick = onAmountClick
            )
            // Огноо
            Text(transaction.date, style = MaterialTheme.typography.bodyLarge)
            // Тусламж текст
            Text(
                text = stringResource(R.string.tap_to_show) + " • " + stringResource(R.string.long_press_hint),
                style = MaterialTheme.typography.bodySmall
            )
        }
    }
}

/*
 * Шаардлага 2: анх "**** ₮", дээр нь нэг дарахад бодит дүн.
 */
@Composable
private fun AmountText(
    transaction: Transaction,
    isVisible: Boolean,
    onClick: () -> Unit
) {
    val isIncome = transaction.type == TransactionType.INCOME
    val text = if (isVisible) {
        val sign = if (isIncome) "+" else "-"
        sign + " " + formatAmount(transaction.amount) + " ₮"     // "+ 2,000,000 ₮"
    } else {
        stringResource(R.string.hidden_amount)                    // "**** ₮"
    }
    Text(
        text = text,
        modifier = Modifier.clickable(onClick = onClick),
        style = MaterialTheme.typography.displaySmall,
        color = if (isIncome) IncomeGreen else ExpenseRed,
        fontWeight = FontWeight.Bold
    )
}

// 15000 → "15,000"
private fun formatAmount(amount: Long): String = String.format(Locale.US, "%,d", amount)

/*
 * Доод талын товчнууд.
 * Шаардлага 7: бүртгэл байхгүй (isEmpty) үед НЭМЭХ-ээс бусад нь идэвхгүй (enabled = false).
 */
@Composable
private fun HomeButtons(
    uiState: HomeUiState,
    onPrevious: () -> Unit,
    onNext: () -> Unit,
    onAdd: () -> Unit,
    onEdit: () -> Unit,
    onDelete: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(modifier = modifier, verticalArrangement = Arrangement.spacedBy(8.dp)) {
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            OutlinedButton(
                onClick = onPrevious,
                enabled = uiState.hasPrevious,       // эхний бүртгэл дээр эсвэл хоосон үед идэвхгүй
                modifier = Modifier.weight(1f)
            ) { Text(stringResource(R.string.previous)) }

            OutlinedButton(
                onClick = onNext,
                enabled = uiState.hasNext,           // сүүлийн бүртгэл дээр эсвэл хоосон үед идэвхгүй
                modifier = Modifier.weight(1f)
            ) { Text(stringResource(R.string.next)) }
        }
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            Button(
                onClick = onAdd,                     // НЭМЭХ үргэлж идэвхтэй
                modifier = Modifier.weight(1f),
                colors = ButtonDefaults.buttonColors(containerColor = IncomeGreen)
            ) { Text(stringResource(R.string.add)) }

            Button(
                onClick = onEdit,
                enabled = !uiState.isEmpty,
                modifier = Modifier.weight(1f)
            ) { Text(stringResource(R.string.edit)) }

            Button(
                onClick = onDelete,
                enabled = !uiState.isEmpty,
                modifier = Modifier.weight(1f),
                colors = ButtonDefaults.buttonColors(containerColor = ExpenseRed)
            ) { Text(stringResource(R.string.delete)) }
        }
    }
}

/*
 * Шаардлага 9: "Устгах уу?" диалог. ТИЙМ → устгана, ҮГҮЙ → хаана.
 */
@Composable
private fun DeleteConfirmationDialog(
    onConfirm: () -> Unit,
    onDismiss: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,          // диалогийн гадна дарахад хаагдана
        title = { Text(stringResource(R.string.delete_title)) },
        text = { Text(stringResource(R.string.delete_message)) },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text(stringResource(R.string.no)) }
        },
        confirmButton = {
            TextButton(onClick = onConfirm) {
                Text(stringResource(R.string.yes), color = ExpenseRed)
            }
        }
    )
}
```

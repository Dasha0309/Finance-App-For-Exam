package com.example.financetracker.ui.home

import android.content.res.Configuration
import androidx.annotation.StringRes
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowLeft
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.financetracker.R
import com.example.financetracker.data.TransactionType
import com.example.financetracker.ui.AppViewModelProvider
import com.example.financetracker.ui.FinanceTopAppBar
import com.example.financetracker.ui.theme.ExpenseBg
import com.example.financetracker.ui.theme.ExpenseRed
import com.example.financetracker.ui.theme.IncomeBg
import com.example.financetracker.ui.theme.IncomeGreen
import com.example.financetracker.ui.theme.ScreenBg
import java.util.Locale

/*
 * ҮҮРЭГ: Үндсэн дэлгэц . логик бүгд HomeViewModel-д.
 *
 * Шаардлага:
 *   1  Нэг нэгээр харуулах             → TransactionCard (current бүртгэл)
 *   2  "**** ₮", дарахад бодит дүн      → amountText + toggleAmountVisibility()
 *   3  ӨМНӨХ / ДАРААХ                  → RecordNavigator (< > товч)
 *   4  Засах, устгах                   → ActionButtons (ЗАСАХ, УСТГАХ)
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
    // StateFlow-г Compose state болгоно утга өөрчлөгдөхөд дэлгэц автоматаар дахин зурагдана
    val uiState by viewModel.uiState.collectAsState()

    // Диалог нээлттэй эсэх. rememberSaveable → утсыг эргүүлсэн ч диалог хаагдахгүй
    var showDeleteDialog by rememberSaveable { mutableStateOf(false) }

    // Шаардлага 10: утас хөндлөн эсэх
    val isLandscape = LocalConfiguration.current.orientation == Configuration.ORIENTATION_LANDSCAPE

    Scaffold(
        containerColor = ScreenBg,
        topBar = {
            FinanceTopAppBar(
                title = stringResource(R.string.home_title),
                canNavigateBack = false,
                action = {
                    // Шаардлага 6: цэснээс тохиргооны дэлгэц нээх
                    IconButton(onClick = navigateToSettings) {
                        Icon(
                            Icons.Filled.Settings,
                            contentDescription = stringResource(R.string.settings),
                            tint = MaterialTheme.colorScheme.onPrimary
                        )
                    }
                }
            )
        }
    ) { innerPadding ->
        // 2 газар ашиглах тул lambda болгож нэг удаа бичив
        val navigator = @Composable {
            RecordNavigator(
                uiState = uiState,
                onPrevious = viewModel::showPrevious,
                onNext = viewModel::showNext
            )
        }
        val totals = @Composable {
            TotalBoxes(uiState = uiState, onClick = viewModel::toggleAmountVisibility)
        }
        val card = @Composable { modifier: Modifier ->
            TransactionCard(
                uiState = uiState,
                onAmountClick = viewModel::toggleAmountVisibility,
                onLongClick = { id -> navigateToEdit(id) },
                modifier = modifier
            )
        }
        val buttons = @Composable { isVertical: Boolean ->
            ActionButtons(
                uiState = uiState,
                onAdd = navigateToAdd,
                onEdit = { uiState.current?.let { navigateToEdit(it.id) } },
                onDelete = { showDeleteDialog = true },
                isVertical = isVertical
            )
        }

        if (isLandscape) {
            Row(
                modifier = Modifier
                    .padding(innerPadding)
                    .fillMaxSize()
                    .padding(16.dp),
                horizontalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                card(Modifier.weight(1f).fillMaxHeight())
                Column(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxHeight()
                        .verticalScroll(rememberScrollState()),   // намхан дэлгэцэнд гүйлгэнэ
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    navigator()
                    totals()
                    buttons(true)
                }
            }
        } else {
            Column(
                modifier = Modifier
                    .padding(innerPadding)
                    .fillMaxSize()
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                navigator()
                totals()
                card(Modifier.fillMaxWidth().weight(1f))
                buttons(false)
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


@Composable
private fun RecordNavigator(
    uiState: HomeUiState,
    onPrevious: () -> Unit,
    onNext: () -> Unit,
    modifier: Modifier = Modifier
) {
    val current = uiState.current
    Card(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth().padding(4.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(onClick = onPrevious, enabled = uiState.hasPrevious) {
                Icon(
                    Icons.AutoMirrored.Filled.KeyboardArrowLeft,
                    contentDescription = stringResource(R.string.previous)
                )
            }
            Column(
                modifier = Modifier.weight(1f),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // "2026.10.29" → "2026-10"
                Text(
                    text = current?.date?.take(7)?.replace('.', '-') ?: "—",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold
                )
                // "2 / 5" — хэд дэх бүртгэл
                if (current != null) {
                    Text(
                        stringResource(R.string.record_position, uiState.currentIndex + 1, uiState.transactions.size),
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
            IconButton(onClick = onNext, enabled = uiState.hasNext) {
                Icon(
                    Icons.AutoMirrored.Filled.KeyboardArrowRight,
                    contentDescription = stringResource(R.string.next)
                )
            }
        }
    }
}

/*
 * Орлого, зарлагын нийт дүн. Шүүлтүүр ALL бол хоёр хайрцаг зэрэгцэнэ,
 * эс бөгөөс зөвхөн нэг нь бүтэн өргөнөөр харагдана.
 */
@Composable
private fun TotalBoxes(
    uiState: HomeUiState,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Row(modifier = modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
        if (uiState.showIncomeTotal) {
            TotalBox(
                title = stringResource(R.string.income),
                amount = uiState.totalIncome,
                isIncome = true,
                isVisible = uiState.isAmountVisible,
                onClick = onClick,
                modifier = Modifier.weight(1f)
            )
        }
        if (uiState.showExpenseTotal) {
            TotalBox(
                title = stringResource(R.string.expense),
                amount = uiState.totalExpense,
                isIncome = false,
                isVisible = uiState.isAmountVisible,
                onClick = onClick,
                modifier = Modifier.weight(1f)
            )
        }
    }
}

@Composable
private fun TotalBox(
    title: String,
    amount: Long,
    isIncome: Boolean,
    isVisible: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val color = if (isIncome) IncomeGreen else ExpenseRed
    Card(
        onClick = onClick,
        modifier = modifier,
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = if (isIncome) IncomeBg else ExpenseBg)
    ) {
        Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                // Дугуй дотор ↑ (орлого) эсвэл ↓ (зарлага)
                Box(
                    modifier = Modifier.size(32.dp).background(color, CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        if (isIncome) Icons.Filled.KeyboardArrowUp else Icons.Filled.KeyboardArrowDown,
                        contentDescription = null,
                        tint = Color.White
                    )
                }
                Text(title, color = color, fontWeight = FontWeight.Bold)
            }
            Text(
                text = amountText(amount, isVisible),
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = color,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }
    }
}

/*
 *  Шаардлага 1, 8 Нэг бүртгэлийг харуулах карт.
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
        Card(
            modifier = modifier,
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = Color.White)
        ) {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Text(stringResource(R.string.no_records), style = MaterialTheme.typography.titleMedium)
            }
        }
        return
    }

    val isIncome = transaction.type == TransactionType.INCOME
    val color = if (isIncome) IncomeGreen else ExpenseRed

    Card(
        modifier = modifier.combinedClickable(
            onClick = {},                                  // энгийн дарахад юу ч болохгүй
            onLongClick = { onLongClick(transaction.id) }  // удаан дарахад засах
        ),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())     // хөндлөн үед багтахгүй бол гүйлгэнэ
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(12.dp, Alignment.CenterVertically)
        ) {

            Row(
                modifier = Modifier
                    .background(if (isIncome) IncomeBg else ExpenseBg, RoundedCornerShape(50))
                    .padding(horizontal = 12.dp, vertical = 4.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                Icon(
                    if (isIncome) Icons.Filled.KeyboardArrowUp else Icons.Filled.KeyboardArrowDown,
                    contentDescription = null,
                    tint = color,
                    modifier = Modifier.size(18.dp)
                )
                Text(
                    text = stringResource(if (isIncome) R.string.income else R.string.expense),
                    color = color,
                    fontWeight = FontWeight.Bold
                )
            }
            // Нэр
            Text(
                text = transaction.name,
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.Bold,
                textAlign = TextAlign.Center
            )
            // Дүн (нууцлагдсан эсвэл бодит) — дарахад солигдоно (шаардлага 2)
            Text(
                text = amountText(transaction.amount, uiState.isAmountVisible, sign = if (isIncome) "+ " else "- "),
                modifier = Modifier.clickable(onClick = onAmountClick),
                style = MaterialTheme.typography.displaySmall,
                color = color,
                fontWeight = FontWeight.Bold
            )
            // Огноо
            Text(
                text = transaction.date.replace('.', '-'),
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            // Тусламж текст
            Text(
                text = stringResource(R.string.tap_to_show) + " • " + stringResource(R.string.long_press_hint),
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = TextAlign.Center
            )
        }
    }
}

/*
 * НЭМЭХ / ЗАСАХ / УСТГАХ. Босоо үед нэг мөрөнд, хөндлөн үед доошоо цуварна.
 * Шаардлага 4, 5, 7: бүртгэл байхгүй (isEmpty) үед НЭМЭХ-ээс бусад нь идэвхгүй.
 */
@Composable
private fun ActionButtons(
    uiState: HomeUiState,
    onAdd: () -> Unit,
    onEdit: () -> Unit,
    onDelete: () -> Unit,
    isVertical: Boolean,
    modifier: Modifier = Modifier
) {
    val add = @Composable { m: Modifier ->
        ActionButton(R.string.add, Icons.Filled.Add, IncomeGreen, enabled = true, onClick = onAdd, modifier = m)
    }
    val edit = @Composable { m: Modifier ->
        ActionButton(R.string.edit, Icons.Filled.Edit, MaterialTheme.colorScheme.primary, enabled = !uiState.isEmpty, onClick = onEdit, modifier = m)
    }
    val delete = @Composable { m: Modifier ->
        ActionButton(R.string.delete, Icons.Filled.Delete, ExpenseRed, enabled = !uiState.isEmpty, onClick = onDelete, modifier = m)
    }

    if (isVertical) {
        Column(modifier = modifier.fillMaxWidth(), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            add(Modifier.fillMaxWidth())
            edit(Modifier.fillMaxWidth())
            delete(Modifier.fillMaxWidth())
        }
    } else {
        Row(modifier = modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            add(Modifier.weight(1f))
            edit(Modifier.weight(1f))
            delete(Modifier.weight(1f))
        }
    }
}

@Composable
private fun ActionButton(
    @StringRes textRes: Int,
    icon: ImageVector,
    color: Color,
    enabled: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Button(
        onClick = onClick,
        enabled = enabled,
        modifier = modifier.height(48.dp),
        shape = RoundedCornerShape(12.dp),
        contentPadding = PaddingValues(horizontal = 8.dp),
        colors = ButtonDefaults.buttonColors(containerColor = color)
    ) {
        Icon(icon, contentDescription = null, modifier = Modifier.size(18.dp))
        Spacer(Modifier.width(4.dp))
        Text(stringResource(textRes), maxLines = 1)
    }
}

/*
 * Шаардлага 2: анх "**** ₮", дарахад бодит дүн ("+ 2,000,000 ₮")
 */
@Composable
private fun amountText(amount: Long, isVisible: Boolean, sign: String = ""): String =
    if (isVisible) sign + formatAmount(amount) + " ₮" else stringResource(R.string.hidden_amount)

// 15000 → "15,000"
private fun formatAmount(amount: Long): String = String.format(Locale.US, "%,d", amount)

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

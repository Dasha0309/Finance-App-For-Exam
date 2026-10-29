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
    onNavigateUp: () -> Unit,
    viewModel: EntryViewModel = viewModel(factory = AppViewModelProvider.Factory)
) {
    val uiState = viewModel.uiState
    // save() нь suspend тул товч дарахад coroutine эхлүүлэх scope хэрэгтэй
    val coroutineScope = rememberCoroutineScope()

    Scaffold(
        topBar = {
            FinanceTopAppBar(
                // id != 0 бол "Бүртгэл засах", үгүй бол "Бүртгэл нэмэх" (Шаардлага 5)
                title = stringResource(if (uiState.isEditMode) R.string.edit_title else R.string.add_title),
                canNavigateBack = true,
                navigateUp = onNavigateUp
            )
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .padding(innerPadding)
                .verticalScroll(rememberScrollState())   // Landscape үед гүйлгэнэ (Шаардлага 10)
                .padding(16.dp)
                .fillMaxWidth(),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Төрөл: Орлого / Зарлага
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
            // zuvhun uurchlugdsun talbariig soliod busdiin heveer ni uldeene
            OutlinedTextField(
                value = uiState.name,
                onValueChange = { viewModel.updateUiState(uiState.copy(name = it)) },
                label = { Text(stringResource(R.string.name_label)) },
                singleLine = true,
                modifier = Modifier.fillMaxWidth()
            )
            OutlinedTextField(
                value = uiState.amount,

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
                        viewModel.save()   // Room-д insert/update хийж дуустал хүлээнэ
                        navigateBack()     // дараа нь үндсэн дэлгэц рүү буцна (Flow-оор шинэчлэгдэнэ)
                    }
                },
                enabled = uiState.isValid, // талбарууд буруу бол ХАДГАЛАХ идэвхгүй
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(stringResource(R.string.save))
            }
        }
    }
}
//Songogdson turul bol filled button ugui bol outlined button
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
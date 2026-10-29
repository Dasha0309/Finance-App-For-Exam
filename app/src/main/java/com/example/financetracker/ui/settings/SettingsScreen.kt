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
    viewModel: SettingsViewModel = viewModel(factory
    = AppViewModelProvider.Factory)
) {
    val savedFilter by viewModel.savedFilter.collectAsState()

    var selected by rememberSaveable(savedFilter) {
        mutableStateOf(savedFilter) }
    val coroutineScope = rememberCoroutineScope()

    //Filter lists
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
    ) {
        innerPadding ->
        Column(
            modifier = Modifier
                .padding(innerPadding)
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
            Arrangement.spacedBy(8.dp)
        //Шаардлага 6: бүртгэлийн төрөл сонгох
        ) {
            options.forEach { (filter, label) ->
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .selectable(selected = selected == filter, onClick = {
                            selected = filter })
                        .padding(vertical = 8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    RadioButton(selected = selected == filter,
                        onClick = {selected = filter})
                    Text(label)
                }
            }
            Button(
                onClick = {
                    coroutineScope.launch {
                        viewModel.save(selected)
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
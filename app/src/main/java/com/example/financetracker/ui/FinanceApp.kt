package com.example.financetracker.ui

import androidx.compose.foundation.layout.RowScope
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.ui.res.stringResource
import androidx.navigation.compose.rememberNavController
import com.example.financetracker.R
import com.example.financetracker.ui.navigation.FinanceNavHost

/*
 * ҮҮРЭГ: Аппын "үндэс" composable + бүх дэлгэцэд дахин ашиглах дээд талын цэнхэр мөр (TopAppBar).
 *
 * FinanceApp()        — NavController үүсгээд FinanceNavHost-д өгнө. MainActivity үүнийг дуудна.
 * FinanceTopAppBar()  — "← Гарчиг ⚙" хэлбэрийн дээд мөр.
 */

@Composable
fun FinanceApp() {
    val navController = rememberNavController()
    FinanceNavHost(navController = navController)
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FinanceTopAppBar(
    title: String,
    canNavigateBack: Boolean,
    navigateUp: () -> Unit = {},
    action: @Composable RowScope.() -> Unit = {}
) {
    TopAppBar(
        title = {Text(title) },
        navigationIcon = {
            if (canNavigateBack) {
                IconButton(onClick = navigateUp) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                        contentDescription = stringResource(R.string.back)
                    )
                }
            }
        },
        actions = action,
        colors = TopAppBarDefaults.mediumTopAppBarColors(
            containerColor = MaterialTheme.colorScheme.primary,
            titleContentColor = MaterialTheme.colorScheme.onPrimary,
            navigationIconContentColor = MaterialTheme.colorScheme.onPrimary
        )
    )
}

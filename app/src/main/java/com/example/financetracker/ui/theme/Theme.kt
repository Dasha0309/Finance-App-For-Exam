package com.example.financetracker.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

/*
 * ҮҮРЭГ: Аппын өнгө, загвар (theme).
 * Энэ файлыг бэлэн өгсөн — шалгалтын оноонд хамаагүй, зөвхөн гадаад төрх.
 */

// Загвар зураг дээрх цэнхэр, ногоон, улаан өнгөнүүд
val Blue = Color(0xFF1E63D6)
val IncomeGreen = Color(0xFF1E8E3E)
val ExpenseRed = Color(0xFFE53935)
val IncomeBg = Color(0xFFDDF5E3)
val ExpenseBg = Color(0xFFFDE2E2)
val ScreenBg = Color(0xFFF2F4F8)

private val LightColors = lightColorScheme(
    primary = Blue,
    onPrimary = Color.White,
    secondary = IncomeGreen,
    error = ExpenseRed,
)

@Composable
fun FinanceTrackerTheme(content: @Composable () -> Unit) {
    MaterialTheme(
        colorScheme = LightColors,
        content = content
    )
}

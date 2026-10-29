# Алхам 3 — ViewModel үйлдвэр ба навигаци

3 дэлгэц хоорондын шилжилт:

```
             НЭМЭХ  → "entry"
 "home"  ── ЗАСАХ / удаан дарах → "entry?transactionId=5"
             ⚙      → "settings"
 "entry", "settings"  ── ХАДГАЛАХ эсвэл ← ──→ popBackStack() → "home"
```

Энэ алхмын файлууд `HomeViewModel`, `EntryScreen` гэх мэт хараахан бичээгүй классуудыг дуудна — Алхам 4–6-г дуусгатал улаан байх нь хэвийн.

## `ui/AppViewModelProvider.kt`

Файл: [ui/AppViewModelProvider.kt](../app/src/main/java/com/example/financetracker/ui/AppViewModelProvider.kt)

ViewModel бүрийг Repository-тэй нь хэрхэн үүсгэхийг заасан жор.

```kotlin
package com.example.financetracker.ui

import androidx.lifecycle.ViewModelProvider.AndroidViewModelFactory
import androidx.lifecycle.createSavedStateHandle
import androidx.lifecycle.viewmodel.CreationExtras
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import com.example.financetracker.FinanceApplication
import com.example.financetracker.ui.entry.EntryViewModel
import com.example.financetracker.ui.home.HomeViewModel
import com.example.financetracker.ui.settings.SettingsViewModel

/*
 * ҮҮРЭГ: ViewModel ҮЙЛДВЭР (Factory).
 *
 * Асуудал: ViewModel-ууд Repository авах ёстой, гэтэл Android ViewModel-ийг өөрөө үүсгэдэг
 *          тул constructor-т параметр дамжуулж чадахгүй.
 * Шийдэл:  "HomeViewModel хэрэгтэй бол ингэж үүсгэ" гэсэн жорыг энд бичнэ.
 *
 * Дэлгэц дээр:  viewModel(factory = AppViewModelProvider.Factory)
 */
object AppViewModelProvider {
    val Factory = viewModelFactory {

        // Үндсэн дэлгэц: бүртгэл + тохиргоо хоёулаа хэрэгтэй (шүүлтүүрийн төлөө)
        initializer {
            HomeViewModel(
                financeApplication().container.transactionRepository,
                financeApplication().container.settingsRepository
            )
        }

        // Нэмэх/засах дэлгэц: SavedStateHandle-аас навигацын аргумент (id)-ийг уншина
        initializer {
            EntryViewModel(
                this.createSavedStateHandle(),
                financeApplication().container.transactionRepository
            )
        }

        // Тохиргооны дэлгэц: зөвхөн DataStore
        initializer {
            SettingsViewModel(financeApplication().container.settingsRepository)
        }
    }
}

/*
 * CreationExtras-аас Application объектыг аваад FinanceApplication болгож хувиргана.
 * Ингэснээр .container-т хандаж чадна.
 */
fun CreationExtras.financeApplication(): FinanceApplication =
    (this[AndroidViewModelFactory.APPLICATION_KEY] as FinanceApplication)
```

## `ui/FinanceApp.kt`

Файл: [ui/FinanceApp.kt](../app/src/main/java/com/example/financetracker/ui/FinanceApp.kt)

NavController үүсгэх + бүх дэлгэцийн дээд цэнхэр мөр.

```kotlin
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
    // NavController — дэлгэц хооронд шилжүүлэгч. rememberNavController эргэлтэнд ч хадгалагдана.
    val navController = rememberNavController()
    FinanceNavHost(navController = navController)
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FinanceTopAppBar(
    title: String,
    canNavigateBack: Boolean,             // true бол "←" товч харагдана
    navigateUp: () -> Unit = {},          // "←" дарахад юу хийх
    actions: @Composable RowScope.() -> Unit = {}   // баруун талын товчнууд (жишээ нь ⚙)
) {
    TopAppBar(
        title = { Text(title) },
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
        actions = actions,
        colors = TopAppBarDefaults.topAppBarColors(
            containerColor = MaterialTheme.colorScheme.primary,
            titleContentColor = MaterialTheme.colorScheme.onPrimary,
            navigationIconContentColor = MaterialTheme.colorScheme.onPrimary,
            actionIconContentColor = MaterialTheme.colorScheme.onPrimary
        )
    )
}
```

## `ui/navigation/FinanceNavGraph.kt`

Файл: [ui/navigation/FinanceNavGraph.kt](../app/src/main/java/com/example/financetracker/ui/navigation/FinanceNavGraph.kt)

Route-ууд болон NavHost. Нэмэх/засах нэг дэлгэцийг `transactionId` аргументаар ялгана.

```kotlin
package com.example.financetracker.ui.navigation

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.navArgument
import com.example.financetracker.ui.entry.EntryScreen
import com.example.financetracker.ui.home.HomeScreen
import com.example.financetracker.ui.settings.SettingsScreen

/*
 * ҮҮРЭГ: Навигаци — аль дэлгэц ямар "хаяг" (route)-тай, хоорондоо яаж шилжихийг тодорхойлно.
 *
 *   "home"                        → Үндсэн дэлгэц (апп асахад эхэлнэ)
 *   "entry"                       → Бүртгэл НЭМЭХ  (id өгөөгүй → NEW_ID = -1)
 *   "entry?transactionId=5"       → 5 дугаар бүртгэлийг ЗАСАХ
 *   "settings"                    → Тохиргоо
 *
 * Нэмэх, засах хоёр НЭГ дэлгэц ашиглана — ялгаа нь зөвхөн id дамжуулсан эсэх.
 */

object HomeDestination {
    const val route = "home"
}

object EntryDestination {
    const val route = "entry"
    const val ARG_ID = "transactionId"
    const val NEW_ID = -1                             // "шинэ бүртгэл" гэсэн тэмдэг
    const val routeWithArgs = "$route?$ARG_ID={$ARG_ID}"   // "entry?transactionId={transactionId}"

    // id өгвөл засах хаяг, өгөхгүй бол нэмэх хаяг буцаана
    fun createRoute(id: Int? = null): String =
        if (id == null) route else "$route?$ARG_ID=$id"
}

object SettingsDestination {
    const val route = "settings"
}

@Composable
fun FinanceNavHost(
    navController: NavHostController,
    modifier: Modifier = Modifier
) {
    NavHost(
        navController = navController,
        startDestination = HomeDestination.route,
        modifier = modifier
    ) {
        // 1) Үндсэн дэлгэц
        composable(route = HomeDestination.route) {
            HomeScreen(
                navigateToAdd = { navController.navigate(EntryDestination.createRoute()) },
                navigateToEdit = { id -> navController.navigate(EntryDestination.createRoute(id)) },
                navigateToSettings = { navController.navigate(SettingsDestination.route) }
            )
        }

        // 2) Нэмэх / засах дэлгэц. transactionId аргументыг Int төрлөөр, анхны утга -1
        composable(
            route = EntryDestination.routeWithArgs,
            arguments = listOf(navArgument(EntryDestination.ARG_ID) {
                type = NavType.IntType
                defaultValue = EntryDestination.NEW_ID
            })
        ) {
            EntryScreen(navigateBack = { navController.popBackStack() })
        }

        // 3) Тохиргооны дэлгэц
        composable(route = SettingsDestination.route) {
            SettingsScreen(navigateBack = { navController.popBackStack() })
        }
    }
}
```

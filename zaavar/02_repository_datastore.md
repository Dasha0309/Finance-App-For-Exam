# Алхам 2 — Repository, DataStore, AppContainer

Шалгуур 2: **ViewModel, Repository, Flow ашигласан зөв архитектур (2 оноо)**.
Шалгуур 3: **Тохиргоог DataStore ашиглан хадгалсан байх (2 оноо)**.

```
ViewModel  →  Repository  →  DAO  →  Room DB
ViewModel  →  SettingsRepository  →  DataStore (settings файл)
```

`AppContainer` нь Repository-уудыг нэг удаа үүсгэж хадгална, `FinanceApplication` нь апп асахад AppContainer-ийг үүсгэнэ.

## `data/TransactionRepository.kt`

Файл: [data/TransactionRepository.kt](../app/src/main/java/com/example/financetracker/data/TransactionRepository.kt)

Interface + Room-д суурилсан хэрэгжүүлэлт нэг файлд.

```kotlin
package com.example.financetracker.data

import kotlinx.coroutines.flow.Flow

/*
 * ҮҮРЭГ: Repository — ViewModel ба өгөгдлийн эх үүсвэр (DAO) хоёрын ДУНДАХ давхарга.
 *
 * Яагаад хэрэгтэй вэ?
 *   ViewModel "өгөгдөл хаанаас ирж байна (Room уу, интернэт үү)" гэдгийг мэдэх шаардлагагүй.
 *   Тэр зөвхөн Repository-оос асууна. Дараа нь эх үүсвэр солигдсон ч ViewModel өөрчлөгдөхгүй.
 *
 * Энд interface (гэрээ), доорх класс нь түүний хэрэгжүүлэлт.
 */
interface TransactionRepository {
    fun getAllStream(): Flow<List<Transaction>>
    fun getByTypeStream(type: TransactionType): Flow<List<Transaction>>
    fun getByIdStream(id: Int): Flow<Transaction?>
    suspend fun insert(transaction: Transaction)
    suspend fun update(transaction: Transaction)
    suspend fun delete(transaction: Transaction)
}

/*
 * Room (офлайн DB) ашигладаг хэрэгжүүлэлт. Бүх ажлыг DAO руу дамжуулна.
 */
class OfflineTransactionRepository(
    private val dao: TransactionDao
) : TransactionRepository {
    override fun getAllStream() = dao.getAll()
    override fun getByTypeStream(type: TransactionType) = dao.getByType(type)
    override fun getByIdStream(id: Int) = dao.getById(id)
    override suspend fun insert(transaction: Transaction) = dao.insert(transaction)
    override suspend fun update(transaction: Transaction) = dao.update(transaction)
    override suspend fun delete(transaction: Transaction) = dao.delete(transaction)
}
```

## `data/SettingsRepository.kt`

Файл: [data/SettingsRepository.kt](../app/src/main/java/com/example/financetracker/data/SettingsRepository.kt)

`FilterType` enum (тохиргооны 3 сонголт) + DataStore унших/бичих.

```kotlin
package com.example.financetracker.data

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

/*
 * ҮҮРЭГ: Тохиргоог (ямар төрлийн бүртгэл харуулах) DataStore-д хадгалах, унших.
 * Шалгуур 3: "Харагдах бүртгэлийн төрлийн тохиргоог DataStore ашиглан хадгалсан байх" (2 оноо)
 *
 * DataStore = жижиг key-value (түлхүүр-утга) хадгалах газар. Апп хаагдсан ч утга үлдэнэ.
 *   "filter" → "ALL" / "INCOME" / "EXPENSE"
 *
 * Room-оос ялгаа: Room — олон мөртэй хүснэгт, DataStore — цөөн тохиргооны утга.
 */

// Тохиргооны 3 сонголт (Тохиргооны дэлгэцийн 3 radio button)
enum class FilterType {
    ALL,      // а) бүгдийг харуулах (анхны тохиргоо)
    INCOME,   // б) зөвхөн орлого
    EXPENSE   // в) зөвхөн зарлага
}

class SettingsRepository(
    private val dataStore: DataStore<Preferences>
) {
    // Түлхүүрийн нэр. Энэ нэрээр утгаа хадгалж, уншина.
    private companion object {
        val FILTER_KEY = stringPreferencesKey("filter")
    }

    // УНШИХ: DataStore-оос утгыг Flow хэлбэрээр авна.
    // Хадгалсан утга байхгүй бол (анх удаа) ALL буцаана.
    val filter: Flow<FilterType> = dataStore.data.map { preferences ->
        val saved = preferences[FILTER_KEY] ?: FilterType.ALL.name
        FilterType.valueOf(saved)          // "INCOME" текстийг FilterType.INCOME болгоно
    }

    // БИЧИХ: шинэ утгыг хадгална
    suspend fun saveFilter(filter: FilterType) {
        dataStore.edit { preferences ->
            preferences[FILTER_KEY] = filter.name
        }
    }
}
```

## `data/AppContainer.kt`

Файл: [data/AppContainer.kt](../app/src/main/java/com/example/financetracker/data/AppContainer.kt)

`preferencesDataStore` нь файлын ДЭЭД түвшинд (класс дотор биш) зарлагдана.

```kotlin
package com.example.financetracker.data

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.preferencesDataStore

/*
 * ҮҮРЭГ: Dependency container — Repository-уудыг НЭГ ГАЗАР үүсгэж, бусдад өгөх "агуулах".
 * FinanceApplication үүнийг нэг удаа үүсгэнэ → ViewModel-ууд эндээс Repository-гоо авна.
 */

// Context дээр "settings" нэртэй DataStore нэмж байна (файлын дээд түвшинд заавал зарлана —
// ингэснээр апп даяар ганцхан DataStore үүснэ).
private val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "settings")

interface AppContainer {
    val transactionRepository: TransactionRepository
    val settingsRepository: SettingsRepository
}

class DefaultAppContainer(private val context: Context) : AppContainer {

    // by lazy → анх хэрэглэх үед л үүснэ (апп хурдан асна)
    override val transactionRepository: TransactionRepository by lazy {
        OfflineTransactionRepository(FinanceDatabase.getDatabase(context).transactionDao())
    }

    override val settingsRepository: SettingsRepository by lazy {
        SettingsRepository(context.dataStore)
    }
}
```

## `FinanceApplication.kt`

Файл: [FinanceApplication.kt](../app/src/main/java/com/example/financetracker/FinanceApplication.kt)

Manifest дээр `android:name=".FinanceApplication"` гэж бүртгэсэн тул апп асахад үүснэ. `createNotificationChannel` нь Алхам 7-д бичигдэнэ — тэр хүртэл улаан байх нь хэвийн.

```kotlin
package com.example.financetracker

import android.app.Application
import com.example.financetracker.data.AppContainer
import com.example.financetracker.data.DefaultAppContainer
import com.example.financetracker.worker.createNotificationChannel

/*
 * ҮҮРЭГ: Апп асахад ХАМГИЙН ТҮРҮҮНД (Activity-оос ч өмнө) ажиллах класс.
 * AndroidManifest.xml дотор android:name=".FinanceApplication" гэж бүртгэсэн.
 *
 *  1) AppContainer-ийг үүсгэнэ (DB, DataStore, Repository-ууд)
 *  2) Мэдэгдлийн channel үүсгэнэ (Android 8+ дээр channel-гүй бол мэдэгдэл гарахгүй)
 */
class FinanceApplication : Application() {

    // lateinit → onCreate дотор утга оноохыг амлаж байна
    lateinit var container: AppContainer

    override fun onCreate() {
        super.onCreate()
        container = DefaultAppContainer(this)
        createNotificationChannel(this)
    }
}
```

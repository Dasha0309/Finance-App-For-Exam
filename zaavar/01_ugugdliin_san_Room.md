# Алхам 1 — Өгөгдлийн сан (Room)

Шалгуур 1: **SQLite эсвэл Room өгөгдлийн санд хадгалсан байх (3 оноо)**.

Room 3 хэсгээс бүтдэг:

```
Transaction (@Entity)   — хүснэгт ямар багануудтай вэ
      ↓
TransactionDao (@Dao)    — ямар SQL командууд байх вэ (CRUD)
      ↓
FinanceDatabase (@Database) — бүгдийг нэгтгэсэн DB объект (singleton)
```

Энэ 3 файлыг бичсэний дараа **Build → Make Project** хийхэд алдаагүй бол Room (KSP) DAO-гийн кодыг өөрөө үүсгэнэ.

## `data/Transaction.kt`

Файл: [data/Transaction.kt](../app/src/main/java/com/example/financetracker/data/Transaction.kt)

Хүснэгтийн бүтэц. `TransactionType` enum-ийг мөн энд зарласан.

```kotlin
package com.example.financetracker.data

import androidx.room.Entity
import androidx.room.PrimaryKey

/*
 * ҮҮРЭГ: Өгөгдлийн сангийн ХҮСНЭГТ (table)-ийг тодорхойлно.
 * Нэг Transaction объект = хүснэгтийн нэг мөр = нэг бүртгэл.
 *
 * PDF-ийн 3-р хэсэг: ID, Төрөл, Нэр, Мөнгөн дүн, Огноо гэсэн 5 талбар.
 *
 * Үүсэх SQL хүснэгт (Room автоматаар үүсгэнэ):
 *   CREATE TABLE transactions (
 *       id     INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,
 *       type   TEXT    NOT NULL,   -- 'INCOME' эсвэл 'EXPENSE'
 *       name   TEXT    NOT NULL,   -- 'Хоол'
 *       amount INTEGER NOT NULL,   -- 15000
 *       date   TEXT    NOT NULL    -- '2026.10.23'
 *   )
 */
@Entity(tableName = "transactions")          // @Entity → энэ класс бол хүснэгт
data class Transaction(
    @PrimaryKey(autoGenerate = true)          // id-г Room өөрөө 1, 2, 3... гэж өгнө
    val id: Int = 0,                          // 0 өгвөл "шинэ бүртгэл" гэж ойлгоод автоматаар дугаарлана
    val type: TransactionType,                // Орлого эсвэл Зарлага (enum-ийг Room TEXT болгож хадгална)
    val name: String,                         // Гүйлгээний нэр
    val amount: Long,                         // Мөнгөн дүн (₮). Long — том тоо багтана
    val date: String                          // Огноо "2026.10.23" хэлбэрээр
)

/*
 * Бүртгэлийн төрөл. String-ээр "орлого" гэж бичихийн оронд enum ашиглавал
 * үсгийн алдаа гарахгүй. Room 2.3+ enum-ийг нэрээр нь ('INCOME') автоматаар хадгалдаг.
 */
enum class TransactionType {
    INCOME,   // Орлого
    EXPENSE   // Зарлага
}
```

## `data/TransactionDao.kt`

Файл: [data/TransactionDao.kt](../app/src/main/java/com/example/financetracker/data/TransactionDao.kt)

`suspend` = нэг удаа бичих, `Flow` = DB өөрчлөгдөх бүрт шинэ жагсаалт ирнэ.

```kotlin
package com.example.financetracker.data

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Update
import kotlinx.coroutines.flow.Flow

/*
 * ҮҮРЭГ: DAO (Data Access Object) — өгөгдлийн сантай ярих SQL командууд.
 * Энд зөвхөн interface бичнэ, жинхэнэ кодыг Room (KSP) өөрөө үүсгэнэ.
 *
 * CRUD:
 *   C – Create  → insert()
 *   R – Read    → getAll(), getByType(), getById()
 *   U – Update  → update()
 *   D – Delete  → delete()
 *
 * suspend fun  → нэг удаа ажиллаад дуусдаг (бичих үйлдэл). Coroutine дотроос дуудна.
 * Flow<...>    → DB өөрчлөгдөх бүрт шинэ утга автоматаар "урсаж" ирнэ (уншихад).
 *                Тиймээс бүртгэл нэмэхэд үндсэн дэлгэц өөрөө шинэчлэгдэнэ.
 */
@Dao
interface TransactionDao {

    // Шинэ бүртгэл нэмэх
    @Insert
    suspend fun insert(transaction: Transaction)

    // Байгаа бүртгэлийг засах (id-гаар нь олж солино)
    @Update
    suspend fun update(transaction: Transaction)

    // Бүртгэл устгах (id-гаар нь олж устгана)
    @Delete
    suspend fun delete(transaction: Transaction)

    // Бүх бүртгэл (тохиргоо = "хоёуланг нь харуулах")
    @Query("SELECT * FROM transactions ORDER BY date DESC, id DESC")
    fun getAll(): Flow<List<Transaction>>

    // Зөвхөн нэг төрлийн бүртгэл (тохиргоо = "зөвхөн орлого" / "зөвхөн зарлага")
    // :type → функцийн type параметрийн утга энд орно
    @Query("SELECT * FROM transactions WHERE type = :type ORDER BY date DESC, id DESC")
    fun getByType(type: TransactionType): Flow<List<Transaction>>

    // Нэг бүртгэлийг id-гаар нь авах (засах дэлгэц дээр талбаруудыг бөглөхөд)
    @Query("SELECT * FROM transactions WHERE id = :id")
    fun getById(id: Int): Flow<Transaction?>
}
```

## `data/FinanceDatabase.kt`

Файл: [data/FinanceDatabase.kt](../app/src/main/java/com/example/financetracker/data/FinanceDatabase.kt)

Codelab-ын InventoryDatabase-тай яг ижил загвар.

```kotlin
package com.example.financetracker.data

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase

/*
 * ҮҮРЭГ: Room өгөгдлийн санг өөрөө (finance_database файл) үүсгэх.
 *
 * entities = [Transaction::class] → ямар хүснэгтүүд байхыг заана
 * version = 1                     → хүснэгтийн бүтэц өөрчлөгдвөл тоог нэмнэ
 * exportSchema = false            → схемийн JSON файл гаргахгүй
 *
 * Singleton загвар: апп даяар DB ганцхан хуулбартай байх ёстой.
 * (Олон хуулбар үүсгэвэл удаан, алдаа гарна.)
 */
@Database(entities = [Transaction::class], version = 1, exportSchema = false)
abstract class FinanceDatabase : RoomDatabase() {

    // Room энэ функцийн биеийг өөрөө бичнэ — бид зөвхөн зарлана
    abstract fun transactionDao(): TransactionDao

    companion object {
        // @Volatile → бүх thread хамгийн сүүлийн утгыг харна
        @Volatile
        private var Instance: FinanceDatabase? = null

        fun getDatabase(context: Context): FinanceDatabase {
            // Хэрэв аль хэдийн үүссэн бол түүнийгээ буцаана,
            // үгүй бол synchronized дотор (нэг зэрэг 2 thread үүсгэхээс сэргийлж) шинээр үүсгэнэ
            return Instance ?: synchronized(this) {
                Room.databaseBuilder(context, FinanceDatabase::class.java, "finance_database")
                    .fallbackToDestructiveMigration() // version өөрчлөгдвөл хуучин DB-г устгаад шинээр үүсгэнэ
                    .build()
                    .also { Instance = it }
            }
        }
    }
}
```

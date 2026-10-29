package com.example.financetracker.data

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Update
import kotlinx.coroutines.flow.Flow

/*
 * DAO (Data Access Object) — өгөгдлийн сантай ярих SQL командууд.
 *
 * suspend fun  → нэг удаа ажиллаад дуусдаг (бичих үйлдэл). Coroutine дотроос дуудна.
 * Flow<...>    → DB өөрчлөгдөх бүрт шинэ утга автоматаар "урсаж" ирнэ (уншихад).
 *                Тиймээс бүртгэл нэмэхэд үндсэн дэлгэц өөрөө шинэчлэгдэнэ.
 */

@Dao
interface TransactionDao {
    @Insert
    suspend fun insert(transaction: Transaction)

    @Update
    suspend fun update(transaction: Transaction)

    @Delete
    suspend fun delete(transaction: Transaction)

    @Query("SELECT * FROM transactions ORDER BY date DESC, id DESC")
    fun getAll(): Flow<List<Transaction>>

    @Query("SELECT * FROM transactions WHERE type = :type ORDER BY date DESC, id DESC")
    fun getByType(type: TransactionType): Flow<List<Transaction>>

    @Query("SELECT * FROM transactions WHERE id = :id")
    fun getById(id: Int): Flow<Transaction?>
}
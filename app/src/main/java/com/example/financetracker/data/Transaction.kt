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

@Entity(tableName = "transactions")

data class Transaction(
    @PrimaryKey(autoGenerate = true) //id g room uuruu 1s ehelj ugnu

    val id: Int = 0, //0 gevel shine burtgel geed automataar dugaarlana
    val type: TransactionType, //type: orlogo esvel zarlaga Room text bolgoj hadgalna
    val name: String,
    val amount: Long,
    val date: String
)

enum class TransactionType{
    INCOME,
    EXPENSE
}
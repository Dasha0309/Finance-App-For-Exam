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

@Database(entities = [Transaction::class], version = 1,
    exportSchema = false)
    abstract class FinanceDatabase : RoomDatabase() {

        //Room d ene function biyiig bichne endees zarlaj baina
        abstract fun transactionDao(): TransactionDao

        companion object {

            @Volatile //buh thread hamgiin suuliin utgiig harna
            private var Instance: FinanceDatabase? = null

            fun getDatabase(context: Context): FinanceDatabase {
                //Uussen bol tuuniigee butsaana
                //neg zereg 2 thread uusehees sergiilj shineer uusgene
                return Instance ?: synchronized(this) {
                    Room.databaseBuilder(context, FinanceDatabase::class.java, "finance_database")
                        .fallbackToDestructiveMigration()
                        .build()
                        .also { Instance = it }
                }
            }
        }
    }
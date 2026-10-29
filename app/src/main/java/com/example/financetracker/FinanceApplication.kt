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

    lateinit var container: AppContainer

    override fun onCreate() {
        super.onCreate()
        container = DefaultAppContainer(this)
        createNotificationChannel(this)
    }
}
package com.example.financetracker.worker

import android.content.Context
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.OneTimeWorkRequest
import androidx.work.PeriodicWorkRequest
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import java.util.concurrent.TimeUnit

/*
 * ҮҮРЭГ: ReminderWorker-ийг ХЭЗЭЭ ажиллуулахыг WorkManager-т захиалах / цуцлах.
 *
 * Логик ("апп-аас гарснаас хойш 24 цаг тутамд"):
 *   - Хэрэглэгч аппаас ГАРАХАД (MainActivity.onStop)  → schedule():
 *         24 цагийн дараа эхэлж, түүнээс хойш 24 цаг тутам давтагдах ажил захиална.
 *   - Хэрэглэгч апп руу ОРОХОД (MainActivity.onStart) → cancel():
 *         аппаа ашиглаж байгаа тул сануулга хэрэггүй, захиалгыг цуцална.
 *   Дахин гарахад тоолуур 0-ээс эхэлнэ.
 *
 */

private const val REMINDER_WORK_NAME = "finance_reminder"

private const val REMINDER_INTERVAL_HOURS = 24L

object ReminderScheduler {
    //Шалгуур 4: аппаас гарахад дуудагдана

    fun schedule(context: Context) {

        val testRequest = androidx.work.OneTimeWorkRequestBuilder<ReminderWorker>()
            .setInitialDelay(5, java.util.concurrent.TimeUnit.SECONDS)
            .build()

        WorkManager.getInstance(context).enqueueUniqueWork(
            REMINDER_WORK_NAME,
            androidx.work.ExistingWorkPolicy.REPLACE,
            testRequest
        )
    }

    fun cancel(context: Context) {
        WorkManager.getInstance(context).cancelUniqueWork(REMINDER_WORK_NAME)
    }
}

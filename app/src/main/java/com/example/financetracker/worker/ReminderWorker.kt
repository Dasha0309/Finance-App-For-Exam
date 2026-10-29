package com.example.financetracker.worker

import android.content.Context
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters

/*
 * ҮҮРЭГ: WorkManager-ийн "ажил" — цаг нь болоход арын горимд (апп хаалттай байсан ч) ажиллана.
 * Хийх ажил нь ганцхан: сануулах мэдэгдэл харуулах.
 *
 * Шалгуур 4: "Апп-аас гарснаас хойш 24 цаг тутамд санхүүгийн бүртгэлээ хөтлөхийг
 *             сануулсан мэдэгдэл илгээдэг байх" (2 оноо)
 *
 * ХЭЗЭЭ ажиллахыг энэ класс мэдэхгүй — үүнийг ReminderScheduler тохируулна.
 */

class ReminderWorker(
    context: Context,
    params: WorkerParameters
) : CoroutineWorker(context, params) {

    override suspend fun doWork(): Result {
        showReminderNotification(applicationContext)
        return Result.success()
    }
}
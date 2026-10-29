package com.example.financetracker.worker

import android.Manifest
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.core.content.ContextCompat
import com.example.financetracker.MainActivity
import com.example.financetracker.R
import kotlin.jvm.java
import androidx.compose.ui.platform.LocalContext

/*
 * ҮҮРЭГ: Мэдэгдэл (notification)-тэй холбоотой туслах функцууд.
 *   createNotificationChannel() — channel үүсгэнэ (FinanceApplication.onCreate-д дуудна)
 *   showReminderNotification()  — мэдэгдлийг харуулна (ReminderWorker дуудна)
 *
 * Шалгуур 5: "Ирсэн мэдэгдэл дээр дарахад аппликейшний үндсэн дэлгэц нээгддэг байх"
 *   → PendingIntent нь MainActivity-г нээнэ. MainActivity-ийн эхний дэлгэц = Үндсэн дэлгэц.
 */

const val CHANNEL_ID = "finance_reminder_channel"
private const val NOTIFICATION_ID = 1

fun createNotificationChannel(context: Context) {
    if (Build.VERSION.SDK_INT >=
        Build.VERSION_CODES.O) {
        val channel = NotificationChannel(
            CHANNEL_ID,
            context.getString(R.string.channel_name),
            NotificationManager.IMPORTANCE_DEFAULT
        )
        val manager = context.getSystemService(NotificationManager::class.java)
        manager.createNotificationChannel(channel)
    }
}

//Шалгуур 4: Сануулах мэдэгдлийг харуулах

fun showReminderNotification(context: Context) {
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU &&
        ContextCompat.checkSelfPermission(context,
            Manifest.permission.POST_NOTIFICATIONS)
        != PackageManager.PERMISSION_GRANTED
        ) {
        return
    }

    //Шалгуур 5: Мэдэгдэл дээр дарахад main activity рүү үсрэнэ
    val intent = Intent(context, MainActivity::class.java).apply {
        flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
    }

    val pendingIntent = PendingIntent.getActivity(
        context,
        0,
        intent,
        PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
    )

    val notification = NotificationCompat.Builder(context, CHANNEL_ID)
        .setSmallIcon(R.drawable.ic_launcher_foreground)
        .setContentTitle(context.getString(R.string.notification_title))
        .setContentText(context.getString(R.string.notification_text))
        .setContentIntent(pendingIntent)
        .setAutoCancel(true)
        .setPriority(NotificationCompat.PRIORITY_DEFAULT)
        .build()

    NotificationManagerCompat.from(context).notify(NOTIFICATION_ID, notification)
}
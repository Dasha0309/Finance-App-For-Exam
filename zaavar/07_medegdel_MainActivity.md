# Алхам 7 — Мэдэгдэл (WorkManager) ба MainActivity

Шалгуур 4: **Апп-аас гарснаас хойш 24 цаг тутамд сануулга (2 оноо)**.
Шалгуур 5: **Мэдэгдэл дээр дарахад үндсэн дэлгэц нээгдэх (1 оноо)**.

```
MainActivity.onStop()  ─→ ReminderScheduler.schedule()  ─→ WorkManager (24 цагийн дараа, 24 цаг тутам)
MainActivity.onStart() ─→ ReminderScheduler.cancel()                         │
                                                                             ↓
                                    ReminderWorker.doWork() ─→ showReminderNotification()
                                                                             │ (дарахад)
                                                                             ↓
                                                           PendingIntent ─→ MainActivity (үндсэн дэлгэц)
```

### Багшид үзүүлэхдээ (24 цаг хүлээхгүйгээр) турших

`ReminderScheduler.schedule()`-ийн доторхыг түр ингэж солиод аппаасаа гарч (Home товч) 10 секунд хүлээ:

```kotlin
val testRequest = OneTimeWorkRequestBuilder<ReminderWorker>()
    .setInitialDelay(10, TimeUnit.SECONDS)
    .build()
WorkManager.getInstance(context).enqueueUniqueWork(
    REMINDER_WORK_NAME, ExistingWorkPolicy.REPLACE, testRequest
)
```

(import: `androidx.work.OneTimeWorkRequestBuilder`, `androidx.work.ExistingWorkPolicy`). Туршсаны дараа буцааж 24 цагийн хувилбарт оруулаарай.

## `worker/NotificationHelper.kt`

Файл: [worker/NotificationHelper.kt](../app/src/main/java/com/example/financetracker/worker/NotificationHelper.kt)

Channel үүсгэх + мэдэгдэл харуулах. `PendingIntent` нь MainActivity-г нээнэ.

```kotlin
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
    // Channel зөвхөн Android 8.0 (API 26)-аас хойш байдаг
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
        val channel = NotificationChannel(
            CHANNEL_ID,
            context.getString(R.string.channel_name),
            NotificationManager.IMPORTANCE_DEFAULT
        )
        val manager = context.getSystemService(NotificationManager::class.java)
        manager.createNotificationChannel(channel)
    }
}

fun showReminderNotification(context: Context) {
    // Android 13+ дээр хэрэглэгч зөвшөөрөл өгөөгүй бол мэдэгдэл харуулахгүй
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU &&
        ContextCompat.checkSelfPermission(context, Manifest.permission.POST_NOTIFICATIONS)
        != PackageManager.PERMISSION_GRANTED
    ) {
        return
    }

    // Мэдэгдэл дээр дарахад MainActivity нээгдэнэ
    val intent = Intent(context, MainActivity::class.java).apply {
        flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
    }
    // PendingIntent = "дараа нь (мэдэгдэл дарахад) энэ Intent-ийг ажиллуул" гэсэн зөвшөөрөл
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
        .setContentIntent(pendingIntent)   // дарахад юу болох
        .setAutoCancel(true)               // дарсны дараа мэдэгдэл алга болно
        .setPriority(NotificationCompat.PRIORITY_DEFAULT)
        .build()

    NotificationManagerCompat.from(context).notify(NOTIFICATION_ID, notification)
}
```

## `worker/ReminderWorker.kt`

Файл: [worker/ReminderWorker.kt](../app/src/main/java/com/example/financetracker/worker/ReminderWorker.kt)

Ганцхан ажилтай: мэдэгдэл харуулах.

```kotlin
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
        return Result.success()   // амжилттай дууссан гэж WorkManager-т мэдэгдэнэ
    }
}
```

## `worker/ReminderScheduler.kt`

Файл: [worker/ReminderScheduler.kt](../app/src/main/java/com/example/financetracker/worker/ReminderScheduler.kt)

Хэзээ ажиллуулахыг WorkManager-т захиална.

```kotlin
package com.example.financetracker.worker

import android.content.Context
import androidx.work.ExistingPeriodicWorkPolicy
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
 * Unique work: "finance_reminder" нэртэй ганцхан ажил байна, давхардахгүй.
 *
 * ТУРШИХ ҮЕД: 24 цаг хүлээх боломжгүй тул доорх REMINDER_INTERVAL_HOURS-ийн оронд
 *   OneTimeWorkRequestBuilder<ReminderWorker>().setInitialDelay(10, TimeUnit.SECONDS)
 *   ашиглаад WorkManager.enqueueUniqueWork(...) гэж туршиж болно (заавар/07-д бичсэн).
 */

private const val REMINDER_WORK_NAME = "finance_reminder"
private const val REMINDER_INTERVAL_HOURS = 24L

object ReminderScheduler {

    fun schedule(context: Context) {
        val request = PeriodicWorkRequestBuilder<ReminderWorker>(
            REMINDER_INTERVAL_HOURS, TimeUnit.HOURS            // давтагдах хугацаа: 24 цаг
        )
            .setInitialDelay(REMINDER_INTERVAL_HOURS, TimeUnit.HOURS) // анхных нь 24 цагийн дараа
            .build()

        WorkManager.getInstance(context).enqueueUniquePeriodicWork(
            REMINDER_WORK_NAME,
            ExistingPeriodicWorkPolicy.CANCEL_AND_REENQUEUE,   // хуучин захиалга байвал устгаад шинээр (тоолуур 0-ээс)
            request
        )
    }

    fun cancel(context: Context) {
        WorkManager.getInstance(context).cancelUniqueWork(REMINDER_WORK_NAME)
    }
}
```

## `MainActivity.kt`

Файл: [MainActivity.kt](../app/src/main/java/com/example/financetracker/MainActivity.kt)

Хамгийн сүүлд бич — бүх зүйлийг холбоно.

```kotlin
package com.example.financetracker

import android.Manifest
import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import com.example.financetracker.ui.FinanceApp
import com.example.financetracker.ui.theme.FinanceTrackerTheme
import com.example.financetracker.worker.ReminderScheduler

/*
 * ҮҮРЭГ: Аппын цорын ганц Activity. Compose-оор бүх дэлгэцийг зурна.
 *
 *  onCreate  — мэдэгдлийн зөвшөөрөл асуух, FinanceApp()-ийг харуулах
 *  onStart   — апп руу орлоо → сануулгын захиалгыг цуцлах
 *  onStop    — аппаас гарлаа → 24 цагийн дараах сануулга захиалах (шалгуур 4)
 */
class MainActivity : ComponentActivity() {

    // Android 13+ дээр "Мэдэгдэл илгээхийг зөвшөөрөх үү?" цонх харуулах
    private val notificationPermissionLauncher =
        registerForActivityResult(ActivityResultContracts.RequestPermission()) { /* хариу хэрэггүй */ }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            notificationPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
        }

        setContent {
            FinanceTrackerTheme {
                FinanceApp()
            }
        }
    }

    override fun onStart() {
        super.onStart()
        ReminderScheduler.cancel(this)
    }

    override fun onStop() {
        super.onStop()
        // Утсыг эргүүлэх үед ч onStop дуудагддаг — тэр үед жинхэнэ "гарах" биш тул алгасна
        if (!isChangingConfigurations) {
            ReminderScheduler.schedule(this)
        }
    }
}
```

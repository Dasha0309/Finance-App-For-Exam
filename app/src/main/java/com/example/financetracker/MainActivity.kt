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
    private val notificationPermissionLauncher =
        registerForActivityResult(ActivityResultContracts.RequestPermission()) {}

    override fun onCreate(savedInstancesState: Bundle?) {
        super.onCreate(savedInstancesState)
        enableEdgeToEdge()

        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            notificationPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
        }
        //FinanceApp ni ehnii undsen delgets
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
        if(!isChangingConfigurations) {
            ReminderScheduler.schedule(this)
        }
    }
}
package com.example.nourishfit.workers

import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.example.nourishfit.NourishFitApp
import com.example.nourishfit.R
import com.google.firebase.auth.FirebaseAuth
import java.time.LocalDate
import java.time.format.DateTimeFormatter

class NotificationWorker(
    private val context: Context,
    workerParams: WorkerParameters
) : CoroutineWorker(context, workerParams) {

    override suspend fun doWork(): Result {
        // Get the repository from the Application class (simple way without Dagger/Hilt)
        val repository = (applicationContext as NourishFitApp).repository
        val auth = FirebaseAuth.getInstance()
        val userId = auth.currentUser?.uid

        // If no user is logged in, don't do anything
        if (userId == null) {
            return Result.success()
        }

        // --- This is the "AI" Logic ---
        val todayStr = LocalDate.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd"))
        val foodCount = repository.getFoodCountForDate(todayStr, userId)

        // IF the user has logged 0 items today, send a reminder.
        if (foodCount == 0) {
            sendNotification(
                context = context,
                title = "Don't Forget to Log!",
                message = "You haven't logged any meals today. Tap to add your food and stay on track!"
            )
        }

        return Result.success()
    }

    private fun sendNotification(context: Context, title: String, message: String) {
        val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        val channelId = "NourishFitReminderChannel"

        // Create a notification channel for Android 8.0+
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                channelId,
                "Daily Reminders",
                NotificationManager.IMPORTANCE_DEFAULT
            ).apply {
                description = "Reminders to log meals and stay on track"
            }
            notificationManager.createNotificationChannel(channel)
        }

        // Build the notification
        val notification = NotificationCompat.Builder(context, channelId)
            .setSmallIcon(R.drawable.ic_launcher_foreground) // Uses your app icon
            .setContentTitle(title)
            .setContentText(message)
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
            .setAutoCancel(true) // Dismisses when tapped
            .build()

        // Show the notification
        notificationManager.notify(1, notification)
    }
}

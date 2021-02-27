package com.example.mobilecomputing

import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.work.*
import java.util.concurrent.TimeUnit

class ReminderNotification(context: Context, uid: Int, timeMs: Long, message: String) {
    init {
        val params = Data.Builder()
            .putString("message", message)
            .putInt("uid", uid)
            .build()

        val worker = OneTimeWorkRequest.Builder(AsyncReminder::class.java)
            .setInputData(params)
            .setInitialDelay(getTimeUntilRemindsMs(timeMs), TimeUnit.MILLISECONDS)
            .build()

        WorkManager.getInstance(context).enqueue(worker)
    }

    private fun getTimeUntilRemindsMs(timeMs: Long) : Long {
        if (timeMs > System.currentTimeMillis()) {
            return timeMs - System.currentTimeMillis()
        }
        return 0L
    }
}

class AsyncReminder(private val context: Context, params: WorkerParameters) : Worker(context, params) {
    override fun doWork() : Result {
        val channelId = "REMINDER_APP"
        var builder = NotificationCompat.Builder(context, channelId)
            .setContentTitle(context.getString(R.string.app_name))
            .setContentText(inputData.getString("message"))
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
            .setGroup(channelId)
            .setSmallIcon(R.drawable.ic_launcher_background)

        val manager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val notificationChannel = NotificationChannel(
                channelId,
                context.getString(R.string.app_name),
                NotificationManager.IMPORTANCE_DEFAULT).apply() {
                description = context.getString(R.string.app_name)
            }
            manager.createNotificationChannel(notificationChannel)
        }

        var uid = inputData.getInt("uid", 0)
        val db = getReminderDbMainThread(applicationContext)
        val dao = db.reminderDao()
        var reminder = dao.getReminder(uid)
        reminder.reminderOccurred = true
        dao.update(reminder)
        db.close()
        manager.notify(uid, builder.build())
        return Result.success()
    }
}
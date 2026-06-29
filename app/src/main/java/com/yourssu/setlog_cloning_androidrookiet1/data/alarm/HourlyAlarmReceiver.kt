package com.yourssu.setlog_cloning_androidrookiet1.data.alarm

import android.app.AlarmManager
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.os.Build
import androidx.core.app.NotificationCompat
import com.yourssu.setlog_cloning_androidrookiet1.MainActivity
import com.yourssu.setlog_cloning_androidrookiet1.R
import java.util.Calendar
import java.util.Locale
import java.util.TimeZone

class HourlyAlarmReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        val calendar = Calendar.getInstance(TimeZone.getTimeZone("Asia/Seoul"))
        val currentHour = calendar.get(Calendar.HOUR_OF_DAY)

        if (currentHour in 4..23 || currentHour == 0) {
            val timeString = String.format(Locale.KOREA, "%02d:00", currentHour)
            sendNotification(context, timeString)
        }

        scheduleNextAlarm(context)
    }

    private fun sendNotification(context: Context, timeString: String) {
        val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        val channelId = "setlog_hourly_channel"

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                channelId,
                "정각 알림",
                NotificationManager.IMPORTANCE_HIGH
            )
            notificationManager.createNotificationChannel(channel)
        }

        val intent = Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        }
        val pendingIntent = PendingIntent.getActivity(
            context, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val builder = NotificationCompat.Builder(context, channelId)
            .setSmallIcon(R.mipmap.ic_launcher)
            .setContentTitle("SETLOG")
            .setContentText("${timeString}입니다 셋로그를 올릴 시간이에요")
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setAutoCancel(true)
            .setContentIntent(pendingIntent)

        val notificationId = Calendar.getInstance(TimeZone.getTimeZone("Asia/Seoul")).get(Calendar.HOUR_OF_DAY)
        notificationManager.notify(notificationId, builder.build())
    }

    companion object {
        fun scheduleNextAlarm(context: Context) {
            val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
            val intent = Intent(context, HourlyAlarmReceiver::class.java)
            val pendingIntent = PendingIntent.getBroadcast(
                context, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
            )

            val calendar = Calendar.getInstance(TimeZone.getTimeZone("Asia/Seoul")).apply {
                add(Calendar.HOUR_OF_DAY, 1)
                set(Calendar.MINUTE, 0)
                set(Calendar.SECOND, 0)
                set(Calendar.MILLISECOND, 0)
            }

            val nextHour = calendar.get(Calendar.HOUR_OF_DAY)
            if (nextHour in 1..3) {
                calendar.set(Calendar.HOUR_OF_DAY, 4)
            }

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                if (alarmManager.canScheduleExactAlarms()) {
                    alarmManager.setExactAndAllowWhileIdle(
                        AlarmManager.RTC_WAKEUP,
                        calendar.timeInMillis,
                        pendingIntent
                    )
                }
            } else {
                alarmManager.setExactAndAllowWhileIdle(
                    AlarmManager.RTC_WAKEUP,
                    calendar.timeInMillis,
                    pendingIntent
                )
            }
        }
    }
}
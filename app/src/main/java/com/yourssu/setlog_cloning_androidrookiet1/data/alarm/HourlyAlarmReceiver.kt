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
import com.yourssu.setlog_cloning_androidrookiet1.data.model.NotificationItem
import com.yourssu.setlog_cloning_androidrookiet1.data.repository.LogAlarmSettingsRepository
import com.yourssu.setlog_cloning_androidrookiet1.data.repository.NotificationRepository
import java.util.Calendar
import java.util.Locale
import java.util.TimeZone

class HourlyAlarmReceiver : BroadcastReceiver() {

    override fun onReceive(context: Context, intent: Intent) {
        // BOOT_COMPLETED 처리: 재부팅 후 알람 재등록
        if (intent.action == Intent.ACTION_BOOT_COMPLETED) {
            scheduleNextAlarm(context)
            return
        }

        NotificationRepository.init(context)

        val calendar = Calendar.getInstance(TimeZone.getTimeZone("Asia/Seoul"))
        val currentHour = calendar.get(Calendar.HOUR_OF_DAY)

        // 새벽 1~3시는 알림 발송 안 함 (오전 4시 ~ 자정)
        if (currentHour in 4..23 || currentHour == 0) {
            // 예: 11시에 발동 → "11:00 로그 남길 시간!" 표시
            val timeString = String.format(Locale.KOREA, "%02d:00", currentHour)
            val notificationBody = "$timeString 로그 남길 시간!"

            // 1) 알림함에 저장 (읽음 상태 = false)
            val notifItem = NotificationItem(
                id = "hourly_${calendar.timeInMillis}",
                title = "SETLOG",
                body = notificationBody,
                timestamp = calendar.timeInMillis,
                isRead = false
            )
            NotificationRepository.addNotification(context, notifItem)

            // 2) 시스템 푸시 알림 발송
            sendPushNotification(context, notificationBody, notifItem.id)
        }

        scheduleNextAlarm(context)
    }

    private fun sendPushNotification(context: Context, body: String, notifId: String) {
        val notificationManager =
            context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        val channelId = "setlog_hourly_channel"

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                channelId,
                "정각 알림",
                NotificationManager.IMPORTANCE_HIGH
            )
            notificationManager.createNotificationChannel(channel)
        }

        // 알림 클릭 시 MainActivity를 열고 알림함을 표시하도록 extra 전달
        val tapIntent = Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_SINGLE_TOP
            putExtra(MainActivity.EXTRA_OPEN_NOTIFICATIONS, true)
            putExtra(MainActivity.EXTRA_NOTIFICATION_ID, notifId)
        }
        val pendingIntent = PendingIntent.getActivity(
            context,
            notifId.hashCode(),
            tapIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val builder = NotificationCompat.Builder(context, channelId)
            .setSmallIcon(R.mipmap.ic_launcher)
            .setContentTitle("SETLOG")
            .setContentText(body)
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setAutoCancel(true)
            .setContentIntent(pendingIntent)

        val systemNotifId =
            Calendar.getInstance(TimeZone.getTimeZone("Asia/Seoul")).get(Calendar.HOUR_OF_DAY)
        notificationManager.notify(systemNotifId, builder.build())
    }

    companion object {

        private fun pendingIntentFor(context: Context): PendingIntent {
            val intent = Intent(context, HourlyAlarmReceiver::class.java)
            return PendingIntent.getBroadcast(
                context, 0, intent,
                PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
            )
        }

        /** 사용자가 설정한 주기/분 값을 기준으로 다음 알람을 예약합니다. "끔"이면 예약을 취소합니다. */
        fun scheduleNextAlarm(context: Context) {
            val settings = LogAlarmSettingsRepository.get(context)
            val intervalHours = settings.interval.intervalHours

            val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
            val pendingIntent = pendingIntentFor(context)

            if (intervalHours <= 0) {
                // "끔" 설정 → 예약된 알람을 취소하고 종료
                alarmManager.cancel(pendingIntent)
                return
            }

            val minute = settings.minute
            val now = System.currentTimeMillis()

            val calendar = Calendar.getInstance(TimeZone.getTimeZone("Asia/Seoul")).apply {
                set(Calendar.MINUTE, minute)
                set(Calendar.SECOND, 0)
                set(Calendar.MILLISECOND, 0)
            }

            // 다음 정시 후보 중, intervalHours의 배수이면서 새벽 1~3시를 피하고
            // 현재 시각보다 미래인 가장 가까운 시각을 찾는다.
            while (
                calendar.timeInMillis <= now ||
                calendar.get(Calendar.HOUR_OF_DAY) % intervalHours != 0 ||
                calendar.get(Calendar.HOUR_OF_DAY) in 1..3
            ) {
                calendar.add(Calendar.HOUR_OF_DAY, 1)
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

        /** 알림 설정을 "끔"으로 바꿨을 때 즉시 예약된 알람을 취소합니다. */
        fun cancelAlarm(context: Context) {
            val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
            alarmManager.cancel(pendingIntentFor(context))
        }
    }
}

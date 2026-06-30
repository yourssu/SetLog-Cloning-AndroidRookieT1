package com.yourssu.setlog_cloning_androidrookiet1.data.repository

import android.content.Context
import com.yourssu.setlog_cloning_androidrookiet1.data.alarm.LogAlarmInterval
import com.yourssu.setlog_cloning_androidrookiet1.data.alarm.LogAlarmSettings

/**
 * 로그 알림(주기 / 분) 설정을 SharedPreferences에 저장/불러오는 레포지토리.
 */
object LogAlarmSettingsRepository {

    private const val PREFS_NAME = "setlog_alarm_settings"
    private const val KEY_INTERVAL = "interval"
    private const val KEY_MINUTE = "minute"

    fun get(context: Context): LogAlarmSettings {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        val interval = LogAlarmInterval.fromName(prefs.getString(KEY_INTERVAL, null))
        val minute = prefs.getInt(KEY_MINUTE, 20)
        return LogAlarmSettings(interval = interval, minute = minute)
    }

    fun save(context: Context, settings: LogAlarmSettings) {
        context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
            .edit()
            .putString(KEY_INTERVAL, settings.interval.name)
            .putInt(KEY_MINUTE, settings.minute)
            .apply()
    }
}

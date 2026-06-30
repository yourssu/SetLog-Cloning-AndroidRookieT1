package com.yourssu.setlog_cloning_androidrookiet1.data.alarm

/**
 * 로그 알림 주기 옵션.
 * intervalHours = 0 은 "끔" (알림 사용 안 함)을 의미합니다.
 */
enum class LogAlarmInterval(val label: String, val intervalHours: Int) {
    HOURLY("매시간", 1),
    EVERY_2_HOURS("2시간", 2),
    EVERY_3_HOURS("3시간", 3),
    EVERY_6_HOURS("6시간", 6),
    DAILY("하루", 24),
    OFF("끔", 0);

    companion object {
        fun fromName(name: String?): LogAlarmInterval =
            entries.firstOrNull { it.name == name } ?: HOURLY
    }
}

/** 분 옵션: 00, 10, 20, 30, 40, 50 */
val LOG_ALARM_MINUTE_OPTIONS = listOf(0, 10, 20, 30, 40, 50)

data class LogAlarmSettings(
    val interval: LogAlarmInterval = LogAlarmInterval.HOURLY,
    val minute: Int = 20
)

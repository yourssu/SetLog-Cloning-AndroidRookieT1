package com.yourssu.setlog_cloning_androidrookiet1.data.repository

import android.content.Context
import com.yourssu.setlog_cloning_androidrookiet1.data.model.NotificationItem
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import org.json.JSONArray
import org.json.JSONObject

/**
 * 알림 데이터를 SharedPreferences에 저장/불러오는 레포지토리.
 * 싱글톤으로 사용해 앱 전역에서 상태를 공유합니다.
 */
object NotificationRepository {

    private const val PREFS_NAME = "setlog_notifications"
    private const val KEY_NOTIFICATIONS = "notifications"
    private const val MAX_NOTIFICATIONS = 50

    private val _notifications = MutableStateFlow<List<NotificationItem>>(emptyList())
    val notifications: StateFlow<List<NotificationItem>> = _notifications.asStateFlow()

    /** 앱 시작 시 호출해 SharedPreferences에서 알림을 불러옵니다. */
    fun init(context: Context) {
        _notifications.value = loadFromPrefs(context)
    }

    /** 새 알림을 추가합니다. 최대 50개를 넘으면 가장 오래된 알림을 삭제합니다. */
    fun addNotification(context: Context, item: NotificationItem) {
        val current = _notifications.value.toMutableList()
        current.add(0, item) // 최신 항목을 맨 앞에
        if (current.size > MAX_NOTIFICATIONS) {
            current.removeAt(current.lastIndex)
        }
        _notifications.value = current
        saveToPrefs(context, current)
    }

    /** 특정 알림을 읽음 처리합니다. */
    fun markAsRead(context: Context, id: String) {
        val updated = _notifications.value.map { item ->
            if (item.id == id) item.copy(isRead = true) else item
        }
        _notifications.value = updated
        saveToPrefs(context, updated)
    }

    /** 모든 알림을 읽음 처리합니다. */
    fun markAllAsRead(context: Context) {
        val updated = _notifications.value.map { it.copy(isRead = true) }
        _notifications.value = updated
        saveToPrefs(context, updated)
    }

    fun getUnreadCount(): Int = _notifications.value.count { !it.isRead }

    // ── SharedPreferences helpers ──────────────────────────────────────────

    private fun loadFromPrefs(context: Context): List<NotificationItem> {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        val json = prefs.getString(KEY_NOTIFICATIONS, null) ?: return emptyList()
        return try {
            val array = JSONArray(json)
            (0 until array.length()).map { i ->
                val obj = array.getJSONObject(i)
                NotificationItem(
                    id = obj.getString("id"),
                    title = obj.getString("title"),
                    body = obj.getString("body"),
                    timestamp = obj.getLong("timestamp"),
                    isRead = obj.getBoolean("isRead")
                )
            }
        } catch (e: Exception) {
            emptyList()
        }
    }

    private fun saveToPrefs(context: Context, items: List<NotificationItem>) {
        val array = JSONArray()
        items.forEach { item ->
            val obj = JSONObject().apply {
                put("id", item.id)
                put("title", item.title)
                put("body", item.body)
                put("timestamp", item.timestamp)
                put("isRead", item.isRead)
            }
            array.put(obj)
        }
        context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
            .edit()
            .putString(KEY_NOTIFICATIONS, array.toString())
            .apply()
    }
}

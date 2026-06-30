package com.yourssu.setlog_cloning_androidrookiet1.data.model

data class NotificationItem(
    val id: String,
    val title: String,
    val body: String,
    val timestamp: Long,
    val isRead: Boolean = false
)

package com.yourssu.setlog_cloning_androidrookiet1.data.model

import com.google.firebase.Timestamp
import com.google.firebase.firestore.ServerTimestamp

data class Room(
    val roomId: String = "",
    val roomName: String = "",
    val inviteCode: String = "",
    val ownerUid: String = "",
    val memberCount: Int = 4,
    @ServerTimestamp
    val createdAt: Timestamp? = null
)

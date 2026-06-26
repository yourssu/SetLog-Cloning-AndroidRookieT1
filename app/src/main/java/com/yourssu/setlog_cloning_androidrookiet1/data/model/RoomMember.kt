package com.yourssu.setlog_cloning_androidrookiet1.data.model

import com.google.firebase.Timestamp
import com.google.firebase.firestore.ServerTimestamp

data class RoomMember(
    val uid: String = "",
    val nickname: String = "",
    @ServerTimestamp
    val joinedAt: Timestamp? = null
)


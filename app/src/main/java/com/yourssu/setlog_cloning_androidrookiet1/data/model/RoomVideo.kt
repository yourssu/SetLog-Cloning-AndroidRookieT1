package com.yourssu.setlog_cloning_androidrookiet1.data.model

import com.google.firebase.Timestamp
import com.google.firebase.firestore.ServerTimestamp

data class RoomVideo(
    val videoId: String = "",
    val roomId: String = "",
    val uploaderUid: String = "",
    val videoUrl: String = "",
    val date: String = "",
    @ServerTimestamp
    val createdAt: Timestamp? = null
)

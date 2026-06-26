package com.yourssu.setlog_cloning_androidrookiet1.data.model

import com.google.firebase.Timestamp
import com.google.firebase.firestore.ServerTimestamp

data class User(
    val uid: String = "",
    val email: String = "",
    val nickname: String = "",
    @ServerTimestamp
    val createdAt: Timestamp? = null
)

package com.yourssu.setlog_cloning_androidrookiet1.data.repository

import android.net.Uri
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.storage.FirebaseStorage
import com.yourssu.setlog_cloning_androidrookiet1.data.model.Room
import com.yourssu.setlog_cloning_androidrookiet1.data.model.RoomMember
import com.yourssu.setlog_cloning_androidrookiet1.data.model.RoomVideo
import com.yourssu.setlog_cloning_androidrookiet1.data.model.User
import com.yourssu.setlog_cloning_androidrookiet1.data.model.UserRoom
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.tasks.await
import java.util.UUID

class RoomRepository(
    private val db: FirebaseFirestore = FirebaseFirestore.getInstance(),
    private val storage: FirebaseStorage = FirebaseStorage.getInstance()
) {
    suspend fun createRoom(uid: String, roomName: String, memberCount: Int): Result<Unit> = runCatching {
        val user = getUser(uid)
        val roomRef = db.collection(ROOMS).document()
        val inviteCode = createUniqueInviteCode()
        val room = Room(
            roomId = roomRef.id,
            roomName = roomName.trim(),
            inviteCode = inviteCode,
            ownerUid = uid,
            memberCount = memberCount
        )

        val batch = db.batch()
        batch.set(roomRef, room)
        batch.set(
            roomRef.collection(MEMBERS).document(uid),
            RoomMember(uid = uid, nickname = user.nickname)
        )
        batch.set(
            db.collection(USERS).document(uid).collection(ROOMS).document(roomRef.id),
            UserRoom(
                roomId = roomRef.id,
                roomName = room.roomName,
                inviteCode = inviteCode,
                memberCount = memberCount
            )
        )
        batch.commit().await()
    }

    suspend fun joinRoom(uid: String, inviteCode: String): Result<Unit> = runCatching {
        val normalizedCode = inviteCode.trim().uppercase()
        val roomSnapshot = db.collection(ROOMS)
            .whereEqualTo("inviteCode", normalizedCode)
            .limit(1)
            .get()
            .await()
            .documents
            .firstOrNull()
            ?: error("초대 코드에 해당하는 방을 찾지 못했습니다.")

        val room = roomSnapshot.toObject(Room::class.java)
            ?: error("방 정보를 불러오지 못했습니다.")
        val user = getUser(uid)
        val memberRef = roomSnapshot.reference.collection(MEMBERS).document(uid)

        if (memberRef.get().await().exists()) {
            error("이미 참여 중인 방입니다.")
        }
        val currentMemberCount = roomSnapshot.reference.collection(MEMBERS)
            .get()
            .await()
            .size()
        if (currentMemberCount >= room.memberCount) {
            error("방 인원이 가득 찼습니다.")
        }

        val batch = db.batch()
        batch.set(memberRef, RoomMember(uid = uid, nickname = user.nickname))
        batch.set(
            db.collection(USERS).document(uid).collection(ROOMS).document(room.roomId),
            UserRoom(
                roomId = room.roomId,
                roomName = room.roomName,
                inviteCode = room.inviteCode,
                memberCount = room.memberCount
            )
        )
        batch.commit().await()
    }

    fun observeUserRooms(uid: String): Flow<List<UserRoom>> = callbackFlow {
        val registration = db.collection(USERS)
            .document(uid)
            .collection(ROOMS)
            .orderBy("joinedAt")
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    close(error)
                    return@addSnapshotListener
                }

                val rooms = snapshot?.documents
                    ?.mapNotNull { it.toObject(UserRoom::class.java) }
                    .orEmpty()

                trySend(rooms)
            }

        awaitClose { registration.remove() }
    }

    suspend fun uploadRecord(
        roomId: String,
        uid: String,
        caption: String,
        dateHour: String,
        thumbnailBase64: String,
        videoUri: Uri?
    ): Result<Unit> = runCatching {
        val documentId = "${uid}_${dateHour}"
        val videoRef = db.collection(ROOMS).document(roomId).collection("videos").document(documentId)
        val existingRecord = videoRef.get().await().toObject(RoomVideo::class.java)
        val videoUrl = if (videoUri != null) {
            val storageRef = storage.reference.child("rooms/$roomId/videos/$documentId.mp4")
            storageRef.putFile(videoUri).await()
            storageRef.downloadUrl.await().toString()
        } else {
            existingRecord?.videoUrl.orEmpty()
        }

        val record = RoomVideo(
            videoId = documentId,
            roomId = roomId,
            uploaderUid = uid,
            videoUrl = videoUrl,
            thumbnailBase64 = thumbnailBase64,
            caption = caption,
            date = dateHour
        )

        val batch = db.batch()
        batch.set(videoRef, record)
        batch.commit().await()
    }

    fun observeRoomRecords(roomId: String): Flow<List<String>> = callbackFlow {
        val registration = db.collection(ROOMS)
            .document(roomId)
            .collection("videos")
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    close(error)
                    return@addSnapshotListener
                }
                val dates = snapshot?.documents
                    ?.mapNotNull { it.getString("date")?.take(10) }
                    ?.distinct()
                    .orEmpty()
                trySend(dates)
            }
        awaitClose { registration.remove() }
    }

    fun observeMyRoomRecords(roomId: String, uid: String): Flow<List<RoomVideo>> = callbackFlow {
        val registration = db.collection(ROOMS)
            .document(roomId)
            .collection("videos")
            .whereEqualTo("uploaderUid", uid)
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    close(error)
                    return@addSnapshotListener
                }

                val records = snapshot?.documents
                    ?.mapNotNull { it.toObject(RoomVideo::class.java) }
                    .orEmpty()

                trySend(records)
            }
        awaitClose { registration.remove() }
    }

    fun observeRoomMembers(roomId: String): Flow<List<RoomMember>> = callbackFlow {
        val room = db.collection(ROOMS)
            .document(roomId)
            .get()
            .await()
            .toObject(Room::class.java)
        val ownerUid = room?.ownerUid.orEmpty()

        val registration = db.collection(ROOMS)
            .document(roomId)
            .collection(MEMBERS)
            .orderBy("joinedAt")
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    close(error)
                    return@addSnapshotListener
                }

                val members = snapshot?.documents
                    ?.mapNotNull { it.toObject(RoomMember::class.java) }
                    ?.sortedWith(
                        compareByDescending<RoomMember> { it.uid == ownerUid }
                            .thenBy { it.joinedAt?.seconds ?: Long.MAX_VALUE }
                    )
                    .orEmpty()

                trySend(members)
            }

        awaitClose { registration.remove() }
    }

    private suspend fun getUser(uid: String): User {
        return db.collection(USERS)
            .document(uid)
            .get()
            .await()
            .toObject(User::class.java)
            ?: error("사용자 정보를 찾지 못했습니다.")
    }

    private suspend fun createUniqueInviteCode(): String {
        repeat(MAX_INVITE_CODE_ATTEMPTS) {
            val code = UUID.randomUUID().toString()
                .replace("-", "")
                .take(INVITE_CODE_LENGTH)
                .uppercase()
            val exists = db.collection(ROOMS)
                .whereEqualTo("inviteCode", code)
                .limit(1)
                .get()
                .await()
                .isEmpty
                .not()
            if (!exists) return code
        }
        error("초대 코드를 생성하지 못했습니다. 다시 시도해주세요.")
    }

    private companion object {
        const val USERS = "users"
        const val ROOMS = "rooms"
        const val MEMBERS = "members"
        const val INVITE_CODE_LENGTH = 6
        const val MAX_INVITE_CODE_ATTEMPTS = 5
    }
}

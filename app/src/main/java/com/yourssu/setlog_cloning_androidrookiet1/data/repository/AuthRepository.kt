package com.yourssu.setlog_cloning_androidrookiet1.data.repository

import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.auth.GoogleAuthProvider
import com.google.firebase.firestore.FirebaseFirestore
import com.yourssu.setlog_cloning_androidrookiet1.data.model.User
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.tasks.await

class AuthRepository {

    private val auth = FirebaseAuth.getInstance()
    private val db = FirebaseFirestore.getInstance()

    fun getCurrentUser() = auth.currentUser

    fun getCurrentUid(): String? {
        return auth.currentUser?.uid
    }

    fun getCurrentDisplayName(): String {
        val user = auth.currentUser
        return user?.displayName
            ?: user?.email?.substringBefore("@")
            ?: "User"
    }

    fun observeCurrentUser(): Flow<FirebaseUser?> = callbackFlow {
        val listener = FirebaseAuth.AuthStateListener { firebaseAuth ->
            trySend(firebaseAuth.currentUser)
        }
        auth.addAuthStateListener(listener)
        awaitClose { auth.removeAuthStateListener(listener) }
    }

    suspend fun signUp(
        email: String,
        password: String,
        nickname: String
    ): Result<Unit> {
        return try {
            val result = auth.createUserWithEmailAndPassword(email, password).await()

            val uid = result.user?.uid
                ?: return Result.failure(Exception("회원 정보를 가져오지 못했습니다."))

            val user = User(
                uid = uid,
                email = email,
                nickname = nickname
            )

            db.collection("users")
                .document(uid)
                .set(user)
                .await()

            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun login(
        email: String,
        password: String
    ): Result<Unit> {
        return try {
            auth.signInWithEmailAndPassword(email, password).await()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun signInWithGoogle(idToken: String): Result<Unit> {
        return try {
            val credential = GoogleAuthProvider.getCredential(idToken, null)
            val result = auth.signInWithCredential(credential).await()
            val firebaseUser = result.user
                ?: return Result.failure(Exception("Google 사용자 정보를 가져오지 못했습니다."))
            val userRef = db.collection("users").document(firebaseUser.uid)

            if (!userRef.get().await().exists()) {
                userRef.set(
                    User(
                        uid = firebaseUser.uid,
                        email = firebaseUser.email.orEmpty(),
                        nickname = firebaseUser.displayName
                            ?: firebaseUser.email?.substringBefore("@").orEmpty()
                    )
                ).await()
            }

            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    fun logout() {
        auth.signOut()
    }
}

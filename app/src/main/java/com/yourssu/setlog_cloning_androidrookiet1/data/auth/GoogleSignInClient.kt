package com.yourssu.setlog_cloning_androidrookiet1.data.auth

import android.content.Context
import androidx.credentials.CredentialManager
import androidx.credentials.CustomCredential
import androidx.credentials.GetCredentialRequest
import androidx.credentials.exceptions.NoCredentialException
import com.google.android.libraries.identity.googleid.GetSignInWithGoogleOption
import com.google.android.libraries.identity.googleid.GoogleIdTokenCredential
import com.google.android.libraries.identity.googleid.GoogleIdTokenCredential.Companion.TYPE_GOOGLE_ID_TOKEN_SIWG_CREDENTIAL
import com.google.android.libraries.identity.googleid.GoogleIdTokenCredential.Companion.TYPE_GOOGLE_ID_TOKEN_CREDENTIAL
import com.yourssu.setlog_cloning_androidrookiet1.R

class GoogleSignInClient {
    suspend fun getIdToken(context: Context): Result<String> {
        return try {
            val signInWithGoogleOption = GetSignInWithGoogleOption.Builder(
                context.getString(R.string.default_web_client_id)
            ).build()
            val request = GetCredentialRequest.Builder()
                .addCredentialOption(signInWithGoogleOption)
                .build()
            val credential = CredentialManager.create(context)
                .getCredential(context, request)
                .credential

            require(
                credential is CustomCredential &&
                    credential.type in setOf(
                        TYPE_GOOGLE_ID_TOKEN_CREDENTIAL,
                        TYPE_GOOGLE_ID_TOKEN_SIWG_CREDENTIAL
                    )
            ) {
                "Google 인증 정보를 확인하지 못했습니다."
            }
            Result.success(GoogleIdTokenCredential.createFrom(credential.data).idToken)
        } catch (e: NoCredentialException) {
            Result.failure(
                Exception(
                    "사용 가능한 Google 계정이 없습니다. Google Play가 포함된 에뮬레이터에서 Google 계정을 추가한 뒤 다시 시도해주세요.",
                    e
                )
            )
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}

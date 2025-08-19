package com.santoso.moku.data

import android.content.Context
import android.net.Uri
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import javax.inject.Singleton
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.auth.FirebaseAuth
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File
import java.io.FileOutputStream
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException

@Singleton
class ProfileRepository @Inject constructor(
    @ApplicationContext private val context: Context,
    private val firestore: FirebaseFirestore,
    private val auth: FirebaseAuth
) {

    fun currentUid(): String? = auth.currentUser?.uid
    fun currentEmail(): String = auth.currentUser?.email ?: ""

    /** Salin gambar yang dipilih ke internal storage dengan nama unik. */
    suspend fun copyImageToInternal(src: Uri): String = withContext(Dispatchers.IO) {
        val input = context.contentResolver.openInputStream(src)
            ?: throw IllegalStateException("Tidak bisa membuka stream gambar")
        val fileName = "profile_${System.currentTimeMillis()}.jpg"
        val outFile = File(context.filesDir, fileName)
        input.use { i ->
            FileOutputStream(outFile).use { o -> i.copyTo(o) }
        }
        outFile.absolutePath
    }

    /** Ambil data profil dari Firestore (users/{uid}). */
    suspend fun loadProfile(): Map<String, Any?> = suspendCancellableCoroutine { cont ->
        val uid = currentUid() ?: run {
            cont.resume(emptyMap())
            return@suspendCancellableCoroutine
        }
        firestore.collection("users").document(uid).get()
            .addOnSuccessListener { doc ->
                cont.resume(doc.data ?: emptyMap())
            }
            .addOnFailureListener { e -> cont.resumeWithException(e) }
    }

    /** Simpan data profil ke Firestore. */
    suspend fun saveProfile(data: Map<String, Any?>): Unit = suspendCancellableCoroutine { cont ->
        val uid = currentUid() ?: run {
            cont.resume(Unit)
            return@suspendCancellableCoroutine
        }
        firestore.collection("users").document(uid).set(data)
            .addOnSuccessListener { cont.resume(Unit) }
            .addOnFailureListener { e -> cont.resumeWithException(e) }
    }
}

package com.santoso.moku.data.repository

import android.content.Context
import android.net.Uri
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import java.io.File
import java.io.FileOutputStream

class ProfileRepository(
    private val context: Context,
    private val firestore: FirebaseFirestore,
    private val auth: FirebaseAuth
) {

    fun getUserEmail(): String? {
        return auth.currentUser?.email
    }

    fun getProfileData(callback: (Map<String, Any>?) -> Unit) {
        val uid = auth.currentUser?.uid ?: return callback(null)
        firestore.collection("users").document(uid).get()
            .addOnSuccessListener { doc ->
                callback(doc.data)
            }
            .addOnFailureListener {
                callback(null)
            }
    }

    fun saveProfileData(data: Map<String, Any>, callback: (Boolean) -> Unit) {
        val uid = auth.currentUser?.uid ?: return callback(false)
        firestore.collection("users").document(uid).set(data)
            .addOnSuccessListener { callback(true) }
            .addOnFailureListener { callback(false) }
    }

    fun saveImageToLocal(uri: Uri): String {
        val file = File(context.filesDir, "profile_image.jpg")
        context.contentResolver.openInputStream(uri)?.use { input ->
            FileOutputStream(file).use { output ->
                input.copyTo(output)
            }
        }
        return file.absolutePath
    }

    fun getLocalImagePath(): String? {
        val file = File(context.filesDir, "profile_image.jpg")
        return if (file.exists()) file.absolutePath else null
    }
}

package com.example.a6times.data

import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.FirebaseDatabase
import kotlinx.coroutines.tasks.await


class UsersRepository : IUsersRepository{


    private val auth = FirebaseAuth.getInstance()
    val database = FirebaseDatabase.getInstance().getReference("Users")


    override suspend fun saveUser(user: Users): Boolean{

        return try {

            val task = auth.createUserWithEmailAndPassword(user.userEmail, user.userPassword).await()
            val authId = auth.currentUser?.uid
            user.userPassword = user.userPassword.toSHA256()
            database.child(authId.toString()).setValue(user)

            task.user != null

        }catch (e: Exception){
            false
        }

    }

    fun String.toSHA256(): String {
        val bytes = java.security.MessageDigest.getInstance("SHA-256").digest(this.toByteArray())
        return bytes.joinToString("") { "%02x".format(it) }
    }
}
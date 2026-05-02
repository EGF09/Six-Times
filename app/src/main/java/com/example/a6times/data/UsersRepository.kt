package com.example.a6times.data

import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.FirebaseDatabase
import kotlinx.coroutines.tasks.await


class UsersRepository : IUsersRepository{

    private val url = "https://six-times-228d1-default-rtdb.europe-west1.firebasedatabase.app/"
    private val auth = FirebaseAuth.getInstance()
    private val database = FirebaseDatabase.getInstance(url)
                            .reference
                            .child("Users")

    override suspend fun saveUser(user: Users): Boolean{

        return try {
            val task = auth.createUserWithEmailAndPassword(user.userEmail, user.userPassword).await()
            task.user != null
        }catch (e: Exception){
            false
        }

    }
}
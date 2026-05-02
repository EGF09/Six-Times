package com.example.a6times.data

interface IUsersRepository {
    suspend fun saveUser(user: Users): Boolean
}
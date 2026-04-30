package com.example.a6times.data

interface IUsersRepository {
    suspend fun saveUser(user: Users): Result<Unit>
    suspend fun getUser(user: Users): Result<Users?>
}
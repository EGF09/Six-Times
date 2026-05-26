package com.example.a6times.data

/**
 * Uygulama kullanıcılarını temsil eden veri sınıfı.
 * 
 * @property userName Kullanıcının adı.
 * @property userEmail Kullanıcının e-posta adresi.
 * @property userPassword Kullanıcının şifresi.
 */
data class Users (
    var userName: String = "",
    var userEmail: String = "",
    var userPassword: String = ""
)

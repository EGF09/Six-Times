package com.example.a6times.utils

import java.security.MessageDigest

/**
 * SHA256 extension function.
 */
fun String.toSHA256(): String {
    val bytes = MessageDigest.getInstance("SHA-256").digest(this.toByteArray())
    return bytes.joinToString("") { "%02x".format(it) } // Hexadecimal format
}

package com.example.a6times.data

/**
 * Kullanıcı verilerinin yönetimi için gerekli arayüz tanımlaması.
 */
interface IUsersRepository {
    /**
     * Kullanıcıyı sisteme kaydeder.
     * @param user Kaydedilecek kullanıcı nesnesi.
     * @return İşlem başarılı ise true, aksi halde false döner.
     */
    suspend fun saveUser(user: Users): Boolean
}

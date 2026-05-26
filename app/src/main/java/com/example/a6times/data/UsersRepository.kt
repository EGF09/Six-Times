package com.example.a6times.data

import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.FirebaseDatabase
import kotlinx.coroutines.tasks.await
import com.example.a6times.utils.toSHA256

/**
 * Kullanıcı işlemlerini (kayıt, şifreleme vb.) yöneten depo sınıfı.
 * [IUsersRepository] arayüzünü uygular.
 */
class UsersRepository : IUsersRepository{

    private val auth = FirebaseAuth.getInstance()
    val database = FirebaseDatabase.getInstance().getReference("Users")

    //region kullanıcı kaydı işlemi
    /**
     * Yeni bir kullanıcıyı Firebase Auth ve Realtime Database üzerine kaydeder.
     * 
     * @param user Kaydedilecek kullanıcı bilgilerini içeren nesne.
     * @return Kayıt başarılı ise true, hata durumunda false döner.
     */
    override suspend fun saveUser(user: Users): Boolean{
        return try {
            // Firebase Auth ile kullanıcı oluşturma
            val task = auth.createUserWithEmailAndPassword(user.userEmail, user.userPassword).await()
            val authId = auth.currentUser?.uid
            
            // Şifreyi güvenlik için SHA-256 ile özetle
            user.userPassword = user.userPassword.toSHA256()
            
            // Kullanıcı bilgilerini veritabanına yaz
            database.child(authId.toString()).setValue(user)
            task.user != null
        } catch (e: Exception){
            // Hata oluşursa false dön
            false
        }
    }

    /**
     * Verilen UID'ye sahip kullanıcının bilgilerini veritabanından çeker.
     * 
     * @param uid Kullanıcının benzersiz kimliği.
     * @return Kullanıcı nesnesi veya hata durumunda null.
     */
    suspend fun getUser(uid: String): Users? {
        return try {
            val snapshot = database.child(uid).get().await()
            snapshot.getValue(Users::class.java)
        } catch (e: Exception) {
            null
        }
    }
    //endregion
}

package com.exory550.exorygallery.utils.helpers

import java.io.File
import java.security.MessageDigest
import javax.crypto.Cipher
import javax.crypto.CipherInputStream
import javax.crypto.CipherOutputStream
import javax.crypto.spec.IvParameterSpec
import javax.crypto.spec.SecretKeySpec
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class EncryptionHelper @Inject constructor() {
    private val algorithm = "AES/CBC/PKCS5Padding"
    private val iv = ByteArray(16) { it.toByte() }

    private fun keyFromPin(pin: String): SecretKeySpec {
        val digest = MessageDigest.getInstance("SHA-256")
        val keyBytes = digest.digest(pin.toByteArray()).copyOf(16)
        return SecretKeySpec(keyBytes, "AES")
    }

    fun encryptFile(source: File, dest: File, pin: String) {
        val cipher = Cipher.getInstance(algorithm)
        cipher.init(Cipher.ENCRYPT_MODE, keyFromPin(pin), IvParameterSpec(iv))
        source.inputStream().use { input ->
            CipherOutputStream(dest.outputStream(), cipher).use { output ->
                input.copyTo(output)
            }
        }
    }

    fun decryptFile(source: File, dest: File, pin: String) {
        val cipher = Cipher.getInstance(algorithm)
        cipher.init(Cipher.DECRYPT_MODE, keyFromPin(pin), IvParameterSpec(iv))
        CipherInputStream(source.inputStream(), cipher).use { input ->
            dest.outputStream().use { output ->
                input.copyTo(output)
            }
        }
    }

    fun hashPin(pin: String): String {
        val digest = MessageDigest.getInstance("SHA-256")
        return digest.digest(pin.toByteArray()).joinToString("") { "%02x".format(it) }
    }

    fun verifyPin(pin: String, hash: String): Boolean = hashPin(pin) == hash
}

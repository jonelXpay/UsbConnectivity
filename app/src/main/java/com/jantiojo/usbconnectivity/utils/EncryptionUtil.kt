package com.jantiojo.usbconnectivity.utils

import android.util.Base64
import javax.crypto.Cipher
import javax.crypto.KeyGenerator
import javax.crypto.SecretKey
import javax.crypto.spec.IvParameterSpec
import javax.crypto.spec.SecretKeySpec

object EncryptionUtil {
    private const val ALGORITHM = "AES"
    private const val TRANSFORMATION = "AES/CBC/PKCS5Padding"
    private const val KEY_SIZE = 256

    private val secretKey: SecretKey = generateKey()
    private val ivParameterSpec: IvParameterSpec = generateIv()

    private fun generateKey(): SecretKey {
        val keyGenerator = KeyGenerator.getInstance(ALGORITHM)
        keyGenerator.init(KEY_SIZE)
        return keyGenerator.generateKey()
    }

    private fun generateIv(): IvParameterSpec {
        val iv = ByteArray(16)
        java.security.SecureRandom().nextBytes(iv)
        return IvParameterSpec(iv)
    }

    fun encrypt(data: String): String {
        val cipher = Cipher.getInstance(TRANSFORMATION)
        cipher.init(Cipher.ENCRYPT_MODE, secretKey, ivParameterSpec)
        val encryptedBytes = cipher.doFinal(data.toByteArray())
        return Base64.encodeToString(ivParameterSpec.iv + encryptedBytes, Base64.DEFAULT)
    }

    fun decrypt(data: String): String {
        val decodedBytes = Base64.decode(data, Base64.DEFAULT)
        val iv = IvParameterSpec(decodedBytes.take(16).toByteArray())
        val encryptedBytes = decodedBytes.drop(16).toByteArray()

        val cipher = Cipher.getInstance(TRANSFORMATION)
        cipher.init(Cipher.DECRYPT_MODE, secretKey, iv)
        val decryptedBytes = cipher.doFinal(encryptedBytes)
        return String(decryptedBytes)
    }
}

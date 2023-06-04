/*
 * gcUnicorn
 * Copyright (C) 2023  Martin Misiarz
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License version 2
 * as published by the Free Software Foundation.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02110-1301, USA.
 */

package cz.babi.gcunicorn.android.security

import android.os.Build
import android.util.Base64
import cz.babi.gcunicorn.android.security.Security.Companion.CIPHER
import java.nio.ByteBuffer
import java.security.SecureRandom
import javax.crypto.Cipher
import javax.crypto.spec.GCMParameterSpec
import javax.crypto.spec.IvParameterSpec
import javax.crypto.spec.SecretKeySpec

/**
 * Security class which encrypts and decrypts given messages.
 *
 * It uses [CIPHER] cipher with randomly generated IV.
 *
 * @since 1.0.0
 */
class Security {

    companion object {
        private const val CIPHER = "AES/GCM/NoPadding"
    }

    /**
     * Encrypts given plain text with given [Base64] encoded key and returns [Base64] encoded output.
     * @param base64EncodedKey [Base64] encoded key.
     * @param plainMessage Message in plain text to be encrypted.
     * @return [Base64] encoded encrypted message.
     */
    fun encryptWithBase64(base64EncodedKey: String, plainMessage: String): String = Base64.encodeToString(
            encrypt(Base64.decode(base64EncodedKey, Base64.DEFAULT),
                    plainMessage.toByteArray()),
            Base64.DEFAULT
    )

    /**
     * Encrypts given message with given key.
     * @param key Key to be used in encryption.
     * @param message Message to be encrypted.
     * @return Encrypted message.
     */
    fun encrypt(key: ByteArray, message: ByteArray): ByteArray {
        val secureRandom = SecureRandom()
        val iv = ByteArray(12)
        secureRandom.nextBytes(iv)

        val cipher = Cipher.getInstance(CIPHER)
        cipher.init(Cipher.ENCRYPT_MODE, SecretKeySpec(key, "AES"), generateAlgorithmParameterSpecification(iv))

        val encrypted = cipher.doFinal(message)

        val ret = ByteBuffer.allocate(4 + iv.size + encrypted.size).apply {
            putInt(iv.size)
            put(iv)
            put(encrypted)
        }.array()

        key.fill(0)

        return ret
    }

    /**
     * Decrypts given [Base64] encoded message with given [Base64] encoded key.
     * @param base64EncodedKey [Base64] encoded key.
     * @param base64EncodedMessage [Base64] encoded message.
     * @return Decrypted message in plain text.
     */
    fun decryptWithBase64(base64EncodedKey: String, base64EncodedMessage: String) = String(
            decrypt(Base64.decode(base64EncodedKey, Base64.DEFAULT),
                    Base64.decode(base64EncodedMessage, Base64.DEFAULT))
    )

    /**
     * Decrypts given message with given key.
     * @param key Key to be used in decryption.
     * @param message Message to be decrypted.
     * @return Decrypted message.
     */
    fun decrypt(key: ByteArray, message: ByteArray): ByteArray {
        val byteBuffer = ByteBuffer.wrap(message)

        val ivSize = byteBuffer.int
        if (ivSize !in 12..15) {
            throw IllegalArgumentException("bla bla.")
        }

        val iv = ByteArray(ivSize)
        byteBuffer.get(iv)

        val encrypted = ByteArray(byteBuffer.remaining())
        byteBuffer.get(encrypted)

        val cipher = Cipher.getInstance(CIPHER)
        cipher.init(Cipher.DECRYPT_MODE, SecretKeySpec(key, "AES"), generateAlgorithmParameterSpecification(iv))
        return cipher.doFinal(encrypted)
    }

    /**
     * Generates algorithm parameter specification based on the platform.
     * @param iv IV.
     * @return Algorithm parameter specification.
     */
    private fun generateAlgorithmParameterSpecification(iv: ByteArray) = when {
        Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT -> GCMParameterSpec(128, iv)
        else -> IvParameterSpec(iv)
    }
}
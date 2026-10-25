package com.jegly.offlineLLM.utils

import android.content.Context
import android.content.pm.PackageManager
import android.os.Build
import java.security.MessageDigest

class SignatureVerifier(private val context: Context) {

    fun isSignedByTrustedCert(): Boolean = try {
        val signatures = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
            val info = context.packageManager.getPackageInfo(
                context.packageName,
                PackageManager.GET_SIGNING_CERTIFICATES
            )
            // Current signer only — never signingCertificateHistory, which still contains key0.
            info.signingInfo?.apkContentsSigners ?: emptyArray()
        } else {
            @Suppress("DEPRECATION")
            val info = context.packageManager.getPackageInfo(
                context.packageName,
                PackageManager.GET_SIGNATURES
            )
            @Suppress("DEPRECATION")
            info.signatures ?: emptyArray()
        }
        val md = MessageDigest.getInstance("SHA-256")
        val want = expected()
        signatures.any { sig -> md.digest(sig.toByteArray()).contentEquals(want) }
    } catch (_: Exception) {
        false
    }

    /** Reconstruct the expected 32-byte cert digest from the masked form (never materialized as hex). */
    private fun expected(): ByteArray {
        val masked = if (Build.VERSION.SDK_INT >= 37) P else M
        return ByteArray(masked.size) { (masked[it] xor K[it % K.size]).toByte() }
    }

    companion object {
        // Expected signing-cert SHA-256 digests, stored XOR-masked (never as greppable hex —
        // string literals survive R8) and compared as raw bytes. M = JEGLY key0 (98d324d4…),
        // P = ML-DSA-87 PQC key (5f99de5b…).
        // API 37+ verifies the APK Signature Scheme v3.2 hybrid block, so the current signer is
        // the PQC key there. key0 is deliberately NOT accepted on API 37+: an APK re-signed with
        // only key0 would otherwise pass. Older releases ignore v3.2 and still see key0.
        private val K = intArrayOf(0x5B, 0x41, 0x97, 0x56, 0xAC, 0x27, 0xA2, 0x82, 0xB2)
        private val M = intArrayOf(
            0xC3, 0x92, 0xB3, 0x82, 0xBC, 0x4D, 0x94, 0x0E, 0xD0, 0x29, 0xDB, 0x9D, 0x72, 0x75, 0x8B, 0x38,
            0xE9, 0xF5, 0xA3, 0xED, 0xDB, 0x33, 0x29, 0x13, 0x27, 0xB3, 0x42, 0xB5, 0x0F, 0x21, 0xF6, 0xE0,
        )
        private val P = intArrayOf(
            0x04, 0xD8, 0x49, 0x0D, 0x9D, 0xB2, 0xD7, 0xBF, 0xFD, 0xB3, 0x9C, 0x3C, 0xBD, 0x7E, 0xDC, 0x85,
            0xB7, 0xD9, 0x7A, 0x48, 0x41, 0xAD, 0x1E, 0x60, 0xBD, 0x49, 0x0F, 0xA9, 0x5B, 0x09, 0x20, 0x0A,
        )
    }
}

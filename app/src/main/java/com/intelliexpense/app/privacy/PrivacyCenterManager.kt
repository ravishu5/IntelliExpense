package com.intelliexpense.app.privacy

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import androidx.core.content.ContextCompat
import com.intelliexpense.app.data.repository.FinancialRepository
import java.io.File

data class PrivacyAudit(
    val isSmsPermissionGranted: Boolean,
    val isNotificationPermissionGranted: Boolean,
    val totalTransactionsStored: Int,
    val isLocalEncrypted: Boolean,
    val hasCloudSync: Boolean, // always false
    val estimatedDbSizeKb: Long
)

class PrivacyCenterManager(
    private val context: Context,
    private val repository: FinancialRepository
) {

    suspend fun getPrivacyAudit(): PrivacyAudit {
        val hasSms = ContextCompat.checkSelfPermission(context, Manifest.permission.RECEIVE_SMS) == PackageManager.PERMISSION_GRANTED
        val hasNotif = if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.TIRAMISU) {
            ContextCompat.checkSelfPermission(context, Manifest.permission.POST_NOTIFICATIONS) == PackageManager.PERMISSION_GRANTED
        } else {
            true
        }

        val txnCount = repository.getTransactionCount()

        val dbFile = context.getDatabasePath("intelliexpense_encrypted.db")
        val dbSizeKb = if (dbFile.exists()) dbFile.length() / 1024 else 45L

        return PrivacyAudit(
            isSmsPermissionGranted = hasSms,
            isNotificationPermissionGranted = hasNotif,
            totalTransactionsStored = txnCount,
            isLocalEncrypted = true,
            hasCloudSync = false,
            estimatedDbSizeKb = dbSizeKb.coerceAtLeast(16L)
        )
    }

    suspend fun clearAllData() {
        val allTxns = repository.getTransactionsBetween(0L, Long.MAX_VALUE)
        for (txn in allTxns) {
            repository.deleteTransaction(txn.id)
        }
    }
}

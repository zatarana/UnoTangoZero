package com.unotangozero.app.backup

import android.content.Context
import android.os.Environment
import com.unotangozero.app.data.db.AppDatabase
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import javax.inject.Inject
import javax.inject.Singleton

data class BackupFile(
    val name: String,
    val path: String,
    val sizeBytes: Long,
    val lastModified: Long
)

@Singleton
class BackupManager @Inject constructor(
    @ApplicationContext private val context: Context,
    private val database: AppDatabase
) {
    private val databaseName = "uno_tango_zero.db"

    suspend fun exportBackup(): Result<BackupFile> = withContext(Dispatchers.IO) {
        runCatching {
            database.openHelper.writableDatabase.query("PRAGMA wal_checkpoint(FULL)").close()

            val source = context.getDatabasePath(databaseName)
            require(source.exists()) { "Banco de dados ainda não existe." }

            val backupDir = getBackupDir()
            if (!backupDir.exists()) backupDir.mkdirs()

            val timestamp = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(Date())
            val destination = File(backupDir, "uno_tango_zero_backup_$timestamp.db")
            source.copyTo(destination, overwrite = true)

            BackupFile(
                name = destination.name,
                path = destination.absolutePath,
                sizeBytes = destination.length(),
                lastModified = destination.lastModified()
            )
        }
    }

    suspend fun listBackups(): List<BackupFile> = withContext(Dispatchers.IO) {
        getBackupDir()
            .listFiles { file -> file.isFile && file.extension == "db" }
            ?.sortedByDescending { it.lastModified() }
            ?.map {
                BackupFile(
                    name = it.name,
                    path = it.absolutePath,
                    sizeBytes = it.length(),
                    lastModified = it.lastModified()
                )
            }
            ?: emptyList()
    }

    suspend fun restoreLatestBackup(): Result<BackupFile> = withContext(Dispatchers.IO) {
        runCatching {
            val latest = listBackups().firstOrNull() ?: error("Nenhum backup encontrado.")
            val backupFile = File(latest.path)
            require(backupFile.exists()) { "Arquivo de backup não encontrado." }

            database.close()

            val destination = context.getDatabasePath(databaseName)
            val parent = destination.parentFile
            if (parent != null && !parent.exists()) parent.mkdirs()

            backupFile.copyTo(destination, overwrite = true)
            File(destination.absolutePath + "-wal").delete()
            File(destination.absolutePath + "-shm").delete()

            latest
        }
    }

    private fun getBackupDir(): File {
        val documentsDir = context.getExternalFilesDir(Environment.DIRECTORY_DOCUMENTS)
            ?: context.filesDir
        return File(documentsDir, "backups")
    }
}

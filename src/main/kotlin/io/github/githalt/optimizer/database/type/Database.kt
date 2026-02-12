package io.github.githalt.optimizer.database.type

import io.github.githalt.optimizer.model.CleanupLog

interface Database {
    fun saveCleanupRecord(record: CleanupLog)
    fun getCleanupHistory(limit: Int = 100): List<CleanupLog>
    fun clearHistory() // Nueva función
    fun connect()
    fun disconnect()
}
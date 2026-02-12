package io.github.githalt.optimizer.model

import com.google.gson.Gson
import java.text.SimpleDateFormat
import java.util.*

data class CleanupLog(
    val id: UUID = UUID.randomUUID(),
    val timestamp: Long,
    val entitiesRemoved: Int,
    val memoryFreed: Long
) {
    fun toJson(): String = Gson().toJson(this)

    fun getFormattedDate(): String {
        val dateFormat = SimpleDateFormat("MM/dd/yyyy HH:mm")
        return dateFormat.format(Date(timestamp))
    }

    fun getMemoryFreedMB(): Double {
        return memoryFreed / 1024.0 / 1024.0
    }
}
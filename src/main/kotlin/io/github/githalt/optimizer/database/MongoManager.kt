package io.github.githalt.optimizer.database

import com.mongodb.client.MongoClient
import com.mongodb.client.MongoClients
import com.mongodb.client.MongoCollection
import com.mongodb.client.MongoDatabase
import io.github.githalt.optimizer.config.ConfigManager
import io.github.githalt.optimizer.model.CleanupLog
import org.bson.Document
import java.util.UUID

class MongoManager(private val config: ConfigManager) {

    private lateinit var client: MongoClient
    lateinit var database: MongoDatabase
    lateinit var cleanupCollection: MongoCollection<Document>

    fun connect() {
        client = MongoClients.create("mongodb://${config.mongoHost}:${config.mongoPort}")
        database = client.getDatabase(config.mongoDatabase)
        cleanupCollection = database.getCollection("cleanup_logs")
    }

    fun disconnect() {
        client.close()
    }

    fun insertLog(cleanupLog: CleanupLog) {
        val doc = Document()
            .append("_id", cleanupLog.id.toString())
            .append("timestamp", cleanupLog.timestamp)
            .append("entities_removed", cleanupLog.entitiesRemoved)
            .append("memory_freed", cleanupLog.memoryFreed)

        cleanupCollection.insertOne(doc)
    }

    fun getCleanupHistory(limit: Int = 100): List<CleanupLog> {
        val records = mutableListOf<CleanupLog>()

        try {
            cleanupCollection
                .find()
                .sort(Document("timestamp", -1)) // Ordenar por fecha descendente (más reciente primero)
                .limit(limit)
                .forEach { doc ->
                    try {
                        // Manejo robusto de tipos para evitar ClassCastException
                        val timestamp = when (val ts = doc.get("timestamp")) {
                            is Long -> ts
                            is Int -> ts.toLong()
                            is String -> ts.toLongOrNull() ?: System.currentTimeMillis()
                            else -> System.currentTimeMillis()
                        }

                        val entitiesRemoved = when (val er = doc.get("entities_removed")) {
                            is Int -> er
                            is Long -> er.toInt()
                            is String -> er.toIntOrNull() ?: 0
                            else -> 0
                        }

                        val memoryFreed = when (val mf = doc.get("memory_freed")) {
                            is Long -> mf
                            is Int -> mf.toLong()
                            is Double -> mf.toLong()
                            is String -> mf.toLongOrNull() ?: 0L
                            else -> 0L
                        }

                        records.add(
                            CleanupLog(
                                timestamp = timestamp,
                                entitiesRemoved = entitiesRemoved,
                                memoryFreed = memoryFreed
                            )
                        )
                    } catch (e: Exception) {
                        // Log pero continúa con los demás registros
                        println("Error parsing cleanup record: ${e.message}")
                        e.printStackTrace()
                    }
                }
        } catch (e: Exception) {
            println("Error fetching cleanup history: ${e.message}")
            e.printStackTrace()
        }

        return records
    }

    fun clearHistory() {
        cleanupCollection.deleteMany(Document())
    }

    fun getLastCleanup(): CleanupLog? {
        return try {
            val doc = cleanupCollection
                .find()
                .sort(Document("timestamp", -1))
                .limit(1)
                .first() ?: return null

            val id = UUID.fromString(doc.getString("_id"))

            val timestamp = when (val ts = doc.get("timestamp")) {
                is Long -> ts
                is Int -> ts.toLong()
                is String -> ts.toLongOrNull() ?: return null
                else -> return null
            }

            val entitiesRemoved = when (val er = doc.get("entities_removed")) {
                is Int -> er
                is Long -> er.toInt()
                is String -> er.toIntOrNull() ?: 0
                else -> 0
            }

            val memoryFreed = when (val mf = doc.get("memory_freed")) {
                is Long -> mf
                is Int -> mf.toLong()
                is Double -> mf.toLong()
                is String -> mf.toLongOrNull() ?: 0L
                else -> 0L
            }

            CleanupLog(
                id = id,
                timestamp = timestamp,
                entitiesRemoved = entitiesRemoved,
                memoryFreed = memoryFreed
            )
        } catch (e: Exception) {
            println("Error fetching last cleanup: ${e.message}")
            null
        }
    }
}
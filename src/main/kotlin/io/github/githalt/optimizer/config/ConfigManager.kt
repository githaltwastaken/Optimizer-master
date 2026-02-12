package io.github.githalt.optimizer.config

import com.google.gson.Gson
import com.google.gson.GsonBuilder
import com.google.gson.JsonObject
import com.google.gson.JsonParser
import org.bukkit.plugin.java.JavaPlugin
import java.io.File

class ConfigManager(private val plugin: JavaPlugin) {

    private val file = File(plugin.dataFolder, "config.json")
    private val gson: Gson = GsonBuilder().setPrettyPrinting().create()
    private var json: JsonObject

    init {
        if (!file.exists()) {
            plugin.dataFolder.mkdirs()
            file.createNewFile()
            file.writeText(getDefaultConfig())
        }
        json = JsonParser.parseString(file.readText()).asJsonObject
    }

    val mongoHost: String
        get() = json["mongodb"].asJsonObject["host"].asString

    val mongoPort: Int
        get() = json["mongodb"].asJsonObject["port"].asInt

    val mongoDatabase: String
        get() = json["mongodb"].asJsonObject["database"].asString

    var cleanerEnabled: Boolean
        get() = json["cleaner"].asJsonObject["enabled"].asBoolean
        set(value) {
            json["cleaner"].asJsonObject.addProperty("enabled", value)
            save()
        }

    var intervalSeconds: Long
        get() = json["cleaner"].asJsonObject["intervalSeconds"].asLong
        set(value) {
            json["cleaner"].asJsonObject.addProperty("intervalSeconds", value)
            save()
        }

    var broadcastBeforeSeconds: Long
        get() = json["cleaner"].asJsonObject["broadcastBeforeSeconds"].asLong
        set(value) {
            json["cleaner"].asJsonObject.addProperty("broadcastBeforeSeconds", value)
            save()
        }

    var enableGC: Boolean
        get() = json["cleaner"].asJsonObject["enableGarbageCollector"].asBoolean
        set(value) {
            json["cleaner"].asJsonObject.addProperty("enableGarbageCollector", value)
            save()
        }

    val entityThreshold: Int
        get() = json["chunk"].asJsonObject["entityThreshold"].asInt

    private fun save() {
        file.writeText(gson.toJson(json))
    }

    fun reload() {
        json = JsonParser.parseString(file.readText()).asJsonObject
    }

    private fun getDefaultConfig(): String {
        return """
        {
          "mongodb": {
            "host": "localhost",
            "port": 27017,
            "database": "optimizer"
          },
          "cleaner": {
            "enabled": true,
            "intervalSeconds": 1800,
            "broadcastBeforeSeconds": 10,
            "enableGarbageCollector": true
          },
          "chunk": {
            "entityThreshold": 50
          }
        }
        """.trimIndent()
    }
}
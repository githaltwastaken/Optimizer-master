package io.github.githalt.optimizer

import io.github.githalt.optimizer.cleaner.CleanerService
import io.github.githalt.optimizer.command.OptimizerCommand
import io.github.githalt.optimizer.config.ConfigManager
import io.github.githalt.optimizer.database.MongoManager
import io.github.githalt.optimizer.monitor.ChunkMonitor
import org.bukkit.plugin.java.JavaPlugin

class OptimizerPlugin : JavaPlugin() {


    lateinit var configManager: ConfigManager
    lateinit var mongoManager: MongoManager
    lateinit var cleanerService: CleanerService
    lateinit var chunkMonitor: ChunkMonitor

    override fun onEnable() {
        instance = this

        saveResource("config.json", false)

        configManager = ConfigManager(this)
        mongoManager = MongoManager(configManager)
        mongoManager.connect()

        cleanerService = CleanerService(this)
        chunkMonitor = ChunkMonitor(this)

        getCommand("optimizer")?.executor = OptimizerCommand(this)

        cleanerService.start()
        chunkMonitor.start()

        logger.info("Optimizer enabled.")
    }

    override fun onDisable() {
        cleanerService.stop()
        mongoManager.disconnect()
        logger.info("Optimizer disabled.")
    }

    companion object {
        lateinit var instance: OptimizerPlugin
    }
}

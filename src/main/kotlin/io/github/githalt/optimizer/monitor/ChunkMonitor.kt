package io.github.githalt.optimizer.monitor

import io.github.githalt.optimizer.OptimizerPlugin
import org.bukkit.Bukkit
import org.bukkit.scheduler.BukkitRunnable

class ChunkMonitor(private val plugin: OptimizerPlugin) {

    fun start() {
        object : BukkitRunnable() {
            override fun run() {
                Bukkit.getWorlds().forEach { world ->
                    world.loadedChunks.forEach { chunk ->
                        if (chunk.entities.size > plugin.configManager.entityThreshold) {
                            plugin.logger.warning("Chunk ${chunk.x},${chunk.z} in ${world.name} is problematic (${chunk.entities.size} entities)")
                        }
                    }
                }
            }
        }.runTaskTimer(plugin, 200L, 200L)
    }
}

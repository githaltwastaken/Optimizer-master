package io.github.githalt.optimizer.cleaner

import io.github.githalt.optimizer.OptimizerPlugin
import io.github.githalt.optimizer.model.CleanupLog
import org.bson.Document
import org.bukkit.Bukkit
import org.bukkit.World
import org.bukkit.entity.Entity
import org.bukkit.entity.Player
import org.bukkit.scheduler.BukkitTask
import java.util.concurrent.atomic.AtomicBoolean

class CleanerService(private val plugin: OptimizerPlugin) {

    private var task: BukkitTask? = null
    private var lastCleanup: Long? = null
    private var nextCleanup: Long? = null

    private var lastEntitiesRemoved: Int = 0
    private var lastMemoryFreedMB: Double = 0.0

    private val running = AtomicBoolean(false)

    /*
     * START SCHEDULER
     */
    fun start() {
        if (!plugin.configManager.cleanerEnabled) return
        schedule()
    }

    fun reloadConfig() {
        stop()
        plugin.configManager.reload()
        start()
    }

    private fun schedule() {
        val intervalTicks = plugin.configManager.intervalSeconds * 20L
        val intervalMillis = plugin.configManager.intervalSeconds * 1000L

        nextCleanup = System.currentTimeMillis() + intervalMillis

        task = Bukkit.getScheduler().runTaskTimer(plugin, Runnable {

            broadcastWarning()

            Bukkit.getScheduler().runTaskLater(plugin, Runnable {
                executeCleanup(false)
            }, plugin.configManager.broadcastBeforeSeconds * 20L)

        }, intervalTicks, intervalTicks)
    }

    /*
     * FORCE CLEANUP (for command)
     */
    fun forceCleanup() {
        executeCleanup(true)
        reschedule()
    }

    private fun reschedule() {
        task?.cancel()
        schedule()
    }

    /*
     * MAIN CLEANUP LOGIC
     */
    private fun executeCleanup(forced: Boolean) {

        if (running.get()) return
        running.set(true)

        try {

            val beforeEntities = countEntities()
            val beforeMemory = getUsedMemoryMB()

            var removed = 0

            for (world: World in Bukkit.getWorlds()) {
                for (entity: Entity in world.entities) {
                    if (entity !is Player) {
                        entity.remove()
                        removed++
                    }
                }
            }

            if (plugin.configManager.enableGC) {
                System.gc()
            }

            val afterMemory = getUsedMemoryMB()

            lastCleanup = System.currentTimeMillis()

            val intervalMillis = plugin.configManager.intervalSeconds * 1000L
            nextCleanup = System.currentTimeMillis() + intervalMillis

            lastEntitiesRemoved = removed
            lastMemoryFreedMB = beforeMemory - afterMemory

            logToDatabase(removed, lastMemoryFreedMB)

            Bukkit.broadcastMessage(
                "§6§lOptimizer §8» §aCleanup completed! " +
                        "§7Removed: §f$removed §7| Freed: §f${format(lastMemoryFreedMB)} MB"
            )

        } catch (ex: Exception) {

            Bukkit.broadcastMessage(
                "§6§lOptimizer §8» §cCleanup failed: ${ex.message}"
            )

        } finally {
            running.set(false)
        }
    }

    /*
     * BROADCAST WARNING
     */
    private fun broadcastWarning() {
        Bukkit.broadcastMessage(
            "§6§lOptimizer §8» §eCleanup in §f${plugin.configManager.broadcastBeforeSeconds} §eseconds!"
        )
    }

    /*
     * ASYNC DATABASE LOG
     */
    private fun logToDatabase(removed: Int, memoryFreed: Double) {

        if (lastCleanup == null) {
            return
        }

        Bukkit.getScheduler().runTaskAsynchronously(plugin, Runnable {
            try {
                val log = CleanupLog(
                    timestamp = lastCleanup!!,
                    entitiesRemoved = removed,
                    memoryFreed = memoryFreed.toLong()
                )

                plugin.mongoManager.insertLog(log)

            } catch (_: Exception) {
            }
        })
    }

    /*
     * ENTITY COUNT (1.8 SAFE)
     */
    private fun countEntities(): Int {
        var total = 0
        for (world in Bukkit.getWorlds()) {
            total += world.entities.size
        }
        return total
    }

    /*
     * MEMORY USAGE
     */
    private fun getUsedMemoryMB(): Double {
        val runtime = Runtime.getRuntime()
        val used = runtime.totalMemory() - runtime.freeMemory()
        return used / 1024.0 / 1024.0
    }

    private fun format(value: Double): String {
        return String.format("%.2f", value)
    }

    /*
     * STOP SCHEDULER
     */
    fun stop() {
        task?.cancel()
    }

    fun getCleanupHistory(limit: Int = 100): List<CleanupLog> {
        return plugin.mongoManager.getCleanupHistory(limit)
    }

    /*
     * GETTERS FOR COMMANDS
     */

    fun getLastCleanup(): Long? = lastCleanup

    fun getNextCleanup(): Long? = nextCleanup

    fun getInterval(): Long = plugin.configManager.intervalSeconds

    fun getLastEntitiesRemoved(): Int = lastEntitiesRemoved

    fun getLastMemoryFreedMB(): Double = lastMemoryFreedMB

    fun isRunning(): Boolean = running.get()
}
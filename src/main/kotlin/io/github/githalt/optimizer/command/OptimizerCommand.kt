package io.github.githalt.optimizer.command

import io.github.githalt.optimizer.OptimizerPlugin
import org.bukkit.Bukkit
import org.bukkit.command.Command
import org.bukkit.command.CommandExecutor
import org.bukkit.command.CommandSender
import java.text.DecimalFormat
import java.text.SimpleDateFormat
import java.util.*

class OptimizerCommand(private val plugin: OptimizerPlugin) : CommandExecutor {

    private val df = DecimalFormat("#.##")
    private val dateFormat = SimpleDateFormat("MM/dd/yyyy HH:mm")

    override fun onCommand(
        sender: CommandSender,
        command: Command,
        label: String,
        args: Array<out String>
    ): Boolean {

        if (!sender.hasPermission("optimizer.admin")) {
            sender.sendMessage("§c§lOptimizer §8» §cYou do not have permission.")
            return true
        }

        if (args.isEmpty()) {
            sendHelp(sender)
            return true
        }

        when (args[0].toLowerCase()) {

            "stats" -> sendStats(sender)

            "memory" -> sendMemory(sender)

            "entities" -> sendEntities(sender)

            "tps" -> sendTPS(sender)

            "last" -> sendLastCleanup(sender)

            "next" -> sendNextCleanup(sender)

            "force" -> forceCleanup(sender)

            "history" -> {
                val page = args.getOrNull(1)?.toIntOrNull() ?: 1
                sendHistory(sender, page)
            }

            "interval" -> {
                if (args.size < 3) {
                    sender.sendMessage("§c§lOptimizer §8» §cUsage: /optimizer interval <time> <unit>")
                    sender.sendMessage("§7Units: §fseconds, minutes, hours")
                    sender.sendMessage("§7Example: §f/optimizer interval 30 minutes")
                    return true
                }
                setInterval(sender, args[1], args[2])
            }

            "reload" -> {
                plugin.cleanerService.reloadConfig()
                sender.sendMessage("§6§lOptimizer §8» §aConfiguration reloaded successfully.")
            }

            else -> sendHelp(sender)
        }

        return true
    }

    private fun sendHelp(sender: CommandSender) {
        sender.sendMessage("§8§m------------------------------------------------")
        sender.sendMessage("§6§lOptimizer §7- Performance Manager")
        sender.sendMessage("§8§m------------------------------------------------")
        sender.sendMessage("§e/optimizer stats §7» General overview")
        sender.sendMessage("§e/optimizer memory §7» Memory details")
        sender.sendMessage("§e/optimizer entities §7» Entity count")
        sender.sendMessage("§e/optimizer tps §7» Server TPS")
        sender.sendMessage("§e/optimizer last §7» Last cleanup")
        sender.sendMessage("§e/optimizer next §7» Next cleanup")
        sender.sendMessage("§e/optimizer force §7» Force cleanup")
        sender.sendMessage("§e/optimizer history [page] §7» Cleanup history")
        sender.sendMessage("§e/optimizer interval <time> <unit> §7» Set interval")
        sender.sendMessage("§e/optimizer reload §7» Reload config")
        sender.sendMessage("§8§m------------------------------------------------")
    }

    private fun sendStats(sender: CommandSender) {
        val memoryUsed = getUsedMemoryMB()
        val entities = getTotalEntities()
        val tps = getTPS()

        sender.sendMessage("§8§m------------------------------------------------")
        sender.sendMessage("§6§lOptimizer §7- Statistics Overview")
        sender.sendMessage("§8§m------------------------------------------------")
        sender.sendMessage("§7TPS: §f${df.format(tps)}")
        sender.sendMessage("§7Memory Used: §f${df.format(memoryUsed)} MB")
        sender.sendMessage("§7Total Entities: §f$entities")
        sender.sendMessage("§7Cleanup Interval: §f${getIntervalFormatted()}")
        sender.sendMessage("§8§m------------------------------------------------")
    }

    private fun sendMemory(sender: CommandSender) {
        val runtime = Runtime.getRuntime()
        val used = (runtime.totalMemory() - runtime.freeMemory()) / 1024.0 / 1024.0
        val max = runtime.maxMemory() / 1024.0 / 1024.0

        sender.sendMessage("§8§m------------------------------------------------")
        sender.sendMessage("§6§lMemory Information")
        sender.sendMessage("§8§m------------------------------------------------")
        sender.sendMessage("§7Used: §f${df.format(used)} MB")
        sender.sendMessage("§7Max: §f${df.format(max)} MB")
        sender.sendMessage("§7Usage: §f${df.format((used / max) * 100)}%")
        sender.sendMessage("§8§m------------------------------------------------")
    }

    private fun sendEntities(sender: CommandSender) {
        val total = getTotalEntities()

        sender.sendMessage("§8§m------------------------------------------------")
        sender.sendMessage("§6§lEntity Monitor")
        sender.sendMessage("§8§m------------------------------------------------")
        sender.sendMessage("§7Total Entities: §f$total")
        sender.sendMessage("§8§m------------------------------------------------")
    }

    private fun sendTPS(sender: CommandSender) {
        val tps = getTPS()

        sender.sendMessage("§8§m------------------------------------------------")
        sender.sendMessage("§6§lTPS Monitor")
        sender.sendMessage("§8§m------------------------------------------------")
        sender.sendMessage("§7Current TPS: §f${df.format(tps)}")
        sender.sendMessage("§8§m------------------------------------------------")
    }

    private fun sendLastCleanup(sender: CommandSender) {
        val last: Long? = plugin.cleanerService.getLastCleanup()

        sender.sendMessage("§8§m------------------------------------------------")
        sender.sendMessage("§6§lCleanup History")
        sender.sendMessage("§8§m------------------------------------------------")
        sender.sendMessage("§7Last Cleanup: §f${formatDate(last)}")
        sender.sendMessage("§8§m------------------------------------------------")
    }

    private fun sendNextCleanup(sender: CommandSender) {
        val next = plugin.cleanerService.getNextCleanup()

        sender.sendMessage("§8§m------------------------------------------------")
        sender.sendMessage("§6§lScheduled Cleanup")
        sender.sendMessage("§8§m------------------------------------------------")
        sender.sendMessage("§7Next Cleanup: §f${formatDate(next)}")
        sender.sendMessage("§8§m------------------------------------------------")
    }

    private fun forceCleanup(sender: CommandSender) {
        sender.sendMessage("§6§lOptimizer §8» §eForcing cleanup...")
        plugin.cleanerService.forceCleanup()
        sender.sendMessage("§6§lOptimizer §8» §aCleanup executed.")
    }

    private fun sendHistory(sender: CommandSender, page: Int) {
        val history = plugin.cleanerService.getCleanupHistory()

        if (history.isEmpty()) {
            sender.sendMessage("§c§lOptimizer §8» §cNo cleanup history found.")
            return
        }

        val itemsPerPage = 10
        val totalPages = (history.size + itemsPerPage - 1) / itemsPerPage
        val validPage = page.coerceIn(1, totalPages)

        val startIndex = (validPage - 1) * itemsPerPage
        val endIndex = (startIndex + itemsPerPage).coerceAtMost(history.size)

        sender.sendMessage("§8§m------------------------------------------------")
        sender.sendMessage("§6§lCleanup History §7(Page $validPage/$totalPages)")
        sender.sendMessage("§8§m------------------------------------------------")

        for (i in startIndex until endIndex) {
            val record = history[i]
            sender.sendMessage("§7${i + 1}. §f${formatDate(record.timestamp)} §7- §a${record.entitiesRemoved} entities §7- §e${df.format(record.memoryFreed)} MB freed")
        }

        sender.sendMessage("§8§m------------------------------------------------")

        if (validPage < totalPages) {
            sender.sendMessage("§7Use §f/optimizer history ${validPage + 1} §7for next page")
        }
    }

    private fun setInterval(sender: CommandSender, timeStr: String, unitStr: String) {
        val time = timeStr.toLongOrNull()

        if (time == null || time <= 0) {
            sender.sendMessage("§c§lOptimizer §8» §cInvalid time value. Must be a positive number.")
            return
        }

        val seconds = when (unitStr.toLowerCase()) {
            "second", "seconds", "s", "sec" -> time
            "minute", "minutes", "m", "min" -> time * 60
            "hour", "hours", "h", "hr" -> time * 3600
            else -> {
                sender.sendMessage("§c§lOptimizer §8» §cInvalid unit. Use: seconds, minutes, or hours")
                return
            }
        }

        plugin.configManager.intervalSeconds = seconds

        sender.sendMessage("§6§lOptimizer §8» §aCleanup interval set to §f${getIntervalFormatted()}")
        sender.sendMessage("§6§lOptimizer §8» §7Next cleanup: §f${formatDate(plugin.cleanerService.getNextCleanup())}")
    }

    private fun getIntervalFormatted(): String {
        val seconds = plugin.cleanerService.getInterval()

        return when {
            seconds % 3600 == 0L -> "${seconds / 3600} hour(s)"
            seconds % 60 == 0L -> "${seconds / 60} minute(s)"
            else -> "$seconds second(s)"
        }
    }

    private fun formatDate(millis: Long?): String {
        return if (millis != null) {
            dateFormat.format(Date(millis))
        } else {
            "Never"
        }
    }

    private fun getTotalEntities(): Int {
        var total = 0
        for (world in Bukkit.getWorlds()) {
            total += world.entities.size
        }
        return total
    }

    private fun getUsedMemoryMB(): Double {
        val runtime = Runtime.getRuntime()
        return (runtime.totalMemory() - runtime.freeMemory()) / 1024.0 / 1024.0
    }

    private fun getTPS(): Double {
        return try {
            val server = Bukkit.getServer()
            val method = server.javaClass.getMethod("getTPS")
            val tps = method.invoke(server) as DoubleArray
            tps[0]
        } catch (ex: Exception) {
            20.0
        }
    }
}
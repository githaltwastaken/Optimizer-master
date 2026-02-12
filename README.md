🚀 Optimizer - Performance Manager
Keep your Minecraft server running smoothly with automated cleanup and real-time monitoring.

Optimizer is a lightweight performance plugin that automatically removes unnecessary entities, manages memory, and provides detailed analytics. Perfect for servers of any size.

✨ Features
Automatic Entity Cleanup - Removes dropped items, arrows, and expired experience orbs
Memory Optimization - Triggers garbage collection and manages chunk loading
Performance Monitoring - Real-time TPS, memory usage, and entity tracking
Configurable Intervals - Set cleanup frequency in seconds, minutes, or hours
Database Support - MongoDB and JSON storage for cleanup history
Detailed Analytics - Track up to 100 past cleanup operations with timestamps
Easy Management - Simple commands for monitoring and configuration
📋 Commands
All commands require optimizer.admin permission.

Command Description
/optimizer Show help menu
/optimizer stats View TPS, memory usage, and entity count
/optimizer memory Detailed memory information
/optimizer tps Current server TPS
/optimizer entities Total entity count
/optimizer last Last cleanup details
/optimizer next When next cleanup is scheduled
/optimizer force Execute immediate cleanup
/optimizer history [page] Browse cleanup history (10 per page)
/optimizer interval <time> <unit> Set cleanup interval (s/m/h)
/optimizer reload Reload configuration
/optimizer clearhistory Clear cleanup history
Examples:

/optimizer interval 30 minutes - Clean every 30 minutes
/optimizer interval 2 hours - Clean every 2 hours
/optimizer history 2 - View page 2 of history
🎮 How It Works
Scans all loaded worlds for removable entities
Removes items, arrows, and old experience orbs
Optimizes memory and unloads unused chunks
Logs every operation with UUID, timestamp, and stats
Schedules next automatic cleanup
📦 Requirements
Minecraft: 1.8.x - 1.20.x (Spigot/Paper/Purpur)
Java: 11+
Database: MongoDB 3.6+ or JSON file storage
🔐 Permissions
optimizer.admin - Access to all commands (default: op)
🌐 Use Cases
Survival Servers - Keep spawn areas clean
Networks - Manage entities across multiple worlds
Minigames - Force cleanup between rounds
Performance - Maintain consistent TPS during peak hours
⭐ Why Optimizer?
✅ Multi-version support (1.8 - 1.20+)
✅ Lightweight and efficient
✅ MongoDB & JSON storage
✅ Real-time performance tracking
✅ Configurable intervals
✅ Detailed cleanup history

Download now and boost your server performance!

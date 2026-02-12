package io.github.githalt.optimizer.util

import java.time.Duration
import java.time.Instant

object TimeUtil {

    fun formatDuration(from: Instant): String {
        val duration = Duration.between(from, Instant.now())
        return "${duration.toMinutes()} minutes ago"
    }
}

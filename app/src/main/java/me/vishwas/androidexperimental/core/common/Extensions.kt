package me.vishwas.androidexperimental.core.common

import java.text.SimpleDateFormat
import java.util.Locale

fun String.toRelativeTime(): String {
    return try {
        val sdf = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'", Locale.getDefault())
        val date = sdf.parse(this) ?: return this
        val diff = System.currentTimeMillis() - date.time
        when {
            diff < 60_000L -> "Just now"
            diff < 3_600_000L -> "${diff / 60_000}m ago"
            diff < 86_400_000L -> "${diff / 3_600_000}h ago"
            diff < 604_800_000L -> "${diff / 86_400_000}d ago"
            else -> SimpleDateFormat("MMM dd", Locale.getDefault()).format(date)
        }
    } catch (e: Exception) {
        this
    }
}

fun String.toFormattedDate(): String {
    return try {
        val input = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'", Locale.getDefault())
        val output = SimpleDateFormat("MMM dd, yyyy • HH:mm", Locale.getDefault())
        val date = input.parse(this) ?: return this
        output.format(date)
    } catch (e: Exception) {
        this
    }
}

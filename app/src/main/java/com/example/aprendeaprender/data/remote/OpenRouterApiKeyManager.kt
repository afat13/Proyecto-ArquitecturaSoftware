package com.example.aprendeaprender.data.remote

import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class NoAvailableOpenRouterKeyException(message: String) : Exception(message)

class OpenRouterApiKeyManager(
    rawKeys: List<String>,
    private val maxRequestsPerMinute: Int = 18,
    private val maxRequestsPerDay: Int = 48,
    private val nowProvider: () -> Long = { System.currentTimeMillis() }
) {
    private data class KeyUsage(
        val key: String,
        var minuteWindowStart: Long = 0L,
        var requestsThisMinute: Int = 0,
        var day: String = "",
        var requestsToday: Int = 0,
        var blockedDay: String? = null
    )

    private val keys = rawKeys
        .map { it.trim() }
        .filter { it.isNotBlank() }
        .distinct()
        .map { KeyUsage(key = it) }

    private var currentIndex = 0

    fun hasKeys(): Boolean = keys.isNotEmpty()

    fun totalKeys(): Int = keys.size.coerceAtLeast(1)

    @Synchronized
    fun nextAvailableKey(): String {
        if (keys.isEmpty()) {
            throw NoAvailableOpenRouterKeyException("No hay API keys de OpenRouter configuradas.")
        }

        repeat(keys.size) { offset ->
            val index = (currentIndex + offset) % keys.size
            val usage = keys[index]
            resetWindowsIfNeeded(usage)

            val availableByDay = usage.blockedDay != currentDay() && usage.requestsToday < maxRequestsPerDay
            val availableByMinute = usage.requestsThisMinute < maxRequestsPerMinute

            if (availableByDay && availableByMinute) {
                currentIndex = index
                return usage.key
            }
        }

        val allBlockedByDay = keys.all { usage ->
            resetWindowsIfNeeded(usage)
            usage.blockedDay == currentDay() || usage.requestsToday >= maxRequestsPerDay
        }

        if (allBlockedByDay) {
            throw NoAvailableOpenRouterKeyException("Todas las API keys alcanzaron el límite diario.")
        }

        throw NoAvailableOpenRouterKeyException("Todas las API keys alcanzaron el límite por minuto.")
    }

    @Synchronized
    fun markRequestStarted(key: String) {
        val usage = keys.firstOrNull { it.key == key } ?: return
        resetWindowsIfNeeded(usage)
        usage.requestsThisMinute += 1
        usage.requestsToday += 1
    }

    @Synchronized
    fun markMinuteLimitReached(key: String) {
        val usage = keys.firstOrNull { it.key == key } ?: return
        resetWindowsIfNeeded(usage)
        usage.requestsThisMinute = maxRequestsPerMinute
        currentIndex = (keys.indexOf(usage) + 1).floorMod(keys.size)
    }

    @Synchronized
    fun markDailyLimitReached(key: String) {
        val usage = keys.firstOrNull { it.key == key } ?: return
        resetWindowsIfNeeded(usage)
        usage.requestsToday = maxRequestsPerDay
        usage.blockedDay = currentDay()
        currentIndex = (keys.indexOf(usage) + 1).floorMod(keys.size)
    }

    private fun resetWindowsIfNeeded(usage: KeyUsage) {
        val now = nowProvider()
        val currentDay = currentDay()

        if (usage.day != currentDay) {
            usage.day = currentDay
            usage.requestsToday = 0
            usage.blockedDay = null
        }

        if (usage.minuteWindowStart == 0L || now - usage.minuteWindowStart >= 60_000L) {
            usage.minuteWindowStart = now
            usage.requestsThisMinute = 0
        }
    }

    private fun currentDay(): String {
        return SimpleDateFormat("yyyy-MM-dd", Locale.US).format(Date(nowProvider()))
    }

    private fun Int.floorMod(other: Int): Int {
        if (other == 0) return 0
        return ((this % other) + other) % other
    }
}

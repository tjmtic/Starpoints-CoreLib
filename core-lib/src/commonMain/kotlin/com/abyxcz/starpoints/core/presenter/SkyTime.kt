package com.abyxcz.starpoints.core.presenter

private const val MAX_OFFSET_MINUTES = 720
private const val MINUTES_TO_MILLIS = 60_000L

data class SkyTime(val offsetMinutes: Int = 0)

fun SkyTime.withOffset(minutes: Int): SkyTime =
    copy(offsetMinutes = minutes.coerceIn(-MAX_OFFSET_MINUTES, MAX_OFFSET_MINUTES))

fun SkyTime.epochMillis(nowMillis: Long): Long =
    nowMillis + offsetMinutes.toLong() * MINUTES_TO_MILLIS

val SkyTime.isLive: Boolean
    get() = offsetMinutes == 0

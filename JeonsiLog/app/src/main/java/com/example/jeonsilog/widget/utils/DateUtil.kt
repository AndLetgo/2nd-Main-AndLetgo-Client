package com.example.jeonsilog.widget.utils

import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import java.time.temporal.ChronoUnit

class DateUtil {
    fun formatElapsedTime(dateTimeString: String): String {
        val formatter = DateTimeFormatter.ISO_DATE_TIME
        val pastDateTime = LocalDateTime.parse(dateTimeString, formatter)
        val currentDateTime = LocalDateTime.now()

        val elapsedSecond = ChronoUnit.SECONDS.between(pastDateTime, currentDateTime)
        val elapsedMinutes = ChronoUnit.MINUTES.between(pastDateTime, currentDateTime)
        val elapsedHours = ChronoUnit.HOURS.between(pastDateTime, currentDateTime)
        val elapsedDays = ChronoUnit.DAYS.between(pastDateTime, currentDateTime)

        return when {
            elapsedSecond < 60 -> "지금"
            elapsedMinutes < 60 -> "${elapsedMinutes}분 전"
            elapsedHours < 24 -> "${elapsedHours}시간 전"
            elapsedHours < 48 -> "어제"
            elapsedDays < 7 -> "${elapsedDays}일 전"
            else -> LocalDateTime.parse(dateTimeString).format(DateTimeFormatter.ofPattern("MM.dd"))
        }
    }
}
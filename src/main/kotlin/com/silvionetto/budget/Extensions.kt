package com.silvionetto.budget

import java.time.LocalDate
import java.time.ZoneId
import java.time.format.DateTimeFormatter

fun String.toSlug() = toLowerCase()
        .replace("\n", " ")
        .replace("[^a-z\\d\\s]".toRegex(), " ")
        .split(" ")
        .joinToString("-")
        .replace("-+".toRegex(), "-")

fun String.toDate() = java.util.Date.from(LocalDate.parse(this, DateTimeFormatter.ofPattern("yyyyMMdd")).atStartOfDay()
        .atZone(ZoneId.systemDefault())
        .toInstant())


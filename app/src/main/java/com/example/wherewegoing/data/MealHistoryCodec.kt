package com.example.wherewegoing.data

import com.example.wherewegoing.model.MealRecord

fun readMealHistory(value: String): List<MealRecord> = value
    .split(";")
    .mapNotNull { entry ->
        val parts = entry.split("|")
        val recordedAt = parts.getOrNull(0)?.toLongOrNull()
        val dealId = parts.getOrNull(1)
        val result = parts.getOrNull(2)
        if (recordedAt != null && !dealId.isNullOrBlank() && result in setOf("worked", "did_not_work")) {
            MealRecord(dealId, result.orEmpty(), recordedAt)
        } else null
    }
    .sortedByDescending { it.recordedAt }

fun writeMealHistory(records: List<MealRecord>): String = records.joinToString(";") {
    "${it.recordedAt}|${it.dealId}|${it.result}"
}

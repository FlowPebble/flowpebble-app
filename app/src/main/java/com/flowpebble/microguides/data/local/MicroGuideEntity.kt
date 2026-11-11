package com.flowpebble.microguides.data.local

import androidx.room.Entity
import androidx.room.PrimaryKey
import org.json.JSONArray

@Entity(tableName = "micro_guides")
data class MicroGuideEntity(
    @PrimaryKey val id: String,
    val title: String,
    val category: String,
    val emoji: String,
    val summary: String,
    val stepsJson: String,
    val checkedStepsMask: Int = 0,
    val timerSeconds: Int = 0,
    val isSaved: Boolean = false,
    val isCompleted: Boolean = false,
    val isBonusVault: Boolean = false,
    val isCustom: Boolean = false,
    val createdAt: Long = System.currentTimeMillis()
) {
    fun getStepsList(): List<String> {
        return try {
            val array = JSONArray(stepsJson)
            buildList(array.length()) {
                for (i in 0 until array.length()) {
                    add(array.optString(i, ""))
                }
            }.filter { it.isNotBlank() }
        } catch (e: Exception) {
            emptyList()
        }
    }

    fun isStepChecked(index: Int): Boolean {
        return (checkedStepsMask and (1 shl index)) != 0
    }

    fun withToggledStep(index: Int): MicroGuideEntity {
        val updatedMask = checkedStepsMask xor (1 shl index)
        return copy(checkedStepsMask = updatedMask)
    }

    fun checkedStepCount(totalSteps: Int): Int {
        var count = 0
        for (i in 0 until totalSteps) {
            if (isStepChecked(i)) count++
        }
        return count
    }

    companion object {
        fun encodeSteps(steps: List<String>): String {
            val array = JSONArray()
            steps.forEach { array.put(it.trim()) }
            return array.toString()
        }
    }
}

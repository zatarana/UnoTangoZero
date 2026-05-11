package com.unotangozero.app.domain.models

import java.time.LocalDateTime
import java.util.UUID

data class FocusProfile(
    val id: String = UUID.randomUUID().toString(),
    val name: String,
    val focusMinutes: Int,
    val shortBreakMinutes: Int,
    val longBreakMinutes: Int,
    val cyclesUntilLongBreak: Int,
    val totalCycles: Int
)

data class FocusSessionLog(
    val id: String = UUID.randomUUID().toString(),
    val taskName: String,
    val profileName: String,
    val focusedMinutes: Int,
    val completedCycles: Int,
    val projectId: String? = null,
    val projectTitle: String? = null,
    val createdAt: LocalDateTime = LocalDateTime.now()
)

data class FocusProjectSummary(
    val projectId: String?,
    val projectTitle: String,
    val totalMinutes: Int,
    val sessionCount: Int
)

enum class FocusPhase(val displayName: String) {
    IDLE("Pronto"),
    FOCUS("Foco"),
    SHORT_BREAK("Pausa curta"),
    LONG_BREAK("Pausa longa"),
    FINISHED("Finalizado")
}

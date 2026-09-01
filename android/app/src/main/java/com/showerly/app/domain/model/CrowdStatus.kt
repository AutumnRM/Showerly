package com.showerly.app.domain.model

data class CrowdStatus(
    val total: Int,
    val capacity: Int,
    val occupancyRatio: Float,
    val timeText: String?,
    val statusLabel: String,
    val isDemo: Boolean = false
)

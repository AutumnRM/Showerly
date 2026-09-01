package com.showerly.app.data.remote.dto

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

// 字段按常见校方接口形态预置，抓包后再按实际响应调整 @SerialName 映射。
@Serializable
data class CrowdApiResponse(
    @SerialName("code") val code: Int? = null,
    @SerialName("msg") val msg: String? = null,
    @SerialName("message") val message: String? = null,
    @SerialName("data") val data: CrowdData? = null,
    @SerialName("total") val total: Int? = null,
    @SerialName("capacity") val capacity: Int? = null,
    @SerialName("time") val time: String? = null,
    @SerialName("timestamp") val timestamp: Long? = null,
    @SerialName("status") val status: String? = null
)

@Serializable
data class CrowdData(
    val total: Int? = null,
    val count: Int? = null,
    val current: Int? = null,
    val used: Int? = null,
    val capacity: Int? = null,
    val totalBays: Int? = null,
    val time: String? = null,
    val timestamp: Long? = null,
    val status: String? = null,
    val bays: List<BayDto>? = null
)

@Serializable
data class BayDto(
    val id: Int? = null,
    val index: Int? = null,
    @SerialName("is_occupied") val isOccupied: Boolean? = null,
    val occupied: Boolean? = null,
    val free: Boolean? = null,
    val status: String? = null
)

package com.showerly.app.data.remote.dto

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

// 校方澡堂接口真实结构（2026-09-01 抓包确认，云达人 App）。
// GET https://cloudman.jinghaojian.net/bathroom?campusId=4&uid=...
// Authorization: Bearer <JWT>
@Serializable
data class CrowdApiResponse(
    @SerialName("code") val code: String? = null,
    @SerialName("msg") val msg: String? = null,
    @SerialName("data") val data: List<BathroomDto>? = null
)

@Serializable
data class BathroomDto(
    val id: Int? = null,
    val name: String? = null,
    val sex: Int? = null,
    @SerialName("maxLoad") val maxLoad: Int? = null,
    @SerialName("useCount") val useCount: Int? = null,
    @SerialName("bookingDeviceCnt") val bookingDeviceCnt: Int? = null,
    @SerialName("availableBookingDeviceCnt") val availableBookingDeviceCnt: Int? = null
) {
    // 当前空位数（容量 - 使用数），下限 0。
    val vacant: Int?
        get() = maxLoad?.let { (it - (useCount ?: 0)).coerceAtLeast(0) }
}

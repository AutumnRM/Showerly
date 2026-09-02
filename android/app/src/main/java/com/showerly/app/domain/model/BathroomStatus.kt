package com.showerly.app.domain.model

/** 性别筛选项。sex: 0=男, 1=女。 */
enum class Gender(val label: String, val sex: Int) {
    MALE("男", 0),
    FEMALE("女", 1)
}

/** 校区。campusId: 校方接口参数。长安=4 可用；太白=3 已知映射，但接口异常，暂置灰等待更新。 */
enum class Campus(val label: String, val campusId: String, val supported: Boolean) {
    CHANGAN("长安校区", "4", true),
    TAIBAI("太白校区", "3", false)
}

/** 深色模式偏好。 */
enum class DarkModePref(val label: String) {
    SYSTEM("跟随系统"),
    LIGHT("浅色"),
    DARK("深色")
}

/** 单个浴室的实时状态（由校方 BathroomDto 映射而来）。 */
data class BathroomStatus(
    val id: Int,
    val name: String,
    val sex: Int,
    val maxLoad: Int,
    val useCount: Int,
    val vacant: Int,
    val capacity: Int,
    val occupancyRatio: Float,
    val statusLabel: String
) {
    val isFull: Boolean get() = occupancyRatio >= 0.9f
}

package com.showerly.app.domain.model

/** 主题色调色盘。argb 为含 Alpha 的 ARGB 色值（Long），供 Compose Color(argb) 使用。 */
enum class ThemePreset(val label: String, val argb: Long) {
    TEAL("湖水蓝绿", 0xFF00657A),
    BLUE("清爽蓝", 0xFF1E6FD9),
    SKY("天蓝", 0xFF0097E6),
    INDIGO("靛蓝", 0xFF3F51B5),
    PURPLE("神秘紫", 0xFF6A4FA3),
    VIOLET("藕紫", 0xFF8E24AA),
    ROSE("玫红", 0xFFC2185B),
    RED("活力红", 0xFFE53935),
    ORANGE_RED("朱橘", 0xFFF4511E),
    ORANGE("暖橙", 0xFFFB8C00),
    AMBER("金橙", 0xFFFFB300),
    YELLOW("柠檬黄", 0xFFFDD835),
    LIME("青柠", 0xFF7CB342),
    GREEN("生机绿", 0xFF43A047),
    MINT("薄荷青", 0xFF00BFA5),
    CYAN("深海青", 0xFF00838F),
    SLATE("石青", 0xFF607D8B),
    BROWN("暖棕", 0xFF795548)
}
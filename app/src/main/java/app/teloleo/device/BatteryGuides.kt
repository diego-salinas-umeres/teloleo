package app.teloleo.device

import java.util.Locale

enum class BatteryGuide {
    Xiaomi,
    Samsung,
    Transsion,
    Huawei,
    OppoFamily,
    Generic,
}

object BatteryGuides {
    fun forManufacturer(manufacturer: String): BatteryGuide {
        val name = manufacturer.trim().lowercase(Locale.ROOT)
        return when {
            name.contains("xiaomi") || name.contains("redmi") || name.contains("poco") -> BatteryGuide.Xiaomi
            name.contains("samsung") -> BatteryGuide.Samsung
            name.contains("transsion") ||
                name.contains("infinix") ||
                name.contains("tecno") ||
                name.contains("itel") -> BatteryGuide.Transsion
            name.contains("huawei") || name.contains("honor") -> BatteryGuide.Huawei
            name.contains("oppo") ||
                name.contains("realme") ||
                name.contains("vivo") ||
                name.contains("oneplus") -> BatteryGuide.OppoFamily
            else -> BatteryGuide.Generic
        }
    }
}

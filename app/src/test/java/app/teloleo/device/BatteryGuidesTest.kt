package app.teloleo.device

import org.junit.Assert.assertEquals
import org.junit.Test

class BatteryGuidesTest {
    @Test
    fun manufacturerMapsToTheGuideThatPreventsBackgroundDeath() {
        assertEquals(BatteryGuide.Xiaomi, BatteryGuides.forManufacturer("Xiaomi"))
        assertEquals(BatteryGuide.Xiaomi, BatteryGuides.forManufacturer("POCO"))
        assertEquals(BatteryGuide.Samsung, BatteryGuides.forManufacturer("samsung"))
        assertEquals(BatteryGuide.Transsion, BatteryGuides.forManufacturer("Infinix"))
        assertEquals(BatteryGuide.Huawei, BatteryGuides.forManufacturer("HONOR"))
        assertEquals(BatteryGuide.OppoFamily, BatteryGuides.forManufacturer("realme"))
        assertEquals(BatteryGuide.Generic, BatteryGuides.forManufacturer("Google"))
    }
}

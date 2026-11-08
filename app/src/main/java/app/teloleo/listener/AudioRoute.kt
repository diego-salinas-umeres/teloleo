package app.teloleo.listener

import android.media.AudioDeviceInfo

object AudioRoute {
    private val headphoneTypes = intArrayOf(
        AudioDeviceInfo.TYPE_WIRED_HEADPHONES,
        AudioDeviceInfo.TYPE_WIRED_HEADSET,
        AudioDeviceInfo.TYPE_BLUETOOTH_A2DP,
        AudioDeviceInfo.TYPE_BLUETOOTH_SCO,
        AudioDeviceInfo.TYPE_USB_HEADSET,
        AudioDeviceInfo.TYPE_HEARING_AID,
        AudioDeviceInfo.TYPE_BLE_HEADSET,
    )

    fun isHeadphone(type: Int): Boolean = headphoneTypes.contains(type)

    fun connected(outputTypes: Collection<Int>): Boolean = outputTypes.any(::isHeadphone)
}

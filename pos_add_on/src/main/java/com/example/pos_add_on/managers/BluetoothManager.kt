package com.example.pos_add_on.managers

import android.bluetooth.BluetoothAdapter


class BluetoothManager {

    val isBluetoothSupported: Boolean
        get() = BluetoothAdapter.getDefaultAdapter() != null

    /**
     * Whether current Android device Bluetooth is enabled.
     *
     * @return true：Bluetooth is enabled false：Bluetooth not enabled
     */
    val isBluetoothEnabled: Boolean
        get() {
            val bluetoothAdapter = BluetoothAdapter
                    .getDefaultAdapter()

            return bluetoothAdapter?.isEnabled ?: false

        }

    /**
     * Force to turn on Bluetooth on Android device.
     *
     * @return true：force to turn on Bluetooth　success　
     * false：force to turn on Bluetooth failure
     */
    fun turnOnBluetooth(): Boolean {
        val bluetoothAdapter = BluetoothAdapter
                .getDefaultAdapter()

        return bluetoothAdapter?.enable() ?: false

    }
}
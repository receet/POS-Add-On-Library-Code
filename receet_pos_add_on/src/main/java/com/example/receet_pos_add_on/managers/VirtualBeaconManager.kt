package com.example.receet_pos_add_on.managers

import android.app.Activity
import android.bluetooth.*
import android.bluetooth.BluetoothManager
import android.bluetooth.le.AdvertiseCallback
import android.bluetooth.le.AdvertiseData
import android.bluetooth.le.AdvertiseSettings
import android.content.Context
import android.os.ParcelUuid
import android.util.Log
import com.example.receet_pos_add_on.GlobalKeys.Companion.UserDefaultsKeys.BEACON_ID_KEY_USER_DEFAULTS
import com.example.receet_pos_add_on.SharedPreference
import com.example.receet_pos_add_on.SingletonHolder
import com.example.receet_pos_add_on.interfaces.VirtualBeaconActionsInterface
import java.util.*

    class VirtualBeaconManager  private constructor(private val context: Context)  {

        private val ServiceUUID: UUID = UUID.fromString("0000fffe-0000-1000-8000-00805f9b34fb")
        private val characteristicUUID: UUID = UUID.fromString("B1428E10-9894-4D5E-BB39-9136E124CA10")
        val sharedPreference: SharedPreference =
            SharedPreference(context)
        private lateinit var bluetoothManager: BluetoothManager
        private var bluetoothGattServer: BluetoothGattServer? = null

        private lateinit var virtualBeaconActionsInterface : VirtualBeaconActionsInterface

        companion object : SingletonHolder<VirtualBeaconManager, Context>(::VirtualBeaconManager)

        fun getVirtualBeaconActionsInterface(virtualBeaconActionsInterface : VirtualBeaconActionsInterface){
        this.virtualBeaconActionsInterface   = virtualBeaconActionsInterface
        }

        fun startTransmitting(){
            if( !BluetoothAdapter.getDefaultAdapter().isMultipleAdvertisementSupported ) {
                Log.d("lolo","check bluetooth")
            }
            startBLEAdvertising()
            startGATTServer()
        }

        fun stopTransmitting() {
            val advertiser = BluetoothAdapter.getDefaultAdapter().bluetoothLeAdvertiser
            advertiser.stopAdvertising(advertisingCallback)
            bluetoothGattServer?.clearServices()
            bluetoothGattServer?.close()
        }


        private val callback = object : BluetoothGattServerCallback() {
            override fun onCharacteristicReadRequest(device: BluetoothDevice?, requestId: Int, offset: Int, characteristic: BluetoothGattCharacteristic?){
                super.onCharacteristicReadRequest(device, requestId, offset, characteristic)
                val beaconId = sharedPreference.getValueString(BEACON_ID_KEY_USER_DEFAULTS)
                 Log.d("virtual beacon id", beaconId)
                bluetoothGattServer?.sendResponse(device,requestId, BluetoothGatt.GATT_SUCCESS,0,beaconId?.toByteArray())
            }
        }
        private fun startGATTServer(){
            bluetoothManager = context.getSystemService(Context.BLUETOOTH_SERVICE) as BluetoothManager
            bluetoothGattServer = bluetoothManager.openGattServer(context, callback)

            val service = BluetoothGattService(ServiceUUID,
                BluetoothGattService.SERVICE_TYPE_PRIMARY)


            val currentTime = BluetoothGattCharacteristic(characteristicUUID,
                //Read-only characteristic, supports notifications
                BluetoothGattCharacteristic.PROPERTY_READ or BluetoothGattCharacteristic.PROPERTY_NOTIFY,
                BluetoothGattCharacteristic.PERMISSION_READ)


            service.addCharacteristic(currentTime)


            bluetoothGattServer?.addService(service)
                ?: Log.w("gatt", "Unable to create GATT server")

        }

        private val advertisingCallback = object : AdvertiseCallback() {
            override fun onStartFailure(errorCode: Int) {
                Log.e("BLE", "Advertising onStartFailure: $errorCode")
                super.onStartFailure(errorCode)
            }
        }

        private fun startBLEAdvertising(){
            val advertiser = BluetoothAdapter.getDefaultAdapter().bluetoothLeAdvertiser

            val settings = AdvertiseSettings.Builder()
                .setAdvertiseMode(AdvertiseSettings.ADVERTISE_MODE_LOW_LATENCY)
                .setTxPowerLevel(AdvertiseSettings.ADVERTISE_TX_POWER_HIGH)
                .setConnectable(true)
                .build()



            val pUuid = ParcelUuid(ServiceUUID)



            val data = AdvertiseData.Builder()
                .setIncludeDeviceName(true)
                .setIncludeTxPowerLevel(false)
                .addServiceUuid(pUuid)
                .build()


            advertiser.startAdvertising(settings, data, advertisingCallback)
            (context as Activity).runOnUiThread {
                virtualBeaconActionsInterface.virtualBeaconManagerDidStartAdvertising()
            }

        }
    }

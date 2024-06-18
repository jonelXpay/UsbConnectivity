package com.jantiojo.usbconnectivity.bluetooth

import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothDevice
import android.bluetooth.BluetoothGatt
import android.bluetooth.BluetoothGattCharacteristic
import android.bluetooth.BluetoothGattServer
import android.bluetooth.BluetoothGattServerCallback
import android.bluetooth.BluetoothGattService
import android.bluetooth.BluetoothManager
import android.bluetooth.BluetoothProfile
import android.bluetooth.le.AdvertiseCallback
import android.bluetooth.le.AdvertiseData
import android.bluetooth.le.AdvertiseSettings
import android.content.Context
import java.util.UUID

class BluetoothConnectionManager(
    private val context: Context,
    private val bluetoothManager: BluetoothManager,
    private val serviceUUID: UUID,
    private val writeCharacteristicUUID: UUID,
    private val readCharacteristicUUID: UUID,
    private val listener: BluetoothConnectionListener? = null
) {

    private val bluetoothAdapter: BluetoothAdapter? by lazy { bluetoothManager.adapter }
    var gattServer: BluetoothGattServer? = null
    lateinit var jsonCharacteristic: BluetoothGattCharacteristic
    var connectedDevice: BluetoothDevice? = null

    fun initialize(): Boolean {
        val adapter = bluetoothAdapter ?: return false

        if (!adapter.isEnabled) {
            listener?.onError("Bluetooth is not enabled or not available")
            return false
        }

        return true
    }

    fun startServer() {
        gattServer = bluetoothManager.openGattServer(context, gattServerCallback)
        if (gattServer == null){
            listener?.onError("Unable to create GATT server")
            return
        }
        val service =
            BluetoothGattService(serviceUUID, BluetoothGattService.SERVICE_TYPE_PRIMARY)
        val writeCharacteristic = BluetoothGattCharacteristic(
            writeCharacteristicUUID,
            BluetoothGattCharacteristic.PROPERTY_WRITE,
            BluetoothGattCharacteristic.PERMISSION_WRITE
        )

        val readCharacteristic = BluetoothGattCharacteristic(
            readCharacteristicUUID,
            BluetoothGattCharacteristic.PROPERTY_READ,
            BluetoothGattCharacteristic.PERMISSION_READ
        )

        jsonCharacteristic = readCharacteristic
        service.addCharacteristic(writeCharacteristic)
        service.addCharacteristic(jsonCharacteristic)

        gattServer?.addService(service)
        bluetoothAdapter?.bluetoothLeAdvertiser?.startAdvertising(
            advertisingSettings(),
            advertiseData(),
            advertiseCallback
        )
    }


    private fun advertisingSettings(): AdvertiseSettings {
        return AdvertiseSettings.Builder()
            .setAdvertiseMode(AdvertiseSettings.ADVERTISE_MODE_LOW_LATENCY)
            .setTxPowerLevel(AdvertiseSettings.ADVERTISE_TX_POWER_HIGH)
            .setConnectable(true)
            .build()
    }

    private fun advertiseData(): AdvertiseData {
        return AdvertiseData.Builder()
            .setIncludeDeviceName(true)
            .build()
    }


    val gattServerCallback = object : BluetoothGattServerCallback() {
        override fun onConnectionStateChange(device: BluetoothDevice?, status: Int, newState: Int) {
            super.onConnectionStateChange(device, status, newState)
            if (device == null) {
                listener?.onError("No connected device to send notification")
                return
            }
            if (newState == BluetoothProfile.STATE_CONNECTED) {
                connectedDevice = device
                listener?.onDeviceConnected(device.address)
            } else if (newState == BluetoothProfile.STATE_DISCONNECTED) {
                connectedDevice = null
                listener?.onDeviceDisconnected(device.address)
            }
        }

        override fun onCharacteristicReadRequest(
            device: BluetoothDevice?,
            requestId: Int,
            offset: Int,
            characteristic: BluetoothGattCharacteristic?
        ) {
            super.onCharacteristicReadRequest(device, requestId, offset, characteristic)

            if (device == null || characteristic == null) {
                listener?.onError("No connected Device or Characteristic")
                return
            }

            if (characteristic.uuid == readCharacteristicUUID) {
                gattServer?.sendResponse(
                    device,
                    requestId,
                    BluetoothGatt.GATT_SUCCESS,
                    0,
                    characteristic.value
                )
            }
        }

        override fun onCharacteristicWriteRequest(
            device: BluetoothDevice?,
            requestId: Int,
            characteristic: BluetoothGattCharacteristic?,
            preparedWrite: Boolean,
            responseNeeded: Boolean,
            offset: Int,
            value: ByteArray?
        ) {
            super.onCharacteristicWriteRequest(
                device,
                requestId,
                characteristic,
                preparedWrite,
                responseNeeded,
                offset,
                value
            )
            if (device == null || characteristic == null || value == null) {
                listener?.onError("No connected Device, Characteristic, or value")
                return
            }

            if (characteristic.uuid == writeCharacteristicUUID) {
                characteristic.value = value
                val data = value.toString(Charsets.UTF_8)
                listener?.onDataReceived(data)
                if (responseNeeded) {
                    gattServer?.sendResponse(device, requestId, BluetoothGatt.GATT_SUCCESS, 0, null)
                }
            }
        }

        override fun onNotificationSent(device: BluetoothDevice?, status: Int) {
            super.onNotificationSent(device, status)
            if (device == null) {
                listener?.onError("No connected device to send notification")
                return
            }
            if (status == BluetoothGatt.GATT_SUCCESS) {
                listener?.onDataSent(device.address)
            } else {
                listener?.onError("Failed to send notification to device: ${device.address}, status: $status")
            }

        }
    }


    private val advertiseCallback = object : AdvertiseCallback() {
        override fun onStartSuccess(settingsInEffect: AdvertiseSettings) {
            super.onStartSuccess(settingsInEffect)
            listener?.onStartSuccess()
        }

        override fun onStartFailure(errorCode: Int) {
            super.onStartFailure(errorCode)
            listener?.onError("Advertising failed with error code: $errorCode")
        }
    }

    fun sendJsonData(jsonString: String) {
        when {
            connectedDevice == null && gattServer == null -> {
                listener?.onError("No connected device to send notification")
            }

            jsonString.isEmpty() -> listener?.onError("JSON Data is invalid")

            else -> {
                jsonCharacteristic.value = jsonString.toByteArray(Charsets.UTF_8)
                gattServer?.notifyCharacteristicChanged(
                    connectedDevice!!,
                    jsonCharacteristic,
                    false
                )
            }
        }

    }

}
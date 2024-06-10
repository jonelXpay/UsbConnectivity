package com.jantiojo.usbconnectivity.bluetooth

import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothDevice
import android.bluetooth.BluetoothGatt
import android.bluetooth.BluetoothGattCharacteristic
import android.bluetooth.BluetoothGattDescriptor
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

class BluetoothDataSenderManager(
    private val context: Context,
    private val listener: BluetoothDataSenderListener? = null
) {

    private var bluetoothManager: BluetoothManager? = null
    private var bluetoothAdapter: BluetoothAdapter? = null
    private var gattServer: BluetoothGattServer? = null
    private var jsonCharacteristic: BluetoothGattCharacteristic? = null
    private var connectedDevice: BluetoothDevice? = null

    fun initialize(): Boolean {
        bluetoothManager = context.getSystemService(Context.BLUETOOTH_SERVICE) as BluetoothManager
        bluetoothAdapter = bluetoothManager?.adapter

        if (bluetoothAdapter == null || !bluetoothAdapter!!.isEnabled) {
            listener?.onError("Bluetooth is not enabled or not available")
            return false
        }

        return true
    }

    fun startServer(serviceUUID: UUID, charUUID: UUID) {
        gattServer = bluetoothManager?.openGattServer(context, gattServerCallback)

        if (gattServer == null) {
            listener?.onError("Unable to create GATT server")
            return
        }

        val service = BluetoothGattService(serviceUUID, BluetoothGattService.SERVICE_TYPE_PRIMARY)
        jsonCharacteristic = BluetoothGattCharacteristic(
            charUUID,
            BluetoothGattCharacteristic.PROPERTY_READ or BluetoothGattCharacteristic.PROPERTY_WRITE or BluetoothGattCharacteristic.PROPERTY_NOTIFY,
            BluetoothGattCharacteristic.PERMISSION_READ or BluetoothGattCharacteristic.PERMISSION_WRITE
        )

        val descriptor = BluetoothGattDescriptor(
            UUID.fromString("00002902-0000-1000-8000-00805f9b34fb"),
            BluetoothGattDescriptor.PERMISSION_READ or BluetoothGattDescriptor.PERMISSION_WRITE
        )
        descriptor.value = BluetoothGattDescriptor.ENABLE_NOTIFICATION_VALUE
        jsonCharacteristic?.addDescriptor(descriptor)

        service.addCharacteristic(jsonCharacteristic)
        gattServer?.addService(service)

        val settings = AdvertiseSettings.Builder()
            .setAdvertiseMode(AdvertiseSettings.ADVERTISE_MODE_LOW_LATENCY)
            .setTxPowerLevel(AdvertiseSettings.ADVERTISE_TX_POWER_HIGH)
            .setConnectable(true)
            .build()

        val data = AdvertiseData.Builder()
            .setIncludeDeviceName(true)
            .build()

        bluetoothAdapter?.bluetoothLeAdvertiser?.startAdvertising(settings, data, advertiseCallback)
    }

    private val gattServerCallback = object : BluetoothGattServerCallback() {
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

            if (characteristic == jsonCharacteristic) {
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

            if (characteristic == jsonCharacteristic) {
                characteristic.value = value
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
        jsonCharacteristic?.value = jsonString.toByteArray(Charsets.UTF_8)
        connectedDevice?.let { device ->
            gattServer?.notifyCharacteristicChanged(device, jsonCharacteristic, false)
        } ?: run {
            listener?.onError("No connected device to send notification")
        }
    }

}
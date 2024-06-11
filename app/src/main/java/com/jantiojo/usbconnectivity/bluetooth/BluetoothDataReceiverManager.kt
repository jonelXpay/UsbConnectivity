package com.jantiojo.usbconnectivity.bluetooth

import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothDevice
import android.bluetooth.BluetoothGatt
import android.bluetooth.BluetoothGattCallback
import android.bluetooth.BluetoothGattCharacteristic
import android.bluetooth.BluetoothGattDescriptor
import android.bluetooth.BluetoothManager
import android.bluetooth.BluetoothProfile
import android.bluetooth.le.ScanCallback
import android.bluetooth.le.ScanResult
import android.content.Context
import android.util.Log
import java.util.UUID

class BluetoothDataReceiverManager(
    private val context: Context,
    private val listener: BLEClientListener
) {
    private val bluetoothManager: BluetoothManager =
        context.getSystemService(Context.BLUETOOTH_SERVICE) as BluetoothManager
    private val bluetoothAdapter: BluetoothAdapter = bluetoothManager.adapter
    private var bluetoothGatt: BluetoothGatt? = null
    private var scanCallback: ScanCallback? = null

    private val serviceUUID = UUID.fromString("12345678-1234-5678-1234-56789abcdef0")
    private val charUUID = UUID.fromString("87654321-4321-6789-4321-0fedcba98765")

    interface BLEClientListener {
        fun onDataReceived(data: String)
    }

    fun startScan() {
        val scanner = bluetoothAdapter.bluetoothLeScanner
        scanCallback = object : ScanCallback() {
            override fun onScanResult(callbackType: Int, result: ScanResult) {
                super.onScanResult(callbackType, result)
                val device = result.device
                Log.e(TAG, "onScanResult deviceName: ${device.name}")
                if (device.name == "Jonel Antiojo’s MacBook Pro") { // Replace with your Python device name
                    scanner.stopScan(this)
                    connectToDevice(device)
                }
            }

            override fun onScanFailed(errorCode: Int) {
                super.onScanFailed(errorCode)
                Log.e(TAG, "Scan failed with error code: $errorCode")
            }
        }
        scanner.startScan(scanCallback)
    }

    private fun connectToDevice(device: BluetoothDevice) {
        bluetoothGatt = device.connectGatt(context, false, gattCallback)
    }

    private val gattCallback = object : BluetoothGattCallback() {
        override fun onConnectionStateChange(gatt: BluetoothGatt?, status: Int, newState: Int) {
            super.onConnectionStateChange(gatt, status, newState)
            if (newState == BluetoothProfile.STATE_CONNECTED) {
                gatt?.discoverServices()
            }
        }

        override fun onServicesDiscovered(gatt: BluetoothGatt?, status: Int) {
            super.onServicesDiscovered(gatt, status)
            if (status == BluetoothGatt.GATT_SUCCESS) {
                val service = gatt?.getService(serviceUUID)
                val characteristic = service?.getCharacteristic(charUUID)
                gatt?.setCharacteristicNotification(characteristic, true)
                val descriptor =
                    characteristic?.getDescriptor(UUID.fromString(CLIENT_CHARACTERISTIC_CONFIG))
                descriptor?.value = BluetoothGattDescriptor.ENABLE_NOTIFICATION_VALUE
                gatt?.writeDescriptor(descriptor)
            }
        }

        override fun onCharacteristicChanged(
            gatt: BluetoothGatt,
            characteristic: BluetoothGattCharacteristic,
            value: ByteArray
        ) {
            super.onCharacteristicChanged(gatt, characteristic, value)

            if (characteristic.uuid == charUUID) {
                val data = characteristic.value.toString(Charsets.UTF_8)
                listener.onDataReceived(data)
            }
        }

        override fun onCharacteristicRead(
            gatt: BluetoothGatt,
            characteristic: BluetoothGattCharacteristic,
            status: Int
        ) {
            if (status == BluetoothGatt.GATT_SUCCESS) {
                val jsonData = characteristic.value.toString(Charsets.UTF_8)
                Log.i(TAG, "Received data: $jsonData")
            }
        }
    }

    companion object {
        private val TAG: String = BluetoothDataReceiverManager::class.java.simpleName
        private const val CLIENT_CHARACTERISTIC_CONFIG = "00002902-0000-1000-8000-00805f9b34fb"
    }

}
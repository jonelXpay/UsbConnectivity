package com.jantiojo.usbconnectivity.usbconnection

import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.content.pm.PackageManager
import android.hardware.usb.UsbDevice
import android.hardware.usb.UsbDeviceConnection
import android.hardware.usb.UsbEndpoint
import android.hardware.usb.UsbInterface
import android.hardware.usb.UsbManager
import android.os.Build
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class UsbConnectionManager(private val context: Context) {

    private lateinit var usbInterface: UsbInterface
    private lateinit var endpointIn: UsbEndpoint
    private lateinit var endpointOut: UsbEndpoint
    private lateinit var usbConnection: UsbDeviceConnection
    private lateinit var usbCommunication: UsbCommunication
    private val usbManager = context.getSystemService(Context.USB_SERVICE) as UsbManager
    private var listener: UsbCommunicationListener? = null
    private var payload: String? = null
    private lateinit var usbDevicesReceiver: UsbDevicesReceiver

    fun setUsbCommunicationListener(listener: UsbCommunicationListener) {
        this.listener = listener
    }

    fun setPayload(payload: String?) {
        this.payload = payload
    }


    fun openUsbConnection() {
        if (isUsbHostSupported(context)) {
            usbDevicesReceiver = UsbDevicesReceiver(usbListener)
            val filter = IntentFilter(ACTION_USB_PERMISSION)
            context.registerReceiver(usbDevicesReceiver, filter)
            listUsbDevices()
        } else {
            Log.d(TAG, "USB Host is not supported on this device.")
            listener?.onError("USB Host is not supported on this device.")
        }
    }

    private val usbListener = object : UsbDevicesListener {
        override fun usbDeviceDetached() {
            closeDeviceCommunication()
            Log.d(TAG, "Permission ACTION_USB_DEVICE_DETACHED")
        }

        override fun usbDeviceAttached(usbDevice: UsbDevice?) {
            usbDevice?.let {
                Log.d(TAG, "Permission granted for device ACTION_USB_PERMISSION")
                setupDeviceCommunication(it)
            }
        }
    }

    fun closeUsbConnection() {
        if (isUsbHostSupported(context)) {
            context.unregisterReceiver(usbDevicesReceiver)
            closeDeviceCommunication()
        }
    }

    fun isUsbHostSupported(context: Context): Boolean {
        return context.packageManager.hasSystemFeature(PackageManager.FEATURE_USB_HOST)
    }

    private fun listUsbDevices() {
        val deviceList = usbManager.deviceList
        Log.d(TAG, "Number of USB devices detected: ${deviceList.size}")
        if (deviceList.isEmpty()) {
            Log.d(TAG, "No USB devices found.")
            listener?.onError("No USB devices found.")
        } else {
            for (device in deviceList.values) {
                Log.d(TAG, "Device: ${device.deviceName}, ID: ${device.deviceId}")
                requestUsbPermission(device)
            }
        }
    }

    private fun requestUsbPermission(device: UsbDevice) {
        val permissionIntent = PendingIntent.getBroadcast(
            context, 0, Intent(ACTION_USB_PERMISSION),
            PendingIntent.FLAG_UPDATE_CURRENT
        )
        usbManager.requestPermission(device, permissionIntent)
    }

    private fun setupDeviceCommunication(device: UsbDevice) {
        usbInterface = device.getInterface(0)
        endpointIn = usbInterface.getEndpoint(0)
        endpointOut = usbInterface.getEndpoint(1)

        usbConnection = usbManager.openDevice(device)
        usbConnection.claimInterface(usbInterface, true)

        usbCommunication = UsbCommunication(usbManager)
        // Start communication
        sendAndReceiveData(device)
    }

    private fun closeDeviceCommunication() {
        if (::usbConnection.isInitialized && ::usbInterface.isInitialized) {
            usbConnection.releaseInterface(usbInterface)
            usbConnection.close()
        }
    }

    private fun sendAndReceiveData(usbDevice: UsbDevice) {
        CoroutineScope(Dispatchers.IO).launch {
            try {
                payload?.let {
                    // Send data
                    val payload = it.toByteArray()
                    val isSendSuccess = usbCommunication.sendData(usbDevice, payload)
                    if (isSendSuccess) {
                        // Update UI or handle received data
                        listener?.onDataSent()
                    } else {
                        listener?.onError("Failed to send data")
                    }
                }

                // Receive data
                val buffer = ByteArray(64)
                val receivedBytes = usbCommunication.receiveData(usbDevice, buffer)
                if (receivedBytes > 0) {
                    val receivedString = String(buffer, 0, receivedBytes)
                    withContext(Dispatchers.Main) {
                        Log.d(TAG, "Received: $receivedString")
                        // Update UI or handle received data
                        listener?.onDataReceived(receivedString)
                    }
                } else {
                    listener?.onError("Failed to receive data")
                }
            } catch (e: Exception) {
                e.printStackTrace()
                withContext(Dispatchers.Main) {
                    Log.d(TAG, "Error: ${e.message}")
                    // Handle error
                    listener?.onError(e.message.orEmpty())
                }
            }
        }
    }

    companion object {
        private val TAG = UsbConnectionManager::class.java.simpleName
        private const val ACTION_USB_PERMISSION = "com.android.example.USB_PERMISSION"
    }
}
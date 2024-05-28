package com.jantiojo.usbconnectivity.usbconnection

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.hardware.usb.UsbDevice
import android.hardware.usb.UsbManager
import android.util.Log

class UsbDevicesReceiver(private val usbDevicesListener: UsbDevicesListener) : BroadcastReceiver() {
    companion object {
        private const val TAG = "UsbReceiver"
    }

    override fun onReceive(context: Context?, intent: Intent?) {
        when (intent?.action) {
            UsbManager.ACTION_USB_DEVICE_ATTACHED -> {
                val device: UsbDevice? = intent.getParcelableExtra(UsbManager.EXTRA_DEVICE)
                device?.let {
                    Log.d(TAG, "USB Device Attached: ${it.deviceName}")
                    // Handle the USB device attached
                    usbDevicesListener.usbDeviceDetached()
                }
            }
            UsbManager.ACTION_USB_DEVICE_DETACHED -> {
                val device: UsbDevice? = intent.getParcelableExtra(UsbManager.EXTRA_DEVICE)
                device?.let {
                    Log.d(TAG, "USB Device Detached: ${it.deviceName}")
                    // Handle the USB device detached
                    usbDevicesListener.usbDeviceAttached(it)
                }
            }
        }
    }
}
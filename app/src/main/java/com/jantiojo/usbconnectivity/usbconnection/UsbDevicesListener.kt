package com.jantiojo.usbconnectivity.usbconnection

import android.hardware.usb.UsbDevice

interface UsbDevicesListener {
    fun usbDeviceDetached()
    fun usbDeviceAttached(usbDevice: UsbDevice?)
}
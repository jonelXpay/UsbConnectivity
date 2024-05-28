package com.jantiojo.usbconnectivity.usbconnection

import android.hardware.usb.UsbDevice
import android.hardware.usb.UsbManager

class UsbCommunication(private val usbManager: UsbManager) {

    fun sendData(device: UsbDevice, data: ByteArray): Boolean {
        val connection = usbManager.openDevice(device) ?: return false
        val endpoint = device.getInterface(0).getEndpoint(0)
        val sentBytes = connection.bulkTransfer(endpoint, data, data.size, 1000)
        connection.close()
        return sentBytes == data.size
    }

    fun receiveData(device: UsbDevice, buffer: ByteArray): Int {
        val connection = usbManager.openDevice(device) ?: return -1
        val endpoint = device.getInterface(0).getEndpoint(1)
        val receivedBytes = connection.bulkTransfer(endpoint, buffer, buffer.size, 1000)
        connection.close()
        return receivedBytes
    }
}

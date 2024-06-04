package com.jantiojo.usbconnectivity.usbconnection

interface UsbCommunicationListener {

    fun usbDeviceDetected(count: Int)
    fun onDataSent()
    fun onDataReceived(data: String)
    fun onError(message: String)
}
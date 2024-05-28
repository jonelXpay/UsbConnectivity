package com.jantiojo.usbconnectivity.usbconnection

interface UsbCommunicationListener {

    fun onDataSent()
    fun onDataReceived(data: String)
    fun onError(message: String)
}
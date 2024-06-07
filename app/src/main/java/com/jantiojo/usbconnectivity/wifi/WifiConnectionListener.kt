package com.jantiojo.usbconnectivity.wifi

interface WifiConnectionListener {
    fun onResponseReceived(response: String)
    fun onError(message: String)
}
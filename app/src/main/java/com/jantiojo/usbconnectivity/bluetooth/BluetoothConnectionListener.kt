package com.jantiojo.usbconnectivity.bluetooth


interface BluetoothConnectionListener {
    fun onDataSent(deviceAddress: String)

    fun onDataReceived(data: String)

    fun onStartSuccess()

    fun onDeviceConnected(address: String)
    fun onDeviceDisconnected(address: String)

    fun onError(message: String)
}
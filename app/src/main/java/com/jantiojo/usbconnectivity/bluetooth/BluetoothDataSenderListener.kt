package com.jantiojo.usbconnectivity.bluetooth


interface BluetoothDataSenderListener {
    fun onDataSent(deviceAddress: String)

    fun onStartSuccess()

    fun onDeviceConnected(address: String)
    fun onDeviceDisconnected(address: String)

    fun onError(message: String)
}
package com.jantiojo.usbconnectivity.socket

interface WebSocketConnectionListener {
    fun onMessageReceived(response: String)
    fun onFailure(message: String)
    fun onConnectionClosed(code: Int, reason: String)
}
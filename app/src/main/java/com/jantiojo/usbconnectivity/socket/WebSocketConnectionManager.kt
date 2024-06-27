package com.jantiojo.usbconnectivity.socket

import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.Response
import okhttp3.WebSocket
import okhttp3.WebSocketListener
import okio.ByteString
import org.json.JSONObject

class WebSocketConnectionManager(
    private val urlEndpoint: String
) {

    private val client = OkHttpClient()
    private val webSocket: WebSocket
    private var listener: WebSocketConnectionListener? = null

    init {
        val request = Request.Builder()
            .url(urlEndpoint).build()
        val webSocketListener = EchoWebSocketListener()
        webSocket = client.newWebSocket(request, webSocketListener)
        client.dispatcher.executorService.shutdown()
    }

    fun setListener(listener: WebSocketConnectionListener) {
        this.listener = listener
    }

    fun sendDataToServer(
        status: String,
        transactionId: String,
        receiptNo: String,
        date: String
    ) {
        val jsonData = JSONObject().apply {
            put("status", status)
            put("transactionId", transactionId)
            put("receiptNo", receiptNo)
            put("date", date)
        }
        val message = jsonData.toString()
        webSocket.send(message)
    }

    fun close() {
        webSocket.close(1000, "App closed")
    }

    private inner class EchoWebSocketListener : WebSocketListener() {
        override fun onOpen(webSocket: WebSocket, response: Response) {
            println("WebSocket opened")
        }

        override fun onMessage(webSocket: WebSocket, text: String) {
            println("Received message: $text")
            listener?.onMessageReceived(text)
        }

        override fun onMessage(webSocket: WebSocket, bytes: ByteString) {
            println("Received bytes: " + bytes.hex())
        }

        override fun onClosing(webSocket: WebSocket, code: Int, reason: String) {
            webSocket.close(1000, null)
            println("Closing: $code / $reason")
            listener?.onConnectionClosed(code, reason)
        }

        override fun onFailure(webSocket: WebSocket, t: Throwable, response: Response?) {
            t.printStackTrace()
            listener?.onFailure(t.message ?: "Unknown error")
        }
    }
}
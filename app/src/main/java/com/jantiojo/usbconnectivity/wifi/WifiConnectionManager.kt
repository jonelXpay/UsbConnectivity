package com.jantiojo.usbconnectivity.wifi

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.BufferedReader
import java.io.InputStreamReader
import java.io.OutputStreamWriter
import java.net.Socket

class WifiConnectionManager(private val host: String, private val port: Int) {

    private var listener: WifiConnectionListener? = null

    fun setConnectionListener(listener: WifiConnectionListener) {
        this.listener = listener
    }

    suspend fun sendData(jsonData: String) {
        if (host.isBlank()) {
            withContext(Dispatchers.Main) {
                listener?.onError("Invalid Host")
            }
            return
        }

        if (port < 4) {
            withContext(Dispatchers.Main) {
                listener?.onError("Invalid Port")
            }
            return
        }

        withContext(Dispatchers.IO) {
            try {
                val socket = Socket(host, port)  // Replace with the server IP and port
                val out = OutputStreamWriter(socket.getOutputStream())
                val inStream = BufferedReader(InputStreamReader(socket.getInputStream()))

                // Send JSON data
                out.write(jsonData)
                out.flush()

                // Read response
                val response = inStream.readLine()

                withContext(Dispatchers.Main) {
                    listener?.onResponseReceived(response)
                }

                socket.close()
            } catch (e: Exception) {
                e.printStackTrace()
                withContext(Dispatchers.Main) {
                    listener?.onError(e.message ?: "Unknown error")
                }
            }
        }
    }
}
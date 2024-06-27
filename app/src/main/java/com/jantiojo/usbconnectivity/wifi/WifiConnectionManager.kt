package com.jantiojo.usbconnectivity.wifi

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.BufferedReader
import java.io.DataOutputStream
import java.io.IOException
import java.io.InputStreamReader
import java.net.ServerSocket
import java.net.Socket

/**
 * Facilitates sending and receiving data over a WiFi network.
 */
class WifiConnectionManager {

    private var listener: WifiConnectionListener? = null
    private var sendingSocket: Socket? = null
    private var outputStream: DataOutputStream? = null
    private var serverSocket: ServerSocket? = null
    private var receivingSocket: Socket? = null

    /**
     * Sets the listener to receive connection events.
     *
     *@param listener An optional listener to receive events like errors or successful data .
     */
    fun setConnectionListener(listener: WifiConnectionListener) {
        this.listener = listener
    }


    /**
     * Sends data to the server.
     *
     * @param serverIpAddress The IP address of the server to communicate with.
     *  @param sendPort The port number on the server for sending data.
     * @param data The string data to send.
     */
    suspend fun sendData(
        serverIpAddress: String,
        sendPort: Int,
        data: String
    ) {
        if (serverIpAddress.isBlank()) {
            withContext(Dispatchers.Main) {
                listener?.onError("Invalid Host")
            }
            return
        }

        if (sendPort < 4) {
            withContext(Dispatchers.Main) {
                listener?.onError("Invalid Send Port")
            }
            return
        }

        withContext(Dispatchers.IO) {
            try {
                sendingSocket = Socket(serverIpAddress, sendPort)
                sendingSocket?.let {
                    val outputStream = DataOutputStream(it.getOutputStream())

                    // Send JSON data
                    outputStream.write(data.toByteArray())
                    outputStream.flush()
                }

            } catch (e: Exception) {
                println("Error 1: ${e.message}")
                listener?.onError(e.message ?: "Error sending data")
            } finally {
                // Close resources to reset the connection
                try {
                    closeSendingConnection()
                } catch (e: IOException) {
                    println("Error 2: ${e.message}")
                    // Handle exception if closing fails
                    withContext(Dispatchers.Main) {
                        listener?.onError("Error closing socket: ${e.message}")
                    }
                }
            }
        }
    }

    fun closeSendingConnection() {
        outputStream?.close()
        sendingSocket?.close()
    }

    /**
     * Listens for and receives data on the specified port.
     *
     * @param receivePort The port number to listen on.
     */
    suspend fun receiveData(receivePort: Int) {
        if (receivePort < 4) {
            withContext(Dispatchers.Main) {
                listener?.onError("Invalid Receive Port")
            }
            return
        }

        withContext(Dispatchers.IO) {
            try {
                serverSocket = ServerSocket(receivePort)
                while (true) {
                    try {
                        receivingSocket = serverSocket?.accept()

                        val inputStream =
                            BufferedReader(InputStreamReader(receivingSocket?.getInputStream()))

                        // Read response from client
                        val response = inputStream.readLine()

                        // Update UI or process received data
                        withContext(Dispatchers.Main) {
                            listener?.onResponseReceived(response)
                        }

                    } catch (e: Exception) {
                        withContext(Dispatchers.Main) {
                            listener?.onError("Error receiving data: ${e.message}")
                        }
                    } finally {
                        receivingSocket?.close()
                    }
                }
            } catch (e: Exception) {
                listener?.onError(e.message ?: "Error receiving data")
            } finally {
                serverSocket?.close()
            }
        }
    }


    fun closeReceivingConnection() {
        outputStream?.close()
        sendingSocket?.close()
    }

}
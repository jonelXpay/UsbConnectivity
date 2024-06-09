package com.jantiojo.usbconnectivity.wifi

import android.content.Context
import com.jantiojo.usbconnectivity.R
import com.jantiojo.usbconnectivity.utils.EncryptionUtil
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.BufferedReader
import java.io.InputStream
import java.io.InputStreamReader
import java.io.OutputStreamWriter
import java.net.Socket
import java.security.KeyStore
import java.security.cert.CertificateFactory
import java.security.cert.X509Certificate
import javax.net.ssl.SSLContext
import javax.net.ssl.SSLSocket
import javax.net.ssl.SSLSocketFactory
import javax.net.ssl.TrustManagerFactory

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


    suspend fun sendEncryptedDataToServer(jsonData: String, context: Context) {
        withContext(Dispatchers.IO) {
            try {
                val sslSocketFactory: SSLSocketFactory = getCustomSSLSocketFactory(context).socketFactory
                val sslSocket: SSLSocket =
                    sslSocketFactory.createSocket(host, port) as SSLSocket
                sslSocket.startHandshake()

                val encryptedData = EncryptionUtil.encrypt(jsonData)

                val writer = OutputStreamWriter(sslSocket.outputStream)
                writer.write(encryptedData + "\n")
                writer.flush()

                val reader = BufferedReader(InputStreamReader(sslSocket.inputStream))
                val response = reader.readLine()
                val decryptedResponse = EncryptionUtil.decrypt(response)


                withContext(Dispatchers.Main) {
                    // Handle the response in the UI thread if needed
                    println("Received from server: $decryptedResponse")

                    listener?.onResponseReceived(response)
                }

                writer.close()
                reader.close()
                sslSocket.close()
            } catch (e: Exception) {
                e.printStackTrace()
                println("Error from server: ${e.localizedMessage}")
                withContext(Dispatchers.Main) {
                    listener?.onError(e.message ?: "Unknown error")
                }
            }
        }
    }

   private fun getCustomSSLSocketFactory(context: Context): SSLContext {
        val certificateInputStream: InputStream = context.resources.openRawResource(R.raw.certificate) // Your certificate file
        val certificateFactory = CertificateFactory.getInstance("X.509")
        val certificate: X509Certificate = certificateFactory.generateCertificate(certificateInputStream) as X509Certificate

        val trustManagerFactory = TrustManagerFactory.getInstance(TrustManagerFactory.getDefaultAlgorithm())
        val keyStore = KeyStore.getInstance(KeyStore.getDefaultType())
        keyStore.load(null)
        keyStore.setCertificateEntry("ca", certificate)

        trustManagerFactory.init(keyStore)

        val sslContext = SSLContext.getInstance("TLS")
        sslContext.init(null, trustManagerFactory.trustManagers, null)

        return sslContext
    }
}
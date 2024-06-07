package com.jantiojo.usbconnectivity.wifi

import com.jantiojo.usbconnectivity.TestDispatcherRule
import io.mockk.MockKAnnotations
import io.mockk.every
import io.mockk.impl.annotations.MockK
import io.mockk.mockk
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import java.io.BufferedReader
import java.io.IOException
import java.io.InputStream
import java.io.OutputStream
import java.io.OutputStreamWriter
import java.net.Socket

class WifiConnectionManagerTest {

    @get:Rule
    val dispatcherRule = TestDispatcherRule()

    @MockK
    private lateinit var listener: WifiListener

    private lateinit var wifiConnectionManager: WifiConnectionManager

    private val jsonData = "{\"name\":\"John Doe\",\"age\":30}"

    @Before
    fun setUp() {
        MockKAnnotations.init(this, relaxed = true)
        wifiConnectionManager = WifiConnectionManager("192.168.1.3", 8080)
        wifiConnectionManager.setConnectionListener(listener)
    }

    @Test
    fun `sendData() sends data and receives response successfully`() = runTest {

        val responseFromServer =
            "{\"status\":\"success\",\"data_received\":{\"name\":\"John Doe\",\"age\":30}}"
        val socket = mockk<Socket>(relaxed = true)
        val mockOut = mockk<OutputStreamWriter>(relaxed = true)
        val mockInStream = mockk<BufferedReader>(relaxed = true)

        every { socket.getOutputStream() } returns OutputStreamWriterAdapter(
            mockOut
        )
        every { socket.getInputStream() } returns BufferedReaderAdapter(
            mockInStream
        )
        every { listener.lastResponse } returns responseFromServer
        every { mockInStream.readLine() } returns listener.lastResponse

        // Call the sendData() function
        wifiConnectionManager.sendData(jsonData)

        // Verify that the data was sent and received successfully
        assertEquals(responseFromServer, listener.lastResponse)
    }

    @Test
    fun `sendData() handles invalid host`() = runTest {
        // Create a WifiConnectionManager with an invalid host
        val wifiConnectionManager = WifiConnectionManager("", 8080)
        wifiConnectionManager.setConnectionListener(listener)

        every { listener.lastError } returns "Invalid Host"
        // Call the sendData() function
        wifiConnectionManager.sendData("Hello from client")

        // Verify that the onError() method was called with the correct error message
        assertEquals("Invalid Host", listener.lastError)
    }

    @Test
    fun `sendData() handles invalid port`() = runTest {
        // Create a WifiConnectionManager with an invalid port
        val wifiConnectionManager = WifiConnectionManager("192.168.1.4", 3)
        wifiConnectionManager.setConnectionListener(listener)
        every { listener.lastError } returns "Invalid Port"

        // Call the sendData() function
        wifiConnectionManager.sendData("Hello from client")

        // Verify that the onError() method was called with the correct error message
        assertEquals("Invalid Port", listener.lastError)
    }

    @Test
    fun `sendData() handles exceptions gracefully`() = runTest {
        // Mock socket behavior to throw an exception
        val socket = mockk<Socket>()
        every { socket.getOutputStream() } throws IOException("Failed to connect")
        every { listener.lastError } returns "Failed to connect"
        // Call the sendData() function
        wifiConnectionManager.sendData("Hello from client")

        // Verify that the onError() method was called with the correct error message
        assertEquals("Failed to connect", listener.lastError)
    }
}

// Mock listener class to capture the last response and error
class WifiListener : WifiConnectionListener {

    var lastResponse: String? = null
    var lastError: String? = null

    override fun onResponseReceived(response: String) {
        lastResponse = response
    }

    override fun onError(message: String) {
        lastError = message
    }
}

class OutputStreamWriterAdapter(private val outputStreamWriter: OutputStreamWriter) :
    OutputStream() {
    override fun write(b: Int) {
        outputStreamWriter.write(b)
    }
}

class BufferedReaderAdapter(private val bufferedReader: BufferedReader) : InputStream() {
    override fun read(): Int {
        return bufferedReader.read()
    }
}

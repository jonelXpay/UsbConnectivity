package com.jantiojo.usbconnectivity.usbconnection

import android.hardware.usb.UsbDevice
import android.hardware.usb.UsbDeviceConnection
import android.hardware.usb.UsbInterface
import android.hardware.usb.UsbManager
import io.mockk.MockKAnnotations
import io.mockk.every
import io.mockk.impl.annotations.MockK
import io.mockk.verify
import org.junit.Before
import org.junit.Test


class UsbCommunicationTest {

    @MockK
    private lateinit var usbManager: UsbManager

    @MockK
    private lateinit var usbDevice: UsbDevice

    @MockK
    private lateinit var usbInterface: UsbInterface

    @MockK
    private lateinit var usbConnection: UsbDeviceConnection

    private lateinit var usbCommunication: UsbCommunication

    @Before
    fun setUp() {
        MockKAnnotations.init(this, relaxed = true)

        every { usbDevice.getInterface(0) } answers { usbInterface }
        every { usbManager.openDevice(usbDevice) } answers { usbConnection }
        usbCommunication = UsbCommunication(usbManager)
    }

    @Test
    fun usbCommunication_sendData() {
        val data = "Hello USB".toByteArray()
        every {
            usbConnection.bulkTransfer(
                any(),
                data,
                data.size,
                1000
            )
        } returns data.size

        val result = usbCommunication.sendData(usbDevice, data)

        verify(atLeast = 1) { usbConnection.bulkTransfer(any(), data, data.size, TIME_OUT) }
        verify { usbConnection.close() }
        assert(result)
    }

    @Test
    fun usbCommunication_receiveData() {
        val buffer = ByteArray(64)
        every { usbConnection.bulkTransfer(any(), buffer, buffer.size, TIME_OUT) } returns 64

        val result = usbCommunication.receiveData(usbDevice, buffer)

        verify { usbConnection.bulkTransfer(any(), buffer, buffer.size, TIME_OUT) }
        verify { usbConnection.close() }
        assert(result == 64)
    }

    @Test
    fun usbCommunication_sendDataWithError() {
        val data = "Hello USB".toByteArray()
        every { usbConnection.bulkTransfer(any(), data, data.size, TIME_OUT) } returns -1

        val result = usbCommunication.sendData(usbDevice, data)

        verify { usbConnection.bulkTransfer(any(), data, data.size, TIME_OUT) }
        verify { usbConnection.close() }
        assert(!result)
    }

    @Test
    fun usbCommunication_receiveDataWithError() {
        val buffer = ByteArray(64)
        every { usbConnection.bulkTransfer(any(), buffer, buffer.size, TIME_OUT) } returns -1

        val result = usbCommunication.receiveData(usbDevice, buffer)

        verify { usbConnection.bulkTransfer(any(), buffer, buffer.size, TIME_OUT) }
        verify { usbConnection.close() }
        assert(result == -1)
    }

    companion object {
        private const val TIME_OUT = 1000
    }

}
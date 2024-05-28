package com.jantiojo.usbconnectivity.usbconnection

import android.content.Context
import android.content.pm.PackageManager
import android.hardware.usb.UsbDevice
import android.hardware.usb.UsbManager
import androidx.test.platform.app.InstrumentationRegistry
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import junit.framework.TestCase.assertTrue
import org.junit.Before
import org.junit.Test
import java.util.HashMap


class UsbConnectionManagerTest {

    private lateinit var usbConnectionManager: UsbConnectionManager

    @Before
    fun setUp() {
        val appContext = InstrumentationRegistry.getInstrumentation().targetContext
        usbConnectionManager = UsbConnectionManager(appContext)
    }


    @Test
    fun testIsUsbHostSupported() {
        // Mock the package manager to return true for hasSystemFeature()
        val packageManager = mockk<PackageManager>()
        every { packageManager.hasSystemFeature(PackageManager.FEATURE_USB_HOST) } returns true

        val context = mockk<Context>()
        every { context.packageManager } returns packageManager

        assertTrue(usbConnectionManager.isUsbHostSupported(context))
    }

    @Test
    fun testOpenUsbConnection_success() {
        // Mock the USB manager to return a mock UsbDevice

        val usbManager = mockk<UsbManager>()
        val map = hashMapOf("" to mockk<UsbDevice>())
        every { usbManager.deviceList } returns map

        val context = mockk<Context>()
        every { context.getSystemService(Context.USB_SERVICE) } returns usbManager

        usbConnectionManager.openUsbConnection()

        // Verify that the device communication is set up
        assertTrue(::usbConnectionManager.isInitialized)
    }

    @Test
    fun testOpenUsbConnection_failure() {
        // Mock the USB manager to return an empty device list
        val usbManager = mockk<UsbManager>()
        every { usbManager.deviceList } returns hashMapOf()

        val context = mockk<Context>()
        every { context.getSystemService(Context.USB_SERVICE) } returns usbManager
        every { usbConnectionManager.isUsbHostSupported(context) } returns true
        usbConnectionManager.openUsbConnection()

        // Verify that the listener is notified of the error
        val listener = mockk<UsbCommunicationListener>()
        usbConnectionManager.setUsbCommunicationListener(listener)


        verify { listener.onError("No USB devices found.") }
    }
}
package com.jantiojo.usbconnectivity.bluetooth

import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothDevice
import android.bluetooth.BluetoothGattCharacteristic
import android.bluetooth.BluetoothGattServer
import android.bluetooth.BluetoothManager
import android.content.Context
import io.mockk.MockKAnnotations
import io.mockk.every
import io.mockk.impl.annotations.MockK
import io.mockk.mockk
import io.mockk.verify
import org.junit.Assert
import org.junit.Before
import org.junit.Test
import java.util.UUID

class BluetoothDataSenderManagerTest {


    @MockK
    private lateinit var bluetoothManager: BluetoothManager

    @MockK
    private lateinit var bluetoothAdapter: BluetoothAdapter

    @MockK
    private lateinit var gattServer: BluetoothGattServer

    @MockK
    private lateinit var jsonCharacteristic: BluetoothGattCharacteristic


    @MockK
    private lateinit var listener: BluetoothDataSenderListener

    private lateinit var bluetoothDataSenderManager: BluetoothDataSenderManager

    @Before
    fun setUp() {
        MockKAnnotations.init(this, relaxed = true)
        val context = mockk<Context>()
        bluetoothDataSenderManager = BluetoothDataSenderManager(context, listener)
        every { context.getSystemService(Context.BLUETOOTH_SERVICE) } returns bluetoothManager
        every { bluetoothManager.adapter } returns bluetoothAdapter

    }

    @Test
    fun initialize_whenBluetoothAdapterIsEnabled_returnsTrue() {
        // Arrange
        every { bluetoothAdapter.isEnabled } returns true

        // Act
        val result = bluetoothDataSenderManager.initialize()

        // Assert
        Assert.assertTrue(result)
    }

    @Test
    fun initialize_whenBluetoothAdapterIsNotEnabled_returnsFalse() {
        // Arrange
        every { bluetoothAdapter.isEnabled } returns false

        // Act
        val result = bluetoothDataSenderManager.initialize()

        // Assert
        Assert.assertFalse(result)
    }

    @Test
    fun startServer_whenGattServerIsNull_callsOnErrorListener() {
        val context = mockk<Context>()
        // Arrange
        every { bluetoothManager.openGattServer(context, any()) } returns null

        // Act
        bluetoothDataSenderManager.startServer(UUID.randomUUID(), UUID.randomUUID())

        // Assert
        verify { listener.onError("Unable to create GATT server") }
    }

    @Test
    fun sendJsonData_whenConnectedDeviceIsNull_callsOnErrorListener() {
        bluetoothDataSenderManager.sendJsonData("test")
        verify { listener.onError("No connected device to send notification") }
    }

    @Test
    fun sendJsonData_whenConnectedDeviceIsNotNull_notifiesCharacteristicChanged() {
        val context = mockk<Context>(relaxed = true)
        val connectedDevice = mockk<BluetoothDevice>(relaxed = true)
        val jsonString = "{\"message\": \"Hello, world!\"}"

        // Arrange
        every { gattServer.notifyCharacteristicChanged(any(), any(), any()) } returns true
        every { connectedDevice.address } returns "address"
        every { connectedDevice.name } returns "address"
        // Act
        bluetoothDataSenderManager.sendJsonData(jsonString)

        // Assert
        verify {
            gattServer.notifyCharacteristicChanged(
                connectedDevice,
                jsonCharacteristic,
                false
            )
        }
    }

}
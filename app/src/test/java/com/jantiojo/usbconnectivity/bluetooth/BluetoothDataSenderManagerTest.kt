package com.jantiojo.usbconnectivity.bluetooth

import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothDevice
import android.bluetooth.BluetoothGattCharacteristic
import android.bluetooth.BluetoothGattServer
import android.bluetooth.BluetoothGattService
import android.bluetooth.BluetoothManager
import android.content.Context
import io.mockk.MockKAnnotations
import io.mockk.Runs
import io.mockk.every
import io.mockk.impl.annotations.MockK
import io.mockk.just
import io.mockk.mockk
import io.mockk.verify
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import java.util.UUID

class BluetoothConnectionManagerTest {

    @MockK
    private lateinit var bluetoothManager: BluetoothManager

    @MockK
    private lateinit var bluetoothAdapter: BluetoothAdapter

    @MockK
    private lateinit var gattServer: BluetoothGattServer

    @MockK
    private lateinit var jsonCharacteristic: BluetoothGattCharacteristic

    @MockK
    private lateinit var listener: BluetoothConnectionListener

    private lateinit var bluetoothConnectionManager: BluetoothConnectionManager
    private val serviceUUID = UUID.fromString("0000180a-0000-1000-8000-00805f9b34fb")
    private val writeCharacteristicUUID = UUID.fromString("00002a29-0000-1000-8000-00805f9b34fb")
    private val readCharacteristicUUID = UUID.fromString("00002a24-0000-1000-8000-00805f9b34fb")

    @Before
    fun setUp() {
        MockKAnnotations.init(this, relaxed = true)
        val context = mockk<Context>()
        every { context.getSystemService(Context.BLUETOOTH_SERVICE) } returns bluetoothManager
        every { bluetoothManager.adapter } returns bluetoothAdapter

        bluetoothConnectionManager = BluetoothConnectionManager(
            context,
            bluetoothManager,
            serviceUUID,
            writeCharacteristicUUID,
            readCharacteristicUUID,
            listener
        )
    }

    @Test
    fun initialize_whenBluetoothAdapterIsEnabled_returnsTrue() {
        every { bluetoothAdapter.isEnabled } returns true
        val result = bluetoothConnectionManager.initialize()
        assertTrue(result)
    }

    @Test
    fun initialize_whenBluetoothAdapterIsNotEnabled_returnsFalse() {
        every { bluetoothAdapter.isEnabled } returns false
        val result = bluetoothConnectionManager.initialize()
        assertFalse(result)
    }

    @Test
    fun startServer_opensGattServerAndStartsAdvertising() {
        val mockService = mockk<BluetoothGattService>(relaxed = true)
        every { mockService.addCharacteristic(any()) } returns true


        every { bluetoothManager.openGattServer(any(), any()) } returns gattServer
        every {
            BluetoothGattService(serviceUUID, BluetoothGattService.SERVICE_TYPE_PRIMARY)
        } returns mockService
        bluetoothConnectionManager.startServer()

        verify { bluetoothManager.openGattServer(any(), any()) }
        verify { bluetoothAdapter.bluetoothLeAdvertiser?.startAdvertising(any(), any(), any()) }
    }

    @Test
    fun startServer_whenGattServerIsNull_callsOnErrorListener() {
        every { bluetoothManager.openGattServer(any(), any()) } returns null
        bluetoothConnectionManager.startServer()
        verify { listener.onError("Unable to create GATT server") }
    }

    @Test
    fun sendJsonData_whenConnectedDeviceIsNull_callsOnErrorListener() {
        bluetoothConnectionManager.sendJsonData("test")
        verify { listener.onError("No connected device to send notification") }
    }

    @Test
    fun sendJsonData_whenJsonStringIsEmpty_callsOnErrorListener() {
        val connectedDevice = mockk<BluetoothDevice>(relaxed = true)
        bluetoothConnectionManager.connectedDevice = connectedDevice
        bluetoothConnectionManager.sendJsonData("")
        verify { listener.onError("JSON Data is invalid") }
    }

    @Test
    fun sendJsonData_whenConnectedDeviceIsNotNull_notifiesCharacteristicChanged() {
        val connectedDevice = mockk<BluetoothDevice>(relaxed = true)
        val jsonString = "{\"message\": \"Hello, world!\"}"

        every { gattServer.notifyCharacteristicChanged(any(), any(), any()) } returns true
        every { connectedDevice.address } returns "address"
        bluetoothConnectionManager.connectedDevice = connectedDevice
        bluetoothConnectionManager.gattServer = gattServer
        bluetoothConnectionManager.jsonCharacteristic = jsonCharacteristic

        bluetoothConnectionManager.sendJsonData(jsonString)

        verify {
            gattServer.notifyCharacteristicChanged(
                connectedDevice,
                jsonCharacteristic,
                false
            )
        }
    }

}

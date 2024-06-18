package com.jantiojo.usbconnectivity

import android.bluetooth.BluetoothManager
import android.content.Context
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.jantiojo.usbconnectivity.bluetooth.BluetoothConnectionListener
import com.jantiojo.usbconnectivity.bluetooth.BluetoothConnectionManager
import com.jantiojo.usbconnectivity.databinding.FragmentSecondBinding
import org.json.JSONObject
import java.util.UUID

/**
 * A simple [Fragment] subclass as the second destination in the navigation.
 */
class SecondFragment : Fragment() {

    private var _binding: FragmentSecondBinding? = null

    // This property is only valid between onCreateView and
    // onDestroyView.
    private val binding get() = _binding!!

    private lateinit var bluetoothDataSenderManager: BluetoothConnectionManager

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {

        _binding = FragmentSecondBinding.inflate(inflater, container, false)
        return binding.root

    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val serviceUUID = UUID.fromString("12345678-1234-5678-1234-56789abcdef0")
        val writeCharacteristicUUID = UUID.fromString("87654321-4321-6789-4321-0fedcba98788")
        val readCharacteristicUUID = UUID.fromString("00002902-0000-1000-8000-00805f9b3444")

        bluetoothDataSenderManager = BluetoothConnectionManager(
            requireContext(),
            bluetoothManager = requireContext().getSystemService(Context.BLUETOOTH_SERVICE) as BluetoothManager,
            serviceUUID = serviceUUID,
            writeCharacteristicUUID = writeCharacteristicUUID,
            readCharacteristicUUID = readCharacteristicUUID,
            object : BluetoothConnectionListener {
                override fun onDataSent(deviceAddress: String) {
                    Log.i(TAG, "Notification sent successfully to device: $deviceAddress")
                }

                override fun onDataReceived(data: String) {
                    requireActivity().runOnUiThread {
                        binding.textviewSecond.text = data
                    }
                }

                override fun onStartSuccess() {
                    Log.i(TAG, "onStartSuccess")
                }

                override fun onDeviceConnected(address: String) {
                    Log.i(TAG, "onDeviceConnected in $address")
                }

                override fun onDeviceDisconnected(address: String) {
                    Log.i(TAG, "onDeviceDisconnected in $address")
                }

                override fun onError(message: String) {
                    Log.i(TAG, "onError : $message")
                }
            })

        if (bluetoothDataSenderManager.initialize()) {

            bluetoothDataSenderManager.startServer()
        }

        binding.buttonSecond.setOnClickListener {
            findNavController().navigate(R.id.action_SecondFragment_to_FirstFragment)
        }

        binding.buttonSendData.setOnClickListener {
            // Example JSON data
            val jsonData = JSONObject().apply {
                put("name", "John Doe")
                put("age", 30)
            }
            bluetoothDataSenderManager.sendJsonData(jsonData.toString())
        }

    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    companion object {
        private val TAG: String = SecondFragment::class.java.simpleName
    }
}
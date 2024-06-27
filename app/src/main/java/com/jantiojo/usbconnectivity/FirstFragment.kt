package com.jantiojo.usbconnectivity

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import com.jantiojo.usbconnectivity.databinding.FragmentFirstBinding
import com.jantiojo.usbconnectivity.wifi.WifiConnectionListener
import com.jantiojo.usbconnectivity.wifi.WifiConnectionManager
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import org.json.JSONObject

/**
 * A simple [Fragment] subclass as the default destination in the navigation.
 */
class FirstFragment : Fragment() {

    private var _binding: FragmentFirstBinding? = null

    // This property is only valid between onCreateView and
    // onDestroyView.
    private val binding get() = _binding!!

    private lateinit var wifiConnectionManager: WifiConnectionManager

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {

        _binding = FragmentFirstBinding.inflate(inflater, container, false)
        return binding.root

    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        wifiConnectionManager = WifiConnectionManager()
        wifiConnectionManager.setConnectionListener(
            object : WifiConnectionListener {
                override fun onResponseReceived(response: String) {
                    binding.textviewFirst.text = response
                }

                override fun onError(message: String) {
                    println("Failed to send/receive data: $message")
                }
            }
        )

        binding.buttonFirst.setOnClickListener {
            findNavController().navigate(R.id.action_FirstFragment_to_SecondFragment)
        }

        binding.buttonSendData.setOnClickListener {
            // Example JSON data
            val jsonData = JSONObject().apply {
                put("status", "Approved")
                put("transactionId", "4b546514-fb54-47a2-96f7-6a465c3b3422")
                put("receiptNo", "6a465c3b342")
                put("date", "8/9/2024 12:00:00")
            }
            lifecycleScope.launch(Dispatchers.IO) {
                wifiConnectionManager.sendData(
                    serverIpAddress = SERVER_IP,
                    sendPort = SEND_PORT,
                    data = jsonData.toString()
                )
            }
        }

        lifecycleScope.launch(Dispatchers.IO) {
            wifiConnectionManager.receiveData(receivePort = RECEIVE_PORT)
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
        wifiConnectionManager.closeSendingConnection()
        wifiConnectionManager.closeReceivingConnection()
    }


    companion object {
        private const val SERVER_IP = "192.168.1.6"  // Replace with your Python server's IP
        private const val SEND_PORT = 5002          // Port for sending data to Python server
        private const val RECEIVE_PORT = 5001       // Port for receiving data from Python server
    }
}
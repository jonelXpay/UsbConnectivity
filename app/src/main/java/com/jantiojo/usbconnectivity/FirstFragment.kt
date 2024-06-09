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

        wifiConnectionManager = WifiConnectionManager("192.168.1.8",39446)
        wifiConnectionManager.setConnectionListener(object : WifiConnectionListener {
            override fun onResponseReceived(response: String) {
                binding.textviewFirst.text = response
            }

            override fun onError(message: String) {
                println("Failed to send/receive data: $message")
            }
        })

        binding.buttonSendData.setOnClickListener {
            // Example JSON data
            val jsonData = JSONObject().apply {
                put("name", "John Doe")
                put("age", 30)
            }

            lifecycleScope.launch(Dispatchers.IO) {
                wifiConnectionManager.sendEncryptedDataToServer(jsonData.toString(),requireContext())
            }
        }
        binding.buttonFirst.setOnClickListener {
            findNavController().navigate(R.id.action_FirstFragment_to_SecondFragment)
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
package com.jantiojo.usbconnectivity

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.jantiojo.usbconnectivity.databinding.FragmentFirstBinding
import com.jantiojo.usbconnectivity.wifi.WifiConnectionListener
import com.jantiojo.usbconnectivity.wifi.WifiConnectionManager

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

        wifiConnectionManager = WifiConnectionManager("192.168.1.9",8080)
        wifiConnectionManager.setConnectionListener(object : WifiConnectionListener {
            override fun onResponseReceived(response: String) {
                binding.textviewFirst.text = response
            }

            override fun onError(message: String) {
                println("Failed to send/receive data: $message")
            }
        })

        binding.buttonFirst.setOnClickListener {
            findNavController().navigate(R.id.action_FirstFragment_to_SecondFragment)
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
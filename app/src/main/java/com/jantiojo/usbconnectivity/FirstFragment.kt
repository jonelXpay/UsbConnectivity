package com.jantiojo.usbconnectivity

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.jantiojo.usbconnectivity.databinding.FragmentFirstBinding
import com.jantiojo.usbconnectivity.socket.WebSocketConnectionListener
import com.jantiojo.usbconnectivity.socket.WebSocketConnectionManager

/**
 * A simple [Fragment] subclass as the default destination in the navigation.
 */
class FirstFragment : Fragment() {

    private var _binding: FragmentFirstBinding? = null

    // This property is only valid between onCreateView and
    // onDestroyView.
    private val binding get() = _binding!!


    private lateinit var webSocketConnectionManager: WebSocketConnectionManager

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {

        _binding = FragmentFirstBinding.inflate(inflater, container, false)
        return binding.root

    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        webSocketConnectionManager =
            WebSocketConnectionManager(urlEndpoint = "wss://7f77z033l6.execute-api.ap-southeast-1.amazonaws.com/development")
        webSocketConnectionManager.setListener(object : WebSocketConnectionListener {
            override fun onMessageReceived(response: String) {
                requireActivity().runOnUiThread {
                    binding.textviewFirst.text = response
                }
            }

            override fun onFailure(message: String) {
                Log.e(TAG,"onFailure == $message")
            }

            override fun onConnectionClosed(code: Int, reason: String) {
                Log.e(TAG,"onConnectionClosed == $code / $reason")
            }
        })

        binding.buttonFirst.setOnClickListener {
            findNavController().navigate(R.id.action_FirstFragment_to_SecondFragment)
        }

        binding.buttonSendData.setOnClickListener {
            webSocketConnectionManager.sendDataToServer(
                status = "Approved",
                transactionId = "4b546514-fb54-47a2-96f7-6a465c3b3422",
                receiptNo = "6a465c3b342",
                date = "8/9/2024 12:00:00"
            )
        }

    }
    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
        webSocketConnectionManager.close()
    }

    companion object {
        private val TAG = FirstFragment::class.java.simpleName
    }
}
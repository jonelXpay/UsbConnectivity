package com.jantiojo.usbconnectivity

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.content.pm.PackageManager
import android.hardware.usb.UsbAccessory
import android.hardware.usb.UsbDeviceConnection
import android.hardware.usb.UsbManager
import android.net.wifi.WifiManager
import android.os.Bundle
import android.util.Log
import android.view.Menu
import android.view.MenuItem
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import androidx.navigation.findNavController
import androidx.navigation.ui.AppBarConfiguration
import androidx.navigation.ui.navigateUp
import androidx.navigation.ui.setupActionBarWithNavController
import com.jantiojo.usbconnectivity.databinding.ActivityMainBinding
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import org.json.JSONObject
import java.io.BufferedReader
import java.io.BufferedWriter
import java.io.FileInputStream
import java.io.FileOutputStream
import java.io.IOException
import java.io.InputStreamReader
import java.io.OutputStreamWriter
import java.io.PrintWriter
import java.net.Socket

class MainActivity : AppCompatActivity() {

    private lateinit var appBarConfiguration: AppBarConfiguration
    private lateinit var binding: ActivityMainBinding

    private val serverIP = "192.168.1.5" // Replace with your PC's IP address
    private val serverPort = 46696


    private lateinit var usbManager: UsbManager
    private var usbAccessory: UsbAccessory? = null
    private var usbConnection: UsbDeviceConnection? = null
    private var job: Job? = null

    private val usbReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context, intent: Intent) {
            Log.d(TAG, "USB BroadcastReceiver == ${intent.action}")
            if (intent.action == UsbManager.ACTION_USB_ACCESSORY_ATTACHED) {
                usbAccessory = intent.getParcelableExtra(UsbManager.EXTRA_ACCESSORY)
                if (usbAccessory != null) {
                    setupDeviceCommunication(usbAccessory!!)
                }
            }
        }
    }


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setSupportActionBar(binding.toolbar)

        val navController = findNavController(R.id.nav_host_fragment_content_main)
        appBarConfiguration = AppBarConfiguration(navController.graph)
        setupActionBarWithNavController(navController, appBarConfiguration)

        binding.fab.setOnClickListener { view ->
//            Snackbar.make(view, "Replace with your own action", Snackbar.LENGTH_LONG)
//                .setAction("Action", null)
//                .setAnchorView(R.id.fab).show()

            lifecycleScope.launch(Dispatchers.IO) {
                sendDataToPC()
            }
        }
        usbManager = getSystemService(Context.USB_SERVICE) as UsbManager
        val filter = IntentFilter().apply {
            addAction(UsbManager.ACTION_USB_ACCESSORY_ATTACHED)
            addAction(UsbManager.ACTION_USB_ACCESSORY_DETACHED)
        }
        registerReceiver(usbReceiver, filter, RECEIVER_EXPORTED)

        usbManager = getSystemService(USB_SERVICE) as UsbManager
        val accessoryList = usbManager.accessoryList

        val deviceList = usbManager.deviceList
        Log.d(TAG, "USB Device List == $deviceList")


        if (accessoryList != null && accessoryList.isNotEmpty()) {
            val accessory = accessoryList[0] // Get the first connected accessory
            logAccessoryDetails(accessory)
            runOnUiThread {
                Toast.makeText(
                    this,
                    "USB accessories connected: ${accessoryList.count()}",
                    Toast.LENGTH_LONG
                ).show()
            }
        } else {
            Log.d(TAG, "No USB accessories connected")
            runOnUiThread {
                Toast.makeText(this, "No USB accessories connected", Toast.LENGTH_LONG).show()
            }
        }

        val packageManager: PackageManager = packageManager
        val hasFeature: Boolean =
            packageManager.hasSystemFeature(PackageManager.FEATURE_USB_ACCESSORY)
        Log.d(TAG, "Is Device supports USB accessory mode ==  $hasFeature")

    }

    private fun logAccessoryDetails(accessory: UsbAccessory) {
        Log.d(TAG, "Model: ${accessory.model}")
        Log.d(TAG, "Manufacturer: ${accessory.manufacturer}")
        Log.d(TAG, "Version: ${accessory.version}")
        Log.d(TAG, "Description: ${accessory.description}")
        Log.d(TAG, "URI: ${accessory.uri}")
        Log.d(TAG, "Serial: ${accessory.serial}")
    }

    private fun getDeviceIpAddress(): String {
        val wifiManager = applicationContext.getSystemService(Context.WIFI_SERVICE) as WifiManager

        val ipAddress = wifiManager.connectionInfo.ipAddress
        return String.format(
            "%d.%d.%d.%d",
            (ipAddress and 0xff),
            (ipAddress shr 8 and 0xff),
            (ipAddress shr 16 and 0xff),
            (ipAddress shr 24 and 0xff)
        )
    }

    private fun sendDataToPC() {
        try {
            val clientSocket = Socket(serverIP, serverPort)

            // Send data
            val sendJson = JSONObject()
            sendJson.put("message", "Hello from Android")

            val outputStream = PrintWriter(
                BufferedWriter(OutputStreamWriter(clientSocket.getOutputStream())),
                true
            )
            outputStream.println(sendJson.toString())
            println("Sent to PC: $sendJson")

            // Receive response
            val inputStream = BufferedReader(InputStreamReader(clientSocket.getInputStream()))
            val receivedData = inputStream.readLine()
            val receivedJson = JSONObject(receivedData)
            println("Received from PC: $receivedJson")

            clientSocket.close()
        } catch (e: IOException) {
            e.printStackTrace()
        }
    }


    private fun setupDeviceCommunication(accessory: UsbAccessory) {
        val fileDescriptor = usbManager.openAccessory(accessory)?.fileDescriptor ?: return
        val inputStream = FileInputStream(fileDescriptor)
        val outputStream = FileOutputStream(fileDescriptor)
        if (usbConnection != null) {
            // Get the interface and endpoints


            // Start the data transfer coroutine
            job = CoroutineScope(Dispatchers.IO).launch {
                while (isActive) {
                    // Open USB accessory communication
                    val usbAccessoryConnection = usbManager.openAccessory(usbAccessory)

                    // Read JSON data from USB accessory
                    val buffer = ByteArray(1024)
                    val bytesRead = inputStream.read(buffer)
                    val jsonData = String(buffer, 0, bytesRead)

                    // Deserialize JSON data
                    val json = JSONObject(jsonData)

                    // Process JSON data
                    Log.d("USB", "Received JSON data: $json")

                    // Prepare response JSON
                    val response = JSONObject().apply {
                        put("responseKey", "responseValue")
                    }

                    // Send response JSON to USB accessory
                    outputStream.write(response.toString().toByteArray())

                    // Close USB accessory connection
                    usbAccessoryConnection.close()

                    delay(1000) // Adjust the delay as needed
                }
            }
        }
    }
    override fun onDestroy() {
        unregisterReceiver(usbReceiver)
        usbConnection?.close()
        job?.cancel()
        super.onDestroy()
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        // Inflate the menu; this adds items to the action bar if it is present.
        menuInflater.inflate(R.menu.menu_main, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        return when (item.itemId) {
            R.id.action_settings -> true
            else -> super.onOptionsItemSelected(item)
        }
    }

    override fun onSupportNavigateUp(): Boolean {
        val navController = findNavController(R.id.nav_host_fragment_content_main)
        return navController.navigateUp(appBarConfiguration)
                || super.onSupportNavigateUp()
    }

    companion object {
        private const val TAG = "MainActivity"
    }
}
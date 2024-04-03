package com.inovace.airobot

import android.Manifest
import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothDevice
import android.bluetooth.BluetoothManager
import android.bluetooth.BluetoothSocket
import android.content.Context
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle

import android.util.Log

import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.annotation.RequiresApi
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.width
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.inovace.airobot.ui.theme.AiRobotTheme
import java.io.IOException
import java.io.OutputStream
import java.util.UUID


class MainActivity : ComponentActivity() {

    private val REQUEST_BLUETOOTH_CONNECT_PERMISSION = 1

    @RequiresApi(Build.VERSION_CODES.S)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        if (!isBluetoothPermissionGranted()) {
            requestBluetoothPermission()
        } else {
            // Bluetooth permissions are already granted, proceed with your Bluetooth operations
        }

        setContent {
            AiRobotTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    HomeScreen(context = this)
                }
            }
        }
    }

    @RequiresApi(Build.VERSION_CODES.S)
    private fun isBluetoothPermissionGranted(): Boolean {
        return (ContextCompat.checkSelfPermission(
            this,
            Manifest.permission.BLUETOOTH
        ) == PackageManager.PERMISSION_GRANTED &&
                ContextCompat.checkSelfPermission(
                    this,
                    Manifest.permission.BLUETOOTH_ADMIN
                ) == PackageManager.PERMISSION_GRANTED &&
                ContextCompat.checkSelfPermission(
                    this,
                    Manifest.permission.BLUETOOTH_CONNECT
                ) == PackageManager.PERMISSION_GRANTED &&
                ContextCompat.checkSelfPermission(
                    this,
                    Manifest.permission.ACCESS_FINE_LOCATION
                ) == PackageManager.PERMISSION_GRANTED)

    }

    @RequiresApi(Build.VERSION_CODES.S)
    private fun requestBluetoothPermission() {
        ActivityCompat.requestPermissions(
            this,
            arrayOf(
                Manifest.permission.BLUETOOTH_CONNECT,
                Manifest.permission.BLUETOOTH,
                Manifest.permission.BLUETOOTH_ADMIN,
                Manifest.permission.ACCESS_FINE_LOCATION
            ),
            REQUEST_BLUETOOTH_CONNECT_PERMISSION
        )
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)

        if (requestCode == REQUEST_BLUETOOTH_CONNECT_PERMISSION) {
            if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                // Bluetooth permissions granted, proceed with your Bluetooth operations
            } else {
                // Bluetooth permissions denied
                Toast.makeText(this, "Bluetooth permissions are required", Toast.LENGTH_SHORT)
                    .show()
                finish()
            }
        }
    }


}

@Composable
fun HomeScreen(modifier: Modifier = Modifier, context: Context) {
    val sampleText =""
    var beDeviceName:String by remember { mutableStateOf(sampleText) }
    var bluetoothSocket:BluetoothSocket? by remember { mutableStateOf(null) }
    var bluetoothAdapter:BluetoothAdapter? by remember { mutableStateOf(null) }
    var outputStream:OutputStream? by remember { mutableStateOf(null) }
    var connected:Boolean by remember { mutableStateOf(false) }



    fun connectToDevice(device: BluetoothDevice) {
        val uuid =
            UUID.fromString("00001101-0000-1000-8000-00805F9B34FB") // Standard SerialPortService ID
        try {
            if (ActivityCompat.checkSelfPermission(
                    context,
                    Manifest.permission.BLUETOOTH_CONNECT
                ) != PackageManager.PERMISSION_GRANTED
            ) {
                return
            }
            bluetoothSocket = device.createRfcommSocketToServiceRecord(uuid)
            bluetoothSocket?.connect()
            outputStream = bluetoothSocket?.outputStream
            connected = true


        } catch (e: IOException) {
            Log.e("BluetoothExample", "Error connecting to device: ${e.message}")
        }
    }

    fun BluetoothConnection() {

        val manager: BluetoothManager =
            context.getSystemService(Context.BLUETOOTH_SERVICE) as BluetoothManager

        bluetoothAdapter = manager.adapter

        if (bluetoothAdapter == null) {
            return
        }

        if (!bluetoothAdapter?.isEnabled!!) {
            return
        }


        val pairedDevices: Set<BluetoothDevice> = bluetoothAdapter?.bondedDevices!!
        if (pairedDevices.isNotEmpty()) {
            for (device in pairedDevices) {
                // Replace "YourDeviceName" with the name of your Bluetooth module
                if (ActivityCompat.checkSelfPermission(
                        context,
                        Manifest.permission.BLUETOOTH_CONNECT
                    ) != PackageManager.PERMISSION_GRANTED
                ) {
                    return
                }
                if (device.name == "HC-05") {
                    connectToDevice(device)
                    if (bluetoothSocket?.isConnected!!) {
                        beDeviceName = "You are now connected to HC-05"
                        Toast.makeText(context, "Sucessfully connected", Toast.LENGTH_SHORT).show();
                    }
                    break
                }
            }
        }


    }


    Column(modifier.fillMaxSize(),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally) {

        Text(
            text = beDeviceName,
            modifier = modifier
        )

        Spacer(modifier.height(5.dp))




        Button(onClick = {
            BluetoothConnection()
        }) {
            Text(
                text = "Bluetooth",
                fontSize = 16.sp
            )
        }



        Spacer(modifier.height(15.dp))

        Row {

            Button(onClick = {
                if (connected) {
                    try {
                        val byteToSend: Byte = '0'.toByte()
                        val byteArray = ByteArray(1)
                        byteArray[0] = byteToSend
                        outputStream?.write(byteArray)
                    } catch (e: IOException) {
                        Log.e("BluetoothExample", "Error sending data: ${e.message}")
                    }
                }


            }) {
                Text(
                    text = "Off",
                    fontSize = 16.sp
                )
            }

            Spacer(modifier.width(5.dp))

            Button(onClick = {
                if (connected) {
                    try {
                        val byteToSend: Byte = '1'.toByte()
                        val byteArray = ByteArray(1)
                        byteArray[0] = byteToSend
                        outputStream?.write(byteArray)
                    } catch (e: IOException) {
                        Log.e("BluetoothExample", "Error sending data: ${e.message}")
                    }
                }

            }) {
                Text(
                    text = "On",
                    fontSize = 16.sp
                )
            }

        }




    }
}









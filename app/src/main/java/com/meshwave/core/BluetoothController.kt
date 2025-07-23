package com.meshwave.core

import android.Manifest
import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothDevice
import android.bluetooth.BluetoothManager
import android.bluetooth.BluetoothServerSocket
import android.bluetooth.BluetoothSocket
import android.content.Context
import android.content.IntentFilter
import android.content.pm.PackageManager
import androidx.core.app.ActivityCompat
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import java.io.IOException
import java.util.UUID

data class BluetoothUiState(
    val scannedDevices: List<BluetoothDevice> = emptyList(),
    val pairedDevices: List<BluetoothDevice> = emptyList(),
    val isConnected: Boolean = false,
    val isConnecting: Boolean = false,
    val errorMessage: String? = null
)

class BluetoothController(private val context: Context) {
    private val bluetoothManager by lazy { context.getSystemService(BluetoothManager::class.java) }
    private val bluetoothAdapter by lazy { bluetoothManager?.adapter }
    private val SERVICE_UUID = UUID.fromString("e4a5396c-35a2-4706-92b8-974b88e7e2a2")

    private var dataTransferThread: DataTransferThread? = null
    private var connectThread: ConnectThread? = null
    private var serverThread: AcceptThread? = null

    private val _state = MutableStateFlow(BluetoothUiState())
    val state: StateFlow<BluetoothUiState> = _state.asStateFlow()

    private val foundDeviceReceiver = FoundDeviceReceiver { device ->
        _state.update {
            val newDevices = it.scannedDevices.toMutableList().apply { if (device !in this) add(device) }
            it.copy(scannedDevices = newDevices)
        }
    }

    fun startDiscovery() {
        if (!hasPermission(Manifest.permission.BLUETOOTH_SCAN)) return
        context.registerReceiver(foundDeviceReceiver, IntentFilter(BluetoothDevice.ACTION_FOUND))
        updatePairedDevices()
        bluetoothAdapter?.startDiscovery()
    }

    fun stopDiscovery() {
        if (!hasPermission(Manifest.permission.BLUETOOTH_SCAN)) return
        bluetoothAdapter?.cancelDiscovery()
    }

    fun startServer() {
        closeConnection()
        _state.update { it.copy(isConnecting = true) }
        serverThread = AcceptThread()
        serverThread?.start()
    }

    fun connectToDevice(device: BluetoothDevice) {
        closeConnection()
        _state.update { it.copy(isConnecting = true) }
        connectThread = ConnectThread(device)
        connectThread?.start()
    }

    fun closeConnection() {
        connectThread?.cancel()
        serverThread?.cancel()
        dataTransferThread?.cancel()
        _state.update { it.copy(isConnecting = false, isConnected = false) }
    }

    fun release() {
        try {
            context.unregisterReceiver(foundDeviceReceiver)
        } catch (e: Exception) { /* Ignorar */ }
        closeConnection()
    }

    fun sendMessage(message: String) {
        dataTransferThread?.write(message.toByteArray())
    }

    private fun updatePairedDevices() {
        if (!hasPermission(Manifest.permission.BLUETOOTH_CONNECT)) return
        bluetoothAdapter?.bondedDevices?.let { devices ->
            _state.update { it.copy(pairedDevices = devices.toList()) }
        }
    }

    private fun hasPermission(permission: String): Boolean {
        return ActivityCompat.checkSelfPermission(context, permission) == PackageManager.PERMISSION_GRANTED
    }

    private inner class AcceptThread : Thread() {
        private val serverSocket: BluetoothServerSocket? = if (hasPermission(Manifest.permission.BLUETOOTH_CONNECT)) {
            bluetoothAdapter?.listenUsingInsecureRfcommWithServiceRecord("MeshWaveCore", SERVICE_UUID)
        } else null

        override fun run() {
            val socket = try { serverSocket?.accept() } catch (e: IOException) { null }
            socket?.let {
                serverSocket?.close()
                manageConnection(it)
            }
        }
        fun cancel() = try { serverSocket?.close() } catch (e: IOException) { /* Ignorar */ }
    }

    private inner class ConnectThread(private val device: BluetoothDevice) : Thread() {
        private val socket: BluetoothSocket? = if (hasPermission(Manifest.permission.BLUETOOTH_CONNECT)) {
            device.createRfcommSocketToServiceRecord(SERVICE_UUID)
        } else null

        override fun run() {
            if (!hasPermission(Manifest.permission.BLUETOOTH_SCAN)) return
            bluetoothAdapter?.cancelDiscovery()
            try {
                socket?.connect()
                socket?.let { manageConnection(it) }
            } catch (e: IOException) {
                socket?.close()
            }
        }
        fun cancel() = try { socket?.close() } catch (e: IOException) { /* Ignorar */ }
    }

    private inner class DataTransferThread(private val socket: BluetoothSocket) : Thread() {
        private val inputStream = socket.inputStream
        private val outputStream = socket.outputStream
        override fun run() { /* Lógica de leitura */ }
        fun write(bytes: ByteArray) = try { outputStream.write(bytes) } catch (e: IOException) { /* Ignorar */ }
        fun cancel() = try { socket.close() } catch (e: IOException) { /* Ignorar */ }
    }

    private fun manageConnection(socket: BluetoothSocket) {
        _state.update { it.copy(isConnected = true, isConnecting = false, errorMessage = null) }
        dataTransferThread = DataTransferThread(socket)
        dataTransferThread?.start()
    }
}

package com.meshwave.core

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.IOException
import java.net.InetSocketAddress
import java.net.ServerSocket
import java.net.Socket

class WiFiDirectConnectionManager {
    private val portSequence = listOf(8888, 8889, 8890, 8891)
    private var serverSocket: ServerSocket? = null
    private var clientSocket: Socket? = null

    suspend fun startServer(): Socket? {
        closeAllConnections()
        return withContext(Dispatchers.IO) {
            for (port in portSequence) {
                try {
                    serverSocket = ServerSocket(port)
                    println("[WFD Server] Ouvindo na porta $port")
                    val socket = serverSocket?.accept()
                    println("[WFD Server] Cliente conectado: ${socket?.inetAddress?.hostAddress}")
                    clientSocket = socket
                    return@withContext clientSocket
                } catch (e: IOException) {
                    println("[WFD Server] ERRO na porta $port: ${e.message}")
                    serverSocket?.close()
                }
            }
            return@withContext null
        }
    }

    suspend fun connectToServer(hostAddress: String): Socket? {
        closeAllConnections()
        return withContext(Dispatchers.IO) {
            for (port in portSequence) {
                try {
                    val socket = Socket()
                    socket.connect(InetSocketAddress(hostAddress, port), 5000)
                    println("[WFD Client] Conectado a $hostAddress na porta $port")
                    clientSocket = socket
                    return@withContext clientSocket
                } catch (e: IOException) {
                    println("[WFD Client] ERRO ao conectar na porta $port: ${e.message}")
                }
            }
            return@withContext null
        }
    }

    fun closeAllConnections() {
        try {
            serverSocket?.close()
            clientSocket?.close()
        } catch (e: IOException) { /* Ignorar */ }
    }
}

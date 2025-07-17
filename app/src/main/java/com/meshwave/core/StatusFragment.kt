// StatusFragment.kt
// VERSÃO COM NOMES DE MÉTODOS E CAMPOS ATUALIZADOS

package com.meshwave.core

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ScrollView
import android.widget.TextView
import androidx.fragment.app.Fragment

class StatusFragment : Fragment() {

    private lateinit var textViewCpaOrigin: TextView // Renomeado de textViewDid
    private lateinit var textViewUsername: TextView
    private lateinit var textViewNodeLocation: TextView // Renomeado de textViewGeohash
    private lateinit var textViewWifiStatus: TextView
    private lateinit var textViewLocalCache: TextView
    private lateinit var textViewRemoteCache: TextView
    private lateinit var textViewLog: TextView
    private lateinit var logScrollView: ScrollView

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_status, container, false)

        textViewCpaOrigin = view.findViewById(R.id.textViewCpaOrigin) // ID atualizado
        textViewUsername = view.findViewById(R.id.textViewUsername)
        textViewNodeLocation = view.findViewById(R.id.textViewNodeLocation) // ID atualizado
        textViewWifiStatus = view.findViewById(R.id.textViewWifiStatus)
        textViewLocalCache = view.findViewById(R.id.textViewLocalCache)
        textViewRemoteCache = view.findViewById(R.id.textViewRemoteCache)
        textViewLog = view.findViewById(R.id.textViewLog)
        logScrollView = view.findViewById(R.id.logScrollView)

        return view
    }

    // ✅ Método renomeado para clareza
    fun updateCpaOrigin(cpaGeohash: String) {
        if (::textViewCpaOrigin.isInitialized) {
            textViewCpaOrigin.text = "CPA de Origem: $cpaGeohash"
        }
    }

    fun updateUsername(username: String) {
        if (::textViewUsername.isInitialized) {
            textViewUsername.text = "Username: $username"
        }
    }

    // ✅ Método renomeado para clareza
    fun updateGeohash(nodeGeohash: String) {
        if (::textViewNodeLocation.isInitialized) {
            textViewNodeLocation.text = "Localização Atual: $nodeGeohash"
        }
    }

    fun updateWifiStatus(status: String) {
        if (::textViewWifiStatus.isInitialized) {
            textViewWifiStatus.text = "Wi-Fi Direct: $status"
        }
    }

    fun updateLocalCache(cache: String) {
        if (::textViewLocalCache.isInitialized) {
            textViewLocalCache.text = cache
        }
    }

    fun updateRemoteCache(cache: String) {
        if (::textViewRemoteCache.isInitialized) {
            textViewRemoteCache.text = cache
        }
    }

    fun addLog(logMessage: String) {
        if (::textViewLog.isInitialized) {
            textViewLog.append("\n$logMessage")
            logScrollView.post { logScrollView.fullScroll(View.FOCUS_DOWN) }
        }
    }
}

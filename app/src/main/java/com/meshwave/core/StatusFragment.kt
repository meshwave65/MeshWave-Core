package com.meshwave.core

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ScrollView
import android.widget.TextView
import androidx.fragment.app.Fragment

class StatusFragment : Fragment() {

    // Declaração das Views que vamos controlar.
    private lateinit var textViewDid: TextView
    private lateinit var textViewUsername: TextView
    private lateinit var textViewGeohash: TextView
    private lateinit var textViewWifiStatus: TextView
    private lateinit var textViewLocalCache: TextView
    private lateinit var textViewRemoteCache: TextView
    private lateinit var textViewLog: TextView
    private lateinit var logScrollView: ScrollView

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Infla (cria) a view a partir do nosso arquivo de layout XML.
        val view = inflater.inflate(R.layout.fragment_status, container, false)

        // Conecta cada variável de View ao seu componente correspondente no layout.
        textViewDid = view.findViewById(R.id.textViewDid)
        textViewUsername = view.findViewById(R.id.textViewUsername)
        textViewGeohash = view.findViewById(R.id.textViewGeohash)
        textViewWifiStatus = view.findViewById(R.id.textViewWifiStatus)
        textViewLocalCache = view.findViewById(R.id.textViewLocalCache)
        textViewRemoteCache = view.findViewById(R.id.textViewRemoteCache)
        textViewLog = view.findViewById(R.id.textViewLog)
        logScrollView = view.findViewById(R.id.logScrollView)

        return view
    }

    // --- Métodos Públicos para a MainActivity usar ---

    fun updateDid(did: String) {
        if (this::textViewDid.isInitialized) {
            textViewDid.text = "DID: $did"
        }
    }

    fun updateUsername(username: String) {
        if (this::textViewUsername.isInitialized) {
            textViewUsername.text = "Username: $username"
        }
    }

    fun updateGeohash(geohash: String) {
        if (this::textViewGeohash.isInitialized) {
            textViewGeohash.text = "Geohash: $geohash"
        }
    }

    fun updateWifiStatus(status: String) {
        if (this::textViewWifiStatus.isInitialized) {
            textViewWifiStatus.text = "Wi-Fi Direct: $status"
        }
    }

    fun updateLocalCache(cache: String) {
        if (this::textViewLocalCache.isInitialized) {
            textViewLocalCache.text = cache
        }
    }

    fun updateRemoteCache(cache: String) {
        if (this::textViewRemoteCache.isInitialized) {
            textViewRemoteCache.text = cache
        }
    }

    fun addLog(logMessage: String) {
        if (this::textViewLog.isInitialized) {
            textViewLog.append("\n$logMessage")
            logScrollView.post { logScrollView.fullScroll(View.FOCUS_DOWN) }
        }
    }
}

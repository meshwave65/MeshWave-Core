// Local: app/src/main/java/com/meshwave/core/StatusFragment.kt
package com.meshwave.core

import android.os.Bundle
import android.text.method.ScrollingMovementMethod
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.fragment.app.Fragment

class StatusFragment : Fragment() {

    private lateinit var textCpaOrigin: TextView
    private lateinit var textUsername: TextView
    private lateinit var textCurrentLocation: TextView
    private lateinit var textWifiStatus: TextView
    private lateinit var textMyCache: TextView
    private lateinit var textPartnerCache: TextView
    private lateinit var textLog: TextView

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_status, container, false)

        // Inicializa todas as Views
        textCpaOrigin = view.findViewById(R.id.text_cpa_origin)
        textUsername = view.findViewById(R.id.text_username)
        textCurrentLocation = view.findViewById(R.id.text_current_location)
        textWifiStatus = view.findViewById(R.id.text_wifi_status)
        textMyCache = view.findViewById(R.id.text_my_cache)
        textPartnerCache = view.findViewById(R.id.text_partner_cache)
        textLog = view.findViewById(R.id.text_log)

        // Permite que o log seja rolável
        textLog.movementMethod = ScrollingMovementMethod()

        return view
    }

    // --- MÉTODOS CHAMADOS PELA MAINACTIVITY ---

    fun updateNodeGeohash(geohash: String) {
        textCurrentLocation.text = "Localização Atual: $geohash"
    }

    fun updateCpaData(cpa: NodeCPA) {
        textCpaOrigin.text = "CPA de Origem: ${cpa.cpaGeohash}"
        textUsername.text = "Username: ${cpa.username}"
        textMyCache.text = cpa.toString()
    }

    fun updateWifiStatus(status: String) {
        textWifiStatus.text = "Wi-Fi Direct: $status"
    }

    fun updatePartnerCpa(cpa: NodeCPA) {
        textPartnerCache.text = cpa.toString()
    }

    fun addLog(message: String) {
        textLog.append("\n$message")
    }
}

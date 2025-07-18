package com.meshwave.core

import android.content.Context
import android.os.Bundle
import android.text.method.ScrollingMovementMethod
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment

class StatusFragment : Fragment() {

    interface ManualControlListener {
        fun onBecomeServerClicked()
        fun onBecomeClientClicked()
    }

    private var listener: ManualControlListener? = null

    private lateinit var textCpaOrigin: TextView
    private lateinit var textUsername: TextView
    private lateinit var textCurrentLocation: TextView
    private lateinit var textWifiStatus: TextView
    private lateinit var textMyCache: TextView
    private lateinit var textPartnerCache: TextView
    private lateinit var textLog: TextView
    private lateinit var buttonServer: Button
    private lateinit var buttonClient: Button

    private val logMessages = mutableListOf<String>()

    override fun onAttach(context: Context) {
        super.onAttach(context)
        if (context is ManualControlListener) {
            listener = context
        } else {
            throw RuntimeException("$context must implement ManualControlListener")
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_status, container, false)

        textCpaOrigin = view.findViewById(R.id.text_cpa_origin)
        textUsername = view.findViewById(R.id.text_username)
        textCurrentLocation = view.findViewById(R.id.text_current_location)
        textWifiStatus = view.findViewById(R.id.text_wifi_status)
        textMyCache = view.findViewById(R.id.text_my_cache)
        textPartnerCache = view.findViewById(R.id.text_partner_cache)
        textLog = view.findViewById(R.id.text_log)
        buttonServer = view.findViewById(R.id.button_server)
        buttonClient = view.findViewById(R.id.button_client)

        textLog.movementMethod = ScrollingMovementMethod()

        buttonServer.setOnClickListener {
            listener?.onBecomeServerClicked()
        }
        buttonClient.setOnClickListener {
            listener?.onBecomeClientClicked()
        }

        return view
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        addLog("Log de Eventos iniciado...")
    }

    fun updateLocation(locationData: LocationData) {
        if (!isAdded) return
        val displayText = "Última Localização: ${locationData.geohash}"
        textCurrentLocation.text = displayText
        val colorRes = when (locationData.status) {
            LocationStatus.UPDATED -> R.color.status_green
            LocationStatus.STALE -> R.color.status_yellow
            LocationStatus.FAILED -> R.color.status_red
        }
        textCurrentLocation.setTextColor(ContextCompat.getColor(requireContext(), colorRes))
    }

    fun updateCpaData(cpa: NodeCPA) {
        if (isAdded) {
            textCpaOrigin.text = "CPA de Origem: ${cpa.cpaGeohash}"
            textUsername.text = "Username: ${cpa.username}"
            textMyCache.text = cpa.toString()
        }
    }

    fun updateWifiStatus(status: String) {
        if (isAdded) textWifiStatus.text = "Wi-Fi Direct: $status"
    }

    fun updatePartnerCpa(cpa: NodeCPA?) {
        if (isAdded) {
            textPartnerCache.text = cpa?.toString() ?: "{desconectado}"
        }
    }

    fun addLog(message: String) {
        if (!isAdded) return
        activity?.runOnUiThread {
            if (logMessages.size > 100) {
                logMessages.removeAt(0)
            }
            logMessages.add(message)
            textLog.text = logMessages.joinToString("\n")
            if (textLog.layout != null) {
                val scrollAmount = textLog.layout.getLineTop(textLog.lineCount) - textLog.height
                if (scrollAmount > 0) {
                    textLog.scrollTo(0, scrollAmount)
                } else {
                    textLog.scrollTo(0, 0)
                }
            }
        }
    }
}

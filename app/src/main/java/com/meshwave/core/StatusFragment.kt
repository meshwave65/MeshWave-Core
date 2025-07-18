package com.meshwave.core

import android.os.Bundle
import android.text.method.ScrollingMovementMethod
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ScrollView
import android.widget.TextView
import androidx.fragment.app.Fragment

class StatusFragment : Fragment() {

    // Declaração das Views que vamos controlar.
    private lateinit var textViewCpaOrigin: TextView
    private lateinit var textViewUsername: TextView
    private lateinit var textViewNodeGeohash: TextView
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
        textViewCpaOrigin = view.findViewById(R.id.textViewCpaOrigin)
        textViewUsername = view.findViewById(R.id.textViewUsername)
        textViewNodeGeohash = view.findViewById(R.id.textViewNodeGeohash)
        textViewWifiStatus = view.findViewById(R.id.textViewWifiStatus)
        textViewLocalCache = view.findViewById(R.id.textViewLocalCache)
        textViewRemoteCache = view.findViewById(R.id.textViewRemoteCache)
        textViewLog = view.findViewById(R.id.textViewLog)
        logScrollView = view.findViewById(R.id.logScrollView)

        // Permite que o TextView de log seja rolável
        textViewLog.movementMethod = ScrollingMovementMethod()

        return view
    }

    // --- Métodos Públicos para a MainActivity usar ---

    /**
     * ✅ MÉTODO ADICIONADO: Atualiza a UI com os dados do CPA.
     */
    fun updateCpaData(cpa: NodeCPA) {
        if (!isAdded) return // Garante que o fragmento está anexado
        textViewCpaOrigin.text = "CPA de Origem: ${cpa.cpaGeohash}"
        textViewUsername.text = "Username: ${cpa.username}"
        // Formata o objeto CPA para exibição no cache local
        val cpaText = "NodeCPA(\n" +
                "  did=${cpa.did},\n" +
                "  username=${cpa.username},\n" +
                "  cpaGeohash=${cpa.cpaGeohash},\n" +
                "  creationTimestamp=${cpa.creationTimestamp},\n" +
                "  currentClaGeohash=${cpa.currentClaGeohash},\n" +
                "  status=${cpa.status}\n" +
                ")"
        textViewLocalCache.text = cpaText
    }

    /**
     * ✅ MÉTODO ADICIONADO: Atualiza a UI com o Geohash do nó.
     */
    fun updateNodeGeohash(geohash: String) {
        if (!isAdded) return
        textViewNodeGeohash.text = "Localização Atual: $geohash"
    }

    fun updateWifiStatus(status: String) {
        if (!isAdded) return
        textViewWifiStatus.text = "Wi-Fi Direct: $status"
    }

    fun addLog(logMessage: String) {
        if (!isAdded) return
        // Adiciona a nova mensagem de log com uma quebra de linha.
        textViewLog.append("\n$logMessage")
        // Força o ScrollView a rolar para a parte inferior para mostrar a última mensagem.
        logScrollView.post { logScrollView.fullScroll(View.FOCUS_DOWN) }
    }
}

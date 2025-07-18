// Local: app/src/main/java/com/meshwave/core/IdentityModule.kt
package com.meshwave.core

import android.content.Context
import android.os.Handler
import android.os.Message
import android.util.Log

class IdentityModule(private val context: Context, private val uiHandler: Handler) {

    private val identityManager = IdentityManager(context)
    private var nodeCPA: NodeCPA? = null

    fun start() {
        nodeCPA = identityManager.loadNodeCPA()
        if (nodeCPA != null) {
            Log.d("IdentityModule", "Identidade carregada do armazenamento local: ${nodeCPA?.username}")
            sendCpaToUi(nodeCPA!!)
        } else {
            Log.d("IdentityModule", "Nenhuma identidade local encontrada. Aguardando geohash para criar uma nova.")
        }
    }

    fun generateCpaIfNeeded(areaGeohash: String) {
        if (nodeCPA == null) {
            Log.d("IdentityModule", "Criando nova identidade com geohash de ativação: $areaGeohash")
            nodeCPA = identityManager.createNewIdentity(areaGeohash)
            sendCpaToUi(nodeCPA!!)
        } else {
            if (nodeCPA?.currentClaGeohash != areaGeohash) {
                Log.d("IdentityModule", "Identidade já existe. Atualizando CLA para: $areaGeohash")
                nodeCPA = nodeCPA!!.copy(currentClaGeohash = areaGeohash)
                sendCpaToUi(nodeCPA!!)
            }
        }
    }

    fun getCurrentCPA(): NodeCPA? {
        return nodeCPA
    }

    private fun sendCpaToUi(cpa: NodeCPA) {
        val message = Message.obtain(uiHandler, MainActivity.IDENTITY_UPDATE, cpa)
        uiHandler.sendMessage(message)
    }
}
